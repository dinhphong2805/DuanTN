/**
 * Voucher API - kết nối backend
 */
import client from '../client'

export async function validateVoucher(code, total, userId = null) {
  const { data } = await client.get('/vouchers/validate', {
    params: { code: code.trim(), total, userId },
  })
  return data
}

export async function getMyVouchers(userId) {
  if (!userId) return []
  const { data } = await client.get('/vouchers/my-vouchers', {
    params: { userId }
  })
  return data
}
