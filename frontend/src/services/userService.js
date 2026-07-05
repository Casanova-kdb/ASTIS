import apiClient, { getStoredToken, setStoredAuth } from './apiClient'

export function fetchCurrentUser() {
  return apiClient.get('/users/me')
}

export async function updateCurrentUser(payload) {
  const response = await apiClient.put('/users/me', payload)

  if (response.data) {
    setStoredAuth({ accessToken: getStoredToken(), user: response.data })
  }

  return response
}

export function changePassword(payload) {
  return apiClient.put('/users/me/password', payload)
}
