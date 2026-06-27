import apiClient from './apiClient'

export function fetchAnalyticsSummary() {
  return apiClient.get('/analytics/summary')
}
