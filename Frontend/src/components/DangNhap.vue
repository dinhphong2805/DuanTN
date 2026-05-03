<template>
  <div class="login-page">
    <div class="bg-text-wrapper">
      <div class="bg-text">KESN STORE KESN STORE</div>
      <div class="bg-text reverse">KESN STORE KESN STORE</div>
    </div>

    <div class="login-card">
      <div class="glass-box">
        <h1 class="brand-name">KESN STORE</h1>
        <p class="brand-tagline">SNEAKERS COLLECTION</p>

        <p v-if="registerSuccess" class="alert alert-success small mb-3 rounded-3 py-2 px-3">
          Đăng ký thành công. Vui lòng đăng nhập.
        </p>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="auth-field">
            <label class="auth-label" for="login-email">Email</label>
            <div class="input-field">
              <i class="bi bi-envelope input-field__icon" aria-hidden="true"></i>
              <input
                id="login-email"
                type="email"
                v-model="email"
                class="form-control input-field__control"
                placeholder="Nhập email của bạn"
                autocomplete="username"
                required
              />
            </div>
          </div>

          <div class="auth-field">
            <label class="auth-label" for="login-password">Mật khẩu</label>
            <div class="input-field">
              <i class="bi bi-lock input-field__icon" aria-hidden="true"></i>
              <input
                id="login-password"
                :type="showPassword ? 'text' : 'password'"
                v-model="password"
                class="form-control input-field__control"
                placeholder="••••••••"
                autocomplete="current-password"
                required
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="input-field__toggle"
                :aria-pressed="showPassword"
                aria-label="Hiện hoặc ẩn mật khẩu"
              >
                <i class="bi" :class="showPassword ? 'bi-eye-slash' : 'bi-eye'"></i>
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="checkbox-container">
              <input type="checkbox" v-model="remember" />
              <span>Ghi nhớ tôi</span>
            </label>
            <a href="#" @click.prevent="$router.push('/forgot-password')" class="link">Quên mật khẩu?</a>
          </div>

          <button type="submit" class="btn btn-dark w-100 py-3 rounded-3 fw-bold shadow-sm" :disabled="loading">
            <span v-if="!loading">ĐĂNG NHẬP</span>
            <div v-else class="spinner-border spinner-border-sm" role="status"></div>
          </button>

          <div class="hr-text py-3 text-secondary small fw-bold"><span>HOẶC</span></div>

          <button type="button" class="btn btn-outline-secondary w-100 py-2 rounded-3 d-flex align-items-center justify-content-center gap-2" @click="handleGoogleLogin">
            <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="G" style="width: 18px" />
            <span>Đăng nhập với Google</span>
          </button>
        </form>

        <p class="register-hint">
          Thành viên mới? <a href="#" @click.prevent="$router.push('/register')">Đăng ký ngay</a>
        </p>
      </div>
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
      registerSuccess: false,
    };
  },
  mounted() {
    if (this.$route.query.registered === '1') {
      this.registerSuccess = true
      const q = this.$route.query.email
      if (typeof q === 'string' && q) this.email = q
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true;
      try {
        const { user, token } = await login(this.email, this.password);
        useAuthStore().login(user, token);
        this.$router.push('/');
      } catch (e) {
        alert(e.response?.data?.message || 'Email hoặc mật khẩu không đúng!');
      } finally {
        this.loading = false;
      }
    },
    handleGoogleLogin() {
      window.location.href = "http://localhost:8080/oauth2/authorization/google";
    }
  }
};
</script>

<style scoped>
/* IMPORT FONT Ở ĐÂY ĐỂ TRÁNH LỖI VITE */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800;900&display=swap');

.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f4f4;
  overflow: hidden;
  font-family: 'Inter', sans-serif !important;
}

/* CHỮ NỀN CHẠY */
.bg-text-wrapper {
  position: absolute;
  width: 100%;
  opacity: 0.04; 
  user-select: none;
  z-index: 1;
  top: 50%;
  transform: translateY(-50%);
}
.bg-text {
  font-size: 10rem;
  font-weight: 900;
  white-space: nowrap;
  animation: scrollLeft 60s linear infinite;
  color: #000;
  letter-spacing: -2px;
}
.bg-text.reverse {
  animation: scrollRight 60s linear infinite;
}
@keyframes scrollLeft { from { transform: translateX(0); } to { transform: translateX(-50%); } }
@keyframes scrollRight { from { transform: translateX(-50%); } to { transform: translateX(0); } }

/* CARD ĐĂNG NHẬP */
.login-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}
.glass-box {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 28px;
  padding: 40px 35px;
  box-shadow: 0 30px 60px rgba(0,0,0,0.12);
  text-align: center;
}

.brand-name { 
  font-size: 2.8rem; 
  font-weight: 900; 
  margin-bottom: 5px; 
  color: #000; 
  letter-spacing: -2px;
}
.brand-tagline { 
  font-size: 0.7rem; 
  letter-spacing: 5px; 
  color: #bbb; 
  margin-bottom: 40px; 
  text-transform: uppercase;
}

/* INPUTS — dùng .auth-field thay vì .input-group (tránh xung đột Bootstrap flex) */
.auth-form {
  text-align: left;
}

.auth-field {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
  margin-bottom: 1.25rem;
  text-align: left;
}

.auth-label {
  display: block;
  width: 100%;
  margin-bottom: 0.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.input-field {
  position: relative;
  width: 100%;
  display: block;
}

.input-field__icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  color: #9ca3af;
  font-size: 1rem;
  pointer-events: none;
}

.input-field__control {
  width: 100%;
  padding: 0.75rem 2.75rem 0.75rem 2.65rem;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #f9fafb;
  font-family: 'Inter', sans-serif;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s ease, background 0.2s ease;
  box-sizing: border-box;
}

.input-field__control:focus {
  border-color: #111827;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(17, 24, 39, 0.08);
}

.input-field__toggle {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  padding: 6px;
  border: none;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  line-height: 1;
}

.input-field__toggle:hover {
  color: #374151;
}

/* OPTIONS */
.form-options { display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 30px; }
.checkbox-container { display: flex; align-items: center; gap: 8px; cursor: pointer; color: #888;}
.link { color: #000; text-decoration: none; font-weight: 700; }

/* NÚT SUBMIT - ĐÃ ÉP FONT INTER */
.btn-submit {
  width: 100%;
  padding: 16px;
  background: #000;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-family: 'Inter', sans-serif !important;
  font-weight: 800;
  font-size: 0.95rem;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.3s;
}
.btn-submit:hover { background: #333; transform: translateY(-2px); }

.hr-text { margin: 25px 0; display: flex; align-items: center; color: #eee; font-size: 0.7rem; font-weight: 800; }
.hr-text::before, .hr-text::after { content: ""; flex: 1; height: 1px; background: #eee; }
.hr-text span { padding: 0 15px; color: #bbb; }

/* GOOGLE */
.btn-google {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 14px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
}
.btn-google img { width: 20px; height: 20px; } 

.register-hint { margin-top: 35px; font-size: 0.9rem; color: #aaa; }
.register-hint a { color: #000; text-decoration: none; font-weight: 700; }

.loader {
  width: 20px; height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>