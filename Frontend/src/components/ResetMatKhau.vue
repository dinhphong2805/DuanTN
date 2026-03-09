<template>
  <div class="reset-wrapper">
    <div class="reset-card">
      <h1 class="reset-title">Đặt lại mật khẩu</h1>

      <form class="reset-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            type="email"
            v-model="email"
            placeholder="Email"
            required
            :readonly="!!$route.query.email"
          />
        </div>
        <div class="form-group">
          <label for="code">Mã xác nhận (6 số) *</label>
          <input
            id="code"
            v-model="code"
            placeholder="Nhập mã đã gửi"
            required
            maxlength="6"
          />
        </div>
        <div class="form-group">
          <label for="newPassword">Mật khẩu mới *</label>
          <input
            id="newPassword"
            :type="showPassword ? 'text' : 'password'"
            v-model="newPassword"
            placeholder="Mật khẩu mới"
            required
          />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>
        <p v-if="success" class="success-msg">{{ successMsg }}</p>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu' }}
        </button>
      </form>

      <p class="footer-text">
        <a href="#" class="link-inline" @click.prevent="$router.push('/login')">← Đăng nhập</a>
      </p>
    </div>
  </div>
</template>

<script>
import { resetPassword } from '../api/services/authService'

export default {
  name: "ResetMatKhau",
  data() {
    return {
      email: this.$route.query.email || "",
      code: "",
      newPassword: "",
      showPassword: false,
      loading: false,
      error: null,
      success: false,
      successMsg: "",
    };
  },
  watch: {
    "$route.query.email"(v) {
      if (v) this.email = v;
    },
  },
  methods: {
    async handleSubmit() {
      this.error = null;
      this.loading = true;
      try {
        const res = await resetPassword(this.email, this.code, this.newPassword);
        this.success = true;
        this.successMsg = res.message || "Đã đặt lại mật khẩu. Bạn có thể đăng nhập.";
        setTimeout(() => this.$router.push("/login"), 2000);
      } catch (e) {
        this.error = e.response?.data?.message || e.message || "Có lỗi xảy ra";
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.reset-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
}

.reset-card {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.14);
}

.reset-title {
  margin: 0 0 28px;
  text-align: center;
  font-size: 24px;
  font-weight: 700;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
}

.form-group input:read-only {
  background: #f3f4f6;
}

.error-msg { color: #b91c1c; font-size: 14px; margin-bottom: 12px; }
.success-msg { color: #059669; font-size: 14px; margin-bottom: 12px; }

.btn-primary {
  width: 100%;
  padding: 14px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}

.link-to-reset { margin-top: 12px; }
.footer-text { text-align: center; margin-top: 20px; }
.link-inline { color: #111827; text-decoration: underline; }
</style>
