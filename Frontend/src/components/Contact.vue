<template>
  <div class="contact-page">
    <div class="contact-promo" role="region" aria-label="Cam kết dịch vụ">
      <div class="contact-promo-inner">
        Miễn phí giao từ 3.000.000₫ · Đổi trả trong 14 ngày · Hỗ trợ trong giờ làm việc
      </div>
    </div>

    <header class="contact-hero">
      <div class="contact-hero-inner">
        <nav class="contact-breadcrumb" aria-label="Breadcrumb">
          <button type="button" class="contact-breadcrumb-link" @click="$router.push('/')">Trang chủ</button>
          <span class="contact-breadcrumb-sep" aria-hidden="true">/</span>
          <span class="contact-breadcrumb-current">Liên hệ</span>
        </nav>
        <p class="contact-eyebrow">Kết nối</p>
        <h1 class="contact-title">Liên hệ</h1>
        <p class="contact-subtitle">
          Đội ngũ Kesn Store sẵn sàng hỗ trợ đặt hàng, đổi trả và tư vấn size — phản hồi trong vòng 24 giờ làm việc.
        </p>
      </div>
    </header>

    <div class="contact-shell">
      <div class="contact-layout">
        <section class="contact-info" aria-labelledby="contact-info-heading">
          <div class="contact-card-head">
            <h2 id="contact-info-heading" class="contact-card-title">Thông tin liên hệ</h2>
            <p class="contact-card-desc">Bạn cũng có thể nhắn nhanh qua chat góc màn hình.</p>
          </div>

          <ul class="info-list">
            <li class="info-row">
              <span class="info-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M12 21s7-4.5 7-11a7 7 0 1 0-14 0c0 6.5 7 11 7 11z" />
                  <circle cx="12" cy="10" r="2.5" />
                </svg>
              </span>
              <div class="info-body">
                <span class="info-label">Địa chỉ cửa hàng</span>
                <p class="info-value">123 Đường ABC, Quận 1, TP. Hồ Chí Minh</p>
              </div>
            </li>
            <li class="info-row">
              <span class="info-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.8 19.8 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.8 19.8 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.12.9.33 1.78.63 2.63a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.45-1.2a2 2 0 0 1 2.11-.45c.85.3 1.73.5 2.63.63A2 2 0 0 1 22 16.92z" />
                </svg>
              </span>
              <div class="info-body">
                <span class="info-label">Hotline</span>
                <a class="info-link" href="tel:19001234">1900 1234</a>
              </div>
            </li>
            <li class="info-row">
              <span class="info-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" />
                  <path d="m22 6-10 7L2 6" />
                </svg>
              </span>
              <div class="info-body">
                <span class="info-label">Email</span>
                <a class="info-link" href="mailto:support@kesnstore.com">support@kesnstore.com</a>
              </div>
            </li>
            <li class="info-row">
              <span class="info-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="12" cy="12" r="9" />
                  <path d="M12 7v6l4 2" />
                </svg>
              </span>
              <div class="info-body">
                <span class="info-label">Giờ làm việc</span>
                <p class="info-value">Thứ Hai — Chủ Nhật: 8:00 – 22:00</p>
              </div>
            </li>
          </ul>
        </section>

        <section class="contact-form-block" aria-labelledby="contact-form-heading">
          <div class="contact-card-head">
            <h2 id="contact-form-heading" class="contact-card-title">Gửi tin nhắn</h2>
            <p class="contact-card-desc">Điền form bên dưới — hiện tại là bản demo, không gửi lên máy chủ.</p>
          </div>

          <form class="contact-form" @submit.prevent="submitForm">
            <div class="form-grid">
              <div class="form-group">
                <label for="contact-name">Họ và tên <span class="req">*</span></label>
                <input
                  id="contact-name"
                  v-model="form.fullName"
                  type="text"
                  required
                  autocomplete="name"
                  placeholder="Nguyễn Văn A"
                />
              </div>
              <div class="form-group">
                <label for="contact-email">Email <span class="req">*</span></label>
                <input
                  id="contact-email"
                  v-model="form.email"
                  type="email"
                  required
                  autocomplete="email"
                  placeholder="ban@email.com"
                />
              </div>
            </div>
            <div class="form-group">
              <label for="contact-phone">Số điện thoại</label>
              <input
                id="contact-phone"
                v-model="form.phone"
                type="tel"
                autocomplete="tel"
                placeholder="09xx xxx xxx (tuỳ chọn)"
              />
            </div>
            <div class="form-group">
              <label for="contact-message">Nội dung <span class="req">*</span></label>
              <textarea
                id="contact-message"
                v-model="form.message"
                rows="5"
                required
                placeholder="Ví dụ: Tôi cần tư vấn size giày chạy bộ, đơn hàng #123..."
              />
            </div>

            <div v-if="sent" class="success-banner" role="status">
              <span class="success-icon" aria-hidden="true">✓</span>
              <div>
                <strong>Đã ghi nhận tin nhắn (demo)</strong>
                <p>Trong phiên bản thật, nội dung sẽ được gửi tới email hỗ trợ. Cảm ơn bạn đã liên hệ Kesn Store.</p>
              </div>
            </div>

            <div class="form-actions">
              <button type="submit" class="btn-send" :disabled="sent">
                {{ sent ? 'Đã gửi' : 'Gửi tin nhắn' }}
              </button>
              <router-link to="/faq" class="btn-ghost">Xem FAQ</router-link>
            </div>
          </form>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useAuthStore } from '../authStore'

const auth = useAuthStore()
const form = reactive({
  fullName: auth.state.user?.fullName || '',
  email: auth.state.user?.email || '',
  phone: auth.state.user?.phone || '',
  message: '',
})
const sent = ref(false)

function submitForm() {
  sent.value = true
}
</script>

<style src="../assets/css/contact.css" scoped></style>

