"use client"

import { useState, useEffect } from "react"
import { Plus, Trash2, TestTube, Power, PowerOff, Loader2, CheckCircle2, XCircle, AlertCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import AddConnectionModal from "@/components/add-connection-modal"
import { connectionsApi, type Connection } from "@/lib/connections-api"

export default function ConnectionsList() {
  const [connections, setConnections] = useState<Connection[]>([])
  const [showModal, setShowModal] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [testingIds, setTestingIds] = useState<number[]>([])
  const [togglingIds, setTogglingIds] = useState<number[]>([])
  const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null)

  // Load connections on mount
  useEffect(() => {
    loadConnections()
  }, [])

  const loadConnections = async () => {
    try {
      setIsLoading(true)
      const data = await connectionsApi.getAllConnections()
      setConnections(data)
    } catch (error: any) {
      showNotification('error', error.message || 'Failed to load connections')
    } finally {
      setIsLoading(false)
    }
  }

  const showNotification = (type: 'success' | 'error', message: string) => {
    setNotification({ type, message })
    setTimeout(() => setNotification(null), 3000)
  }

  const handleTestConnection = async (id: number) => {
    setTestingIds([...testingIds, id])

    try {
      const result = await connectionsApi.testConnection(id)

      // Backend returns { status: 'SUCCESS' | 'FAILURE', message: '...' }
      const isSuccess = result.status === 'SUCCESS'

      if (isSuccess) {
        showNotification('success', result.message || 'Connection tested successfully')
      } else {
        showNotification('error', result.message || 'Connection test failed')
      }

      // Reload to get updated testStatus and lastTestedAt
      await loadConnections()
    } catch (error: any) {
      showNotification('error', error.message || 'Failed to test connection')
    } finally {
      setTestingIds(prevIds => prevIds.filter(testId => testId !== id))
    }
  }

  const handleToggleActive = async (id: number, currentStatus: boolean) => {
    setTogglingIds([...togglingIds, id])

    try {
      await connectionsApi.toggleConnection(id, !currentStatus)
      showNotification('success', `Connection ${!currentStatus ? 'activated' : 'deactivated'}`)
      await loadConnections()
    } catch (error: any) {
      showNotification('error', error.message || 'Failed to toggle connection')
    } finally {
      setTogglingIds(prevIds => prevIds.filter(toggleId => toggleId !== id))
    }
  }

  const handleDeleteConnection = async (id: number, name: string) => {
    if (!confirm(`Are you sure you want to delete "${name}"?`)) {
      return
    }

    try {
      await connectionsApi.deleteConnection(id)
      showNotification('success', 'Connection deleted successfully')
      setConnections(connections.filter((c) => c.id !== id))
    } catch (error: any) {
      showNotification('error', error.message || 'Failed to delete connection')
    }
  }

  const getStatusColor = (testStatus: string, isActive: boolean) => {
    if (!isActive) return 'bg-gray-400'
    if (testStatus === 'VALID' || testStatus === 'SUCCESS') return 'bg-green-500'
    if (testStatus === 'INVALID' || testStatus === 'FAILURE') return 'bg-red-500'
    return 'bg-yellow-500'
  }

  const getStatusText = (testStatus: string, isActive: boolean) => {
    if (!isActive) return 'Inactive'
    if (testStatus === 'VALID' || testStatus === 'SUCCESS') return 'Connected'
    if (testStatus === 'INVALID' || testStatus === 'FAILURE') return 'Failed'
    return 'Not Tested'
  }

  const formatDate = (dateString: string | null) => {
    if (!dateString) return 'Never'
    const date = new Date(dateString)
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Notification Toast */}
      {notification && (
        <div className="fixed top-4 right-4 z-50 animate-in slide-in-from-top-2">
          <div className={`flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg backdrop-blur-sm border ${
            notification.type === 'success'
              ? 'bg-green-50/90 dark:bg-green-950/90 border-green-200 dark:border-green-900 text-green-900 dark:text-green-100'
              : 'bg-red-50/90 dark:bg-red-950/90 border-red-200 dark:border-red-900 text-red-900 dark:text-red-100'
          }`}>
            {notification.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5" />
            ) : (
              <XCircle className="w-5 h-5" />
            )}
            <p className="font-medium">{notification.message}</p>
          </div>
        </div>
      )}

      <div className="flex items-center justify-between mb-8">
        <div>
          <h2 className="text-2xl font-bold text-foreground">My Jenkins Connections</h2>
          <p className="text-sm text-muted-foreground mt-1">Manage your Jenkins server connections</p>
        </div>
        <Button
          onClick={() => setShowModal(true)}
          className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white flex gap-2"
        >
          <Plus size={18} />
          Add Connection
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
        </div>
      ) : (
        <div className="grid gap-4">
          {connections.length === 0 ? (
            <div className="text-center py-12 bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800 rounded-lg border border-slate-200 dark:border-slate-700">
              <AlertCircle className="w-12 h-12 mx-auto mb-4 text-slate-400 dark:text-slate-500" />
              <p className="text-slate-600 dark:text-slate-400">No connections yet. Add a connection to get started.</p>
            </div>
          ) : (
            connections.map((conn) => (
              <div
                key={conn.id}
                className="bg-gradient-to-br from-white to-slate-50 dark:from-slate-800 dark:to-slate-900 rounded-lg border border-slate-300 dark:border-slate-700 p-6 hover:shadow-lg hover:border-slate-400 dark:hover:border-slate-600 transition-all"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-slate-900 dark:text-white">{conn.name}</h3>
                      <span
                        className={`inline-block w-2.5 h-2.5 rounded-full ${getStatusColor(conn.testStatus, conn.isActive)}`}
                        title={getStatusText(conn.testStatus, conn.isActive)}
                      ></span>
                      <span className="text-xs text-slate-500 dark:text-slate-400">
                        {getStatusText(conn.testStatus, conn.isActive)}
                      </span>
                    </div>
                    <p className="text-sm text-slate-600 dark:text-slate-400 mb-3">{conn.url}</p>
                    <div className="flex gap-6 text-xs text-slate-500 dark:text-slate-400">
                      <span>Username: <span className="font-medium">{conn.username}</span></span>
                      <span>Last tested: <span className="font-medium">{formatDate(conn.lastTestedAt)}</span></span>
                      <span>Created: <span className="font-medium">{formatDate(conn.createdAt)}</span></span>
                    </div>
                  </div>

                  <div className="flex gap-2">
                    {/* Test Button */}
                    <button
                      onClick={() => handleTestConnection(conn.id)}
                      disabled={testingIds.includes(conn.id)}
                      className="p-2 hover:bg-blue-50 dark:hover:bg-blue-950 rounded-lg transition-colors text-blue-600 dark:text-blue-400 disabled:opacity-50 disabled:cursor-not-allowed"
                      title="Test connection"
                    >
                      {testingIds.includes(conn.id) ? (
                        <Loader2 size={18} className="animate-spin" />
                      ) : (
                        <TestTube size={18} />
                      )}
                    </button>

                    {/* Toggle Active Button */}
                    <button
                      onClick={() => handleToggleActive(conn.id, conn.isActive)}
                      disabled={togglingIds.includes(conn.id)}
                      className={`p-2 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${
                        conn.isActive
                          ? 'hover:bg-orange-50 dark:hover:bg-orange-950 text-orange-600 dark:text-orange-400'
                          : 'hover:bg-green-50 dark:hover:bg-green-950 text-green-600 dark:text-green-400'
                      }`}
                      title={conn.isActive ? 'Deactivate' : 'Activate'}
                    >
                      {togglingIds.includes(conn.id) ? (
                        <Loader2 size={18} className="animate-spin" />
                      ) : conn.isActive ? (
                        <PowerOff size={18} />
                      ) : (
                        <Power size={18} />
                      )}
                    </button>

                    {/* Delete Button */}
                    <button
                      onClick={() => handleDeleteConnection(conn.id, conn.name)}
                      className="p-2 hover:bg-red-50 dark:hover:bg-red-950 rounded-lg transition-colors text-red-600 dark:text-red-400"
                      title="Delete connection"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      <AddConnectionModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onSuccess={loadConnections}
      />
    </div>
  )
}
