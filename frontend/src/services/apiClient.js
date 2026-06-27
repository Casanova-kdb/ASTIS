import axios from 'axios'

export const AUTH_TOKEN_STORAGE_KEY = 'astis_access_token'
export const AUTH_USER_STORAGE_KEY = 'astis_user'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000
})

export function getStoredToken() {
  return localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
}

export function getStoredUser() {
  const rawUser = localStorage.getItem(AUTH_USER_STORAGE_KEY)

  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser)
  } catch {
    localStorage.removeItem(AUTH_USER_STORAGE_KEY)
    return null
  }
}

export function setStoredAuth(authData) {
  if (!authData?.accessToken) {
    return
  }

  localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, authData.accessToken)

  if (authData.user) {
    localStorage.setItem(AUTH_USER_STORAGE_KEY, JSON.stringify(authData.user))
  }
}

export function clearStoredAuth() {
  localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
  localStorage.removeItem(AUTH_USER_STORAGE_KEY)
}

export function getApiErrorMessage(error) {
  return error?.response?.data?.message || error?.message || 'Request failed'
}

apiClient.interceptors.request.use((config) => {
  const token = getStoredToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error)
)

export default apiClient
