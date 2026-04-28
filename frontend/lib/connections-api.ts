import { apiClient } from './api-client'
import { AxiosError } from 'axios'

// Types for Jenkins Connections
export interface CreateConnectionRequest {
  name: string
  url: string
  username: string
  password: string
}

export interface Connection {
  id: number
  name: string
  url: string
  username: string
  isActive: boolean
  testStatus: 'NOT_TESTED' | 'VALID' | 'INVALID' | 'SUCCESS' | 'FAILURE'
  lastTestedAt: string | null
  createdAt: string
}

export interface TestCredentialsRequest {
  name: string
  url: string
  username: string
  password: string
}

export interface TestCredentialsResponse {
  status?: string
  message: string
}

export interface ToggleActiveRequest {
  isActive: boolean
}

export interface Pipeline {
  name: string
  url: string
  color: string
}

export interface Build {
  number: number
  url: string
  result: string
  duration: number
  timestamp: number
}

export interface OptimizeRequest {
  connectionId: number
  pipelineName: string
  buildNumber: number
}

export interface Bottleneck {
  stage: string
  duration: number
  percentage?: number
  issue: string
}

export interface EstimatedGain {
  currentDuration: number
  estimatedDuration: number
  timeSaved?: number
  percentageGain?: number
  reductionPercentage?: string
}

export interface Optimization {
  type?: string
  priority?: number
  title?: string
  description: string
  impact?: string
  estimatedGain?: string
}

export interface OptimizeResponse {
  optimizedScript: string
  analysis: string // JSON string
  estimatedGain: string // JSON string
  optimizations: string // JSON string
  provider: string
}

class ConnectionsApi {
  /**
   * Create a new Jenkins connection
   */
  async createConnection(data: CreateConnectionRequest): Promise<Connection> {
    try {
      const response = await apiClient.post<Connection>('/api/connections', data)
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error creating connection' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Get all connections
   */
  async getAllConnections(): Promise<Connection[]> {
    try {
      const response = await apiClient.get<Connection[]>('/api/connections')
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching connections' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Get active connections only
   */
  async getActiveConnections(): Promise<Connection[]> {
    try {
      const response = await apiClient.get<Connection[]>('/api/connections/active')
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching active connections' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Test connection credentials (before creating)
   */
  async testCredentials(data: TestCredentialsRequest): Promise<TestCredentialsResponse> {
    try {
      const response = await apiClient.post<TestCredentialsResponse>('/api/connections/test-credentials', data)
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error testing credentials' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Test existing connection
   */
  async testConnection(id: number): Promise<TestCredentialsResponse> {
    try {
      const response = await apiClient.post<TestCredentialsResponse>(`/api/connections/${id}/test`)
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error testing connection' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Toggle connection active status
   */
  async toggleConnection(id: number, isActive: boolean): Promise<Connection> {
    try {
      const response = await apiClient.put<Connection>(`/api/connections/${id}/toggle`, { isActive })
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error toggling connection' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Delete connection
   */
  async deleteConnection(id: number): Promise<void> {
    try {
      await apiClient.delete(`/api/connections/${id}`)
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error deleting connection' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Get pipelines for a connection
   */
  async getPipelines(connectionId: number): Promise<Pipeline[]> {
    try {
      const response = await apiClient.get<Pipeline[]>(`/api/pipelines?connectionId=${connectionId}`)
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching pipelines' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Get builds for a pipeline
   */
  async getPipelineBuilds(pipelineName: string, connectionId: number): Promise<Build[]> {
    try {
      const response = await apiClient.get<Build[]>(
        `/api/pipelines/${pipelineName}/builds?connectionId=${connectionId}`
      )
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error fetching pipeline builds' }
      }
      throw { message: 'Network error' }
    }
  }

  /**
   * Optimize pipeline
   */
  async optimizePipeline(data: OptimizeRequest): Promise<OptimizeResponse> {
    try {
      const response = await apiClient.post<OptimizeResponse>('/api/optimization/optimize', data)
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Error optimizing pipeline' }
      }
      throw { message: 'Network error' }
    }
  }
}

// Export singleton instance
export const connectionsApi = new ConnectionsApi()
