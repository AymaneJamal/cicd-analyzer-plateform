import { apiClient } from './api-client'
import { AxiosError } from 'axios'

// History Interfaces
export interface OptimizationHistory {
  id: number
  pipelineName: string
  buildNumber: number
  completedAt: string
  status: 'COMPLETED' | 'FAILED'
  llmProvider: string
  originalScript: string
  optimizedScript: string | null
  analysisJson: string | null // JSON string to parse
  optimizationsJson: string | null // JSON string to parse
  estimatedGainJson: string | null // JSON string to parse
  currentDurationMs: number
  estimatedDurationMs: number | null
  reductionPercentage: number | null
  errorMessage: string | null
}

export interface AnalysisData {
  bottlenecks: Array<{
    stage: string
    duration: number
    percentage?: number
    issue: string
  }>
  detectedIssues?: string[]
  totalIssues?: number
}

export interface OptimizationData {
  priority?: number
  title?: string
  description: string
  estimatedGain?: string
  type?: string
  impact?: string
}

export interface EstimatedGainData {
  currentDuration: number
  estimatedDuration: number
  timeSaved?: number
  percentageGain?: number
  reductionPercentage?: string
}

class HistoryApi {
  /**
   * Get all optimization history for the current user
   */
  async getMyHistory(): Promise<OptimizationHistory[]> {
    try {
      const response = await apiClient.get<OptimizationHistory[]>('/api/optimization/history/me')
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching history' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Get optimization history by connection ID
   */
  async getHistoryByConnection(connectionId: number): Promise<OptimizationHistory[]> {
    try {
      const response = await apiClient.get<OptimizationHistory[]>(
        `/api/optimization/history?connectionId=${connectionId}`
      )
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching history by connection' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Get optimization history by pipeline
   */
  async getHistoryByPipeline(connectionId: number, pipelineName: string): Promise<OptimizationHistory[]> {
    try {
      const response = await apiClient.get<OptimizationHistory[]>(
        `/api/optimization/history/pipeline?connectionId=${connectionId}&pipelineName=${encodeURIComponent(pipelineName)}`
      )
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching history by pipeline' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Get single optimization history by ID
   */
  async getHistoryById(id: number): Promise<OptimizationHistory> {
    try {
      const response = await apiClient.get<OptimizationHistory>(`/api/optimization/history/${id}`)
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching history details' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Parse analysis JSON string
   */
  parseAnalysis(analysisJson: string | null): AnalysisData | null {
    if (!analysisJson) return null
    try {
      return JSON.parse(analysisJson)
    } catch (e) {
      console.error('Failed to parse analysis JSON:', e)
      return null
    }
  }

  /**
   * Parse optimizations JSON string
   */
  parseOptimizations(optimizationsJson: string | null): OptimizationData[] | null {
    if (!optimizationsJson) return null
    try {
      return JSON.parse(optimizationsJson)
    } catch (e) {
      console.error('Failed to parse optimizations JSON:', e)
      return null
    }
  }

  /**
   * Parse estimated gain JSON string
   */
  parseEstimatedGain(estimatedGainJson: string | null): EstimatedGainData | null {
    if (!estimatedGainJson) return null
    try {
      return JSON.parse(estimatedGainJson)
    } catch (e) {
      console.error('Failed to parse estimated gain JSON:', e)
      return null
    }
  }
}

// Export singleton instance
export const historyApi = new HistoryApi()
