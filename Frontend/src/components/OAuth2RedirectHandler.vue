<template>
  <div class="redirect-container">
    <div class="loader-box">
      <div class="spinner"></div>
      <p>Đang đồng bộ tài khoản KESN STORE...</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../authStore' // Đảm bảo đúng đường dẫn tới store của bạn

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  // 1. Lấy dữ liệu từ URL query
  const token = route.query.token
  const email = route.query.email
  const role = route.query.role
  const userIdRaw = route.query.userId
  let userId = userIdRaw != null && userIdRaw !== '' ? Number(userIdRaw) : undefined
  if (userId !== undefined && !Number.isFinite(userId)) userId = undefined
  
  // 2. GIẢI MÃ TIẾNG VIỆT (Hứng từ encodedName của Backend)
  let fullName = route.query.fullName || ""
  try {
    // decodeURIComponent sẽ xử lý các ký tự % và biến chúng về tiếng Việt chuẩn
    fullName = decodeURIComponent(fullName.replace(/\+/g, " "))
  } catch (e) {
    console.error("Lỗi giải mã tên người dùng:", e)
  }

  if (token) {
    // 3. Tạo object user và lưu vào Store (LocalStorage/Pinia)
    const userData = {
      ...(userId != null ? { id: userId } : {}),
      email: email,
      fullName: fullName,
      role: role || 'customer'
    }

    authStore.login(userData, token)

    // 4. Đăng nhập xong thì "đá" về trang chủ
    setTimeout(() => {
      router.push('/')
    }, 500)
  } else {
    // Nếu không có token, quay lại trang đăng nhập
    router.push('/login')
  }
})
</script>

<style scoped>
.redirect-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #f4f4f4;
  font-family: 'Inter', sans-serif;
}
.loader-box { text-align: center; }
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #000;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>