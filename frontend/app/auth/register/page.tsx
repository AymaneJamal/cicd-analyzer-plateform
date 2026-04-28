"use client"

import type React from "react"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { authApi } from "@/lib/auth-api"
import { Eye, EyeOff, Lock, Mail, User, Loader2, UserPlus, CheckCircle2, XCircle } from "lucide-react"

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    passwordConfirm: "",
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null)

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setNotification(null)

    try {
      // Validations
      if (!formData.username || !formData.email || !formData.password || !formData.passwordConfirm) {
        setNotification({ type: 'error', message: 'Please fill in all fields' })
        setIsLoading(false)
        return
      }

      if (formData.password.length < 8) {
        setNotification({ type: 'error', message: 'Password must be at least 8 characters' })
        setIsLoading(false)
        return
      }

      if (formData.password !== formData.passwordConfirm) {
        setNotification({ type: 'error', message: 'Passwords do not match' })
        setIsLoading(false)
        return
      }

      // API Call
      await authApi.register({
        email: formData.email,
        username: formData.username,
        password: formData.password,
      })

      // Success - show notification and redirect
      setNotification({ type: 'success', message: 'Account created! Redirecting to login...' })

      // Redirect
      setTimeout(() => {
        window.location.href = "/auth/login"
      }, 1000)
    } catch (error: any) {
      setNotification({ type: 'error', message: error.message || 'An error occurred during registration' })
      setIsLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 p-4">
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

      <div className="w-full max-w-md">
        {/* Logo & Title */}
        <div className="text-center mb-8 space-y-2">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 mb-4 shadow-lg shadow-blue-500/50">
            <UserPlus className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 dark:from-blue-400 dark:to-indigo-400 bg-clip-text text-transparent">
            Pipeline Optimizer
          </h1>
          <p className="text-slate-600 dark:text-slate-400 text-lg">
            Create your free account
          </p>
        </div>

        {/* Registration Card */}
        <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl rounded-2xl border border-slate-200 dark:border-slate-800 p-8 shadow-2xl shadow-slate-200/50 dark:shadow-slate-950/50">
          <div className="mb-6">
            <h2 className="text-2xl font-semibold text-slate-900 dark:text-white">
              Sign Up
            </h2>
            <p className="text-slate-600 dark:text-slate-400 mt-1">
              Join us to get started
            </p>
          </div>

          <form onSubmit={handleRegister} className="space-y-5">
            {/* Username */}
            <div className="space-y-2">
              <Label htmlFor="username" className="text-slate-700 dark:text-slate-300 font-medium">
                Username
              </Label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <Input
                  id="username"
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  className="pl-10 h-12 bg-slate-50 dark:bg-slate-800 border-slate-300 dark:border-slate-700 focus:border-blue-500 focus:ring-blue-500 text-slate-900 dark:text-slate-100"
                  placeholder="devops_master"
                  disabled={isLoading}
                />
              </div>
            </div>

            {/* Email */}
            <div className="space-y-2">
              <Label htmlFor="email" className="text-slate-700 dark:text-slate-300 font-medium">
                Email
              </Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <Input
                  id="email"
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="pl-10 h-12 bg-slate-50 dark:bg-slate-800 border-slate-300 dark:border-slate-700 focus:border-blue-500 focus:ring-blue-500 text-slate-900 dark:text-slate-100"
                  placeholder="hello@example.com"
                  disabled={isLoading}
                />
              </div>
            </div>

            {/* Password */}
            <div className="space-y-2">
              <Label htmlFor="password" className="text-slate-700 dark:text-slate-300 font-medium">
                Password
              </Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="pl-10 pr-10 h-12 bg-slate-50 dark:bg-slate-800 border-slate-300 dark:border-slate-700 focus:border-blue-500 focus:ring-blue-500 text-slate-900 dark:text-slate-100"
                  placeholder="••••••••"
                  disabled={isLoading}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition-colors"
                  disabled={isLoading}
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Minimum 8 characters
              </p>
            </div>

            {/* Confirm Password */}
            <div className="space-y-2">
              <Label htmlFor="passwordConfirm" className="text-slate-700 dark:text-slate-300 font-medium">
                Confirm Password
              </Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <Input
                  id="passwordConfirm"
                  type={showPasswordConfirm ? "text" : "password"}
                  name="passwordConfirm"
                  value={formData.passwordConfirm}
                  onChange={handleChange}
                  className="pl-10 pr-10 h-12 bg-slate-50 dark:bg-slate-800 border-slate-300 dark:border-slate-700 focus:border-blue-500 focus:ring-blue-500 text-slate-900 dark:text-slate-100"
                  placeholder="••••••••"
                  disabled={isLoading}
                />
                <button
                  type="button"
                  onClick={() => setShowPasswordConfirm(!showPasswordConfirm)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition-colors"
                  disabled={isLoading}
                >
                  {showPasswordConfirm ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Sign Up Button */}
            <Button
              type="submit"
              disabled={isLoading}
              className="w-full h-12 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold rounded-xl shadow-lg shadow-blue-500/30 transition-all duration-200 hover:shadow-xl hover:shadow-blue-500/40"
            >
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  Creating account...
                </>
              ) : (
                "Create Account"
              )}
            </Button>
          </form>

          {/* Separator */}
          <div className="mt-8 pt-6 border-t border-slate-200 dark:border-slate-800 text-center">
            <p className="text-slate-600 dark:text-slate-400">
              Already have an account?{" "}
              <Link
                href="/auth/login"
                className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-semibold hover:underline transition-colors"
              >
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
