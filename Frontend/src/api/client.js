import axios from 'axios'
import { API_BASE_URL } from './config'
import { useAuthStore } from '../authStore'

const client = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  const token = useAuthStore().state?.token
  if (token) config.headers.Authorization = `Bearer ${token}`
  // FormData: bỏ Content-Type để browser tự đặt multipart/form-data + boundary
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) useAuthStore().logout()
    return Promise.reject(err)
  }
)

export default client
