<template>
  <div class="checkout-page">
    <header class="checkout-header">
      <div class="breadcrumb">
        <span class="crumb" @click="$router.push('/')">Trang chủ</span>
        <svg class="sep-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" /></svg>
        <span class="crumb" @click="$router.push('/cart')">Giỏ hàng</span>
        <svg class="sep-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" /></svg>
        <span class="current">Thanh toán</span>
      </div>
      <h1 class="checkout-title">Hoàn tất đơn hàng</h1>
      <p class="checkout-subtitle">Chỉ còn một bước nữa để sở hữu đôi giày yêu thích của bạn.</p>
    </header>

    <section v-if="cart.state.items.length === 0" class="empty-state">
      <div class="empty-icon">🛍️</div>
      <h2>Giỏ hàng của bạn đang trống</h2>
      <p>Hãy chọn cho mình những sản phẩm thật phong cách nhé.</p>
      <button class="btn-primary" @click="$router.push('/product')">Tiếp tục mua sắm</button>
    </section>

    <form v-else class="checkout-layout" @submit.prevent="placeOrder">
      <main class="checkout-main">
        <div class="premium-card">
          <div class="card-header">
            <div class="step-number">1</div>
            <h2>Thông tin liên hệ</h2>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Họ và tên <span class="req">*</span></label>
              <input v-model="form.fullName" required placeholder="Nguyễn Văn A" />
            </div>
            <div class="form-group">
              <label>Số điện thoại <span class="req">*</span></label>
              <input v-model="form.phone" required placeholder="0901234567" />
            </div>
          </div>
          <div class="form-group">
            <label>Email liên hệ <span class="req">*</span></label>
            <input v-model="form.email" type="email" required placeholder="email@example.com" />
          </div>
        </div>

        <div class="premium-card">
          <div class="card-header">
            <div class="step-number">2</div>
            <h2>Địa chỉ nhận hàng</h2>
          </div>
          
          <div class="form-row">
            <div class="form-group custom-select-wrapper">
              <label>Tỉnh/Thành phố <span class="req">*</span></label>
              <div class="custom-select">
                <input v-model="citySearch" @focus="openDropdown('city')" placeholder="Chọn Tỉnh/Thành phố..." required />
                <i class="dropdown-icon"></i>
                <ul v-if="activeDropdown === 'city'" class="dropdown-list">
                  <li v-for="p in filteredProvinces" :key="p.id" @click="selectCity(p)">{{ p.full_name }}</li>
                  <li v-if="!filteredProvinces.length" class="no-result">Không tìm thấy</li>
                </ul>
              </div>
            </div>

            <div class="form-group custom-select-wrapper">
              <label>Quận/Huyện <span class="req">*</span></label>
              <div class="custom-select">
                <input v-model="districtSearch" @focus="openDropdown('district')" :disabled="!selectedCity" placeholder="Chọn Quận/Huyện..." required />
                <i class="dropdown-icon"></i>
                <ul v-if="activeDropdown === 'district'" class="dropdown-list">
                  <li v-for="d in filteredDistricts" :key="d.id" @click="selectDistrict(d)">{{ d.full_name }}</li>
                  <li v-if="!filteredDistricts.length" class="no-result">Không tìm thấy</li>
                </ul>
              </div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group custom-select-wrapper">
              <label>Phường/Xã <span class="req">*</span></label>
              <div class="custom-select">
                <input v-model="wardSearch" @focus="openDropdown('ward')" :disabled="!selectedDistrict" placeholder="Chọn Phường/Xã..." required />
                <i class="dropdown-icon"></i>
                <ul v-if="activeDropdown === 'ward'" class="dropdown-list">
                  <li v-for="w in filteredWards" :key="w.id" @click="selectWard(w)">{{ w.full_name }}</li>
                  <li v-if="!filteredWards.length" class="no-result">Không tìm thấy</li>
                </ul>
              </div>
            </div>

            <div class="form-group">
              <label>Số nhà, Tên đường <span class="req">*</span></label>
              <input v-model="form.address" required placeholder="VD: Số 123, Đường Nguyễn Huệ..." />
            </div>
          </div>

          <div class="form-group">
            <label>Ghi chú cho shipper (Tùy chọn)</label>
            <textarea v-model="form.note" rows="2" placeholder="VD: Giao giờ hành chính, gọi trước khi giao..."></textarea>
          </div>
        </div>

        <div class="premium-card">
          <div class="card-header">
            <div class="step-number">3</div>
            <h2>Phương thức thanh toán</h2>
          </div>
          <div class="payment-options">
            <label class="payment-card" :class="{ active: form.paymentMethod === 'cod' }">
              <div class="radio-custom">
                <input v-model="form.paymentMethod" type="radio" value="cod" />
                <span class="radio-mark"></span>
              </div>
              <div class="payment-info">
                <span class="pay-title">Thanh toán khi nhận hàng (COD)</span>
                <span class="pay-desc">Thanh toán bằng tiền mặt khi shipper giao hàng tới</span>
              </div>
            </label>
            
            <label class="payment-card" :class="{ active: form.paymentMethod === 'bank' }">
              <div class="radio-custom">
                <input v-model="form.paymentMethod" type="radio" value="bank" />
                <span class="radio-mark"></span>
              </div>
              <div class="payment-info">
                <span class="pay-title">Chuyển khoản QR (Khuyên dùng)</span>
                <span class="pay-desc">Hệ thống tự động xác nhận tức thì, giao hàng nhanh hơn</span>
              </div>
            </label>
          </div>
        </div>
      </main>

      <aside class="checkout-sidebar">
        <div class="summary-card glass-effect">
          <h2 class="summary-title">Tóm tắt đơn hàng</h2>
          
          <div class="order-items-scroll">
            <div v-for="it in cart.state.items" :key="it.key" class="order-item">
              <div class="item-img-wrapper">
                <img :src="it.image" :alt="it.name" />
                <span class="item-badge">{{ it.quantity }}</span>
              </div>
              <div class="item-details">
                <p class="item-name">{{ it.name }}</p>
                <p class="item-variant">Size: {{ it.size || '—' }}</p>
              </div>
              <div class="item-price">{{ formatPrice(toNumber(it.price) * it.quantity) }}đ</div>
            </div>
          </div>

          <div class="voucher-section">
            <div class="voucher-input-group">
              <input v-model="voucherCode" placeholder="Nhập mã ưu đãi..." />
              <button type="button" @click="applyVoucher" :disabled="voucherLoading || !voucherCode">
                {{ voucherLoading ? '...' : 'Áp dụng' }}
              </button>
            </div>
            <p v-if="voucherError" class="voucher-msg error">{{ voucherError }}</p>
            <p v-else-if="appliedVoucher" class="voucher-msg success">🎉 Đã áp dụng giảm {{ formatPrice(voucherAmount) }}đ</p>
          </div>

          <div class="financials">
            <div class="fin-row">
              <span>Tạm tính</span>
              <span>{{ formatPrice(subtotal) }} ₫</span>
            </div>
            <div v-if="voucherAmount > 0" class="fin-row highlight">
              <span>Mã giảm giá</span>
              <span>-{{ formatPrice(voucherAmount) }} ₫</span>
            </div>
            <div class="fin-row">
              <span>Phí vận chuyển</span>
              <span class="free-shipping">Miễn phí</span>
            </div>
            <div class="fin-row grand-total">
              <span>Tổng cộng</span>
              <span class="total-amount">{{ formatPrice(total) }} ₫</span>
            </div>
          </div>

          <button type="submit" class="btn-checkout-submit" :disabled="orderLoading">
            <span v-if="orderLoading" class="loader"></span>
            <span v-else>Đặt hàng ngay</span>
            <svg v-if="!orderLoading" class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
          </button>
          
          <p class="secure-checkout">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
            Thanh toán bảo mật & an toàn 100%
          </p>
        </div>
      </aside>
    </form>

    <Transition name="fade-scale">
      <div v-if="qrData" class="qr-overlay">
        <div class="qr-modal">
          <div class="qr-modal-header">
            <h2>Thanh toán bảo mật</h2>
            <p>Sử dụng App ngân hàng để quét mã</p>
          </div>
          
          <div class="qr-bill-info">
            <div class="bill-row">
              <span>Mã đơn hàng</span>
              <strong>DH{{ qrData.id }}</strong>
            </div>
            <div class="bill-row">
              <span>Số tiền</span>
              <strong class="highlight-price">{{ formatPrice(qrData.total) }} ₫</strong>
            </div>
          </div>

          <div class="qr-image-wrapper">
            <img :src="qrData.url" alt="Mã QR Thanh toán" />
            <div class="scan-line"></div>
          </div>
          
          <div class="qr-status-indicator">
            <div class="pulsing-dot"></div>
            <p>Hệ thống đang chờ thanh toán...</p>
          </div>
          
          <button class="btn-cancel-qr" type="button" @click="cancelQR" :disabled="isCancelling">
            {{ isCancelling ? 'Đang xử lý...' : 'Hủy thanh toán' }}
          </button>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '../cartStore'
