import { apiClient } from './api-client'
import { AxiosError } from 'axios'

// Types pour l'authentification
export interface RegisterRequest {
  email: string
  username: string
  password: string
}

export interface RegisterResponse {
  message: string
  email: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  csrfToken: string
}

export interface LogoutResponse {
  message: string
}

export interface AuthError {
  message: string
  email?: string
  password?: string
}

class AuthApi {
  /**
   * Inscription d'un nouvel utilisateur
   */
  async register(data: RegisterRequest): Promise<RegisterResponse> {
    try {
      const response = await apiClient.post<RegisterResponse>('/api/auth/register', data)
      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Erreur lors de l\'inscription' }
      }
      throw { message: 'Erreur réseau' }
    }
  }

  /**
   * Connexion d'un utilisateur
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await apiClient.post<LoginResponse>('/api/auth/login', data)

      // Stocker les tokens après un login réussi
      apiClient.setTokens(response.accessToken, response.csrfToken)

      return response
    } catch (error) {
      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Erreur lors de la connexion' }
      }
      throw { message: 'Erreur réseau' }
    }
  }

  /**
   * Déconnexion de l'utilisateur
   */
  async logout(): Promise<LogoutResponse> {
    try {
      const response = await apiClient.post<LogoutResponse>('/api/auth/logout')

      // Supprimer les tokens après logout
      apiClient.clearTokens()

      return response
    } catch (error) {
      // Même en cas d'erreur, on supprime les tokens locaux
      apiClient.clearTokens()

      if (error instanceof AxiosError) {
        throw error.response?.data || { message: 'Erreur lors de la déconnexion' }
      }
      throw { message: 'Erreur réseau' }
    }
  }

  /**
   * Vérifie si l'utilisateur est authentifié
   */
  isAuthenticated(): boolean {
    return apiClient.isAuthenticated()
  }

  /**
   * Récupère le token d'accès
   */
  getAccessToken(): string | null {
    return apiClient.getToken()
  }

  /**
   * Récupère le CSRF token
   */
  getCsrfToken(): string | null {
    return apiClient.getCsrfToken()
  }

  /**
   * Nettoie les tokens (pour logout manuel)
   */
  clearTokens(): void {
    apiClient.clearTokens()
  }
}

// Export une instance unique
export const authApi = new AuthApi()
