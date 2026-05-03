<template>
  <div class="al-root" :class="{ 'al--collapsed': collapsed, 'al--mobile-open': mobileOpen }">

    <!-- ===================== SIDEBAR ===================== -->
    <aside class="al-sidebar">

      <!-- Brand + Toggle -->
      <div class="al-brand">
        <div class="al-brand__logo">
          <svg viewBox="0 0 34 34" fill="none" width="34" height="34">
            <rect width="34" height="34" rx="10" fill="#6366f1"/>
            <path d="M9 24 L17 11 L25 24" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 19.5 H22" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="al-brand__name">Kesn <em>Admin</em></span>
        <button class="al-collapse-btn" @click="collapsed = !collapsed" :title="collapsed ? 'Mo rong sidebar' : 'Thu gon sidebar'" type="button">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16">
            <path v-if="!collapsed" d="M11 19l-7-7 7-7"/>
            <path v-if="!collapsed" d="M18 19l-7-7 7-7"/>
            <path v-if="collapsed"  d="M13 5l7 7-7 7"/>
            <path v-if="collapsed"  d="M6 5l7 7-7 7"/>
          </svg>
        </button>
      </div>

      <!-- Nav -->
      <nav class="al-nav" aria-label="Menu quan tri">

        <div class="al-nav-section">
          <span class="al-nav-section__label">Tổng quan</span>
          <router-link to="/admin" class="al-nav-link" active-class="al-nav-link--active" exact>
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/>
                <rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Thống kê</span>
          </router-link>
        </div>

        <div class="al-nav-section">
          <span class="al-nav-section__label">Hàng hóa</span>
          <router-link to="/admin/products" class="al-nav-link" active-class="al-nav-link--active">
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Sản phẩm</span>
          </router-link>
          <router-link to="/admin/categories" class="al-nav-link" active-class="al-nav-link--active">
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Danh mục</span>
          </router-link>
          <router-link to="/admin/imports" class="al-nav-link" active-class="al-nav-link--active">
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 7.5h18M5.25 7.5v10.125A2.25 2.25 0 007.5 19.875h9a2.25 2.25 0 002.25-2.25V7.5M9.75 12l2.25 2.25L14.25 12M12 5.25v9"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Nhập hàng</span>
          </router-link>
        </div>

        <div class="al-nav-section">
          <span class="al-nav-section__label">Kinh doanh</span>
          <router-link to="/admin/orders" class="al-nav-link" active-class="al-nav-link--active">
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Đơn hàng</span>
            <span v-if="hasNewOrders" class="al-nav-link__dot" aria-label="Co don hang moi"></span>
          </router-link>
          <router-link to="/admin/vouchers" class="al-nav-link" active-class="al-nav-link--active">
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M16.5 6v.75m0 3v.75m0 3v.75m0 3V18m-9-5.25h5.25M7.5 15h3M3.75 19.5h16.5a2.25 2.25 0 002.25-2.25v-6a2.25 2.25 0 00-2.25-2.25H3.75a2.25 2.25 0 00-2.25 2.25v6a2.25 2.25 0 002.25 2.25z"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Mã giảm giá</span>
          </router-link>
          <router-link to="/admin/users" class="al-nav-link" active-class="al-nav-link--active">
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Người dùng</span>
          </router-link>
        </div>

        <div class="al-nav-section">
          <span class="al-nav-section__label">Cấu hình</span>
          <router-link to="/admin/banners" class="al-nav-link" active-class="al-nav-link--active">
            <span class="al-nav-link__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
            </span>
            <span class="al-nav-link__label">Banner</span>
          </router-link>
        </div>

      </nav>

      <!-- Sidebar Footer -->
      <div class="al-sidebar-foot">
        <router-link to="/" class="al-foot-link">
          <span class="al-nav-link__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"/>
            </svg>
          </span>
          <span class="al-nav-link__label">Trang chủ</span>
        </router-link>
        <button type="button" class="al-logout-btn" @click="logout">
          <span class="al-nav-link__icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75"/>
            </svg>
          </span>
          <span class="al-nav-link__label">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <!-- Mobile backdrop -->
    <div class="al-backdrop" @click="mobileOpen = false" aria-hidden="true"></div>

    <!-- ===================== MAIN ===================== -->
    <div class="al-main">

      <!-- TOPBAR -->
      <header class="al-topbar">
        <div class="al-topbar__left">
          <button type="button" class="al-hamburger" @click="mobileOpen = !mobileOpen" aria-label="Mo menu">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" width="20" height="20">
              <path d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
          </button>
          <!-- Breadcrumb -->
          <div class="al-breadcrumb">
            <span class="al-breadcrumb__icon">
              <svg viewBox="0 0 20 20" fill="#94a3b8" width="13" height="13">
                <path fill-rule="evenodd" d="M9.293 2.293a1 1 0 011.414 0l7 7A1 1 0 0117 11h-1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-3a1 1 0 00-1-1H9a1 1 0 00-1 1v3a1 1 0 01-1 1H5a1 1 0 01-1-1v-6H3a1 1 0 01-.707-1.707l7-7z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span class="al-breadcrumb__sep">/</span>
            <span class="al-breadcrumb__page">Quan tri vien</span>
          </div>
        </div>

        <div class="al-topbar__right">
          <!-- Notification Bell -->
          <div class="al-notif" ref="notifRef">
            <button type="button" class="al-notif__btn" @click="toggleNotif" :class="{ active: showNotif }" aria-label="Thong bao">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
                <path d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0"/>
              </svg>
              <span v-if="notifCount > 0" class="al-notif__count">{{ notifCount > 9 ? '9+' : notifCount }}</span>
            </button>

            <!-- Dropdown -->
            <transition name="notif-drop">
              <div v-if="showNotif" class="al-notif-panel">
                <div class="al-notif-panel__head">
                  <span>Thong bao</span>
                  <button type="button" class="al-notif-panel__clear" @click="clearNotifs">Danh dau da doc</button>
                </div>
                <div class="al-notif-panel__body">
                  <template v-if="notifications.length">
                    <div
                      v-for="n in notifications"
                      :key="n.id"
                      class="al-notif-item"
                      :class="{ 'al-notif-item--unread': !n.read }"
                      @click="goToOrders(n)"
                    >
                      <span class="al-notif-item__icon" :class="n.type">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16">
                          <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                        </svg>
                      </span>
                      <div class="al-notif-item__content">
                        <p class="al-notif-item__title">{{ n.title }}</p>
                        <p class="al-notif-item__desc">{{ n.desc }}</p>
                        <span class="al-notif-item__time">{{ n.time }}</span>
                      </div>
                      <span v-if="!n.read" class="al-notif-item__dot"></span>
                    </div>
                  </template>
                  <div v-else class="al-notif-empty">
                    <span>Khong co thong bao moi</span>
                  </div>
                </div>
                <div class="al-notif-panel__foot">
                  <router-link to="/admin/orders" @click="showNotif = false">Xem tat ca don hang</router-link>
                </div>
              </div>
            </transition>
          </div>

          <span class="al-topbar__divider" aria-hidden="true"></span>

          <!-- Profile -->
          <div class="al-profile">
            <div class="al-profile__avatar">A</div>
            <div class="al-profile__info">
              <span class="al-profile__name">Admin</span>
              <span class="al-profile__role">Quan tri vien</span>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <main class="al-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../authStore'
