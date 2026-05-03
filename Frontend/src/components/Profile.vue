<template>
  <div class="profile-page">
    <div class="profile-inner">
    <header class="profile-header">
      <p class="profile-eyebrow">KESN Store</p>
      <h1>Tài khoản của tôi</h1>
      <p class="profile-lead">Quản lý thông tin cá nhân và đơn hàng</p>
    </header>

    <div class="profile-layout">
      <nav class="profile-nav" aria-label="Menu tài khoản">
        <button
          type="button"
          class="nav-item"
          :class="{ active: tab === 'info' }"
          @click="tab = 'info'"
        >
          Thông tin cá nhân
        </button>
        <button
          type="button"
          class="nav-item"
          :class="{ active: tab === 'password' }"
          @click="tab = 'password'"
        >
          Đổi mật khẩu
        </button>
        <button
          type="button"
          class="nav-item"
          :class="{ active: tab === 'orders' }"
          @click="tab = 'orders'"
        >
          Lịch sử đơn hàng
        </button>
        <button
          v-if="isAdmin"
          type="button"
          class="nav-item nav-item--admin"
          @click="$router.push('/admin')"
        >
          Trang Admin
        </button>
        <button type="button" class="nav-item nav-item--logout" @click="logout">
          Đăng xuất
        </button>
      </nav>

      <main class="profile-main">
        <section v-if="tab === 'info'" class="block block--raised">
          <h2>Thông tin cá nhân</h2>
          <div v-if="user" class="profile-summary">
            <p class="summary-row"><strong>Họ và tên:</strong> {{ user.fullName || user.full_name || '—' }}</p>
            <p class="summary-row"><strong>Email:</strong> {{ user.email || '—' }}</p>
            <p class="summary-row"><strong>Số điện thoại:</strong> {{ user.phone || '—' }}</p>
          </div>
          <form class="profile-form" @submit.prevent="saveProfile">
            <div class="form-group">
              <label>Họ và tên</label>
              <input v-model="form.fullName" placeholder="Họ và tên" />
            </div>
            <div class="form-group">
              <label>Email</label>
              <input v-model="form.email" type="email" disabled placeholder="Email" />
              <span class="hint">Email không thể thay đổi</span>
            </div>
            <div class="form-group">
              <label>Số điện thoại</label>
              <input v-model="form.phone" placeholder="Số điện thoại" />
            </div>
            <button type="submit" class="btn-save">Lưu thay đổi</button>
          </form>
        </section>

        <section v-else-if="tab === 'password'" class="block block--raised">
          <h2>Đổi mật khẩu</h2>
          <form class="profile-form" @submit.prevent="changePassword">
            <div class="form-group">
              <label>Mật khẩu hiện tại</label>
              <input v-model="pwForm.current" type="password" placeholder="Mật khẩu hiện tại" />
            </div>
            <div class="form-group">
              <label>Mật khẩu mới</label>
              <input v-model="pwForm.new" type="password" placeholder="Mật khẩu mới" />
            </div>
            <div class="form-group">
              <label>Xác nhận mật khẩu mới</label>
              <input v-model="pwForm.confirm" type="password" placeholder="Xác nhận" />
            </div>
            <p v-if="pwError" class="error-msg">{{ pwError }}</p>
            <p v-if="pwSuccess" class="success-msg">Đã đổi mật khẩu thành công (demo)</p>
            <button type="submit" class="btn-save">Đổi mật khẩu</button>
          </form>
        </section>

        <section v-else class="block block--raised block--flush">
          <OrderHistory />
        </section>
      </main>
    </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../authStore'
import OrderHistory from './OrderHistory.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const user = computed(() => auth.state?.user)
const isAdmin = computed(() => auth.state?.user?.role === 'admin')

function logout() {
  auth.logout()
  router.push('/login')
}

const tab = ref('info')
const form = reactive({ fullName: '', email: '', phone: '' })
const pwForm = reactive({ current: '', new: '', confirm: '' })
const pwError = ref('')
const pwSuccess = ref('')

onMounted(() => {
  const u = auth.state?.user
  if (u) {
    form.fullName = u.fullName || u.full_name || ''
    form.email = u.email || ''
    form.phone = u.phone || ''
  }
})

