<template>
  <div class="cart-page">
    <div class="cart-promo" role="region" aria-label="Cam kết dịch vụ">
      <div class="cart-promo-inner">
        Miễn phí giao từ 3.000.000₫ · Đổi trả trong 14 ngày · Thanh toán an toàn
      </div>
    </div>

    <header class="cart-hero">
      <div class="cart-hero-inner">
        <nav class="cart-breadcrumb" aria-label="Breadcrumb">
          <button type="button" class="cart-crumb" @click="$router.push('/')">Trang chủ</button>
          <span class="cart-sep" aria-hidden="true">/</span>
          <span class="cart-current">Giỏ hàng</span>
        </nav>
        <p class="cart-eyebrow">Đơn hàng</p>
        <h1 class="cart-title">Giỏ hàng</h1>
        <p class="cart-subtitle">Kiểm tra sản phẩm và số lượng trước khi thanh toán.</p>
      </div>
    </header>

    <div class="cart-shell">
      <section v-if="cart.state.items.length === 0" class="empty">
        <div class="empty-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M6 8h12l-1.2 12H7.2L6 8z" />
            <path d="M9 8V6a3 3 0 0 1 6 0v2" />
          </svg>
        </div>
        <h2 class="empty-title">Giỏ hàng đang trống</h2>
        <p class="empty-text">Thêm sản phẩm yêu thích để tiếp tục mua sắm.</p>
        <button class="empty-btn" type="button" @click="$router.push('/product')">
          Xem sản phẩm
        </button>
      </section>

      <section v-else class="cart-layout">
        <main class="cart-items" aria-label="Sản phẩm trong giỏ">
          <article v-for="it in cart.state.items" :key="it.key" class="item">
            <router-link :to="`/product/${it.id}`" class="item-media" tabindex="-1">
              <img
                class="item-image"
                :src="resolveImage(it.image)"
                :alt="it.name"
                loading="lazy"
                @error="onImgError"
              />
            </router-link>

            <div class="item-body">
              <div class="item-top">
                <router-link :to="`/product/${it.id}`" class="item-text-link">
                  <p class="item-brand">{{ it.brand || '—' }}</p>
                  <h2 class="item-name">{{ it.name }}</h2>
                  <p class="item-meta">
                    <span v-if="it.size">Size {{ it.size }}</span>
                    <span v-else>Size: —</span>
                  </p>
                </router-link>

                <button
                  class="item-remove"
                  type="button"
                  aria-label="Xóa sản phẩm"
                  @click="cart.removeItem(it.key)"
                >
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14zM10 11v6M14 11v6" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                </button>
              </div>

              <div class="item-bottom">
                <div class="qty">
                  <span class="qty-label">Số lượng</span>
                  <div class="qty-stepper">
                    <button
                      type="button"
                      class="qty-btn"
                      aria-label="Giảm"
                      :disabled="it.quantity <= 1"
                      @click="bumpQty(it.key, -1)"
                    >
                      −
                    </button>
                    <input
                      class="qty-input"
                      type="number"
                      min="1"
                      :value="it.quantity"
                      @change="onQtyChange(it.key, $event)"
                    />
                    <button type="button" class="qty-btn" aria-label="Tăng" @click="bumpQty(it.key, 1)">
                      +
                    </button>
                  </div>
                </div>

                <div class="item-price-block">
                  <p class="item-unit">{{ formatMoney(unitPrice(it)) }} / sản phẩm</p>
                  <p class="item-line">{{ formatMoney(lineTotal(it)) }}</p>
                </div>
              </div>
            </div>
          </article>

          <button class="btn-clear" type="button" @click="cart.clear()">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
            Xóa toàn bộ giỏ hàng
          </button>
        </main>

        <aside class="summary" aria-label="Tóm tắt đơn hàng">
          <h2 class="summary-title">Tóm tắt</h2>

          <div class="voucher-row">
            <div class="voucher-input-wrapper">
              <input
                v-model="voucherInput"
                type="text"
                class="voucher-input"
                placeholder="Nhập hoặc chọn mã giảm giá..."
                @focus="showVoucherDropdown = true"
                @input="handleVoucherInput"
                @blur="handleVoucherBlur"
                @keydown.enter="applyVoucher"
              />
              <ul v-if="showVoucherDropdown && filteredVoucherList.length > 0" class="voucher-dropdown">
                <li v-for="v in filteredVoucherList" :key="v.id" @mousedown="selectFromList(v)">
                  <span class="code">{{ v.code }}</span>
                  <span class="discount">Giảm {{ v.type === 'fixed' ? formatMoney(v.discount) : v.discount + '%' }}</span>
                </li>
              </ul>
            </div>
            <button v-if="appliedVoucher" type="button" class="btn-clear-voucher" @click="removeVoucher">
              ✕
            </button>
          </div>
          <p v-if="voucherError" class="voucher-err">{{ voucherError }}</p>
          <p v-else-if="appliedVoucher" class="voucher-ok">Đã giảm {{ formatMoney(voucherAmount) }}</p>

          <div class="summary-rows">
            <div class="summary-row">
              <span>Tạm tính</span>
              <strong>{{ formatMoney(subtotal) }}</strong>
            </div>
            <div v-if="voucherAmount > 0" class="summary-row discount">
              <span>Giảm giá</span>
              <strong>−{{ formatMoney(voucherAmount) }}</strong>
            </div>
            <div class="summary-row">
              <span>Phí vận chuyển</span>
              <strong>{{ formatMoney(0) }}</strong>
            </div>
          </div>

          <div class="summary-total">
            <span>Tổng cộng</span>
            <strong>{{ formatMoney(total) }}</strong>
          </div>

          <button class="btn-checkout" type="button" @click="handleCheckout">
            Thanh toán
          </button>
          <router-link to="/product" class="link-continue">Tiếp tục mua sắm</router-link>
        </aside>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useCart } from '../cartStore'
