<template>
  <div class="payment-return-container">
    <div class="status-card">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Đang kiểm tra kết quả giao dịch...</p>
      </div>

      <div v-else class="result-state">
        <div v-if="vnpResponseCode === '00'" class="success-box">
          <div class="icon-circle success">✓</div>
          <h1>Thanh toán thành công!</h1>
          <p>Mã đơn hàng của bạn: <strong>#{{ orderId }}</strong></p>
          <p>Cảm ơn Nguyên đã tin tưởng mua sắm giày tại cửa hàng.</p>
          <button class="btn-home" @click="router.push('/')">Quay lại trang chủ</button>
        </div>

        <div v-else class="error-box">
          <div class="icon-circle error">✕</div>
          <h1>Thanh toán thất bại</h1>
          <p>Giao dịch đã bị hủy hoặc có lỗi xảy ra (Mã: {{ vnpResponseCode }}).</p>
          <button class="btn-retry" @click="router.push('/checkout')">Thử lại ngay</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCart } from '../cartStore'

const route = useRoute()
const router = useRouter()
const cart = useCart()

const loading = ref(true)
const vnpResponseCode = ref('')
const orderId = ref('')

onMounted(() => {
  // Lấy params từ URL do VNPAY trả về (ví dụ: ?vnp_ResponseCode=00&vnp_TxnRef=123...)
  vnpResponseCode.value = route.query.vnp_ResponseCode
  orderId.value = route.query.vnp_TxnRef

  if (vnpResponseCode.value === '00') {
    cart.clear() // Xóa giỏ hàng khi thành công
  }
  loading.value = false
})
</script>

<style scoped>
.payment-return-container {
  display: flex; justify-content: center; align-items: center;
  min-height: 80vh; background: #fbfbfd;
}
.status-card {
  background: #fff; padding: 40px; border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.05); text-align: center;
  max-width: 420px; width: 100%;
}
.icon-circle {
  width: 70px; height: 70px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 32px; margin: 0 auto 20px;
}
.success { background: #e8f7ed; color: #34c759; }
.error { background: #fff1f0; color: #ff3b30; }
h1 { font-size: 24px; font-weight: 800; margin-bottom: 12px; }
p { color: #86868b; line-height: 1.5; margin-bottom: 24px; }
button {
  width: 100%; padding: 16px; border: none; border-radius: 14px;
  font-weight: 700; cursor: pointer; transition: 0.2s;
}
.btn-home { background: #1d1d1f; color: #fff; }
.btn-retry { background: #ff3b30; color: #fff; }
.spinner {
  border: 4px solid #f3f3f3; border-top: 4px solid #1d1d1f;
  border-radius: 50%; width: 40px; height: 40px;
  animation: spin 1s linear infinite; margin: 0 auto 15px;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>