import { getOrders } from '../../api/services/adminService'

const STORAGE_KEY = 'kesn_admin_notif_read'

const router = useRouter()
const auth = useAuthStore()

const collapsed   = ref(false)
const mobileOpen  = ref(false)
const showNotif   = ref(false)
const notifRef    = ref(null)
const notifications = ref([])

// Lay danh sach id da doc tu localStorage
function getReadIds() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]') } catch { return [] }
}

// Luu danh sach id da doc vao localStorage
function saveReadIds(ids) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(ids)) } catch {}
}

function logout() {
  auth.logout()
  router.push('/login')
}

function toggleNotif() {
  showNotif.value = !showNotif.value
}

function goToOrders(n) {
  // Danh dau da doc va luu
  n.read = true
  const readIds = getReadIds()
  if (!readIds.includes(n.id)) readIds.push(n.id)
  saveReadIds(readIds)
  showNotif.value = false
  router.push('/admin/orders')
}

function clearNotifs() {
  notifications.value.forEach(n => (n.read = true))
  // Luu tat ca id hien tai vao localStorage
  saveReadIds(notifications.value.map(n => n.id))
}

// So luong chua doc - chi tinh don hang pending CHUA duoc danh dau da xem
const notifCount = computed(() => notifications.value.filter(n => !n.read).length)