import { useAuthStore } from '../authStore'
import { validateVoucher } from '../api/services/voucherService'
import { createOrder, deleteOrder, checkOrderStatus } from '../api/services/orderService'

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
  note: '',
  paymentMethod: 'bank', // Default to bank cho xịn
})

const provinces = ref([])
const districts = ref([])
const wards = ref([])

const selectedCity = ref('')
const selectedDistrict = ref('')
const selectedWard = ref('')

const citySearch = ref('')
const districtSearch = ref('')
const wardSearch = ref('')

const activeDropdown = ref(null)

const filteredProvinces = computed(() => {
  return provinces.value.filter(p => p.full_name.toLowerCase().includes(citySearch.value.toLowerCase()))
})
const filteredDistricts = computed(() => {
  return districts.value.filter(d => d.full_name.toLowerCase().includes(districtSearch.value.toLowerCase()))
})
const filteredWards = computed(() => {
  return wards.value.filter(w => w.full_name.toLowerCase().includes(wardSearch.value.toLowerCase()))
})

const voucherCode = ref('')
const voucherLoading = ref(false)
const voucherError = ref('')
const appliedVoucher = ref(null)
const voucherAmount = ref(0)
const orderLoading = ref(false)

const qrData = ref(null)
const isCancelling = ref(false)
let pollingInterval = null

