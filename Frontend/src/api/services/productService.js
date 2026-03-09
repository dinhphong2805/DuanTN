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
