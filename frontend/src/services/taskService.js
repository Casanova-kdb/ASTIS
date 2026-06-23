import apiClient from './apiClient'

export function fetchTasks() {
  return apiClient.get('/tasks')
}

export function fetchRecommendations() {
  return apiClient.get('/tasks/recommendations')
}
