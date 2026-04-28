"use client"

import { useEffect, useState } from "react"
import LoginPage from "@/app/auth/login/page"

export default function Home() {
  const [isLoading, setIsLoading] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    console.log('Home: Checking authentication...')
    const token = localStorage.getItem("token")
    console.log('Home: Token found:', !!token)

    if (token) {
      console.log('Home: User authenticated, redirecting to dashboard...')
      setIsAuthenticated(true)
      window.location.href = "/dashboard"
    } else {
      console.log('Home: No token, showing login page')
      setIsAuthenticated(false)
    }
    setIsLoading(false)
  }, [])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="animate-spin rounded-full h-12 w-12 border-2 border-primary border-t-transparent"></div>
      </div>
    )
  }

  return isAuthenticated ? null : <LoginPage />
}