const subtotal = computed(() =>
  cart.state.items.reduce((sum, it) => sum + toNumber(it.price) * it.quantity, 0)
)
const total = computed(() => Math.max(0, subtotal.value - voucherAmount.value))

const handleClickOutside = (e) => {
  if (!e.target.closest('.custom-select')) {
    activeDropdown.value = null
  }
}

onMounted(async () => {
  const u = auth.state?.user
  if (u) {
    form.fullName = u.fullName || u.full_name || ''
    form.phone = u.phone || ''
    form.email = u.email || ''
  }

  document.addEventListener('click', handleClickOutside)

  // API ESGOO
  try {
    const res = await fetch('https://esgoo.net/api-tinhthanh/1/0.htm')
    const json = await res.json()
    if (json.error === 0) {
      provinces.value = json.data || []
    }
  } catch (err) {
    console.error("Lỗi tải danh sách Tỉnh/TP:", err)
  }
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
  document.removeEventListener('click', handleClickOutside)
})

function openDropdown(type) {
  activeDropdown.value = type
  if (type === 'city') citySearch.value = ''
  if (type === 'district') districtSearch.value = ''
  if (type === 'ward') wardSearch.value = ''
}

async function selectCity(p) {
  selectedCity.value = p.id
  citySearch.value = p.full_name 
  activeDropdown.value = null
  
  selectedDistrict.value = ''
  districtSearch.value = ''
  districts.value = []
  selectedWard.value = ''
  wardSearch.value = ''
  wards.value = []

  try {
    const res = await fetch(`https://esgoo.net/api-tinhthanh/2/${p.id}.htm`)
    const json = await res.json()
    if (json.error === 0) districts.value = json.data || []
  } catch (err) {}
}

async function selectDistrict(d) {
  selectedDistrict.value = d.id
  districtSearch.value = d.full_name
  activeDropdown.value = null

  selectedWard.value = ''
  wardSearch.value = ''
  wards.value = []

  try {
    const res = await fetch(`https://esgoo.net/api-tinhthanh/3/${d.id}.htm`)
    const json = await res.json()
    if (json.error === 0) wards.value = json.data || []
  } catch (err) {}
}

