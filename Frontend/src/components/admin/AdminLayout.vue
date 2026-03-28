<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-brand">Kesn Admin</div>
      <nav class="admin-nav" aria-label="Menu quản trị">
        <router-link to="/admin" class="nav-link" active-class="active">
          <span class="nav-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />
            </svg>
          </span>
          <span>Thống kê</span>
        </router-link>
        <router-link to="/admin/products" class="nav-link" active-class="active">
          <span class="nav-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
          </span>
          <span>Sản phẩm</span>
        </router-link>
        <router-link to="/admin/orders" class="nav-link" active-class="active">
          <span class="nav-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </span>
          <span>Đơn hàng</span>
        </router-link>
        <router-link to="/admin/users" class="nav-link" active-class="active">
          <span class="nav-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
            </svg>
          </span>
          <span>Người dùng</span>
        </router-link>
        <router-link to="/admin/vouchers" class="nav-link" active-class="active">
          <span class="nav-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16.5 6v.75m0 3v.75m0 3v.75m0 3V18m-9-5.25h5.25M1.5 6h15m-15 0A2.25 2.25 0 003.75 3h16.5A2.25 2.25 0 0021 6v12a2.25 2.25 0 01-2.25 2.25H3.75A2.25 2.25 0 011.5 18V6z" />
            </svg>
          </span>
          <span>Mã giảm giá</span>
        </router-link>
      </nav>
      <div class="admin-footer">
        <router-link to="/" class="link link--home">
          <span class="nav-ico nav-ico--sm" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
          </span>
          <span>Về trang chủ</span>
        </router-link>
        <button type="button" class="btn-logout" @click="logout">
          <span class="nav-ico nav-ico--sm" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </span>
          <span>Đăng xuất</span>
        </button>
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
  width: 260px;
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
  padding: 8px 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.15s ease, background 0.15s ease;
}

.nav-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.04);
}

.nav-link.active {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-link.active .nav-ico {
  color: #93c5fd;
}

.nav-ico {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  color: inherit;
}

.nav-ico svg {
  width: 22px;
  height: 22px;
}

.nav-ico--sm {
  width: 18px;
  height: 18px;
}

.nav-ico--sm svg {
  width: 18px;
  height: 18px;
}

.admin-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-footer .link {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.15s ease;
}

.admin-footer .link:hover {
  color: #fff;
}

.btn-logout {
  margin-top: 12px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 14px;
  background: transparent;
  border: 1px solid #6b7280;
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: #9ca3af;
}

.admin-main {
  flex: 1;
  padding: 24px;
  background: #f3f4f6;
  overflow: auto;
}
</style>
