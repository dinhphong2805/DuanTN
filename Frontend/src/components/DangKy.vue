<template>
  <div class="register-wrapper">
    <div class="register-card">
      <h1 class="register-title">Đăng Ký</h1>

      <form class="register-form" @submit.prevent="handleRegister">
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
              @click="toggleField('showPassword')"
            >
              <span class="eye-icon">
                <span v-if="!showPassword" class="eye-pupil"></span>
              </span>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Xác nhận mật khẩu: *</label>
          <div class="password-wrapper">
            <input
              id="confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              v-model="confirmPassword"
              placeholder="Xác nhận mật khẩu *"
              required
            />
            <button
              type="button"
              class="toggle-password"
              @click="toggleField('showConfirmPassword')"
            >
              <span class="eye-icon">
                <span v-if="!showConfirmPassword" class="eye-pupil"></span>
              </span>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="fullName">Họ và tên *</label>
          <input
            id="fullName"
            type="text"
            v-model="fullName"
            placeholder="Họ và tên *"
            required
          />
        </div>

        <div class="form-group">
          <label for="phone">Nhập SDT: *</label>
          <input
            id="phone"
            type="tel"
            v-model="phone"
            placeholder="Nhập số điện thoại *"
            required
          />
        </div>

        <div class="form-group">
          <label for="security">Hãy ghi gì đó để làm câu hỏi bảo mật: *</label>
          <div class="password-wrapper">
            <input
              id="security"
              :type="showSecurity ? 'text' : 'password'"
              v-model="securityAnswer"
              placeholder="Câu trả lời bảo mật *"
              required
            />
            <button
              type="button"
              class="toggle-password"
              @click="toggleField('showSecurity')"
            >
              <span class="eye-icon">
                <span v-if="!showSecurity" class="eye-pupil"></span>
              </span>
            </button>
          </div>
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đăng ký' }}
        </button>

        <p class="footer-text">
          Đã có tài khoản?
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
import { register } from '../api/services/authService'

export default {
  name: "DangKy",
  data() {
    return {
      email: "",
      fullName: "",
      password: "",
      confirmPassword: "",
      phone: "",
      securityAnswer: "",
      showPassword: false,
      showConfirmPassword: false,
      showSecurity: false,
      loading: false,
      error: null,
    };
  },
  methods: {
    toggleField(field) {
      this[field] = !this[field];
    },
    async handleRegister() {
      if (this.password !== this.confirmPassword) {
        this.error = "Mật khẩu và xác nhận mật khẩu không trùng khớp.";
        return;
      }
      this.error = null
      this.loading = true
      try {
        await register({
          email: this.email,
          fullName: this.fullName,
          password: this.password,
          phone: this.phone,
        })
        this.$router.push({
          path: '/login',
          query: { registered: '1', email: this.email },
        })
      } catch (e) {
        this.error = e.response?.data?.message || e.message || 'Đăng ký thất bại'
      } finally {
        this.loading = false
      }
    },
  },
};
</script>

<style scoped>
.register-wrapper {
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

.register-card {
  width: 100%;
  max-width: 520px;
  background-color: #ffffff;
  border-radius: 16px;
  padding: 40px 36px;
  box-sizing: border-box;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.14);
}

.register-title {
  margin: 0 0 40px;
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.register-form {
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

.error-msg {
  color: #b91c1c;
  font-size: 14px;
  margin: 0 0 12px;
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
  .register-card {
    padding: 32px 20px;
  }

  .register-title {
    font-size: 24px;
  }
}
</style>

