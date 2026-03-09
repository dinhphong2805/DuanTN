<template>
  <div class="login-wrapper">
    <div class="login-card">
      <h1 class="login-title">Đăng nhập</h1>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">Nhập gmail *</label>
          <input
            id="email"
            type="email"
            v-model="email"
            placeholder="Nhập gmail *"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">Nhập mật khẩu: *</label>
          <div class="password-wrapper">
            <input
              id="password"
              :type="showPassword ? 'text' : 'password'"
              v-model="password"
              placeholder="Nhập mật khẩu *"
              required
            />
            <button
              type="button"
              class="toggle-password"
              @click="togglePassword"
            >
              <span class="eye-icon">
                <span
                  v-if="!showPassword"
                  class="eye-pupil"
                ></span>
              </span>
            </button>
          </div>
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <div class="form-options">
          <label class="remember-label">
            <input type="checkbox" v-model="remember" />
            <span>Nhớ mật khẩu</span>
          </label>

          <a
            href="#"
            class="link-inline"
            @click.prevent="$router.push('/forgot-password')"
          >
            Quên mật khẩu
          </a>
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đăng nhập' }}
        </button>

        <p class="register">
          Chưa có tài khoản?
          <a
            href="#"
            class="link-inline"
            @click.prevent="$router.push('/register')"
          >
            Đăng ký
          </a>
        </p>
      </form>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../authStore'
import { login } from '../api/services/authService'

export default {
  name: "DangNhap",
  data() {
    return {
      email: "",
      password: "",
      remember: false,
      showPassword: false,
      loading: false,
      error: null,
    };
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword;
    },
    async handleLogin() {
      this.error = null
      this.loading = true
      try {
        const { user, token } = await login(this.email, this.password)
        useAuthStore().login(user, token)
        const redirect = this.$route.query.redirect || '/'
        this.$router.push(redirect)
      } catch (e) {
        this.error = e.response?.data?.message || e.message || 'Đăng nhập thất bại'
      } finally {
        this.loading = false
      }
    },
  },
};
</script>

<style scoped>
.login-wrapper {
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

.login-card {
  width: 100%;
  max-width: 520px;
  background-color: #ffffff;
  border-radius: 16px;
  padding: 40px 36px;
  box-sizing: border-box;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.14);
}

.login-title {
  margin: 0 0 40px;
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.login-form {
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

.error-msg {
  color: #b91c1c;
  font-size: 14px;
  margin: 0 0 12px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  margin-bottom: 28px;
}

.remember-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-options input[type='checkbox'] {
  width: 14px;
  height: 14px;
}

.link-inline {
  color: #000000;
  text-decoration: none;
}

.link-inline:hover {
  text-decoration: underline;
}

.btn-login {
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

.btn-login:hover {
  background-color: #020617;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.32);
  transform: translateY(-1px);
}

.register {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}

.register .link-inline {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-card {
    padding: 40px 24px;
  }

  .login-title {
    font-size: 24px;
  }

  .login-form {
    max-width: 100%;
  }
}
</style>

