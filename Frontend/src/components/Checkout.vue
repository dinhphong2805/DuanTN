<template>
  <div class="checkout-page">
    <header class="checkout-header">
      <div class="breadcrumb">
        <span class="crumb" @click="$router.push('/')">Trang chủ</span>
        <span class="sep">/</span>
        <span class="crumb" @click="$router.push('/cart')">Giỏ hàng</span>
        <span class="sep">/</span>
        <span class="current">Thanh toán</span>
      </div>
      <h1 class="checkout-title">Thanh Toán</h1>
    </header>

    <section v-if="cart.state.items.length === 0" class="empty">
      <p>Giỏ hàng trống. <router-link to="/product">Mua sắm ngay</router-link></p>
    </section>

    <form v-else class="checkout-layout" @submit.prevent="placeOrder">
      <main class="checkout-main">
        <div class="block">
          <h2>Thông tin giao hàng</h2>
          <div class="form-row">
            <div class="form-group">
              <label>Họ và tên *</label>
              <input v-model="form.fullName" required placeholder="Họ và tên" />
            </div>
            <div class="form-group">
              <label>Số điện thoại *</label>
              <input v-model="form.phone" required placeholder="Số điện thoại" />
            </div>
          </div>
          <div class="form-group">
            <label>Email *</label>
            <input v-model="form.email" type="email" required placeholder="Email" />
          </div>
          <div class="form-group">
            <label>Địa chỉ *</label>
            <input v-model="form.address" required placeholder="Địa chỉ chi tiết" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Tỉnh/Thành phố</label>
              <input v-model="form.city" placeholder="VD: TP.HCM" />
            </div>
            <div class="form-group">
              <label>Quận/Huyện</label>
              <input v-model="form.district" placeholder="Quận/Huyện" />
            </div>
          </div>
          <div class="form-group">
            <label>Ghi chú</label>
            <textarea v-model="form.note" rows="2" placeholder="Ghi chú đơn hàng"></textarea>
          </div>
        </div>

        <div class="block">
          <h2>Phương thức thanh toán</h2>
          <div class="payment-options">
            <label class="payment-option">
              <input v-model="form.paymentMethod" type="radio" value="cod" />
              <span>Thanh toán khi nhận hàng (COD)</span>
            </label>
            <label class="payment-option">
              <input v-model="form.paymentMethod" type="radio" value="bank" />
              <span>Chuyển khoản ngân hàng</span>
            </label>
          </div>
        </div>
      </main>

      <aside class="checkout-sidebar">
        <div class="order-summary">
          <h2>Đơn hàng</h2>
          <div class="order-items">
            <div v-for="it in cart.state.items" :key="it.key" class="order-item">
              <img :src="it.image" :alt="it.name" class="order-item-img" />
              <div class="order-item-body">
                <p class="order-item-name">{{ it.name }}</p>
                <p class="order-item-meta">x{{ it.quantity }} · Size {{ it.size || '—' }}</p>
                <p class="order-item-price">{{ formatPrice(toNumber(it.price) * it.quantity) }} VNĐ</p>
              </div>
            </div>
          </div>

          <div class="voucher-row">
            <input v-model="voucherCode" placeholder="Mã giảm giá" class="voucher-input" />
            <button type="button" class="btn-apply" :disabled="voucherLoading" @click="applyVoucher">
              {{ voucherLoading ? 'Đang kiểm tra...' : 'Áp dụng' }}
            </button>
          </div>
          <p v-if="voucherError" class="voucher-err">{{ voucherError }}</p>
          <p v-else-if="appliedVoucher" class="voucher-ok">Đã giảm {{ formatPrice(voucherAmount) }} VNĐ</p>

          <div class="summary-rows">
            <div class="summary-row">
              <span>Tạm tính</span>
              <strong>{{ formatPrice(subtotal) }} VNĐ</strong>
            </div>
            <div v-if="voucherAmount > 0" class="summary-row discount">
              <span>Giảm giá</span>
              <strong>-{{ formatPrice(voucherAmount) }} VNĐ</strong>
            </div>
            <div class="summary-row">
              <span>Phí vận chuyển</span>
              <strong>0 VNĐ</strong>
            </div>
            <div class="summary-row total">
              <span>Tổng thanh toán</span>
              <strong>{{ formatPrice(total) }} VNĐ</strong>
            </div>
          </div>
        </div>

        <button type="submit" class="btn-submit" :disabled="orderLoading">
          {{ orderLoading ? 'Đang xử lý...' : 'Đặt hàng' }}
        </button>
      </aside>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '../cartStore'
