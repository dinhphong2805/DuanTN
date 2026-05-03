<template>
  <div class="app">
    <AppHeader v-if="showHeader" />
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page-transition" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter v-if="showHeader" />
    <SupportChatWidget />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import SupportChatWidget from './components/SupportChatWidget.vue'

const route = useRoute()

const showHeader = computed(() => {
  const p = route.path
  if (p.startsWith('/admin')) return false
  if (['/login', '/register', '/forgot-password', '/reset-password'].includes(p)) return false
  return true
})
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #0a0a0a;
}

.app-main {
  flex: 1 0 auto;
  min-width: 0;
}

/* Page Transitions */
.page-transition-enter-active,
.page-transition-leave-active {
  transition: opacity 0.15s ease-out, transform 0.15s ease-out;
}

.page-transition-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-transition-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
