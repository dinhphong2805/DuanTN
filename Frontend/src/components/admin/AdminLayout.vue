<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-brand">Kesn Admin</div>
      <nav class="admin-nav">
        <router-link to="/admin" class="nav-link" active-class="active">Thống kê</router-link>
        <router-link to="/admin/products" class="nav-link" active-class="active">Sản phẩm</router-link>
        <router-link to="/admin/orders" class="nav-link" active-class="active">Đơn hàng</router-link>
        <router-link to="/admin/users" class="nav-link" active-class="active">Người dùng</router-link>
        <router-link to="/admin/vouchers" class="nav-link" active-class="active">Mã giảm giá</router-link>
      </nav>
      <div class="admin-footer">
        <router-link to="/" class="link">← Về trang chủ</router-link>
        <button type="button" class="btn-logout" @click="logout">Đăng xuất</button>
      </div>
    </aside>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../authStore'

const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 240px;
  background: #111827;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.admin-brand {
  padding: 20px;
  font-size: 18px;
  font-weight: 800;
}

.admin-nav {
  flex: 1;
  padding: 16px 0;
}

.nav-link {
  display: block;
  padding: 12px 20px;
  color: #9ca3af;
  text-decoration: none;
}

.nav-link:hover {
  color: #fff;
}

.nav-link.active {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.admin-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.admin-footer .link {
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
}

.admin-footer .link:hover {
  color: #fff;
}

.btn-logout {
  margin-top: 12px;
  padding: 8px 14px;
  background: transparent;
  border: 1px solid #6b7280;
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-logout:hover {
  background: rgba(255,255,255,0.1);
}

.admin-main {
  flex: 1;
  padding: 24px;
  background: #f3f4f6;
  overflow: auto;
}
</style>
