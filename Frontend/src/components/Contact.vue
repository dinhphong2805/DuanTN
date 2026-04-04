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

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800&display=swap');

.contact-page {
  --cp-ink: #0a0a0a;
  --cp-muted: #6b7280;
  --cp-line: rgba(0, 0, 0, 0.08);
  --cp-surface: #fafafa;

  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  color: var(--cp-ink);
  background: var(--cp-surface);
}

.contact-promo {
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

.contact-promo-inner {
  max-width: 1040px;
  margin: 0 auto;
}

.contact-hero {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: linear-gradient(165deg, #111827 0%, #0a0a0a 48%, #0a0a0a 100%);
  color: #fff;
  padding: 36px 24px 40px;
  margin-bottom: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.contact-hero-inner {
  max-width: 1040px;
  margin: 0 auto;
}

.contact-breadcrumb {
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.04em;
  color: rgba(255, 255, 255, 0.45);
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.contact-breadcrumb-link {
  padding: 0;
  border: none;
  background: none;
  font: inherit;
  color: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: color 0.2s ease;
}

.contact-breadcrumb-link:hover {
  color: #fff;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.contact-breadcrumb-current {
  color: rgba(255, 255, 255, 0.92);
  font-weight: 600;
}

.contact-eyebrow {
  margin: 0 0 4px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
}

.contact-title {
  margin: 0;
  font-size: clamp(28px, 3.2vw, 40px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.12;
}

.contact-subtitle {
  margin: 10px 0 0;
  max-width: 560px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.55;
  color: rgba(255, 255, 255, 0.55);
}

.contact-shell {
  max-width: 1040px;
  margin: 0 auto;
  padding: 32px 16px 72px;
}

.contact-layout {
  display: grid;
  grid-template-columns: minmax(0, 340px) minmax(0, 1fr);
  gap: 28px;
  align-items: start;
}

.contact-info,
.contact-form-block {
  border: 1px solid var(--cp-line);
  border-radius: 18px;
  padding: 24px 22px 26px;
  background: #fff;
  box-shadow: 0 8px 32px rgba(15, 23, 42, 0.06);
}

.contact-info {
  position: sticky;
  top: 96px;
}

.contact-card-head {
  margin-bottom: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.contact-card-title {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.contact-card-desc {
  margin: 0;
  font-size: 13px;
  color: var(--cp-muted);
  line-height: 1.5;
}

.info-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.info-row {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.info-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #f4f4f5;
  color: #111827;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-icon svg {
  width: 20px;
  height: 20px;
}

.info-body {
  min-width: 0;
}

.info-label {
  display: block;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #9ca3af;
  margin-bottom: 4px;
}

.info-value {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
}

.info-link {
  font-size: 14px;
  font-weight: 600;
  color: var(--cp-ink);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s, color 0.2s;
}

.info-link:hover {
  border-bottom-color: currentColor;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #374151;
}

.req {
  color: #dc2626;
  font-weight: 800;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  color: var(--cp-ink);
  background: #fafafa;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #9ca3af;
}

.form-group input:hover,
.form-group textarea:hover {
  border-color: #d1d5db;
  background: #fff;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--cp-ink);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.06);
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

.success-banner {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 14px 16px;
  margin-bottom: 16px;
  border-radius: 14px;
  border: 1px solid rgba(5, 150, 105, 0.25);
  background: linear-gradient(135deg, #ecfdf5, #f0fdf4);
  color: #065f46;
}

.success-banner strong {
  display: block;
  font-size: 14px;
  margin-bottom: 4px;
}

.success-banner p {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  opacity: 0.95;
}

.success-icon {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: #059669;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 800;
}

.form-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.btn-send {
  padding: 14px 28px;
  background: #0a0a0a;
  color: #fff;
  border: none;
  border-radius: 999px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}

.btn-send:hover:not(:disabled) {
  background: #111827;
}

.btn-send:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-send:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-ghost {
  padding: 12px 18px;
  font-size: 13px;
  font-weight: 600;
  color: var(--cp-muted);
  text-decoration: none;
  border-radius: 999px;
  border: 1px solid var(--cp-line);
  transition: color 0.2s, border-color 0.2s, background 0.2s;
}

.btn-ghost:hover {
  color: var(--cp-ink);
  border-color: #d1d5db;
  background: #fafafa;
}

@media (max-width: 900px) {
  .contact-layout {
    grid-template-columns: 1fr;
  }

  .contact-info {
    position: static;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .contact-hero {
    padding: 28px 16px 32px;
  }

  .contact-shell {
    padding: 24px 16px 56px;
  }
}
</style>