import { validateVoucher, getMyVouchers } from '../api/services/voucherService'
import { API_BASE_URL } from '../api/config'
import { useVoucherStore } from '../voucherStore'
import { resolveSessionUserId } from '../authStore'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'

const router = useRouter()

const cart = useCart()
const voucherStore = useVoucherStore()

const myVouchers = ref([])
const voucherInput = ref(voucherStore.voucherCode || '')
const showVoucherDropdown = ref(false)

const voucherLoading = ref(false)
const voucherError = ref('')

const appliedVoucher = computed(() => voucherStore.appliedVoucher)
const voucherAmount = computed(() => voucherStore.discountAmount)

// Lọc danh sách vouchers theo input
const filteredVoucherList = computed(() => {
  const input = voucherInput.value.toUpperCase().trim()
  if (!input) return myVouchers.value
  return myVouchers.value.filter(v => v.code.toUpperCase().includes(input))
})

onMounted(async () => {
  const userId = resolveSessionUserId()
  if (userId) {
    try {
      myVouchers.value = await getMyVouchers(userId)
    } catch (e) {
      console.error(e)
    }
  }
  
  // Re-validate store voucher on mount if total applies
  if (voucherInput.value) {
    applyVoucher()
  }
})

watch(() => cart.state.items, () => {
    // If cart changed, validate again
    if (voucherInput.value) {
        applyVoucher()
    }
}, { deep: true })

const PLACEHOLDER_IMG =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#f4f4f5"/><stop offset="100%" stop-color="#e4e4e7"/></linearGradient></defs><rect fill="url(#g)" width="400" height="400"/><text x="200" y="210" text-anchor="middle" fill="#a1a1aa" font-family="system-ui,sans-serif" font-size="13">Ảnh sản phẩm</text></svg>'
  )

function toDisplayUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const base = API_BASE_URL.replace(/\/api\/?$/, '') || 'http://localhost:8080'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('data:')) return url
  if (url.startsWith('/')) return base + url
  const filename = url.replace(/^.*[/\\]/, '')
  if (filename && /\.(jpe?g|png|gif|webp)$/i.test(filename)) {
    return base + '/uploads/' + filename
  }
  return base + '/uploads/' + (url.replace(/^.*[/\\]/, '') || '')
}

function resolveImage(url) {
  const s = url && String(url).trim()
  if (!s) return PLACEHOLDER_IMG
  return toDisplayUrl(s)
}

function onImgError(e) {
  e.target.src = PLACEHOLDER_IMG
}

function toNumber(priceText) {
  const digits = String(priceText ?? '').replace(/[^\d]/g, '')
  return digits ? Number(digits) : 0
}

function formatMoney(n) {
  return `${Number(n || 0).toLocaleString('vi-VN')} VNĐ`
}

function unitPrice(it) {
  return toNumber(it.price)
}

function lineTotal(it) {
  return unitPrice(it) * (Number(it.quantity) || 0)
}

const subtotal = computed(() =>
  cart.state.items.reduce((sum, it) => sum + lineTotal(it), 0)
)

const total = computed(() => Math.max(0, subtotal.value - voucherAmount.value))

function bumpQty(key, delta) {
  const item = cart.state.items.find((x) => x.key === key)
  if (!item) return
  const next = Math.max(1, (Number(item.quantity) || 1) + delta)
  cart.setQuantity(key, next)
}

function onQtyChange(key, e) {
  const v = e.target?.value
  cart.setQuantity(key, v)
}

function removeVoucher() {
    voucherInput.value = ''
    voucherStore.clearVoucher()
    voucherError.value = ''
}

function handleVoucherInput(e) {
  showVoucherDropdown.value = true
}