watch(
  () => route.query.tab,
  (t) => {
    if (t === 'orders') tab.value = 'orders'
  },
  { immediate: true }
)

function saveProfile() {
  // Mock - khi có backend sẽ gọi API
  alert('Đã lưu thông tin (demo). Backend chưa có.')
}

function changePassword() {
  pwError.value = ''
  pwSuccess.value = ''
  if (pwForm.new !== pwForm.confirm) {
    pwError.value = 'Mật khẩu mới và xác nhận không khớp'
    return
  }
  if (pwForm.new.length < 6) {
    pwError.value = 'Mật khẩu mới phải có ít nhất 6 ký tự'
    return
  }
  pwSuccess.value = 'Đã đổi mật khẩu thành công (demo)'
  pwForm.current = pwForm.new = pwForm.confirm = ''
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.profile-page {
  font-family: 'Inter', -apple-system, system-ui, sans-serif;
  width: 100%;
  background: linear-gradient(-45deg, #0f172a, #000000, #312e81, #0f172a);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  min-height: calc(100vh - 100px);
  color: #fff;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.profile-inner {
  max-width: 1040px;
  margin: 0 auto;
  padding: 28px 18px 80px;
}

.profile-header {
  margin-bottom: 28px;
}

.profile-eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 10px;
}

.profile-header h1 {
  font-size: clamp(26px, 4vw, 32px);
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 8px;
  line-height: 1.15;
  background: linear-gradient(135deg, #fff, #a5b4fc, #fbcfe8);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shine 5s linear infinite;
}

@keyframes shine {
  to { background-position: 200% center; }
}

.profile-lead {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.5;
  max-width: 420px;
}

.profile-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 28px;
  align-items: start;
}

.profile-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  position: sticky;
  top: 88px;
}

.nav-item {
  padding: 12px 14px;
  text-align: left;
  border: 1px solid transparent;
  background: transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #e2e8f0;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-item.active {
  background: linear-gradient(45deg, #4f46e5, #ec4899);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 14px rgba(236, 72, 153, 0.3);
}

.nav-item--admin {
  margin-top: 10px;
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}

.nav-item--admin:hover {
  background: #020617;
  color: #fff;
}

.nav-item--logout {
  margin-top: 8px;
  color: #b91c1c;
}

.nav-item--logout:hover {
  background: #fef2f2;
  color: #991b1b;
}

.nav-item:focus-visible {
  outline: 2px solid #0f172a;
  outline-offset: 2px;
}

.profile-main .block {
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 28px 28px 32px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(16px);
}

.block--raised {
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 16px 48px rgba(15, 23, 42, 0.07);
}

.block--flush {
  padding: 24px 26px 28px;
}

.block h2 {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 22px;
}

.profile-summary {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 20px 22px;
  margin-bottom: 28px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.summary-row {
  margin: 0 0 10px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.summary-row strong {
  color: #fff;
  margin-right: 8px;
  font-weight: 700;
}

.profile-form {
  max-width: 420px;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.9);
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.form-group input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.form-group input:disabled {
  background: rgba(255, 255, 255, 0.5);
  color: #475569;
}

.hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 6px;
  display: block;
}

.error-msg {
  color: #b91c1c;
  font-size: 14px;
  margin-bottom: 12px;
}

.success-msg {
  color: #047857;
  font-size: 14px;
  margin-bottom: 12px;
}

.btn-save {
  padding: 12px 22px;
  background: linear-gradient(45deg, #4f46e5, #ec4899);
  background-size: 200% auto;
  color: #fff;
  border: none;
  border-radius: 14px;
  font-weight: 800;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(236, 72, 153, 0.3);
  transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
}

.btn-save:hover {
  background-position: right center;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.4);
}

.btn-save:active {
  transform: scale(0.98);
}

.btn-save:focus-visible {
  outline: 2px solid #0f172a;
  outline-offset: 2px;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-nav {
    position: static;
    flex-direction: row;
    flex-wrap: wrap;
    padding: 10px;
  }

  .nav-item {
    flex: 1 1 auto;
    min-width: calc(50% - 6px);
    text-align: center;
    font-size: 13px;
    padding: 10px 8px;
  }

  .nav-item--admin,
  .nav-item--logout {
    flex: 1 1 100%;
    margin-top: 4px;
  }
}
</style>
