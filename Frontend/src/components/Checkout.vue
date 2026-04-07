<template>
  <div class="checkout-page promax-theme">
    <header class="checkout-header animate-fade-down">
      <div class="breadcrumb-container">
        <div class="breadcrumb-glass">
          <span class="crumb" @click="$router.push('/')">Trang chủ</span>
          <span class="sep">/</span>
          <span class="crumb" @click="$router.push('/cart')">Giỏ hàng</span>
          <span class="sep">/</span>
          <span class="current">Thanh toán</span>
        </div>
      </div>
      <h1 class="checkout-title">Thanh Toán Đơn Hàng</h1>
      <p class="checkout-subtitle">Mãnh thú Sneaker sắp thuộc về sở hữu của bạn.</p>
    </header>

    <section v-if="cart.state.items.length === 0" class="empty-state-god">
      <div class="empty-content">
        <div class="empty-icon">🛍️</div>
        <h2>Giỏ hàng của bạn đang trống</h2>
        <button class="btn-god-mode" @click="$router.push('/product')">Tiếp tục mua sắm</button>
      </div>
    </section>

    <form v-else class="checkout-layout" @submit.prevent="placeOrder">
      <main class="checkout-main animate-fade-left">
        
        <div class="god-card" :class="{ 'card-error': v$.fullName.$error || v$.phone.$error || v$.email.$error }">
          <div class="card-header">
            <div class="step-badge">1</div>
            <h2>Thông tin liên hệ</h2>
          </div>
          <div class="form-grid">
            <div class="input-wrapper">
              <label>Họ và tên <span class="req">*</span></label>
              <input v-model="form.fullName" placeholder="Nguyễn Văn A" @blur="v$.fullName.$touch()" />
              <span v-if="v$.fullName.$error" class="err-msg">Vui lòng nhập họ tên</span>
            </div>
            <div class="input-wrapper">
              <label>Số điện thoại <span class="req">*</span></label>
              <input v-model="form.phone" placeholder="0901234567" @blur="v$.phone.$touch()" />
              <span v-if="v$.phone.$error" class="err-msg">SĐT phải có 10 chữ số</span>
            </div>
          </div>
          <div class="input-wrapper full-width">
            <label>Email liên hệ <span class="req">*</span></label>
            <input v-model="form.email" type="email" placeholder="email@example.com" @blur="v$.email.$touch()" />
            <span v-if="v$.email.$error" class="err-msg">Email không hợp lệ</span>
          </div>
        </div>

        <div class="god-card" :class="{ 'card-error': v$.address.$error || !selectedWard }">
          <div class="card-header">
            <div class="step-badge">2</div>
            <h2>Địa chỉ nhận hàng</h2>
          </div>
          
          <div class="form-grid">
            <div class="input-wrapper custom-select">
              <label>Tỉnh/Thành phố <span class="req">*</span></label>
              <div class="select-box">
                <input v-model="citySearch" @focus="openDropdown('city')" placeholder="Chọn Tỉnh/Thành phố..." readonly />
                <ul v-if="activeDropdown === 'city'" class="dropdown-god">
                  <li v-for="p in filteredProvinces" :key="p.id" @click="selectCity(p)">{{ p.full_name }}</li>
                </ul>
              </div>
            </div>

            <div class="input-wrapper custom-select">
              <label>Quận/Huyện <span class="req">*</span></label>
              <div class="select-box">
                <input v-model="districtSearch" @focus="openDropdown('district')" :disabled="!selectedCity" placeholder="Chọn Quận/Huyện..." readonly />
                <ul v-if="activeDropdown === 'district'" class="dropdown-god">
                  <li v-for="d in filteredDistricts" :key="d.id" @click="selectDistrict(d)">{{ d.full_name }}</li>
                </ul>
              </div>
            </div>
          </div>

          <div class="form-grid">
            <div class="input-wrapper custom-select">
              <label>Phường/Xã <span class="req">*</span></label>
              <div class="select-box">
                <input v-model="wardSearch" @focus="openDropdown('ward')" :disabled="!selectedDistrict" placeholder="Chọn Phường/Xã..." readonly />
                <ul v-if="activeDropdown === 'ward'" class="dropdown-god">
                  <li v-for="w in filteredWards" :key="w.id" @click="selectWard(w)">{{ w.full_name }}</li>
                </ul>
              </div>
            </div>

            <div class="input-wrapper">
              <label>Số nhà, Tên đường <span class="req">*</span></label>
              <input v-model="form.address" placeholder="Số 123, đường..." @blur="v$.address.$touch()" />
              <span v-if="v$.address.$error" class="err-msg">Vui lòng nhập địa chỉ cụ thể</span>
            </div>
          </div>
          
          <div class="input-wrapper full-width">
            <label>Ghi chú cho shipper</label>
            <textarea v-model="form.note" rows="2" placeholder="Giao giờ hành chính..."></textarea>
          </div>
        </div>

        <div class="god-card">
          <div class="card-header">
            <div class="step-badge">3</div>
            <h2>Phương thức thanh toán</h2>
          </div>
          <div class="payment-grid">
            <label class="payment-box" :class="{ active: form.paymentMethod === 'cod' }">
              <input v-model="form.paymentMethod" type="radio" value="cod" hidden />
              <div class="payment-icon">🚚</div>
              <div class="payment-text">
                <span class="p-title">Thanh toán COD</span>
                <span class="p-sub">Tiền mặt khi nhận hàng</span>
              </div>
            </label>
            
            <label class="payment-box bank-box" :class="{ active: form.paymentMethod === 'bank' }">
              <input v-model="form.paymentMethod" type="radio" value="bank" hidden />
              <div class="payment-icon">⚡</div>
              <div class="payment-text">
                <span class="p-title">VNPAY (ATM / QR-Code)</span>
                <span class="p-sub">Thanh toán tức thì, bảo mật</span>
              </div>
              <div class="recomended-tag">KHUYÊN DÙNG</div>
            </label>
          </div>
        </div>
      </main>

      <aside class="checkout-sidebar animate-fade-right">
        <div class="summary-god-card">
          <h2 class="summary-title">Tóm tắt đơn hàng</h2>
          <div class="item-list scroll-god">
            <div v-for="it in cart.state.items" :key="it.key" class="item-god-row">
              <div class="item-thumb">
                <img :src="it.image" :alt="it.name" />
                <span class="qty-badge">{{ it.quantity }}</span>
              </div>
              <div class="item-info">
                <p class="it-name">{{ it.name }}</p>
                <p class="it-price">{{ formatPrice(toNumber(it.price) * it.quantity) }}đ</p>
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="price-rows">
            <div class="p-row"><span>Tạm tính</span><span>{{ formatPrice(subtotal) }} ₫</span></div>
            <div class="p-row total-row">
              <span>TỔNG CỘNG</span>
              <span class="grand-total">{{ formatPrice(total) }} ₫</span>
            </div>
          </div>

          <button type="submit" class="btn-checkout-god" :disabled="orderLoading">
            <span v-if="orderLoading" class="loader-god"></span>
            <span v-else>XÁC NHẬN ĐẶT HÀNG</span>
          </button>
        </div>
      </aside>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '../cartStore'