function selectWard(w) {
  selectedWard.value = w.id
  wardSearch.value = w.full_name
  activeDropdown.value = null
}

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
  if (!selectedCity.value || !selectedDistrict.value || !selectedWard.value) {
    alert("Vui lòng chọn đúng Tỉnh/Huyện/Xã từ danh sách gợi ý!");
    return;
  }

  orderLoading.value = true
  try {
    const fullAddress = [form.address, wardSearch.value, districtSearch.value, citySearch.value].filter(Boolean).join(', ')
    
    const res = await createOrder({
      userId: auth.state?.user?.id || null,
      customerName: form.fullName,
      customerEmail: form.email,
      customerPhone: form.phone,
      address: fullAddress, 
      total: total.value,
      items: cart.state.items.map(it => ({
        productName: it.name,
        quantity: it.quantity,
        unitPrice: toNumber(it.price),
      })),
      paymentMethod: form.paymentMethod 
    })
    
    if (form.paymentMethod === 'bank' && res.qrUrl) {
      qrData.value = { url: res.qrUrl, id: res.id, total: res.total }
      startPollingPaymentStatus(res.id);
    } else {
      cart.clear()
      alert('Đặt hàng thành công!')
      router.push('/profile?tab=orders')
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.message || 'Có lỗi xảy ra, vui lòng thử lại.'
    alert(msg)
  } finally {
    orderLoading.value = false
  }
}

function startPollingPaymentStatus(orderId) {
  pollingInterval = setInterval(async () => {
    try {
      const res = await checkOrderStatus(orderId);
      if (res.status === 'paid') {
        clearInterval(pollingInterval);
        cart.clear();
        qrData.value = null;
        alert('Thanh toán thành công! Đơn hàng của bạn đang được xử lý.');
        router.push('/profile?tab=orders');
      }
    } catch (error) {
      console.error('Lỗi khi kiểm tra trạng thái thanh toán:', error);
    }
  }, 3000);
}

async function cancelQR() {
  if (!qrData.value?.id) return
  isCancelling.value = true
  try {
    if (pollingInterval) clearInterval(pollingInterval);
    await deleteOrder(qrData.value.id)
  } catch (e) {
    console.error("Lỗi khi hủy đơn:", e)
  } finally {
    isCancelling.value = false
    qrData.value = null 
  }
}
</script>

<style scoped>
/* =========================================================
   PREMIUM PROMAX CSS - APPLE / LUXURY BRAND AESTHETIC
   ========================================================= */

@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.checkout-page {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 80px;
  color: #1d1d1f;
  background-color: #fbfbfd; /* Apple light gray background */
  min-height: 100vh;
}

/* --- HEADER --- */
.checkout-header {
  margin-bottom: 40px;
  text-align: center;
}

.breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #86868b;
  background: #fff;
  padding: 8px 16px;
  border-radius: 99px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  margin-bottom: 24px;
}

.crumb { cursor: pointer; transition: color 0.2s; }
.crumb:hover { color: #1d1d1f; }
.sep-icon { width: 14px; height: 14px; opacity: 0.5; }
.current { color: #1d1d1f; font-weight: 600; }

.checkout-title {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
  margin: 0 0 8px;
}

.checkout-subtitle {
  font-size: 15px;
  color: #86868b;
}

/* --- LAYOUT --- */
.checkout-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 32px;
  align-items: start;
}

.checkout-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* --- CARDS --- */
.premium-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0,0,0,0.02);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.step-number {
  width: 28px;
  height: 28px;
  background: #1d1d1f;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
}

.card-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
}

