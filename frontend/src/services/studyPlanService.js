import apiClient from './apiClient'

export function fetchStudyPlan(days = 7) {
  return apiClient.get('/study-plans', {
    params: {
      days
    }
  })
}
