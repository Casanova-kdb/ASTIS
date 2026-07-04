import apiClient from './apiClient'

export function fetchUserProfile() {
  return apiClient.get('/settings/profile')
}

export function updateUserProfile(payload) {
  return apiClient.put('/settings/profile', payload)
}
