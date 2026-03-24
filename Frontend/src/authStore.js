import { reactive, computed } from 'vue'

const STORAGE_KEY = 'kesn_auth'
const TOKEN_KEY = 'kesn_token'

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function save(user) {
  if (user) localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  else localStorage.removeItem(STORAGE_KEY)
}

function loadToken() {
  return localStorage.getItem(TOKEN_KEY)
}

function saveToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

const state = reactive({
  user: load(),
  token: loadToken(),
})

// Backward compatibility: sessions created before TOKEN_KEY existed.
if (!state.token && state.user?.id) {
  state.token = `jwt-token-${state.user.id}`
  saveToken(state.token)
}

export function useAuthStore() {
  const isLoggedIn = computed(() => !!state.user)
  const user = computed(() => state.user)
  const token = computed(() => state.token)
  const isAdmin = computed(() => state.user?.role === 'admin' || state.user?.role === 'staff')

  function login(userData, accessToken) {
    state.user = userData
    state.token = accessToken || 'mock-token'
    save(userData)
    saveToken(state.token)
  }

  function logout() {
    state.user = null
    state.token = null
    save(null)
    saveToken(null)
  }

  return { state, isLoggedIn, user, token, isAdmin, login, logout }
}
