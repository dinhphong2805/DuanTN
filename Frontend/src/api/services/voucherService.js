/**
 * Voucher API - kết nối backend
 */
import client from '../client'

export async function validateVoucher(code, total) {
  const { data } = await client.get('/vouchers/validate', {
    params: { code: code.trim(), total },
  })
  return data
}
