import apiClient from './apiClient'

export function parseHandbook(file) {
  const formData = new FormData()
  formData.append('file', file)

  return apiClient.post('/handbooks/parse', formData, {
    timeout: 210000
  })
}
