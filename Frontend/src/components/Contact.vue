<template>
  <div class="contact-page">
    <header class="contact-header">
      <h1>Liên hệ</h1>
      <p>Chúng tôi sẵn sàng hỗ trợ bạn</p>
    </header>

    <div class="contact-layout">
      <section class="contact-info">
        <h2>Thông tin liên hệ</h2>
        <div class="info-item">
          <strong>Địa chỉ:</strong>
          <span>123 Đường ABC, Quận 1, TP.HCM</span>
        </div>
        <div class="info-item">
          <strong>Hotline:</strong>
          <span>1900 xxxx</span>
        </div>
        <div class="info-item">
          <strong>Email:</strong>
          <span>support@kesnstore.com</span>
        </div>
        <div class="info-item">
          <strong>Giờ làm việc:</strong>
          <span>8:00 - 22:00 hàng ngày</span>
        </div>
      </section>

      <section class="contact-form-block">
        <h2>Gửi tin nhắn</h2>
        <form class="contact-form" @submit.prevent="submitForm">
          <div class="form-group">
            <label>Họ và tên *</label>
            <input v-model="form.fullName" required placeholder="Họ và tên" />
          </div>
          <div class="form-group">
            <label>Email *</label>
            <input v-model="form.email" type="email" required placeholder="Email" />
          </div>
          <div class="form-group">
            <label>Số điện thoại</label>
            <input v-model="form.phone" placeholder="Số điện thoại" />
          </div>
          <div class="form-group">
            <label>Nội dung *</label>
            <textarea v-model="form.message" rows="4" required placeholder="Nội dung cần hỗ trợ"></textarea>
          </div>
          <p v-if="sent" class="success-msg">Đã gửi tin nhắn thành công (demo). Chúng tôi sẽ phản hồi sớm.</p>
          <button type="submit" class="btn-send">Gửi tin nhắn</button>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useAuthStore } from '../authStore'

const auth = useAuthStore()
const form = reactive({
  fullName: auth.user?.fullName || '',
  email: auth.user?.email || '',
  phone: auth.user?.phone || '',
  message: '',
})
const sent = ref(false)

function submitForm() {
  // Mock - khi có backend sẽ gửi API
  sent.value = true
}
</script>

<style scoped>
.contact-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 72px;
}

.contact-header {
  margin-bottom: 32px;
}

.contact-header h1 {
  font-size: 28px;
  font-weight: 800;
}

.contact-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 8px 0 0;
}

.contact-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 32px;
}

.contact-info,
.contact-form-block {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  background: #fff;
}

.contact-info h2,
.contact-form-block h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 18px;
}

.info-item {
  margin-bottom: 14px;
  font-size: 14px;
}

.info-item strong {
  display: block;
  margin-bottom: 4px;
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

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
}

.success-msg {
  color: #059669;
  font-size: 14px;
  margin-bottom: 12px;
}

.btn-send {
  padding: 12px 24px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
}

@media (max-width: 768px) {
  .contact-layout {
    grid-template-columns: 1fr;
  }
}
</style>
