<template>
  <div class="forgot-wrapper">
    <div class="forgot-card">
      <h1 class="forgot-title">Quên mật khẩu</h1>

      <form class="forgot-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="email">Nhập email *</label>
          <input
            id="email"
            type="email"
            v-model="email"
            placeholder="Nhập email đã đăng ký *"
            required
          />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>
        <p v-if="success" class="success-msg">{{ successMsg }}</p>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Gửi mã xác nhận' }}
        </button>

        <p v-if="success" class="link-to-reset">
          <a href="#" class="link-inline" @click.prevent="$router.push('/reset-password?email=' + encodeURIComponent(email))">
            Nhập mã và đặt lại mật khẩu →
          </a>
        </p>

        <p class="footer-text">
          <a
            href="#"
            class="link-inline"
            @click.prevent="$router.push('/login')"
          >
            Đăng nhập
          </a>
        </p>
      </form>
    </div>
  </div>
</template>

<script>
import { forgotPassword } from '../api/services/authService'

export default {
  name: "QuenMatKhau",
  data() {
    return {
      email: "",
      loading: false,
      error: null,
      success: false,
      successMsg: "",
    };
  },
  methods: {
    async handleSubmit() {
      this.error = null
      this.loading = true
      try {
        const res = await forgotPassword(this.email)
        this.success = true
        this.successMsg = res.message || "Mã xác nhận đã gửi. Kiểm tra console backend (hoặc email khi có cấu hình)."
      } catch (e) {
        this.error = e.response?.data?.message || e.message || 'Có lỗi xảy ra'
      } finally {
        this.loading = false
      }
    },
  },
};
</script>

<style scoped>
.forgot-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
}

.forgot-card {
  width: 100%;
  max-width: 520px;
  background-color: #ffffff;
  border-radius: 16px;
  padding: 40px 36px;
  box-sizing: border-box;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.14);
}

.forgot-title {
  margin: 0 0 40px;
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.forgot-form {
  max-width: 520px;
  margin: 0 auto;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 15px;
}

.error-msg {
  color: #b91c1c;
  font-size: 14px;
  margin: 0 0 12px;
}

.success-msg {
  color: #059669;
  font-size: 14px;
  margin: 0 0 12px;
}

.link-to-reset {
  margin-top: 12px;
}

.form-group label {
  color: #555555;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background-color: #f9fafb;
  font-size: 15px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.18);
}

.password-wrapper {
  display: flex;
  align-items: center;
  background-color: #f9fafb;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding-right: 10px;
}

.password-wrapper input {
  border-radius: 12px;
  border: none;
  background-color: transparent;
  padding-right: 8px;
}

.password-wrapper input:focus {
  outline: none;
}

.toggle-password {
  border: none;
  background: none;
  padding: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.eye-icon {
  position: relative;
  width: 28px;
  height: 18px;
  border-radius: 999px;
  border: 2px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.eye-pupil {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #000;
}

.btn-primary {
  width: 100%;
  padding: 14px 18px;
  margin-top: 8px;
  background-color: #111827;
  color: #ffffff;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 700;
  transition: background-color 0.15s ease, box-shadow 0.15s ease,
    transform 0.08s ease;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.25);
}

.btn-primary:hover {
  background-color: #020617;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.32);
  transform: translateY(-1px);
}

.footer-text {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}

.link-inline {
  color: #000000;
  text-decoration: underline;
}

.link-inline:hover {
  text-decoration: none;
}

@media (max-width: 768px) {
  .forgot-card {
    padding: 32px 20px;
  }

  .forgot-title {
    font-size: 26px;
  }
}
</style>