import { useAuthStore } from '../authStore'
import { createOrder } from '../api/services/orderService'
import { useVuelidate } from '@vuelidate/core'
import { required, email, minLength, maxLength, helpers } from '@vuelidate/validators'

const router = useRouter(); const cart = useCart(); const auth = useAuthStore()
const orderLoading = ref(false); const activeDropdown = ref(null)

const provinces = ref([]); const districts = ref([]); const wards = ref([])
const selectedCity = ref(''); const selectedDistrict = ref(''); const selectedWard = ref('')
const citySearch = ref(''); const districtSearch = ref(''); const wardSearch = ref('')

const form = reactive({
  fullName: '', phone: '', email: '', address: '', note: '',
  paymentMethod: 'bank'
})

// Validation Rules
const rules = {
  fullName: { required },
  phone: { required, minLength: minLength(10), maxLength: maxLength(11) },
  email: { required, email },
  address: { required }
}
const v$ = useVuelidate(rules, form)

const toNumber = (t) => Number(String(t || '').replace(/[^\d]/g, ''))
const formatPrice = (n) => Number(n).toLocaleString('vi-VN')
const subtotal = computed(() => cart.state.items.reduce((sum, it) => sum + toNumber(it.price) * it.quantity, 0))
const total = computed(() => subtotal.value)

