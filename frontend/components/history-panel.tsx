"use client"

import { useState, useEffect } from "react"
import { Eye, Filter, Loader2, AlertCircle, CheckCircle2, XCircle, Clock } from "lucide-react"
import { Button } from "@/components/ui/button"
import { historyApi, type OptimizationHistory } from "@/lib/history-api"
import { connectionsApi, type Connection } from "@/lib/connections-api"
import HistoryDetailModal from "./history-detail-modal"

export default function HistoryPanel() {
  const [histories, setHistories] = useState<OptimizationHistory[]>([])
  const [connections, setConnections] = useState<Connection[]>([])
  const [selectedConnectionId, setSelectedConnectionId] = useState<number | null>(null)

  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [selectedHistory, setSelectedHistory] = useState<OptimizationHistory | null>(null)
  const [showDetailModal, setShowDetailModal] = useState(false)

  useEffect(() => {
    loadConnections()
  }, [])

  useEffect(() => {
    loadHistory()
  }, [selectedConnectionId])

  const loadConnections = async () => {
    try {
      const data = await connectionsApi.getAllConnections()
      setConnections(data)
    } catch (err: any) {
      console.error('Failed to load connections:', err)
    }
  }

  const loadHistory = async () => {
    try {
      setIsLoading(true)
      setError(null)

      let data: OptimizationHistory[]

      if (selectedConnectionId) {
        // Filter by connection
        data = await historyApi.getHistoryByConnection(selectedConnectionId)
      } else {
        // Get all user history
        data = await historyApi.getMyHistory()
      }

      setHistories(data)
    } catch (err: any) {
      setError(err.message || 'Failed to load optimization history')
    } finally {
      setIsLoading(false)
    }
  }

  const handleViewDetails = async (history: OptimizationHistory) => {
    try {
      // Fetch full details by ID to ensure we have complete data
      const fullHistory = await historyApi.getHistoryById(history.id)
      setSelectedHistory(fullHistory)
      setShowDetailModal(true)
    } catch (err: any) {
      setError(err.message || 'Failed to load history details')
    }
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  const formatDuration = (ms: number) => {
    const seconds = Math.floor(ms / 1000)
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    if (minutes > 0) {
      return `${minutes}m ${remainingSeconds}s`
    }
    return `${seconds}s`
  }

  const getStatusIcon = (status: string) => {
    if (status === 'COMPLETED') return <CheckCircle2 className="w-4 h-4 text-green-600 dark:text-green-400" />
    if (status === 'FAILED') return <XCircle className="w-4 h-4 text-red-600 dark:text-red-400" />
    return <Clock className="w-4 h-4 text-yellow-600 dark:text-yellow-400" />
  }

  const getStatusColor = (status: string) => {
    if (status === 'COMPLETED') return 'bg-green-100 dark:bg-green-950 text-green-700 dark:text-green-400 border-green-200 dark:border-green-900'
    if (status === 'FAILED') return 'bg-red-100 dark:bg-red-950 text-red-700 dark:text-red-400 border-red-200 dark:border-red-900'
    return 'bg-yellow-100 dark:bg-yellow-950 text-yellow-700 dark:text-yellow-400 border-yellow-200 dark:border-yellow-900'
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Header with Filter */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h2 className="text-2xl font-bold text-foreground">Optimization History</h2>
          <p className="text-sm text-muted-foreground mt-1">View all your previous pipeline optimizations</p>
        </div>

        {/* Connection Filter */}
        <div className="flex items-center gap-3">
          <Filter className="w-5 h-5 text-slate-500 dark:text-slate-400" />
          <select
            value={selectedConnectionId || ''}
            onChange={(e) => setSelectedConnectionId(Number(e.target.value) || null)}
            className="px-4 py-2 border border-slate-300 dark:border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
          >
            <option value="">All Connections</option>
            {connections.map((conn) => (
              <option key={conn.id} value={conn.id}>
                {conn.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="flex items-center gap-2 px-4 py-3 bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-900 rounded-lg text-red-700 dark:text-red-400 mb-6">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Loading State */}
      {isLoading ? (
        <div className="flex items-center justify-center py-24">
          <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
        </div>
      ) : (
        <>
          {/* Empty State */}
          {histories.length === 0 ? (
            <div className="text-center py-24 bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800 rounded-lg border border-slate-200 dark:border-slate-700">
              <AlertCircle className="w-16 h-16 mx-auto mb-4 text-slate-400 dark:text-slate-500" />
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No History Yet</h3>
              <p className="text-slate-600 dark:text-slate-400">
                {selectedConnectionId
                  ? 'No optimizations found for this connection.'
                  : 'Start optimizing pipelines to see your history here.'}
              </p>
            </div>
          ) : (
            /* History Cards */
            <div className="grid gap-4">
              {histories.map((history) => {
                const gainPercentage = history.reductionPercentage
                  ? `${history.reductionPercentage.toFixed(1)}%`
                  : 'N/A'

                return (
                  <div
                    key={history.id}
                    className="bg-gradient-to-br from-white to-slate-50 dark:from-slate-800 dark:to-slate-900 rounded-lg border border-slate-300 dark:border-slate-700 p-6 hover:shadow-lg hover:border-slate-400 dark:hover:border-slate-600 transition-all"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        {/* Header */}
                        <div className="flex items-center gap-3 mb-3">
                          <h3 className="text-lg font-semibold text-slate-900 dark:text-white">
                            {history.pipelineName}
                          </h3>
                          <span className="text-sm text-slate-500 dark:text-slate-400">
                            Build #{history.buildNumber}
                          </span>
                          <div className={`flex items-center gap-1.5 px-2.5 py-1 rounded-md border text-xs font-medium ${getStatusColor(history.status)}`}>
                            {getStatusIcon(history.status)}
                            {history.status}
                          </div>
                        </div>

                        {/* Details */}
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-3">
                          <div>
                            <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">Provider</p>
                            <p className="text-sm font-medium text-slate-900 dark:text-white">
                              {history.llmProvider}
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">Current Duration</p>
                            <p className="text-sm font-medium text-slate-900 dark:text-white">
                              {formatDuration(history.currentDurationMs)}
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">Estimated Duration</p>
                            <p className="text-sm font-medium text-green-600 dark:text-green-400">
                              {history.estimatedDurationMs ? formatDuration(history.estimatedDurationMs) : 'N/A'}
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-slate-500 dark:text-slate-400 mb-1">Performance Gain</p>
                            <p className="text-sm font-bold text-green-600 dark:text-green-400">
                              {gainPercentage}
                            </p>
                          </div>
                        </div>

                        {/* Date */}
                        <p className="text-xs text-slate-500 dark:text-slate-400">
                          Completed: {formatDate(history.completedAt)}
                        </p>

                        {/* Error Message (if failed) */}
                        {history.status === 'FAILED' && history.errorMessage && (
                          <div className="mt-3 p-3 bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900 rounded-lg">
                            <p className="text-sm text-red-700 dark:text-red-400">
                              <span className="font-semibold">Error:</span> {history.errorMessage}
                            </p>
                          </div>
                        )}
                      </div>

                      {/* Actions */}
                      <div className="ml-4">
                        <Button
                          onClick={() => handleViewDetails(history)}
                          variant="outline"
                          className="flex items-center gap-2 border-blue-600 dark:border-blue-500 text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950"
                        >
                          <Eye size={16} />
                          View Details
                        </Button>
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </>
      )}

      {/* Detail Modal */}
      {selectedHistory && (
        <HistoryDetailModal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false)
            setSelectedHistory(null)
          }}
          history={selectedHistory}
        />
      )}
    </div>
  )
}
