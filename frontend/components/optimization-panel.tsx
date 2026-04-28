"use client"

import { useState, useEffect } from "react"
import { Loader2, Zap, AlertCircle, TrendingDown, CheckCircle2, Copy, Check } from "lucide-react"
import { Button } from "@/components/ui/button"
import { connectionsApi, type Connection, type Pipeline, type Build, type OptimizeResponse, type Bottleneck, type EstimatedGain, type Optimization } from "@/lib/connections-api"

export default function OptimizationPanel() {
  const [connections, setConnections] = useState<Connection[]>([])
  const [pipelines, setPipelines] = useState<Pipeline[]>([])
  const [builds, setBuilds] = useState<Build[]>([])

  const [selectedConnectionId, setSelectedConnectionId] = useState<number | null>(null)
  const [selectedPipelineName, setSelectedPipelineName] = useState<string | null>(null)
  const [selectedBuildNumber, setSelectedBuildNumber] = useState<number | null>(null)

  const [isLoadingConnections, setIsLoadingConnections] = useState(true)
  const [isLoadingPipelines, setIsLoadingPipelines] = useState(false)
  const [isLoadingBuilds, setIsLoadingBuilds] = useState(false)
  const [isOptimizing, setIsOptimizing] = useState(false)

  const [optimizationResult, setOptimizationResult] = useState<OptimizeResponse | null>(null)
  const [parsedAnalysis, setParsedAnalysis] = useState<{ bottlenecks: Bottleneck[], detectedIssues?: string[], totalIssues?: number } | null>(null)
  const [parsedGain, setParsedGain] = useState<EstimatedGain | null>(null)
  const [parsedOptimizations, setParsedOptimizations] = useState<Optimization[] | null>(null)

  const [error, setError] = useState<string | null>(null)
  const [copiedScript, setCopiedScript] = useState(false)

  useEffect(() => {
    loadConnections()
  }, [])

  useEffect(() => {
    if (selectedConnectionId) {
      loadPipelines(selectedConnectionId)
    } else {
      setPipelines([])
      setSelectedPipelineName(null)
    }
  }, [selectedConnectionId])

  useEffect(() => {
    if (selectedConnectionId && selectedPipelineName) {
      loadBuilds(selectedConnectionId, selectedPipelineName)
    } else {
      setBuilds([])
      setSelectedBuildNumber(null)
    }
  }, [selectedConnectionId, selectedPipelineName])

  const loadConnections = async () => {
    try {
      setIsLoadingConnections(true)
      setError(null)
      const data = await connectionsApi.getActiveConnections()
      // Filter only tested and active connections
      const testedConnections = data.filter(conn =>
        conn.isActive &&
        (conn.testStatus === 'VALID' || conn.testStatus === 'SUCCESS')
      )
      setConnections(testedConnections)
    } catch (err: any) {
      setError(err.message || 'Failed to load connections')
    } finally {
      setIsLoadingConnections(false)
    }
  }

  const loadPipelines = async (connectionId: number) => {
    try {
      setIsLoadingPipelines(true)
      setError(null)
      const data = await connectionsApi.getPipelines(connectionId)
      setPipelines(data)
    } catch (err: any) {
      setError(err.message || 'Failed to load pipelines')
      setPipelines([])
    } finally {
      setIsLoadingPipelines(false)
    }
  }

  const loadBuilds = async (connectionId: number, pipelineName: string) => {
    try {
      setIsLoadingBuilds(true)
      setError(null)
      const data = await connectionsApi.getPipelineBuilds(pipelineName, connectionId)
      setBuilds(data)
    } catch (err: any) {
      setError(err.message || 'Failed to load builds')
      setBuilds([])
    } finally {
      setIsLoadingBuilds(false)
    }
  }

  const handleOptimize = async () => {
    if (!selectedConnectionId || !selectedPipelineName || selectedBuildNumber === null) {
      setError('Please select a connection, pipeline, and build')
      return
    }

    try {
      setIsOptimizing(true)
      setError(null)
      setOptimizationResult(null)

      const result = await connectionsApi.optimizePipeline({
        connectionId: selectedConnectionId,
        pipelineName: selectedPipelineName,
        buildNumber: selectedBuildNumber
      })

      setOptimizationResult(result)

      try {
        const analysis = JSON.parse(result.analysis)
        setParsedAnalysis(analysis)
      } catch (e) {
        console.error('Failed to parse analysis:', e)
        setParsedAnalysis(null)
      }

      try {
        const gain = JSON.parse(result.estimatedGain)
        setParsedGain(gain)
      } catch (e) {
        console.error('Failed to parse estimated gain:', e)
        setParsedGain(null)
      }

      try {
        const optimizations = JSON.parse(result.optimizations)
        setParsedOptimizations(optimizations)
      } catch (e) {
        console.error('Failed to parse optimizations:', e)
        setParsedOptimizations(null)
      }

    } catch (err: any) {
      setError(err.message || 'Failed to optimize pipeline')
    } finally {
      setIsOptimizing(false)
    }
  }

  const handleCopyScript = () => {
    if (optimizationResult?.optimizedScript) {
      navigator.clipboard.writeText(optimizationResult.optimizedScript)
      setCopiedScript(true)
      setTimeout(() => setCopiedScript(false), 2000)
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

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-foreground">Pipeline Optimization</h2>
        <p className="text-sm text-muted-foreground mt-1">Analyze and optimize your Jenkins pipelines with AI</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-900 rounded-lg text-red-700 dark:text-red-400">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="space-y-6">
        {/* Step 1: Select Connection */}
        <div className="bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-8 h-8 rounded-full bg-gradient-to-r from-blue-600 to-indigo-600 flex items-center justify-center text-white font-bold text-sm">
              1
            </div>
            <h3 className="text-lg font-semibold text-slate-900 dark:text-white">Select Connection</h3>
          </div>

          {isLoadingConnections ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
            </div>
          ) : connections.length === 0 ? (
            <div className="text-center py-8 px-4 border-2 border-dashed border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-800">
              <AlertCircle className="w-10 h-10 mx-auto mb-3 text-slate-400 dark:text-slate-500" />
              <p className="text-slate-600 dark:text-slate-400 text-sm">
                No active and tested connections available
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {connections.map((conn) => (
                <button
                  key={conn.id}
                  onClick={() => setSelectedConnectionId(conn.id)}
                  className={`p-4 rounded-lg border-2 transition-all text-left ${
                    selectedConnectionId === conn.id
                      ? 'border-blue-600 bg-blue-100 dark:bg-blue-950/50 shadow-md'
                      : 'border-slate-300 dark:border-slate-600 hover:border-blue-400 dark:hover:border-blue-700 bg-white dark:bg-slate-800/50 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                  }`}
                >
                  <div className="flex items-start gap-2 mb-2">
                    <div className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${
                      conn.testStatus === 'VALID' || conn.testStatus === 'SUCCESS'
                        ? 'bg-green-500'
                        : 'bg-yellow-500'
                    }`} />
                    <div className="flex-1 min-w-0">
                      <div className="font-semibold text-slate-900 dark:text-white truncate">
                        {conn.name}
                      </div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 truncate">
                        {conn.url}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 text-xs">
                    <span className="px-2 py-0.5 bg-green-100 dark:bg-green-950 text-green-700 dark:text-green-400 rounded">
                      Connected
                    </span>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Step 2: Select Pipeline */}
        {selectedConnectionId && (
          <div className="bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-blue-600 to-indigo-600 flex items-center justify-center text-white font-bold text-sm">
                2
              </div>
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white">Select Pipeline</h3>
            </div>

            {isLoadingPipelines ? (
              <div className="flex items-center justify-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
              </div>
            ) : pipelines.length === 0 ? (
              <div className="text-center py-8 px-4 border-2 border-dashed border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-800">
                <AlertCircle className="w-10 h-10 mx-auto mb-3 text-slate-400 dark:text-slate-500" />
                <p className="text-slate-600 dark:text-slate-400 text-sm">No pipelines found</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {pipelines.map((pipeline) => {
                  const getPipelineColor = (color: string) => {
                    if (color === 'blue') return 'bg-blue-500'
                    if (color === 'red') return 'bg-red-500'
                    if (color === 'yellow') return 'bg-yellow-500'
                    if (color === 'green') return 'bg-green-500'
                    return 'bg-gray-500'
                  }

                  const getPipelineStatus = (color: string) => {
                    if (color === 'blue') return 'Success'
                    if (color === 'red') return 'Failed'
                    if (color === 'yellow') return 'Unstable'
                    if (color === 'green') return 'Success'
                    return 'Unknown'
                  }

                  return (
                    <button
                      key={pipeline.name}
                      onClick={() => setSelectedPipelineName(pipeline.name)}
                      className={`p-4 rounded-lg border-2 transition-all text-left ${
                        selectedPipelineName === pipeline.name
                          ? 'border-blue-600 bg-blue-100 dark:bg-blue-950/50 shadow-md'
                          : 'border-slate-300 dark:border-slate-600 hover:border-blue-400 dark:hover:border-blue-700 bg-white dark:bg-slate-800/50 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                      }`}
                    >
                      <div className="flex items-start gap-2 mb-2">
                        <div className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${getPipelineColor(pipeline.color)}`} />
                        <div className="flex-1 min-w-0">
                          <div className="font-semibold text-slate-900 dark:text-white truncate">
                            {pipeline.name}
                          </div>
                        </div>
                      </div>
                      <div className="text-xs">
                        <span className={`px-2 py-0.5 rounded ${
                          pipeline.color === 'blue' || pipeline.color === 'green'
                            ? 'bg-green-100 dark:bg-green-950 text-green-700 dark:text-green-400'
                            : pipeline.color === 'red'
                            ? 'bg-red-100 dark:bg-red-950 text-red-700 dark:text-red-400'
                            : 'bg-yellow-100 dark:bg-yellow-950 text-yellow-700 dark:text-yellow-400'
                        }`}>
                          {getPipelineStatus(pipeline.color)}
                        </span>
                      </div>
                    </button>
                  )
                })}
              </div>
            )}
          </div>
        )}

        {/* Step 3: Select Build */}
        {selectedConnectionId && selectedPipelineName && (
          <div className="bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-blue-600 to-indigo-600 flex items-center justify-center text-white font-bold text-sm">
                3
              </div>
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white">Select Build</h3>
            </div>

            {isLoadingBuilds ? (
              <div className="flex items-center justify-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
              </div>
            ) : builds.length === 0 ? (
              <div className="text-center py-8 px-4 border-2 border-dashed border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-800">
                <AlertCircle className="w-10 h-10 mx-auto mb-3 text-slate-400 dark:text-slate-500" />
                <p className="text-slate-600 dark:text-slate-400 text-sm">No builds found</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {builds.map((build) => {
                  const getBuildColor = (result: string) => {
                    if (result === 'SUCCESS') return 'bg-green-500'
                    if (result === 'FAILURE') return 'bg-red-500'
                    if (result === 'UNSTABLE') return 'bg-yellow-500'
                    return 'bg-gray-500'
                  }

                  return (
                    <button
                      key={build.number}
                      onClick={() => setSelectedBuildNumber(build.number)}
                      className={`p-4 rounded-lg border-2 transition-all text-left ${
                        selectedBuildNumber === build.number
                          ? 'border-blue-600 bg-blue-100 dark:bg-blue-950/50 shadow-md'
                          : 'border-slate-300 dark:border-slate-600 hover:border-blue-400 dark:hover:border-blue-700 bg-white dark:bg-slate-800/50 hover:bg-slate-50 dark:hover:bg-slate-700/50'
                      }`}
                    >
                      <div className="flex items-start gap-2 mb-3">
                        <div className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${getBuildColor(build.result)}`} />
                        <div className="flex-1 min-w-0">
                          <div className="font-semibold text-slate-900 dark:text-white">
                            Build #{build.number}
                          </div>
                        </div>
                      </div>
                      <div className="space-y-1.5">
                        <div className="flex items-center gap-2">
                          <span className={`text-xs px-2 py-0.5 rounded ${
                            build.result === 'SUCCESS'
                              ? 'bg-green-100 dark:bg-green-950 text-green-700 dark:text-green-400'
                              : build.result === 'FAILURE'
                              ? 'bg-red-100 dark:bg-red-950 text-red-700 dark:text-red-400'
                              : 'bg-yellow-100 dark:bg-yellow-950 text-yellow-700 dark:text-yellow-400'
                          }`}>
                            {build.result}
                          </span>
                        </div>
                        <div className="text-xs text-slate-600 dark:text-slate-400">
                          Duration: <span className="font-medium">{formatDuration(build.duration)}</span>
                        </div>
                      </div>
                    </button>
                  )
                })}
              </div>
            )}
          </div>
        )}

        {/* Optimize Button */}
        {selectedConnectionId && selectedPipelineName && selectedBuildNumber !== null && (
          <div className="bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-950/30 dark:to-indigo-950/30 rounded-lg border border-blue-200 dark:border-blue-900 p-6">
            <Button
              onClick={handleOptimize}
              disabled={isOptimizing}
              className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white py-6 text-lg"
            >
              {isOptimizing ? (
                <>
                  <Loader2 className="mr-2 h-6 w-6 animate-spin" />
                  Optimizing Pipeline with AI...
                </>
              ) : (
                <>
                  <Zap className="mr-2 h-6 w-6" />
                  Optimize Pipeline with AI
                </>
              )}
            </Button>
          </div>
        )}
      </div>

      {optimizationResult && (
        <div className="space-y-6">
          {parsedGain && (
            <div className="bg-gradient-to-br from-green-50 to-emerald-50 dark:from-green-950/30 dark:to-emerald-950/30 rounded-lg border border-green-200 dark:border-green-900 p-6">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 bg-green-600 rounded-lg">
                  <TrendingDown className="w-6 h-6 text-white" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-green-900 dark:text-green-100">Estimated Performance Gain</h3>
                  <p className="text-sm text-green-700 dark:text-green-300">AI-powered optimization by {optimizationResult.provider}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-white dark:bg-slate-900 rounded-lg p-4 border border-green-200 dark:border-green-800">
                  <div className="text-sm text-slate-600 dark:text-slate-400 mb-1">Current Duration</div>
                  <div className="text-2xl font-bold text-slate-900 dark:text-white">{formatDuration(parsedGain.currentDuration)}</div>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-lg p-4 border border-green-200 dark:border-green-800">
                  <div className="text-sm text-slate-600 dark:text-slate-400 mb-1">Estimated Duration</div>
                  <div className="text-2xl font-bold text-green-600 dark:text-green-400">{formatDuration(parsedGain.estimatedDuration)}</div>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-lg p-4 border border-green-200 dark:border-green-800">
                  <div className="text-sm text-slate-600 dark:text-slate-400 mb-1">Time Saved</div>
                  <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                    {parsedGain.reductionPercentage || `${parsedGain.percentageGain?.toFixed(1)}%`}
                  </div>
                  {parsedGain.timeSaved && (
                    <div className="text-sm text-slate-600 dark:text-slate-400 mt-1">
                      (~{formatDuration(parsedGain.timeSaved)})
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {parsedAnalysis && parsedAnalysis.bottlenecks && parsedAnalysis.bottlenecks.length > 0 && (
            <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-6">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4">Detected Bottlenecks</h3>
              <div className="space-y-3">
                {parsedAnalysis.bottlenecks.map((bottleneck, index) => (
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

          {parsedOptimizations && parsedOptimizations.length > 0 && (
            <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-6">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4">Recommended Optimizations</h3>
              <div className="space-y-3">
                {parsedOptimizations.map((opt, index) => (
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

          <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white">Optimized Pipeline Script</h3>
              <Button
                onClick={handleCopyScript}
                variant="outline"
                className="flex items-center gap-2"
              >
                {copiedScript ? (
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
                {optimizationResult.optimizedScript}
              </pre>
            </div>
          </div>
        </div>
      )}

      {!optimizationResult && !isOptimizing && (
        <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 p-12 text-center">
          <Zap className="w-16 h-16 mx-auto mb-4 text-slate-300 dark:text-slate-600" />
          <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No Optimization Yet</h3>
          <p className="text-sm text-slate-600 dark:text-slate-400">
            Select a connection, pipeline, and build above to start optimizing your Jenkins pipeline with AI.
          </p>
        </div>
      )}
    </div>
  )
}