/* --- FORMS --- */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.req { color: #ff3b30; }

.form-group input, 
.form-group textarea {
  padding: 16px;
  background: #f5f5f7;
  border: 1.5px solid transparent;
  border-radius: 14px;
  font-size: 15px;
  font-family: inherit;
  color: #1d1d1f;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-group input::placeholder, 
.form-group textarea::placeholder {
  color: #86868b;
}

.form-group input:focus, 
.form-group textarea:focus {
  outline: none;
  background: #fff;
  border-color: #1d1d1f;
  box-shadow: 0 0 0 4px rgba(29, 29, 31, 0.1);
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* --- DROPDOWNS --- */
.custom-select-wrapper { position: relative; }
.custom-select { position: relative; }
.dropdown-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 10px; height: 10px;
  border-right: 2px solid #86868b;
  border-bottom: 2px solid #86868b;
  transform: translateY(-70%) rotate(45deg);
  pointer-events: none;
}
.dropdown-list {
  position: absolute;
  top: calc(100% + 8px);
  left: 0; right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 16px;
  max-height: 250px;
  overflow-y: auto;
  z-index: 20;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 8px;
  list-style: none;
}

/* Tùy chỉnh thanh cuộn cho mượt */
.dropdown-list::-webkit-scrollbar { width: 6px; }
.dropdown-list::-webkit-scrollbar-thumb { background: #d2d2d7; border-radius: 10px; }

.dropdown-list li {
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.dropdown-list li:hover:not(.no-result) {
  background-color: #f5f5f7;
  color: #0066cc;
}
.dropdown-list li.no-result { color: #86868b; text-align: center; cursor: default; }

/* --- PHƯƠNG THỨC THANH TOÁN --- */
.payment-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border: 1.5px solid #e5e5ea;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-card:hover {
  border-color: #d2d2d7;
  background: #fbfbfd;
}

.payment-card.active {
  border-color: #1d1d1f;
  background: #f5f5f7;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.radio-custom {
  position: relative;
  width: 22px; height: 22px;
  margin-top: 2px;
}
.radio-custom input { opacity: 0; position: absolute; }
.radio-mark {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  border: 2px solid #c7c7cc; border-radius: 50%;
  transition: all 0.2s ease;
}
.payment-card.active .radio-mark {
  border-color: #1d1d1f;
  border-width: 6px;
}

.payment-info { display: flex; flex-direction: column; gap: 4px; }
.pay-title { font-weight: 600; font-size: 15px; color: #1d1d1f; }
.pay-desc { font-size: 13px; color: #86868b; }

/* --- SIDEBAR --- */
.checkout-sidebar { position: sticky; top: 24px; }

.summary-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0,0,0,0.04);
}

.summary-title {
  font-size: 20px;
  font-weight: 800;
  margin: 0 0 24px;
  letter-spacing: -0.5px;
}

.order-items-scroll {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 8px;
  margin-bottom: 24px;
}
.order-items-scroll::-webkit-scrollbar { width: 4px; }
.order-items-scroll::-webkit-scrollbar-thumb { background: #e5e5ea; border-radius: 10px; }

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.item-img-wrapper {
  position: relative;
  width: 64px; height: 64px;
  border-radius: 12px;
  background: #f5f5f7;
  padding: 4px;
}
.item-img-wrapper img { width: 100%; height: 100%; object-fit: contain; }
.item-badge {
  position: absolute;
  top: -6px; right: -6px;
  background: #1d1d1f; color: #fff;
  width: 20px; height: 20px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700;
}

.item-details { flex: 1; }
.item-name { font-weight: 600; font-size: 14px; margin: 0 0 4px; }
.item-variant { font-size: 12px; color: #86868b; margin: 0; }
.item-price { font-weight: 700; font-size: 14px; }

/* --- VOUCHER --- */
.voucher-section {
  padding: 20px 0;
  border-top: 1px solid #f2f2f7;
  border-bottom: 1px solid #f2f2f7;
  margin-bottom: 24px;
}
.voucher-input-group {
  display: flex; gap: 8px;
}
.voucher-input-group input {
  flex: 1;
  padding: 14px;
  background: #f5f5f7;
  border: 1px solid transparent;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}
.voucher-input-group input:focus { background: #fff; border-color: #1d1d1f; outline: none; }
.voucher-input-group button {
  padding: 0 20px;
  background: #1d1d1f; color: #fff;
  border: none; border-radius: 12px;
  font-weight: 600; font-size: 14px;
  cursor: pointer; transition: background 0.2s;
}
.voucher-input-group button:hover:not(:disabled) { background: #333336; }
.voucher-input-group button:disabled { opacity: 0.5; cursor: not-allowed; }
.voucher-msg { font-size: 13px; font-weight: 500; margin: 8px 0 0; }
.voucher-msg.error { color: #ff3b30; }
.voucher-msg.success { color: #34c759; }

/* --- TÀI CHÍNH --- */
.financials {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}
.fin-row {
  display: flex; justify-content: space-between;
  font-size: 14px; color: #86868b; font-weight: 500;
}
.fin-row span:last-child { color: #1d1d1f; font-weight: 600; }
.fin-row.highlight span { color: #34c759; }
.free-shipping { color: #34c759 !important; text-transform: uppercase; font-size: 12px; letter-spacing: 0.5px; }

.fin-row.grand-total {
  margin-top: 8px; padding-top: 16px;
  border-top: 1px solid #f2f2f7;
  font-size: 18px; color: #1d1d1f; align-items: center;
}
.total-amount { font-size: 24px !important; font-weight: 800 !important; letter-spacing: -0.5px; }

/* --- NÚT SUBMIT --- */
.btn-checkout-submit {
  width: 100%;
  padding: 18px;
  background: #1d1d1f;
  color: #fff;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex; justify-content: center; align-items: center; gap: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}
.btn-checkout-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 25px rgba(0,0,0,0.15);
  background: #333336;
}
.btn-checkout-submit:disabled { opacity: 0.7; cursor: not-allowed; transform: none; box-shadow: none; }
.arrow-icon { width: 18px; height: 18px; transition: transform 0.3s; }
.btn-checkout-submit:hover:not(:disabled) .arrow-icon { transform: translateX(4px); }

.secure-checkout {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  margin-top: 16px; font-size: 13px; font-weight: 500; color: #86868b; text-align: center;
}
.secure-checkout svg { width: 14px; height: 14px; opacity: 0.6; }

/* --- SPINNER --- */
.loader { border: 3px solid rgba(255,255,255,0.3); border-radius: 50%; border-top: 3px solid #fff; width: 24px; height: 24px; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* --- OVERLAY QR (GLASSMORPHISM) --- */
.qr-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(12px); /* Hiệu ứng kính mờ của Apple */
  -webkit-backdrop-filter: blur(12px);
  display: flex; align-items: center; justify-content: center; z-index: 9999;
}

.qr-modal {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 32px;
  padding: 40px;
  width: 90%; max-width: 420px; text-align: center;
  box-shadow: 0 40px 80px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255,255,255,0.2);
}

.qr-modal-header h2 { font-size: 24px; font-weight: 800; letter-spacing: -0.5px; margin: 0 0 8px; color: #1d1d1f; }
.qr-modal-header p { font-size: 15px; color: #86868b; margin-bottom: 32px; }

.qr-bill-info {
  background: #f5f5f7; border-radius: 20px; padding: 20px; margin-bottom: 24px;
}
.bill-row { display: flex; justify-content: space-between; font-size: 15px; margin-bottom: 8px; color: #86868b; font-weight: 500; }
.bill-row:last-child { margin-bottom: 0; }
.bill-row strong { color: #1d1d1f; font-weight: 700; }
.highlight-price { font-size: 20px; color: #ff3b30 !important; }

.qr-image-wrapper {
  position: relative;
  width: 240px; height: 240px;
  margin: 0 auto 32px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  border: 1px solid #e5e5ea;
}
.qr-image-wrapper img { width: 100%; height: 100%; object-fit: contain; }
.scan-line { position: absolute; top: 0; left: 0; right: 0; height: 4px; background: rgba(52, 199, 89, 0.8); box-shadow: 0 0 10px rgba(52, 199, 89, 0.5); animation: scan 3s linear infinite; }
@keyframes scan { 0% { top: 0; opacity: 0; } 10% { opacity: 1; } 90% { opacity: 1; } 100% { top: 100%; opacity: 0; } }

.qr-status-indicator { display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px; font-weight: 600; color: #34c759; margin-bottom: 24px; }
.pulsing-dot { width: 10px; height: 10px; background: #34c759; border-radius: 50%; box-shadow: 0 0 0 0 rgba(52, 199, 89, 0.7); animation: pulse 2s infinite; }
@keyframes pulse { 0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(52, 199, 89, 0.7); } 70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(52, 199, 89, 0); } 100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(52, 199, 89, 0); } }

.btn-cancel-qr { width: 100%; padding: 18px; border-radius: 16px; font-size: 16px; font-weight: 600; border: none; background: #f5f5f7; color: #1d1d1f; cursor: pointer; transition: background 0.2s; }
.btn-cancel-qr:hover:not(:disabled) { background: #e5e5ea; }

/* Hiệu ứng Fade/Scale cho Popup */
.fade-scale-enter-active, .fade-scale-leave-active { transition: opacity 0.3s, transform 0.3s; }
.fade-scale-enter-from, .fade-scale-leave-to { opacity: 0; transform: scale(0.9); }
.fade-scale-enter-to, .fade-scale-leave-from { opacity: 1; transform: scale(1); }

/* --- RESPONSIVE --- */
@media (max-width: 1024px) { .checkout-layout { grid-template-columns: 1fr; } .checkout-sidebar { position: static; } }
@media (max-width: 640px) { .form-row { grid-template-columns: 1fr; } .checkout-page { padding: 20px 16px 80px; } .checkout-title { font-size: 28px; } }
</style>