// Chi hien dot vang khi co don hang pending CHUA doc
const hasNewOrders = computed(() =>
  notifications.value.some(n => !n.read && n.type === 'order')
)

function timeAgo(dateStr) {
  const diff = Date.now() - new Date(dateStr).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return 'Vua xong'
  if (m < 60) return `${m} phut truoc`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h} gio truoc`
  return `${Math.floor(h / 24)} ngay truoc`
}

async function loadNotifications() {
  try {
    const orders = await getOrders()
    const recent = [...orders]
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, 8)

    // Kiem tra xem id nao da duoc doc tu truoc (localStorage)
    const readIds = getReadIds()

    notifications.value = recent.map(o => ({
      id: o.id,
      title: `Don hang moi #${o.id}`,
      desc: `${o.customerName || 'Khach hang'} — ${Number(o.total || 0).toLocaleString('vi-VN')} VND`,
      time: timeAgo(o.createdAt),
      // Da doc neu: khong phai pending HOAC da danh dau trong localStorage
      read: o.status !== 'pending' || readIds.includes(o.id),
      type: o.status === 'cancelled' ? 'danger' : 'order',
    }))
  } catch {
    notifications.value = []
  }
}

// Close notif panel when clicking outside
function onDocumentClick(e) {
  if (notifRef.value && !notifRef.value.contains(e.target)) {
    showNotif.value = false
  }
}

onMounted(() => {
  loadNotifications()
  document.addEventListener('click', onDocumentClick)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', onDocumentClick)
})
</script>

<style scoped>
/* ============================================================
   AdminLayout Scoped — Shell (Sidebar, Topbar, Layout)
   ============================================================ */

.al-root {
  display: flex;
  min-height: 100vh;
  background: #eef0f7;
  font-family: 'Inter', system-ui, sans-serif;
}

/* =========================================================
   SIDEBAR
   ========================================================= */
.al-sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 256px;
  background: #0c1222;
  display: flex;
  flex-direction: column;
  z-index: 500;
  transition: width 0.3s cubic-bezier(0.16,1,0.3,1), transform 0.3s cubic-bezier(0.16,1,0.3,1);
  overflow: hidden;
  border-right: 1px solid rgba(255,255,255,0.04);
  box-shadow: 2px 0 12px rgba(0,0,0,0.15);
}

