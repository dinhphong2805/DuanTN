/**
 * Product API - chỉ dùng dữ liệu từ backend
 */
import client from '../client'

/** Lấy danh sách sản phẩm từ backend */
export async function getProductsFromApi() {
  const { data } = await client.get('/products')
  return data || []
}

/** Lấy chi tiết sản phẩm từ backend */
export async function getProductByIdFromApi(id) {
  const { data } = await client.get(`/products/${id}`)
  return data
}

/** Lấy tất cả tên thương hiệu từ backend */
export async function getBrandNames() {
  const { data } = await client.get('/admin/brands/names')
  return data || []
}

/** Lấy danh sách review của sản phẩm */
export async function getReviews(productId) {
  const { data } = await client.get(`/products/${productId}/reviews`)
  return data || []
}

/** Gửi review mới */
export async function addReview(productId, { userName, rating, comment }) {
  const { data } = await client.post(`/products/${productId}/reviews`, { userName, rating, comment })
  return data
}