function handleVoucherBlur() {
  // Delay để cho phép click vào dropdown item
  setTimeout(() => {
    showVoucherDropdown.value = false
  }, 200)
}

function selectFromList(voucher) {
  voucherInput.value = voucher.code
  showVoucherDropdown.value = false
  applyVoucher()
}

async function applyVoucher() {
  const code = voucherInput.value?.trim()
  if (!code) {
      voucherStore.clearVoucher()
      return
  }
  voucherError.value = ''
  voucherLoading.value = true
  const userId = resolveSessionUserId()
  try {
    const res = await validateVoucher(code, subtotal.value, userId)
    if (res.valid) {
      voucherStore.setVoucher(res.voucher, code, res.amount)
    } else {
      voucherStore.clearVoucher()
      voucherError.value = res.message || 'Mã không hợp lệ'
    }
  } catch {
    voucherStore.clearVoucher()
    voucherError.value = 'Không thể kiểm tra mã'
  } finally {
    voucherLoading.value = false
  }
}

function handleCheckout() {
  const userId = resolveSessionUserId()
  if (!userId) {
    Swal.fire({
      icon: 'warning',
      title: 'Yêu cầu đăng nhập',
      text: 'Vui lòng đăng nhập để thực hiện thanh toán!',
      confirmButtonColor: '#000',
      confirmButtonText: 'Đăng nhập ngay'
    }).then((result) => {
      if (result.isConfirmed) {
        router.push({ path: '/login', query: { redirect: '/checkout' } })
      }
    })
    return
  }
  router.push('/checkout')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800&display=swap');

.cart-page {
  --cp-ink: #0a0a0a;
  --cp-muted: #6b7280;
  --cp-line: rgba(0, 0, 0, 0.08);
  --cp-surface: #fafafa;

  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  color: var(--cp-ink);
  background: var(--cp-surface);
}

.cart-promo {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: #0a0a0a;
  color: rgba(255, 255, 255, 0.82);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  text-align: center;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.cart-promo-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.cart-hero {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: linear-gradient(-45deg, #0f172a, #000000, #312e81, #0f172a);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  color: #fff;
  padding: 50px 24px 60px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  overflow: hidden;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.cart-hero-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.cart-breadcrumb {
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.04em;
  color: rgba(255, 255, 255, 0.45);
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.cart-crumb {
  padding: 0;
  border: none;
  background: none;
  font: inherit;
  color: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: color 0.2s ease;
}

.cart-crumb:hover {
  color: #fff;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.cart-current {
  color: rgba(255, 255, 255, 0.92);
  font-weight: 600;
}

.cart-eyebrow {
  margin: 0 0 4px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
}

.cart-title {
  margin: 0;
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.12;
  background: linear-gradient(135deg, #fff, #a5b4fc, #fbcfe8);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shine 5s linear infinite;
}

@keyframes shine {
  to { background-position: 200% center; }
}

.cart-subtitle {
  margin: 10px 0 0;
  max-width: 480px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.55;
  color: rgba(255, 255, 255, 0.55);
}

.cart-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 16px 72px;
}

.empty {
  border: 1px solid var(--cp-line);
  border-radius: 20px;
  padding: 48px 28px;
  background: #fff;
  box-shadow: 0 8px 32px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
}

.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #f4f4f5;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.empty-icon svg {
  width: 28px;
  height: 28px;
}

.empty-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.empty-text {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--cp-muted);
  max-width: 360px;
  line-height: 1.5;
}

.empty-btn {
  margin-top: 8px;
  padding: 14px 28px;
  border: none;
  border-radius: 999px;
  background: #0a0a0a;
  color: #fff;
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}

.empty-btn:hover {
  background: #111827;
}

.empty-btn:active {
  transform: scale(0.98);
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 28px;
  align-items: start;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item {
  border: 1px solid rgba(229, 231, 235, 0.5);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  overflow: hidden;
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  box-shadow: 0 8px 32px rgba(15, 23, 42, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.item:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(79, 70, 229, 0.1);
}

.item-media {
  display: block;
  background: linear-gradient(145deg, #f3f4f6, #fafafa);
  min-height: 160px;
}

.item-image {
  width: 100%;
  height: 100%;
  min-height: 160px;
  object-fit: cover;
  display: block;
}

.item-body {
  padding: 18px 18px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.item-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.item-text-link {
  text-decoration: none;
  color: inherit;
  min-width: 0;
}

.item-text-link:hover .item-name {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.item-brand {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #9ca3af;
  margin: 0 0 4px;
}

.item-name {
  font-size: 16px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.3;
  margin: 0 0 6px;
}

.item-meta {
  font-size: 13px;
  color: var(--cp-muted);
  margin: 0;
}

.item-remove {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border: 1px solid var(--cp-line);
  border-radius: 12px;
  background: #fafafa;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.item-remove:hover {
  background: #fef2f2;
  border-color: #fecaca;
  color: #b91c1c;
}

.item-bottom {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding-top: 4px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.qty {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.qty-label {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #9ca3af;
}

.qty-stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--cp-line);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.qty-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #fafafa;
  font-size: 18px;
  line-height: 1;
  color: var(--cp-ink);
  cursor: pointer;
  transition: background 0.2s;
}

.qty-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.qty-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.qty-input {
  width: 48px;
  height: 40px;
  border: none;
  border-left: 1px solid var(--cp-line);
  border-right: 1px solid var(--cp-line);
  text-align: center;
  font-size: 14px;
  font-weight: 700;
  font-family: inherit;
  color: var(--cp-ink);
  -moz-appearance: textfield;
}

.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.item-price-block {
  text-align: right;
}

.item-unit {
  margin: 0 0 4px;
  font-size: 11px;
  color: var(--cp-muted);
}

.item-line {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--cp-ink);
}

.btn-clear {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 10px 16px;
  border: 1px solid #fecaca;
  border-radius: 999px;
  background: #fff;
  color: #b91c1c;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.btn-clear:hover {
  background: #fef2f2;
  border-color: #f87171;
}

.summary {
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 24px;
  padding: 26px 24px 28px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  box-shadow: 0 16px 48px rgba(31, 38, 135, 0.08);
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 96px;
}

.summary-title {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #9ca3af;
  margin: 0 0 2px;
}

.voucher-row {
  display: flex;
  gap: 10px;
}

.voucher-input-wrapper {
  flex: 1;
  min-width: 0;
  position: relative;
}

.voucher-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  background: #fafafa;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.voucher-input:focus {
  outline: none;
  border-color: var(--cp-ink);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.06);
}

.voucher-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin: 4px 0 0;
  padding: 6px 0;
  list-style: none;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  z-index: 10;
  max-height: 200px;
  overflow-y: auto;
}

.voucher-dropdown li {
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  transition: background 0.2s;
}

.voucher-dropdown li:hover {
  background: #f3f4f6;
}

.voucher-dropdown li .code {
  font-weight: 600;
  color: var(--cp-ink);
  font-family: monospace;
}

.voucher-dropdown li .discount {
  font-size: 12px;
  color: #ef4444;
  font-weight: 600;
}

.btn-apply {
  flex-shrink: 0;
  padding: 12px 18px;
  border: 1px solid var(--cp-ink);
  background: #fff;
  border-radius: 12px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-apply:hover:not(:disabled) {
  background: #f3f4f6;
}

.btn-apply:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-clear-voucher {
  flex-shrink: 0;
  padding: 0;
  width: 32px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  border-radius: 12px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-clear-voucher:hover {
  background: #f87171;
  color: #fff;
}

.voucher-err {
  color: #b91c1c;
  font-size: 12px;
  margin: -6px 0 0;
}

.voucher-ok {
  color: #059669;
  font-size: 12px;
  font-weight: 600;
  margin: -6px 0 0;
}

.summary-rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 4px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 14px;
  color: #4b5563;
}

.summary-row strong {
  font-weight: 700;
  color: var(--cp-ink);
}

.summary-row.discount strong {
  color: #059669;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding-top: 16px;
  margin-top: 4px;
  border-top: 1px solid var(--cp-line);
  font-size: 15px;
  font-weight: 800;
}

.summary-total strong {
  font-size: 20px;
  letter-spacing: -0.02em;
}

.btn-checkout {
  margin-top: 8px;
  width: 100%;
  padding: 16px 20px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(45deg, #4f46e5, #ec4899);
  background-size: 200% auto;
  color: #fff;
  font-family: inherit;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(236, 72, 153, 0.3);
  transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
}

.btn-checkout:hover {
  background-position: right center;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 16px 32px rgba(79, 70, 229, 0.4);
}

.btn-checkout:active {
  transform: scale(0.99);
}

.link-continue {
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--cp-muted);
  text-decoration: none;
  padding: 8px;
  transition: color 0.2s;
}

.link-continue:hover {
  color: var(--cp-ink);
  text-decoration: underline;
  text-underline-offset: 3px;
}

@media (max-width: 1024px) {
  .cart-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .summary {
    position: static;
  }
}

@media (max-width: 640px) {
  .item {
    grid-template-columns: 120px minmax(0, 1fr);
  }

  .item-media {
    min-height: 120px;
  }

  .item-image {
    min-height: 120px;
  }

  .item-bottom {
    flex-direction: column;
    align-items: stretch;
  }

  .item-price-block {
    text-align: left;
  }

  .cart-shell {
    padding: 24px 16px 56px;
  }

  .cart-hero {
    padding: 28px 16px 32px;
  }
}
</style>