/* --- Brand --- */
.al-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 14px 16px;
  height: 64px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  flex-shrink: 0;
}
.al-brand__logo { flex-shrink: 0; display: flex; }
.al-brand__name {
  font-size: 1.02rem;
  font-weight: 800;
  color: #e2e8f0;
  letter-spacing: -0.02em;
  white-space: nowrap;
  overflow: hidden;
  flex: 1;
}
.al-brand__name em { color: #818cf8; font-style: normal; }

.al-collapse-btn {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.07);
  color: #6b7cb5;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
  flex-shrink: 0;
}
.al-collapse-btn:hover { background: rgba(255,255,255,0.12); color: #c7d4f0; }

/* --- Nav --- */
.al-nav { flex: 1; overflow-y: auto; overflow-x: hidden; padding: 8px 10px; }

.al-nav-section { margin-bottom: 6px; }
.al-nav-section__label {
  display: block;
  font-size: 0.6rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #2e3d5c;
  padding: 10px 8px 4px;
  white-space: nowrap;
  overflow: hidden;
}

.al-nav-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 11px;
  border-radius: 10px;
  color: #7b8fb8;
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.18s, color 0.18s;
  position: relative;
  margin-bottom: 2px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
}
.al-nav-link:hover { background: rgba(255,255,255,0.05); color: #c7d4f0; }

.al-nav-link--active {
  background: linear-gradient(90deg, rgba(99,102,241,0.18), rgba(99,102,241,0.05));
  color: #a5b4fc;
  font-weight: 700;
}
.al-nav-link--active::before {
  content: '';
  position: absolute;
  left: 0; top: 7px; bottom: 7px;
  width: 3px;
  background: #6366f1;
  border-radius: 0 3px 3px 0;
}

.al-nav-link__icon {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.al-nav-link__icon svg { width: 20px; height: 20px; }
.al-nav-link__label { flex: 1; overflow: hidden; text-overflow: ellipsis; }

.al-nav-link__dot {
  width: 7px; height: 7px;
  background: #f59e0b;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 6px rgba(245,158,11,0.6);
}

/* --- Sidebar Footer --- */
.al-sidebar-foot {
  padding: 10px 10px 14px;
  border-top: 1px solid rgba(255,255,255,0.05);
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex-shrink: 0;
}
.al-foot-link {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 11px; border-radius: 10px;
  color: #7b8fb8; font-size: 0.875rem; font-weight: 500;
  text-decoration: none;
  transition: background 0.18s, color 0.18s;
  white-space: nowrap; overflow: hidden;
}
.al-foot-link:hover { background: rgba(255,255,255,0.05); color: #c7d4f0; }

.al-logout-btn {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 11px; border-radius: 10px;
  color: #7b8fb8; background: transparent; border: none;
  font-size: 0.875rem; font-weight: 500; font-family: 'Inter', sans-serif;
  cursor: pointer; width: 100%; text-align: left;
  transition: background 0.18s, color 0.18s;
  white-space: nowrap; overflow: hidden;
}
.al-logout-btn:hover { background: rgba(239,68,68,0.1); color: #fca5a5; }

/* =========================================================
   COLLAPSED STATE
   ========================================================= */
.al-root.al--collapsed .al-sidebar { width: 68px; }

/* Brand: an ten thuong hieu, can chinh nut toggle giua */
.al-root.al--collapsed .al-brand {
  padding: 14px 10px;
  justify-content: center;
  gap: 0;
}
.al-root.al--collapsed .al-brand__logo { display: none; }
.al-root.al--collapsed .al-brand__name { display: none; }
.al-root.al--collapsed .al-collapse-btn { margin: 0; width: 44px; height: 44px; border-radius: 12px; }

/* Nav: an label, can chinh icon giua */
.al-root.al--collapsed .al-nav-section__label { opacity: 0; height: 0; overflow: hidden; padding: 0; margin: 0; pointer-events: none; }
.al-root.al--collapsed .al-nav-link {
  justify-content: center;
  padding: 12px 0;
}
.al-root.al--collapsed .al-nav-link__label,
.al-root.al--collapsed .al-nav-link__dot { display: none; }
.al-root.al--collapsed .al-nav-link--active::before { display: none; }

/* Footer: an label, can giua */
.al-root.al--collapsed .al-sidebar-foot { padding: 10px 8px 14px; }
.al-root.al--collapsed .al-foot-link {
  justify-content: center;
  padding: 10px 0;
}
.al-root.al--collapsed .al-logout-btn {
  justify-content: center;
  padding: 10px 0;
}
.al-root.al--collapsed .al-foot-link .al-nav-link__label,
.al-root.al--collapsed .al-logout-btn .al-nav-link__label { display: none; }

/* Tooltip khi hover vao item luc collapsed */
.al-root.al--collapsed .al-nav-link {
  position: relative;
}
.al-root.al--collapsed .al-nav-link:hover::after {
  content: attr(data-label);
  position: absolute;
  left: calc(100% + 10px);
  top: 50%;
  transform: translateY(-50%);
  background: #1e293b;
  color: #e2e8f0;
  font-size: 0.78rem;
  font-weight: 600;
  padding: 6px 11px;
  border-radius: 8px;
  white-space: nowrap;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
  z-index: 600;
  pointer-events: none;
}

/* =========================================================
   MAIN WRAPPER
   ========================================================= */
.al-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-left: 256px;
  transition: margin-left 0.3s cubic-bezier(0.16,1,0.3,1);
  min-height: 100vh;
  overflow-x: hidden;
}
.al-root.al--collapsed .al-main { margin-left: 68px; }

/* =========================================================
   TOPBAR
   ========================================================= */
.al-topbar {
  position: sticky;
  top: 0;
  z-index: 200;
  height: 64px;
  background: rgba(255,255,255,0.94);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid #dde3f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(15,23,42,0.04);
  flex-shrink: 0;
}

.al-topbar__left { display: flex; align-items: center; gap: 13px; }
.al-hamburger {
  display: none;
  width: 36px; height: 36px;
  border-radius: 9px; background: #f1f5f9; border: none;
  color: #475569; cursor: pointer; align-items: center; justify-content: center;
  transition: background 0.15s;
}
.al-hamburger:hover { background: #e2e8f0; }

.al-breadcrumb {
  display: flex; align-items: center; gap: 7px;
  padding: 7px 12px;
  background: #f1f5f9; border: 1px solid #dde5ef; border-radius: 9px;
}
.al-breadcrumb__icon { display: flex; }
.al-breadcrumb__sep { color: #cbd5e1; font-size: 0.8rem; }
.al-breadcrumb__page { font-size: 0.82rem; font-weight: 700; color: #334155; letter-spacing: -0.01em; }

.al-topbar__right { display: flex; align-items: center; gap: 6px; }
.al-topbar__divider { width: 1px; height: 26px; background: #e2e8f0; margin: 0 5px; border-radius: 1px; }

/* --- Notification Bell --- */
.al-notif { position: relative; }
.al-notif__btn {
  position: relative;
  width: 40px; height: 40px;
  border-radius: 11px;
  background: #f8fafc;
  border: 1px solid #dde5ef;
  color: #64748b;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.al-notif__btn:hover,
.al-notif__btn.active { background: #f1f5f9; border-color: #cbd5e1; color: #334155; }

.al-notif__count {
  position: absolute;
  top: 6px; right: 5px;
  min-width: 17px; height: 17px;
  background: #ef4444;
  color: #fff;
  font-size: 0.62rem;
  font-weight: 800;
  border-radius: 99px;
  border: 2px solid #fff;
  display: flex; align-items: center; justify-content: center;
  padding: 0 3px;
  line-height: 1;
}

/* Dropdown panel */
.al-notif-panel {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 340px;
  background: #fff;
  border: 1px solid #dde5ef;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(15,23,42,0.15), 0 0 0 1px rgba(99,102,241,0.06);
  z-index: 600;
  overflow: hidden;
}

.notif-drop-enter-active { animation: notif-in 0.22s cubic-bezier(0.16,1,0.3,1); }
.notif-drop-leave-active { animation: notif-in 0.18s reverse ease; }
@keyframes notif-in {
  from { opacity: 0; transform: translateY(-8px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.al-notif-panel__head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.875rem; font-weight: 700; color: #0f172a;
}
.al-notif-panel__clear {
  font-size: 0.75rem; font-weight: 600; color: #6366f1;
  background: none; border: none; cursor: pointer;
  padding: 2px 6px; border-radius: 6px; transition: background 0.15s;
  font-family: 'Inter', sans-serif;
}
.al-notif-panel__clear:hover { background: #e0e7ff; }

.al-notif-panel__body { max-height: 340px; overflow-y: auto; }

.al-notif-item {
  display: flex; align-items: flex-start; gap: 11px;
  padding: 13px 18px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #f8fafc;
  position: relative;
}
.al-notif-item:hover { background: #f8fafc; }
.al-notif-item--unread { background: #fafbff; }
.al-notif-item:last-child { border-bottom: none; }

.al-notif-item__icon {
  width: 34px; height: 34px; border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  background: #e0e7ff; color: #4f46e5;
}
.al-notif-item__icon.danger { background: #fee2e2; color: #dc2626; }

.al-notif-item__content { flex: 1; min-width: 0; }
.al-notif-item__title { font-size: 0.84rem; font-weight: 700; color: #0f172a; margin: 0 0 2px; }
.al-notif-item__desc { font-size: 0.78rem; color: #64748b; margin: 0 0 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.al-notif-item__time { font-size: 0.7rem; color: #94a3b8; font-weight: 500; }

.al-notif-item__dot {
  width: 7px; height: 7px;
  background: #6366f1; border-radius: 50%;
  flex-shrink: 0; margin-top: 5px;
}

.al-notif-empty {
  padding: 32px 18px;
  text-align: center;
  color: #94a3b8; font-size: 0.84rem; font-weight: 500;
}

.al-notif-panel__foot {
  padding: 12px 18px;
  border-top: 1px solid #f1f5f9;
  text-align: center;
}
.al-notif-panel__foot a {
  font-size: 0.8125rem; font-weight: 600; color: #6366f1;
  transition: color 0.15s;
}
.al-notif-panel__foot a:hover { color: #4f46e5; }

/* --- Profile --- */
.al-profile {
  display: flex; align-items: center; gap: 9px;
  padding: 5px 11px 5px 5px;
  border-radius: 12px; cursor: default;
  transition: background 0.15s;
  border: 1px solid transparent;
}
.al-profile:hover { background: #f8fafc; border-color: #dde5ef; }

.al-profile__avatar {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: 0.9rem; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(99,102,241,0.35);
  flex-shrink: 0;
}
.al-profile__info { display: flex; flex-direction: column; }
.al-profile__name { font-size: 0.84rem; font-weight: 700; color: #0f172a; line-height: 1.2; letter-spacing: -0.01em; }
.al-profile__role { font-size: 0.7rem; color: #94a3b8; line-height: 1.2; }

/* =========================================================
   CONTENT
   ========================================================= */
.al-content {
  flex: 1;
  width: 100%;
  min-width: 0;
  padding: 28px 30px;
  overflow-anchor: none;
  overflow-x: hidden;
}

/* =========================================================
   MOBILE BACKDROP
   ========================================================= */
.al-backdrop { display: none; }

/* =========================================================
   RESPONSIVE
   ========================================================= */
@media (max-width: 991px) {
  .al-sidebar { transform: translateX(-100%); box-shadow: none; }
  .al-root.al--mobile-open .al-sidebar {
    transform: translateX(0);
    box-shadow: 24px 0 60px rgba(15,23,42,0.28);
  }
  .al-main { margin-left: 0 !important; }

  .al-backdrop {
    display: block;
    position: fixed; inset: 0;
    background: rgba(15,23,42,0.48);
    z-index: 450;
    opacity: 0; pointer-events: none;
    transition: opacity 0.25s;
  }
  .al-root.al--mobile-open .al-backdrop { opacity: 1; pointer-events: auto; }

  .al-hamburger { display: flex; }
  .al-content { padding: 20px 18px; }
}

@media (max-width: 639px) {
  .al-profile__info { display: none; }
  .al-topbar { padding: 0 16px; }
  .al-notif-panel { width: calc(100vw - 32px); right: -10px; }
}

/* =========================================================
   PAGE TRANSITIONS (Pro Max Fluid)
   ========================================================= */
.page-fade-enter-active {
  transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.99);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(1.01);
}

/* Utils */
.mono { font-family: 'JetBrains Mono', 'Fira Code', monospace; }
</style>
