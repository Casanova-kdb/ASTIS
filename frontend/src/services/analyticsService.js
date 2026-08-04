import apiClient from './apiClient'

export function fetchAnalyticsSummary() {
  return apiClient.get('/analytics/summary')
}

export function fetchAnalyticsTrends(weeks = 8) {
  return apiClient.get('/analytics/trends', {
    params: { weeks }
  })
}
