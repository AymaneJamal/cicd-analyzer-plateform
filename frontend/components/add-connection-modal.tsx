"use client"

import type React from "react"

import { useState } from "react"
import { X, TestTube, Loader2, CheckCircle2, XCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import { connectionsApi } from "@/lib/connections-api"

interface AddConnectionModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: () => void
}

export default function AddConnectionModal({ isOpen, onClose, onSuccess }: AddConnectionModalProps) {
  const [formData, setFormData] = useState({
    name: "",
    url: "",
    username: "",
    password: "",
  })
  const [isLoading, setIsLoading] = useState(false)
  const [isTesting, setIsTesting] = useState(false)
  const [testResult, setTestResult] = useState<{ status?: string; message: string } | null>(null)
  const [error, setError] = useState<string | null>(null)

  const handleTestCredentials = async () => {
    if (!formData.name || !formData.url || !formData.username || !formData.password) {
      setError('Please fill in all fields before testing')
      return
    }

    try {
      setIsTesting(true)
      setError(null)
      setTestResult(null)

      const result = await connectionsApi.testCredentials(formData)

      // Backend returns { status: 'SUCCESS' | 'FAILURE', message: '...' }
      const isSuccess = result.status === 'SUCCESS'

      // Clear error if test was successful
      if (isSuccess) {
        setError(null)
      }

      setTestResult(result)
    } catch (err: any) {
      setTestResult(null)
      setError(err.message || 'Failed to test credentials')
    } finally {
      setIsTesting(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.name || !formData.url || !formData.username || !formData.password) {
      setError('Please fill in all fields')
      return
    }

    try {
      setIsLoading(true)
      setError(null)

      await connectionsApi.createConnection(formData)

      // Reset form
      setFormData({
        name: "",
        url: "",
        username: "",
        password: "",
      })
      setTestResult(null)

      onSuccess()
      onClose()
    } catch (err: any) {
      setError(err.message || 'Failed to create connection')
    } finally {
      setIsLoading(false)
    }
  }

  const handleClose = () => {
    setFormData({
      name: "",
      url: "",
      username: "",
      password: "",
    })
    setTestResult(null)
    setError(null)
    onClose()
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 max-w-md w-full p-6 shadow-xl">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-semibold text-slate-900 dark:text-white">Add Jenkins Connection</h3>
          <button
            onClick={handleClose}
            className="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
          >
            <X size={20} className="text-slate-500 dark:text-slate-400" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Error Message - only show if no test result */}
          {error && !testResult && (
            <div className="flex items-center gap-2 px-3 py-2 bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-900 rounded-lg text-red-700 dark:text-red-400 text-sm">
              <XCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {/* Test Result */}
          {testResult && (
            <div className={`flex items-center gap-2 px-3 py-2 border rounded-lg text-sm ${
              testResult.status === 'SUCCESS'
                ? 'bg-green-50 dark:bg-green-950/50 border-green-200 dark:border-green-900 text-green-700 dark:text-green-400'
                : 'bg-red-50 dark:bg-red-950/50 border-red-200 dark:border-red-900 text-red-700 dark:text-red-400'
            }`}>
              {testResult.status === 'SUCCESS' ? <CheckCircle2 size={16} /> : <XCircle size={16} />}
              <span>{testResult.message}</span>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
              Connection Name
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              placeholder="Jenkins Local"
              disabled={isLoading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
              Server URL
            </label>
            <input
              type="text"
              value={formData.url}
              onChange={(e) => setFormData({ ...formData, url: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              placeholder="http://localhost:8080"
              disabled={isLoading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
              Username
            </label>
            <input
              type="text"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              placeholder="admin"
              disabled={isLoading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
              Password
            </label>
            <input
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              placeholder="••••••••"
              disabled={isLoading}
            />
          </div>

          {/* Test Connection Button */}
          <div className="pt-2">
            <Button
              type="button"
              onClick={handleTestCredentials}
              disabled={isTesting || isLoading}
              variant="outline"
              className="w-full border-blue-600 dark:border-blue-500 text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950"
            >
              {isTesting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Testing connection...
                </>
              ) : (
                <>
                  <TestTube className="mr-2 h-4 w-4" />
                  Test Connection
                </>
              )}
            </Button>
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="button"
              onClick={handleClose}
              variant="outline"
              className="flex-1"
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              className="flex-1 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white"
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Adding...
                </>
              ) : (
                'Add Connection'
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
