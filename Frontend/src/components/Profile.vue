<template>
  <div class="profile-page">
    <header class="profile-header">
      <h1>Tài khoản của tôi</h1>
      <p>Quản lý thông tin cá nhân và đơn hàng</p>
    </header>

    <div class="profile-layout">
      <nav class="profile-nav">
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
        <section v-if="tab === 'info'" class="block">
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

        <section v-else-if="tab === 'password'" class="block">
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

        <section v-else class="block">
          <OrderHistory />
        </section>
      </main>
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
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 16px 72px;
}

.profile-header {
  margin-bottom: 24px;
}

.profile-header h1 {
  font-size: 28px;
  font-weight: 800;
}

.profile-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 4px 0 0;
}

.profile-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 24px;
}

.profile-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  padding: 12px 16px;
  text-align: left;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.nav-item:hover {
  background: #f9fafb;
}

.nav-item.active {
  background: #111827;
  color: #fff;
  border-color: #111827;
}

.nav-item--admin {
  margin-top: 8px;
  background: #111827;
  color: #fff;
  border-color: #111827;
}

.nav-item--admin:hover {
  background: #020617;
}

.nav-item--logout {
  margin-top: 12px;
  color: #b91c1c;
}

.nav-item--logout:hover {
  background: #fef2f2;
}

.profile-main .block {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  background: #fff;
}

.block h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 20px;
}

.profile-summary {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.summary-row {
  margin: 0 0 8px;
  font-size: 15px;
  color: #374151;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.summary-row strong {
  color: #111827;
  margin-right: 8px;
}

.profile-form {
  max-width: 400px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
}

.form-group input:disabled {
  background: #f3f4f6;
  color: #6b7280;
}

.hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.error-msg {
  color: #b91c1c;
  font-size: 14px;
  margin-bottom: 12px;
}

.success-msg {
  color: #059669;
  font-size: 14px;
  margin-bottom: 12px;
}

.btn-save {
  padding: 10px 20px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}
</style>
