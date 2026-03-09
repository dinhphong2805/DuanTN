<template>
  <header class="app-header">
    <div class="header-logo" @click="$router.push('/')">
      <span class="brand">Kesn Store</span>
    </div>

    <div class="header-search">
      <input
        v-model="searchKeyword"
        type="search"
        placeholder="Tìm sản phẩm..."
        class="search-input"
        @keyup.enter="doSearch"
      />
      <button type="button" class="search-btn" @click="doSearch">Tìm</button>
    </div>

    <nav class="header-nav">
      <button
        type="button"
        class="nav-link"
        :class="{ active: $route.path === '/' }"
        @click="$router.push('/')"
      >
        Trang chủ
      </button>
      <button
        type="button"
        class="nav-link"
        :class="{ active: $route.path === '/product' }"
        @click="$router.push('/product')"
      >
        Sản phẩm
      </button>
      <button
        type="button"
        class="nav-link"
        :class="{ active: $route.path === '/cart' }"
        @click="$router.push('/cart')"
      >
        Giỏ hàng
      </button>
      <button
        type="button"
        class="nav-link"
        :class="{ active: $route.path === '/faq' }"
        @click="$router.push('/faq')"
      >
        FAQ
      </button>
      <button
        type="button"
        class="nav-link"
        :class="{ active: $route.path === '/contact' }"
        @click="$router.push('/contact')"
      >
        Liên hệ
      </button>

      <template v-if="auth.isLoggedIn">
        <button
          type="button"
          class="nav-link"
          :class="{ active: $route.path === '/profile' }"
          @click="$router.push('/profile')"
        >
          Tài khoản
        </button>
      </template>
      <template v-else>
        <button
          type="button"
          class="nav-link"
          :class="{ active: $route.path === '/login' }"
          @click="$router.push('/login')"
        >
          Đăng nhập
        </button>
        <button
          type="button"
          class="nav-link"
          :class="{ active: $route.path === '/register' }"
          @click="$router.push('/register')"
        >
          Đăng ký
        </button>
      </template>
    </nav>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../authStore'

const router = useRouter()
const auth = useAuthStore()
const searchKeyword = ref('')

function doSearch() {
  const q = searchKeyword.value?.trim()
  router.push({ path: '/product', query: q ? { search: q } : {} })
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 16px 48px;
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.header-search {
  display: flex;
  flex: 1;
  max-width: 360px;
  gap: 8px;
}

.search-input {
  flex: 1;
  padding: 8px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  font-size: 14px;
  background: #fff;
}

.search-input:focus {
  outline: none;
  border-color: #111827;
}

.search-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 999px;
  background: #111827;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.search-btn:hover {
  background: #020617;
}

.header-logo {
  cursor: pointer;
}

.brand {
  font-size: 20px;
  font-weight: 700;
}

.header-nav {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-link {
  background: none;
  border: none;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #4b5563;
}

.nav-link:hover,
.nav-link.active {
  background-color: #111827;
  color: #fff;
}

@media (max-width: 768px) {
  .app-header {
    padding: 12px 16px;
    flex-wrap: wrap;
  }
  .header-search {
    order: 3;
    width: 100%;
    max-width: 100%;
    margin-top: 8px;
  }
  .header-nav {
    width: 100%;
    margin-top: 8px;
  }
}
</style>
