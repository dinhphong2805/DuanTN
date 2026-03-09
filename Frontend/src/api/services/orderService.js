/**
 * Order API - kết nối backend
 */
import client from '../client'

export async function createOrder(payload) {
  const { data } = await client.post('/orders', payload)
  return data
}

export async function getOrdersByUser(userId) {
  const { data } = await client.get(`/orders/user/${userId}`)
  return data
}
