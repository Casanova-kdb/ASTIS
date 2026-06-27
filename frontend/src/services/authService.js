import apiClient, {
  clearStoredAuth,
  getStoredToken,
  getStoredUser,
  setStoredAuth
} from './apiClient'

export async function register(payload) {
  const response = await apiClient.post('/auth/register', payload)
  setStoredAuth(response.data)
  return response
}

export async function login(payload) {
  const response = await apiClient.post('/auth/login', payload)
  setStoredAuth(response.data)
  return response
}

export function logout() {
  clearStoredAuth()
}

export function isAuthenticated() {
  return Boolean(getStoredToken())
}

export function getCurrentUser() {
  return getStoredUser()
}
