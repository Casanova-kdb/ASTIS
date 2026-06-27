import apiClient from './apiClient'

export function createTask(payload) {
  return apiClient.post('/tasks', payload)
}

export function fetchTasks(status) {
  const params = status ? { status } : undefined
  return apiClient.get('/tasks', { params })
}

export function fetchTaskById(taskId) {
  return apiClient.get(`/tasks/${taskId}`)
}

export function updateTask(taskId, payload) {
  return apiClient.put(`/tasks/${taskId}`, payload)
}

export function updateTaskStatus(taskId, status) {
  return apiClient.patch(`/tasks/${taskId}/status`, { status })
}

export function deleteTask(taskId) {
  return apiClient.delete(`/tasks/${taskId}`)
}