const filteredProvinces = computed(() => provinces.value.filter(p => p.full_name.toLowerCase().includes(citySearch.value.toLowerCase())))
const filteredDistricts = computed(() => districts.value.filter(d => d.full_name.toLowerCase().includes(districtSearch.value.toLowerCase())))
const filteredWards = computed(() => wards.value.filter(w => w.full_name.toLowerCase().includes(wardSearch.value.toLowerCase())))

const openDropdown = (type) => activeDropdown.value = type
const handleClickOutside = (e) => { if (!e.target.closest('.select-box')) activeDropdown.value = null }

onMounted(async () => {
  const u = auth.state?.user
  if (u) { form.fullName = u.fullName || ''; form.phone = u.phone || ''; form.email = u.email || '' }
  document.addEventListener('click', handleClickOutside)
  try {
    const res = await fetch('https://esgoo.net/api-tinhthanh/1/0.htm')
    const json = await res.json(); if (json.error === 0) provinces.value = json.data
  } catch (err) { console.error(err) }
})

onUnmounted(() => document.removeEventListener('click', handleClickOutside))

async function selectCity(p) {
  selectedCity.value = p.id; citySearch.value = p.full_name; activeDropdown.value = null
  selectedDistrict.value = ''; districtSearch.value = ''; districts.value = []
  const res = await fetch(`https://esgoo.net/api-tinhthanh/2/${p.id}.htm`)
  const json = await res.json(); if (json.error === 0) districts.value = json.data
}

async function selectDistrict(d) {
  selectedDistrict.value = d.id; districtSearch.value = d.full_name; activeDropdown.value = null
  selectedWard.value = ''; wardSearch.value = ''; wards.value = []
  const res = await fetch(`https://esgoo.net/api-tinhthanh/3/${d.id}.htm`)
  const json = await res.json(); if (json.error === 0) wards.value = json.data
}

function selectWard(w) { selectedWard.value = w.id; wardSearch.value = w.full_name; activeDropdown.value = null }

async function placeOrder() {
  const isFormCorrect = await v$.value.$validate()
  if (!isFormCorrect || !selectedWard.value) {
    alert("Vui lòng kiểm tra lại thông tin và địa chỉ!"); return
  }
  
  orderLoading.value = true
  try {
    const fullAddress = [form.address, wardSearch.value, districtSearch.value, citySearch.value].join(', ')
    const res = await createOrder({
      ...form,
      address: fullAddress,
      total: total.value,
      items: cart.state.items.map(it => ({ productName: it.name, quantity: it.quantity, unitPrice: toNumber(it.price) }))
    })
    
    if (form.paymentMethod === 'bank' && res.paymentUrl) {
      window.location.href = res.paymentUrl;
    } else {
      cart.clear(); router.push('/profile?tab=orders'); alert('Thành công!')
    }
  } catch (e) { alert(e.message) } finally { orderLoading.value = false }
}
</script>

