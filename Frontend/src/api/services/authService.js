/**
 * Auth API - kết nối Spring Boot backend
 */
import client from '../client'

export async function login(email, password) {
  const { data } = await client.post('/auth/login', { email, password })
  return data
}

export async function register(body) {
  const { data } = await client.post('/auth/register', body)
  return data
}

export async function forgotPassword(email) {
  const { data } = await client.post('/auth/forgot-password', { email })
  return data
}

export async function resetPassword(email, code, newPassword) {
  const { data } = await client.post('/auth/reset-password', {
    email,
    code,
    newPassword,
  })
  return data
}
