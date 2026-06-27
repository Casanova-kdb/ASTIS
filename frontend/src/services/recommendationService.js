import apiClient from './apiClient'

export function fetchRecommendedTasks() {
  return apiClient.get('/recommendations/tasks')
}

export function fetchStudyAdvice() {
  return apiClient.get('/recommendations/advice')
}
