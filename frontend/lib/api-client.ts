import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios'

const BASE_URL = 'http://localhost:8085'

class ApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true, // Important pour les cookies HttpOnly
    })

    // Intercepteur de requête pour ajouter les tokens
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token')

        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }

        // Ajouter CSRF token pour POST/PUT/DELETE/PATCH
        if (config.method && ['post', 'put', 'delete', 'patch'].includes(config.method.toLowerCase())) {
          const csrfToken = localStorage.getItem('csrf')
          if (csrfToken) {
            config.headers['X-CSRF-Token'] = csrfToken
          }
        }

        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // Intercepteur de réponse pour gérer les erreurs
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401) {
          // Token expiré ou invalide
          this.clearTokens()
          if (typeof window !== 'undefined') {
            window.location.href = '/auth/login'
          }
        }

        if (error.response?.status === 403) {
          // CSRF invalide - pourrait nécessiter un re-login
          console.error('CSRF token invalide')
        }

        return Promise.reject(error)
      }
    )
  }

  // Méthodes HTTP de base
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.get<T>(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.post<T>(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.put<T>(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.delete<T>(url, config)
    return response.data
  }

  async patch<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.patch<T>(url, data, config)
    return response.data
  }

  // Gestion des tokens
  setTokens(accessToken: string, csrfToken: string): void {
    localStorage.setItem('token', accessToken)
    localStorage.setItem('csrf', csrfToken)
  }

  clearTokens(): void {
    localStorage.removeItem('token')
    localStorage.removeItem('csrf')
    localStorage.removeItem('user_email')
  }

  getToken(): string | null {
    return localStorage.getItem('token')
  }

  getCsrfToken(): string | null {
    return localStorage.getItem('csrf')
  }

  isAuthenticated(): boolean {
    return !!this.getToken()
  }
}

// Export une instance unique (singleton)
export const apiClient = new ApiClient()
