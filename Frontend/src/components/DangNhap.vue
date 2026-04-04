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

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="input-group">
            <label>Email</label>
            <div class="input-field">
              <span class="icon">✉</span>
              <input type="email" v-model="email" placeholder="Nhập email của bạn" required />
            </div>
          </div>

          <div class="input-group">
            <label>Mật khẩu</label>
            <div class="input-field">
              <span class="icon">🔒</span>
              <input :type="showPassword ? 'text' : 'password'" v-model="password" placeholder="••••••••" required />
              <button type="button" @click="showPassword = !showPassword" class="toggle-password">
                {{ showPassword ? '👁️' : '👁️‍🗨️' }}
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

          <button type="submit" class="btn-submit" :disabled="loading">
            <span v-if="!loading">ĐĂNG NHẬP</span>
            <div v-else class="loader"></div>
          </button>

          <div class="hr-text"><span>HOẶC</span></div>

          <button type="button" class="btn-google" @click="handleGoogleLogin">
            <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="G" />
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
    };
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
  opacity: 0.07; 
  user-select: none;
  z-index: 1;
}
.bg-text {
  font-size: 15rem;
  font-weight: 900;
  white-space: nowrap;
  animation: scrollLeft 35s linear infinite;
  color: #000;
  letter-spacing: -5px;
}
.bg-text.reverse {
  animation: scrollRight 35s linear infinite;
}
@keyframes scrollLeft { from { transform: translateX(0); } to { transform: translateX(-50%); } }
@keyframes scrollRight { from { transform: translateX(-50%); } to { transform: translateX(0); } }

/* CARD ĐĂNG NHẬP */
.login-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}
.glass-box {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 24px;
  padding: 50px 40px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.06);
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

/* INPUTS */
.input-group { text-align: left; margin-bottom: 22px; }
.input-group label { 
  display: block; 
  font-size: 0.75rem; 
  font-weight: 700; 
  margin-bottom: 8px; 
  color: #666; 
  text-transform: uppercase;
}

.input-field {
  position: relative;
  display: flex;
  align-items: center;
}
.input-field .icon {
  position: absolute;
  left: 15px;
  color: #ccc;
  font-size: 1rem;
}
.input-field input {
  width: 100%;
  padding: 14px 15px 14px 45px;
  border: 1px solid #eee;
  border-radius: 12px;
  background: #fcfcfc;
  font-family: 'Inter', sans-serif;
  font-size: 0.95rem;
  outline: none;
  transition: 0.3s;
}
.input-field input:focus { border-color: #000; background: #fff; }

.toggle-password { position: absolute; right: 12px; background: none; border: none; cursor: pointer; color: #bbb;}

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