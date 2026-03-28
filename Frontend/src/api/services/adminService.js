/**
 * Admin API - kết nối backend
 */
import axios from 'axios'
import client from '../client'
import { API_BASE_URL } from '../config'
import { useAuthStore } from '../../authStore'

export async function getStats() {
  const { data } = await client.get('admin/stats')
  return data
}

export async function getRevenueReport(limit = 10, range = {}) {
  const params = { limit, ...range }
  const { data } = await client.get('admin/revenue-report', { params })
  return data
}

export async function getAnalytics(range = {}) {
  const { data } = await client.get('admin/analytics', { params: range })
  return data
}

/** Thống kê tổng hợp: periodCards, bestSelling, orderStatusPie, growthPanel, lowStock */
export async function getStatistics(params = {}) {
  const { data } = await client.get('admin/statistics', { params })
  return data
}

export async function getProducts() {
  const { data } = await client.get('admin/products')
  return data
}

export async function getProduct(id) {
  const { data } = await client.get(`admin/products/${id}`)
  return data
}

export async function createProduct(product) {
  const { data } = await client.post('admin/products', product)
  return data
}

export async function updateProduct(id, product) {
  const { data } = await client.put(`admin/products/${id}`, product)
  return data
}

export async function deleteProduct(id) {
  await client.delete(`admin/products/${id}`)
}

export async function getOrders() {
  const { data } = await client.get('admin/orders')
  return data
}

export async function getOrder(id) {
  const { data } = await client.get(`admin/orders/${id}`)
  return data
}

export async function updateOrderStatus(id, status) {
  const { data } = await client.patch(`admin/orders/${id}/status`, { status })
  return data
}

export async function getUsers() {
  const { data } = await client.get('admin/users')
  return data
}

export async function getVouchers() {
  const { data } = await client.get('admin/vouchers')
  return data
}

export async function createVoucher(voucher) {
  const { data } = await client.post('admin/vouchers', voucher)
  return data
}

export async function deleteVoucher(id) {
  await client.delete(`admin/vouchers/${id}`)
}

export async function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  const token = useAuthStore().state?.token
  const { data } = await axios.post(API_BASE_URL + '/admin/upload', formData, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    timeout: 15000,
    // Không set Content-Type - browser tự thêm multipart/form-data + boundary
  })
  return data.url || null
}
