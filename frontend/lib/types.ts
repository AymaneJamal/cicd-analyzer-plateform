export interface User {
  email: string
  username: string
  id: string
}

export interface JenkinsConnection {
  id: string
  name: string
  url: string
  username: string
  password: string
  createdAt: string
}

export interface Pipeline {
  id: string
  name: string
  description: string
  status: "success" | "failure" | "running"
}

export interface Build {
  id: string
  number: number
  status: "SUCCESS" | "FAILURE" | "RUNNING"
  duration: number
  timestamp: string
}

export interface Optimization {
  id: string
  connectionId: string
  pipelineName: string
  buildNumber: number
  status: "completed" | "in_progress" | "failed"
  createdAt: string
  provider: "groq" | "gemini" | "openai"
  analysis: string
  optimizations: string[]
  estimatedGain: number
  optimizedScript: string
}
