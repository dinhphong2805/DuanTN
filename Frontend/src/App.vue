<template>
  <div class="app">
    <AppHeader v-if="showHeader" />
    <router-view />
    <SupportChatWidget />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import SupportChatWidget from './components/SupportChatWidget.vue'

const route = useRoute()

const showHeader = computed(() => {
  const p = route.path
  if (p.startsWith('/admin')) return false
  if (['/login', '/register', '/forgot-password', '/reset-password'].includes(p)) return false
  return true
})
</script>

