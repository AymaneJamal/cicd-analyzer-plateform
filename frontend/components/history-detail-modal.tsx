"use client"

import type React from "react"
import { useState } from "react"
import { X, Copy, Check, AlertCircle, CheckCircle2, TrendingDown } from "lucide-react"
import { Button } from "@/components/ui/button"
import { historyApi, type OptimizationHistory } from "@/lib/history-api"

interface HistoryDetailModalProps {
  isOpen: boolean
  onClose: () => void
  history: OptimizationHistory
}

export default function HistoryDetailModal({ isOpen, onClose, history }: HistoryDetailModalProps) {
  const [copiedOriginal, setCopiedOriginal] = useState(false)
  const [copiedOptimized, setCopiedOptimized] = useState(false)

  // Parse JSON strings
  const analysis = historyApi.parseAnalysis(history.analysisJson)
  const optimizations = historyApi.parseOptimizations(history.optimizationsJson)
  const estimatedGain = historyApi.parseEstimatedGain(history.estimatedGainJson)

  const handleCopyOriginal = () => {
    navigator.clipboard.writeText(history.originalScript)
    setCopiedOriginal(true)
    setTimeout(() => setCopiedOriginal(false), 2000)
  }

  const handleCopyOptimized = () => {
    if (history.optimizedScript) {
      navigator.clipboard.writeText(history.optimizedScript)
      setCopiedOptimized(true)
      setTimeout(() => setCopiedOptimized(false), 2000)
    }
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

  const getPriorityColor = (priority: number) => {
    if (priority === 1) return 'bg-red-100 dark:bg-red-950 text-red-700 dark:text-red-400'
    if (priority === 2) return 'bg-orange-100 dark:bg-orange-950 text-orange-700 dark:text-orange-400'
    return 'bg-blue-100 dark:bg-blue-950 text-blue-700 dark:text-blue-400'
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 overflow-y-auto">
      <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 max-w-6xl w-full max-h-[90vh] overflow-y-auto shadow-xl">
        {/* Header */}
        <div className="sticky top-0 bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 p-6 z-10">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-2xl font-semibold text-slate-900 dark:text-white">
                {history.pipelineName} - Build #{history.buildNumber}
              </h3>
              <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                Optimized by {history.llmProvider}
              </p>
            </div>
            <button
              onClick={onClose}
              className="p-2 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
            >
              <X size={24} className="text-slate-500 dark:text-slate-400" />
            </button>
          </div>
        </div>

        <div className="p-6 space-y-6">
          {/* Status & Performance Gain */}
          {history.status === 'COMPLETED' && estimatedGain && (
            <div className="bg-gradient-to-br from-green-50 to-emerald-50 dark:from-green-950/30 dark:to-emerald-950/30 rounded-lg border border-green-200 dark:border-green-900 p-6">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 bg-green-600 rounded-lg">
                  <TrendingDown className="w-6 h-6 text-white" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-green-900 dark:text-green-100">Estimated Performance Gain</h3>
                  <p className="text-sm text-green-700 dark:text-green-300">AI-powered optimization by {history.llmProvider}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-white dark:bg-slate-900 rounded-lg p-4 border border-green-200 dark:border-green-800">
                  <div className="text-sm text-slate-600 dark:text-slate-400 mb-1">Current Duration</div>
                  <div className="text-2xl font-bold text-slate-900 dark:text-white">
                    {formatDuration(estimatedGain.currentDuration)}
                  </div>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-lg p-4 border border-green-200 dark:border-green-800">
                  <div className="text-sm text-slate-600 dark:text-slate-400 mb-1">Estimated Duration</div>
                  <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                    {formatDuration(estimatedGain.estimatedDuration)}
                  </div>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-lg p-4 border border-green-200 dark:border-green-800">
                  <div className="text-sm text-slate-600 dark:text-slate-400 mb-1">Time Saved</div>
                  <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                    {estimatedGain.reductionPercentage || `${estimatedGain.percentageGain?.toFixed(1)}%`}
                  </div>
                  {estimatedGain.timeSaved && (
                    <div className="text-sm text-slate-600 dark:text-slate-400 mt-1">
                      (~{formatDuration(estimatedGain.timeSaved)})
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Error Message (if failed) */}
          {history.status === 'FAILED' && history.errorMessage && (
            <div className="bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900 rounded-lg p-6">
              <div className="flex items-start gap-3">
                <AlertCircle className="w-6 h-6 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
                <div>
                  <h3 className="text-lg font-semibold text-red-900 dark:text-red-100 mb-2">Optimization Failed</h3>
                  <p className="text-sm text-red-700 dark:text-red-300">{history.errorMessage}</p>
                </div>
              </div>
            </div>
          )}

          {/* Detected Bottlenecks */}
          {analysis && analysis.bottlenecks && analysis.bottlenecks.length > 0 && (
            <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-6">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4">Detected Bottlenecks</h3>
              <div className="space-y-3">
                {analysis.bottlenecks.map((bottleneck, index) => (
                  <div key={index} className="flex items-start gap-3 p-4 bg-red-50 dark:bg-red-950/30 rounded-lg border border-red-200 dark:border-red-900">
                    <AlertCircle className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-semibold text-slate-900 dark:text-white">{bottleneck.stage}</span>
                        {bottleneck.percentage && (
                          <span className="text-sm text-red-600 dark:text-red-400">({bottleneck.percentage.toFixed(1)}%)</span>
                        )}
                      </div>
                      <div className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                        Duration: {formatDuration(bottleneck.duration)}
                      </div>
                      <div className="text-sm text-red-700 dark:text-red-300">{bottleneck.issue}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Recommended Optimizations */}
          {optimizations && optimizations.length > 0 && (
            <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-6">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4">Recommended Optimizations</h3>
              <div className="space-y-3">
                {optimizations.map((opt, index) => (
                  <div key={index} className="p-4 bg-blue-50 dark:bg-blue-950/30 rounded-lg border border-blue-200 dark:border-blue-900">
                    <div className="flex items-start gap-3">
                      <CheckCircle2 className="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" />
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          {opt.priority && (
                            <span className={`px-2 py-0.5 rounded text-xs font-medium ${getPriorityColor(opt.priority)}`}>
                              Priority {opt.priority}
                            </span>
                          )}
                          {opt.impact && (
                            <span className="px-2 py-0.5 rounded text-xs font-medium bg-purple-100 dark:bg-purple-950 text-purple-700 dark:text-purple-400">
                              {opt.impact} impact
                            </span>
                          )}
                        </div>
                        {opt.title && (
                          <div className="font-semibold text-slate-900 dark:text-white mb-1">{opt.title}</div>
                        )}
                        {opt.type && !opt.title && (
                          <div className="font-semibold text-slate-900 dark:text-white mb-1">{opt.type}</div>
                        )}
                        <div className="text-sm text-slate-600 dark:text-slate-400">{opt.description}</div>
                        {opt.estimatedGain && (
                          <div className="text-sm text-blue-600 dark:text-blue-400 mt-1">
                            Estimated gain: {opt.estimatedGain}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Original Script */}
          <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white">Original Pipeline Script</h3>
              <Button
                onClick={handleCopyOriginal}
                variant="outline"
                className="flex items-center gap-2"
              >
                {copiedOriginal ? (
                  <>
                    <Check className="w-4 h-4" />
                    Copied!
                  </>
                ) : (
                  <>
                    <Copy className="w-4 h-4" />
                    Copy Script
                  </>
                )}
              </Button>
            </div>
            <div className="bg-slate-950 rounded-lg p-4 overflow-x-auto">
              <pre className="text-sm text-slate-100 font-mono whitespace-pre">
                {history.originalScript}
              </pre>
            </div>
          </div>

          {/* Optimized Script */}
          {history.optimizedScript && (
            <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white">Optimized Pipeline Script</h3>
                <Button
                  onClick={handleCopyOptimized}
                  variant="outline"
                  className="flex items-center gap-2"
                >
                  {copiedOptimized ? (
                    <>
                      <Check className="w-4 h-4" />
                      Copied!
                    </>
                  ) : (
                    <>
                      <Copy className="w-4 h-4" />
                      Copy Script
                    </>
                  )}
                </Button>
              </div>
              <div className="bg-slate-950 rounded-lg p-4 overflow-x-auto">
                <pre className="text-sm text-slate-100 font-mono whitespace-pre">
                  {history.optimizedScript}
                </pre>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="sticky bottom-0 bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 p-6">
          <Button
            onClick={onClose}
            variant="outline"
            className="w-full"
          >
            Close
          </Button>
        </div>
      </div>
    </div>
  )
}
