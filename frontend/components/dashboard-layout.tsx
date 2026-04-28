"use client"

import type { ReactNode } from "react"
import { Zap, Plug, History, LogOut, Menu, X } from "lucide-react"
import { useState } from "react"

interface DashboardLayoutProps {
  children: ReactNode
  userEmail: string
  activeTab: "optimize" | "connections" | "history"
  onTabChange: (tab: "optimize" | "connections" | "history") => void
  onLogout: () => void
}

export default function DashboardLayout({
  children,
  userEmail,
  activeTab,
  onTabChange,
  onLogout,
}: DashboardLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const username = userEmail.split("@")[0]

  const navItems = [
    { id: "optimize", label: "Optimiser", icon: Zap, description: "Optimiser vos pipelines" },
    { id: "connections", label: "Connexions", icon: Plug, description: "Gérer vos connexions Jenkins" },
    { id: "history", label: "Historique", icon: History, description: "Voir vos optimisations" },
  ]

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <aside
        className={`${
          sidebarOpen ? "w-72" : "w-0"
        } bg-card border-r border-border flex flex-col transition-all duration-300 overflow-hidden`}
      >
        {/* Logo Section */}
        <div className="p-6 border-b border-border">
          <div className="flex items-center gap-3 mb-1">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <Zap size={18} className="text-primary-foreground" />
            </div>
            <h1 className="text-lg font-bold text-foreground">CI/CD Optimizer</h1>
          </div>
          <p className="text-xs text-muted-foreground ml-11">Powered by Groq AI</p>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive = activeTab === item.id
            return (
              <button
                key={item.id}
                onClick={() => onTabChange(item.id as "optimize" | "connections" | "history")}
                className={`w-full text-left px-4 py-3 rounded-lg transition-all duration-200 flex items-start gap-3 group ${
                  isActive
                    ? "bg-primary text-primary-foreground shadow-lg shadow-primary/20"
                    : "text-foreground hover:bg-accent/50"
                }`}
              >
                <Icon
                  size={20}
                  className={`flex-shrink-0 mt-0.5 ${isActive ? "text-primary-foreground" : "text-muted-foreground group-hover:text-foreground"}`}
                />
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-sm">{item.label}</p>
                  <p className={`text-xs ${isActive ? "text-primary-foreground/70" : "text-muted-foreground"}`}>
                    {item.description}
                  </p>
                </div>
              </button>
            )
          })}
        </nav>

        {/* User Section */}
        <div className="p-4 border-t border-border space-y-3">
          <div className="px-2">
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Compte</p>
            <p className="text-sm font-medium text-foreground mt-2 truncate">@{username}</p>
            <p className="text-xs text-muted-foreground truncate">{userEmail}</p>
          </div>
          <button
            onClick={onLogout}
            className="w-full px-4 py-2 rounded-lg bg-destructive/10 text-destructive hover:bg-destructive/20 transition-colors flex items-center justify-center gap-2 font-medium text-sm"
          >
            <LogOut size={16} />
            Déconnexion
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Navbar */}
        <header className="bg-card border-b border-border px-6 py-4 flex items-center gap-4 h-16 flex-shrink-0">
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 hover:bg-accent/50 rounded-lg transition-colors text-foreground lg:hidden"
          >
            {sidebarOpen ? <X size={24} /> : <Menu size={24} />}
          </button>

          {/* Page Title */}
          <div className="flex-1">
            <h2 className="text-xl font-bold text-foreground">
              {navItems.find((item) => item.id === activeTab)?.label}
            </h2>
          </div>

          {/* User Avatar */}
          <div className="flex items-center gap-3 border-l border-border pl-4">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-medium text-foreground">@{username}</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary/60 flex items-center justify-center text-primary-foreground font-bold text-sm">
              {username.charAt(0).toUpperCase()}
            </div>
          </div>
        </header>

        {/* Content Area */}
        <main className="flex-1 overflow-auto">{children}</main>
      </div>
    </div>
  )
}
