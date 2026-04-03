<template>
  <div class="header-wrap">
    <header class="app-header">
      <div class="header-logo" @click="$router.push('/')">
        <span class="brand">Kesn Store</span>
      </div>

      <nav class="header-nav-center" aria-label="Điều hướng chính">
        <button
          type="button"
          class="nav-text"
          :class="{ active: $route.path === '/' }"
          @click="$router.push('/')"
        >
          Trang chủ
        </button>
        <button
          type="button"
          class="nav-text"
          :class="{ active: $route.path === '/product' }"
          @click="$router.push('/product')"
        >
          Sản phẩm
        </button>
        <button
          type="button"
          class="nav-text"
          :class="{ active: $route.path === '/faq' }"
          @click="$router.push('/faq')"
        >
          FAQ
        </button>
        <button
          type="button"
          class="nav-text"
          :class="{ active: $route.path === '/contact' }"
          @click="$router.push('/contact')"
        >
          Liên hệ
        </button>
      </nav>

      <div class="header-actions">
        <button
          type="button"
          class="icon-btn"
          aria-label="Tìm kiếm"
          :class="{ 'icon-btn--active': searchOpen }"
          @click="toggleSearch"
        >
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="7" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
        </button>

        <button type="button" class="icon-btn icon-btn--cart" aria-label="Giỏ hàng" @click="$router.push('/cart')">
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 8h12l-1.2 12H7.2L6 8z" />
            <path d="M9 8V6a3 3 0 0 1 6 0v2" />
          </svg>
          <span v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
        </button>

        <button
          v-if="auth.isLoggedIn"
          type="button"
          class="icon-btn"
          aria-label="Tài khoản"
          :class="{ active: $route.path === '/profile' }"
          @click="$router.push('/profile')"
        >
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
        </button>
        <button
          v-else
          type="button"
          class="icon-btn"
          aria-label="Đăng nhập"
          :class="{ active: $route.path === '/login' }"
          @click="$router.push('/login')"
        >
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
        </button>
      </div>
    </header>

    <div v-if="searchOpen" class="search-expand">
      <div class="search-expand-inner">
        <input
          ref="searchInputRef"
          v-model="searchKeyword"
          type="search"
          class="search-field"
          placeholder="Tìm sản phẩm..."
          @keyup.enter="submitSearch"
        />
        <button type="button" class="search-submit" @click="submitSearch">Tìm</button>
        <button type="button" class="search-close" aria-label="Đóng" @click="closeSearch">×</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../authStore'
import { useCart } from '../cartStore'

const router = useRouter()
const auth = useAuthStore()
const cart = useCart()

const searchKeyword = ref('')
const searchOpen = ref(false)
const searchInputRef = ref(null)

const cartCount = computed(() =>
  cart.state.items.reduce((n, it) => n + (Number(it.quantity) || 0), 0)
)

function toggleSearch() {
  searchOpen.value = !searchOpen.value
}

function closeSearch() {
  searchOpen.value = false
}

watch(searchOpen, async (open) => {
  if (open) {
    await nextTick()
    searchInputRef.value?.focus()
  }
})

function submitSearch() {
  const q = searchKeyword.value?.trim()
  searchOpen.value = false
  router.push({ path: '/product', query: q ? { search: q } : {} })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800&display=swap');

.header-wrap {
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  background-color: #ffffff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 24px;
  max-width: 1280px;
  margin: 0 auto;
}

.header-logo {
  cursor: pointer;
  flex-shrink: 0;
}

.brand {
  font-size: 19px;
  font-weight: 800;
  color: #0a0a0a;
  letter-spacing: -0.03em;
}

.header-nav-center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28px;
  flex: 1;
  flex-wrap: wrap;
}

.nav-text {
  background: none;
  border: none;
  padding: 8px 0;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.22s ease;
  position: relative;
}

.nav-text:hover {
  color: #0a0a0a;
}

.nav-text.active {
  color: #0a0a0a;
  font-weight: 700;
}

.nav-text.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: #111827;
  border-radius: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 22px;
  flex-shrink: 0;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #111827;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, transform 0.15s ease;
}

.icon-btn:hover {
  background: rgba(17, 24, 39, 0.06);
}

.icon-btn:active {
  transform: scale(0.96);
}

.icon-btn--active {
  background: rgba(17, 24, 39, 0.08);
}

.icon-svg {
  width: 22px;
  height: 22px;
}

.icon-btn--cart {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: 4px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
  color: #fff;
  background: #111827;
  border-radius: 999px;
}

.search-expand {
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  background: #fafafa;
  padding: 0 24px 16px;
}

.search-expand-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
}

.search-field {
  flex: 1;
  max-width: 480px;
  padding: 10px 16px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 2px;
  font-size: 14px;
  font-family: inherit;
  background: #fff;
  color: #111827;
}

.search-field:focus {
  outline: none;
  border-color: #111827;
  box-shadow: 0 0 0 2px rgba(17, 24, 39, 0.08);
}

.search-submit {
  padding: 10px 20px;
  border: none;
  border-radius: 2px;
  background: #111827;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s ease;
}

.search-submit:hover {
  background: #020617;
}

.search-close {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 22px;
  line-height: 1;
  color: #6b7280;
  cursor: pointer;
}

.search-close:hover {
  background: rgba(17, 24, 39, 0.06);
  color: #111827;
}

@media (max-width: 900px) {
  .header-nav-center {
    gap: 16px;
    order: 3;
    width: 100%;
    justify-content: flex-start;
  }

  .app-header {
    flex-wrap: wrap;
    padding: 12px 16px;
  }

  .search-expand {
    padding: 0 16px 12px;
  }
}

@media (max-width: 640px) {
  .header-nav-center {
    order: 3;
    width: 100%;
    justify-content: flex-start;
    gap: 18px;
    overflow-x: auto;
    padding-bottom: 6px;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: thin;
  }

  .header-actions {
    gap: 14px;
  }

  .icon-svg {
    width: 20px;
    height: 20px;
  }
}
</style>
