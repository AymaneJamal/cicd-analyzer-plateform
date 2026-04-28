"use client"

import { useState, useEffect } from "react"
import DashboardLayout from "@/components/dashboard-layout"
import ConnectionsList from "@/components/connections-list"
import OptimizationPanel from "@/components/optimization-panel"
import HistoryPanel from "@/components/history-panel"
import { authApi } from "@/lib/auth-api"

export default function DashboardPage() {
  const [activeTab, setActiveTab] = useState<"optimize" | "connections" | "history">("optimize")
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [userEmail, setUserEmail] = useState("")

  useEffect(() => {
    console.log('Dashboard: Checking authentication...')
    const token = localStorage.getItem("token")
    const email = localStorage.getItem("user_email")

    console.log('Dashboard: Token found:', !!token)
    console.log('Dashboard: Email found:', !!email)

    if (!token) {
      console.log('Dashboard: No token, redirecting to login...')
      window.location.href = "/auth/login"
    } else {
      console.log('Dashboard: Authenticated, showing dashboard')
      setIsAuthenticated(true)
      setUserEmail(email || "user@example.com")
    }
  }, [])

  if (!isAuthenticated) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="animate-spin rounded-full h-12 w-12 border-2 border-primary border-t-transparent"></div>
      </div>
    )
  }

  const handleLogout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      window.location.href = "/auth/login"
    }
  }

  return (
    <DashboardLayout userEmail={userEmail} activeTab={activeTab} onTabChange={setActiveTab} onLogout={handleLogout}>
      {activeTab === "optimize" && <OptimizationPanel />}
      {activeTab === "connections" && <ConnectionsList />}
      {activeTab === "history" && <HistoryPanel />}
    </DashboardLayout>
  )
}