<style scoped>
.checkout-page { --primary: #1d1d1f; --accent: #0071e3; --err: #ff3b30; min-height: 100vh; padding: 40px 20px; background: #fbfbfd; color: var(--primary); }

/* Header & Breadcrumb */
.checkout-header { text-align: center; margin-bottom: 50px; }
.breadcrumb-glass { display: inline-flex; gap: 10px; background: rgba(255,255,255,0.8); backdrop-filter: blur(10px); padding: 10px 25px; border-radius: 50px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); font-size: 13px; color: #86868b; }
.checkout-title { font-size: 42px; font-weight: 800; margin: 20px 0 10px; letter-spacing: -1px; }

/* Layout Grid - Balanced */
.checkout-layout { display: grid; grid-template-columns: 1.2fr 0.8fr; gap: 40px; max-width: 1200px; margin: 0 auto; align-items: start; }

/* God Card & Inputs */
.god-card { background: #fff; border-radius: 24px; padding: 35px; margin-bottom: 30px; box-shadow: 0 8px 30px rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.02); transition: 0.3s; }
.card-error { border: 1.5px solid var(--err); }
.card-header { display: flex; align-items: center; gap: 15px; margin-bottom: 30px; }
.step-badge { width: 30px; height: 30px; background: var(--primary); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-wrapper { margin-bottom: 20px; position: relative; }
.input-wrapper label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 10px; color: #1d1d1f; }
.input-wrapper input, textarea { width: 100%; padding: 16px; border-radius: 14px; border: 1px solid #d2d2d7; background: #fcfcfc; font-size: 15px; transition: 0.2s; }
.input-wrapper input:focus { border-color: var(--accent); outline: none; background: #fff; box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1); }
.err-msg { color: var(--err); font-size: 12px; position: absolute; bottom: -18px; left: 5px; font-weight: 500; }

/* Custom Select & Dropdown */
.select-box { position: relative; }
.dropdown-god { position: absolute; top: 105%; left: 0; right: 0; z-index: 100; background: #fff; border-radius: 16px; padding: 10px; box-shadow: 0 15px 40px rgba(0,0,0,0.12); max-height: 250px; overflow-y: auto; list-style: none; border: 1px solid #f2f2f7; }
.dropdown-god li { padding: 12px 15px; border-radius: 10px; cursor: pointer; transition: 0.2s; font-size: 14px; font-weight: 500; }
.dropdown-god li:hover { background: #f5f5f7; color: var(--accent); }

/* Payment Box */
.payment-grid { display: flex; flex-direction: column; gap: 15px; }
.payment-box { display: flex; align-items: center; gap: 20px; padding: 25px; border: 2px solid #e5e5ea; border-radius: 20px; cursor: pointer; transition: 0.3s; position: relative; }
.payment-box.active { border-color: var(--accent); background: #f0f7ff; }
.p-title { font-weight: 700; display: block; }
.p-sub { font-size: 13px; color: #86868b; }
.recomended-tag { position: absolute; right: 15px; top: 15px; background: var(--accent); color: #fff; font-size: 10px; font-weight: 800; padding: 4px 10px; border-radius: 6px; }

/* Sidebar Summary */
.summary-god-card { background: var(--primary); color: #fff; padding: 35px; border-radius: 30px; position: sticky; top: 30px; box-shadow: 0 20px 50px rgba(0,0,0,0.15); }
.summary-title { font-size: 24px; font-weight: 800; margin-bottom: 25px; }
.scroll-god { max-height: 300px; overflow-y: auto; padding-right: 10px; }
.scroll-god::-webkit-scrollbar { width: 4px; }
.scroll-god::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.2); border-radius: 10px; }
.item-god-row { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.item-thumb { width: 55px; height: 55px; background: #fff; border-radius: 12px; padding: 6px; position: relative; }
.item-thumb img { width: 100%; height: 100%; object-fit: contain; }
.qty-badge { position: absolute; top: -8px; right: -8px; background: var(--accent); width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 800; border: 2px solid var(--primary); }
.grand-total { font-size: 28px; color: #fff; font-weight: 800; }
.btn-checkout-god { width: 100%; padding: 20px; background: var(--accent); color: #fff; border: none; border-radius: 18px; font-weight: 800; font-size: 16px; cursor: pointer; margin-top: 30px; transition: 0.3s; }
.btn-checkout-god:hover { transform: translateY(-3px); box-shadow: 0 10px 20px rgba(0, 113, 227, 0.3); }

/* Animation Classes */
.animate-fade-down { animation: fadeDown 0.6s ease-out; }
.animate-fade-left { animation: fadeLeft 0.8s ease-out; }
.animate-fade-right { animation: fadeRight 0.8s ease-out; }
@keyframes fadeDown { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeLeft { from { opacity: 0; transform: translateX(-30px); } to { opacity: 1; transform: translateX(0); } }
@keyframes fadeRight { from { opacity: 0; transform: translateX(30px); } to { opacity: 1; transform: translateX(0); } }

@media (max-width: 1024px) { .checkout-layout { grid-template-columns: 1fr; } .summary-god-card { position: static; } }
</style>