import { useAuthStore } from '../authStore'
import { validateVoucher } from '../api/services/voucherService'
import { createOrder } from '../api/services/orderService'

const router = useRouter()
const cart = useCart()
const auth = useAuthStore()

function toNumber(priceText) {
  const digits = String(priceText || '').replace(/[^\d]/g, '')
  return digits ? Number(digits) : 0
}

function formatPrice(n) {
  return Number(n).toLocaleString('vi-VN')
}

const form = reactive({
  fullName: '',
  phone: '',
  email: '',
  address: '',
  city: '',
  district: '',
  note: '',
  paymentMethod: 'cod',
})

const voucherCode = ref('')
const voucherLoading = ref(false)
const voucherError = ref('')
const appliedVoucher = ref(null)
const voucherAmount = ref(0)
const orderLoading = ref(false)

const subtotal = computed(() =>
  cart.state.items.reduce((sum, it) => sum + toNumber(it.price) * it.quantity, 0)
)

const total = computed(() => Math.max(0, subtotal.value - voucherAmount.value))

onMounted(() => {
  const u = auth.state?.user
  if (u) {
    form.fullName = u.fullName || u.full_name || ''
    form.phone = u.phone || ''
    form.email = u.email || ''
  }
})

async function applyVoucher() {
  const code = voucherCode.value?.trim()
  if (!code) return
  voucherError.value = ''
  voucherLoading.value = true
  try {
    const res = await validateVoucher(code, subtotal.value)
    if (res.valid) {
      appliedVoucher.value = res.voucher
      voucherAmount.value = res.amount
    } else {
      voucherError.value = res.message || 'Mã không hợp lệ'
    }
  } catch {
    voucherError.value = 'Không thể kiểm tra mã'
  } finally {
    voucherLoading.value = false
  }
}

async function placeOrder() {
  orderLoading.value = true
  try {
    const fullAddress = [form.address, form.district, form.city].filter(Boolean).join(', ')
    await createOrder({
      userId: auth.state?.user?.id || null,
      customerName: form.fullName,
      customerEmail: form.email,
      customerPhone: form.phone,
      address: fullAddress || form.address,
      total: total.value,
      items: cart.state.items.map(it => ({
        productName: it.name,
        quantity: it.quantity,
        unitPrice: toNumber(it.price),
      })),
    })
    cart.clear()
    alert('Đặt hàng thành công!')
    router.push('/profile?tab=orders')
  } catch (e) {
    const msg = e.response?.data?.message || e.message || 'Có lỗi xảy ra, vui lòng thử lại.'
    alert(msg)
  } finally {
    orderLoading.value = false
  }
}
</script>

<style scoped>
.checkout-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 72px;
}

.checkout-header {
  margin-bottom: 24px;
}

.breadcrumb {
  font-size: 14px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.crumb {
  cursor: pointer;
}

.crumb:hover {
  text-decoration: underline;
}

.sep {
  opacity: 0.7;
}

.current {
  color: #111827;
  font-weight: 600;
}

.checkout-title {
  font-size: 28px;
  font-weight: 800;
  margin: 8px 0 0;
}

.empty {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 32px;
  text-align: center;
}

.checkout-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 24px;
  align-items: start;
}

.checkout-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.block {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
  background: #fff;
}

.block h2 {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
}

.form-group input,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #111827;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.order-summary {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 18px;
  background: #fff;
}

.order-summary h2 {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 14px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 240px;
  overflow-y: auto;
  margin-bottom: 14px;
}

.order-item {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr);
  gap: 10px;
}

.order-item-img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 8px;
  background: #f3f4f6;
}

.order-item-name {
  font-size: 13px;
  font-weight: 700;
}

.order-item-meta {
  font-size: 12px;
  color: #6b7280;
}

.order-item-price {
  font-size: 12px;
  font-weight: 700;
}

.voucher-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.voucher-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
}

.btn-apply {
  padding: 8px 14px;
  border: 1px solid #111827;
  background: #fff;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.btn-apply:hover:not(:disabled) {
  background: #f3f4f6;
}

.btn-apply:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.voucher-err {
  color: #b91c1c;
  font-size: 12px;
  margin: 0 0 8px;
}

.voucher-ok {
  color: #059669;
  font-size: 12px;
  margin: 0 0 8px;
}

.summary-rows {
  border-top: 1px solid #e5e7eb;
  padding-top: 12px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
}

.summary-row.discount {
  color: #059669;
}

.summary-row.total {
  font-size: 16px;
  font-weight: 800;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.btn-submit {
  width: 100%;
  margin-top: 14px;
  padding: 14px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.btn-submit:hover:not(:disabled) {
  background: #020617;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 1024px) {
  .checkout-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
