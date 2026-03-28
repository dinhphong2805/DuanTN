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

export async function deleteOrder(id) {
  const { data } = await client.delete(`/orders/${id}`)
  return data
}

export async function checkOrderStatus(id) {
  const { data } = await client.get(`/orders/${id}/status`)
  return data
}