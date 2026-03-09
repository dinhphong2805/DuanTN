<template>
  <div class="cart-page">
    <header class="cart-header">
      <div class="cart-breadcrumb">
        <span class="crumb" @click="$router.push('/')">Trang chủ</span>
        <span class="sep">/</span>
        <span class="current">Giỏ hàng</span>
      </div>
      <h1 class="cart-title">Giỏ hàng</h1>
      <p class="cart-subtitle">Kiểm tra sản phẩm trước khi thanh toán.</p>
    </header>

    <section v-if="cart.state.items.length === 0" class="empty">
      <p class="empty-title">Giỏ hàng đang trống</p>
      <button class="btn" type="button" @click="$router.push('/product')">
        Xem sản phẩm
      </button>
    </section>

    <section v-else class="cart-layout">
      <main class="cart-items">
        <article v-for="it in cart.state.items" :key="it.key" class="item">
          <img class="item-image" :src="it.image" :alt="it.name" />

          <div class="item-body">
            <div class="item-top">
              <div>
                <p class="item-brand">{{ it.brand }}</p>
                <p class="item-name">{{ it.name }}</p>
                <p class="item-meta">
                  <span v-if="it.size">Size {{ it.size }}</span>
                  <span v-else>Size: —</span>
                </p>
              </div>

              <button class="link" type="button" @click="cart.removeItem(it.key)">
                Xóa
              </button>
            </div>

            <div class="item-bottom">
              <div class="qty">
                <label class="qty-label">Số lượng</label>
                <input
                  class="qty-input"
                  type="number"
                  min="1"
                  :value="it.quantity"
                  @input="cart.setQuantity(it.key, $event.target.value)"
                />
              </div>

              <p class="item-price">{{ it.price }}</p>
            </div>
          </div>
        </article>

        <button class="link clear" type="button" @click="cart.clear()">
          Xóa hết giỏ hàng
        </button>
      </main>

      <aside class="summary">
        <h2 class="summary-title">Tóm tắt</h2>

        <div class="voucher-row">
          <input v-model="voucherCode" placeholder="Mã giảm giá" class="voucher-input" />
          <button type="button" class="btn-apply" :disabled="voucherLoading" @click="applyVoucher">
            {{ voucherLoading ? 'Đang kiểm tra...' : 'Áp dụng' }}
          </button>
        </div>
        <p v-if="voucherError" class="voucher-err">{{ voucherError }}</p>
        <p v-else-if="appliedVoucher" class="voucher-ok">Đã giảm {{ formatPrice(voucherAmount) }} VNĐ</p>

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
        <div class="summary-row summary-total">
          <span>Tổng</span>
          <strong>{{ formatPrice(total) }} VNĐ</strong>
        </div>

        <button class="btn primary" type="button" @click="$router.push('/checkout')">
          Thanh toán
        </button>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useCart } from '../cartStore'
import { validateVoucher } from '../api/services/voucherService'

const cart = useCart()
const voucherCode = ref('')
const voucherLoading = ref(false)
const voucherError = ref('')
const appliedVoucher = ref(null)
const voucherAmount = ref(0)

function toNumber(priceText) {
  const digits = String(priceText || '').replace(/[^\d]/g, '')
  return digits ? Number(digits) : 0
}

function formatPrice(n) {
  return Number(n).toLocaleString('vi-VN')
}

const subtotal = computed(() =>
  cart.state.items.reduce((sum, it) => sum + toNumber(it.price) * it.quantity, 0)
)

const total = computed(() => Math.max(0, subtotal.value - voucherAmount.value))

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
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 16px 72px;
}

.cart-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 22px;
}

.cart-breadcrumb {
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

.cart-title {
  font-size: 28px;
  font-weight: 800;
}

.cart-subtitle {
  font-size: 14px;
  color: #4b5563;
}

.empty {
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 28px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: flex-start;
}

.empty-title {
  font-size: 16px;
  font-weight: 700;
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 18px;
  align-items: start;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  background: #fff;
  overflow: hidden;
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
}

.item-image {
  width: 100%;
  height: 140px;
  object-fit: cover;
  background: #f3f4f6;
}

.item-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.item-brand {
  font-size: 12px;
  color: #6b7280;
}

.item-name {
  font-size: 15px;
  font-weight: 800;
}

.item-meta {
  font-size: 13px;
  color: #6b7280;
}

.item-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
}

.qty {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.qty-label {
  font-size: 12px;
  color: #6b7280;
}

.qty-input {
  width: 110px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  font-size: 14px;
}

.item-price {
  font-size: 14px;
  font-weight: 800;
}

.link {
  background: none;
  border: none;
  color: #111827;
  cursor: pointer;
  font-weight: 700;
  padding: 0;
}

.link:hover {
  text-decoration: underline;
}

.clear {
  align-self: flex-start;
  color: #b91c1c;
  margin-top: 6px;
}

.summary {
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 16px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-title {
  font-size: 16px;
  font-weight: 800;
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

.voucher-input:focus {
  outline: none;
  border-color: #111827;
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
  margin: -4px 0 8px;
}

.voucher-ok {
  color: #059669;
  font-size: 12px;
  margin: -4px 0 8px;
}

.summary-row.discount {
  color: #059669;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #374151;
}

.summary-total {
  border-top: 1px solid #e5e7eb;
  padding-top: 12px;
  color: #111827;
}

.btn {
  border: 1px solid #111827;
  background: #fff;
  color: #111827;
  border-radius: 14px;
  padding: 10px 14px;
  font-weight: 800;
  cursor: pointer;
}

.btn.primary {
  background: #111827;
  color: #fff;
}

@media (max-width: 1024px) {
  .cart-layout {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 640px) {
  .item {
    grid-template-columns: 110px minmax(0, 1fr);
  }
  .item-image {
    height: 110px;
  }
}
</style>

