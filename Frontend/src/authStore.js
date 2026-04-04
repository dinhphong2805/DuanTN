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

/** OAuth redirect từng chỉ lưu email/fullName; token vẫn là jwt-token-{id} — bổ sung id để lịch sử đơn / checkout khớp DB */
function attachIdFromToken(user, token) {
  if (!user) return user
  const u = { ...user }
  if (u.id != null && u.id !== '') return u
  if (token && typeof token === 'string' && token.startsWith('jwt-token-')) {
    const n = Number(token.slice('jwt-token-'.length))
    if (Number.isFinite(n)) u.id = n
  }
  return u
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

const _loadedUser = load()
const _loadedToken = loadToken()
const _hydratedUser = attachIdFromToken(_loadedUser, _loadedToken)

const state = reactive({
  user: _hydratedUser,
  token: _loadedToken,
})

if (_hydratedUser && _loadedUser && _hydratedUser.id != null && _loadedUser.id == null) {
  save(_hydratedUser)
}

// Backward compatibility: sessions created before TOKEN_KEY existed.
if (!state.token && state.user?.id) {
  state.token = `jwt-token-${state.user.id}`
  saveToken(state.token)
}

/** Id user hiện tại: từ user.id hoặc suy ra từ token jwt-token-{id} (OAuth / phiên cũ). */
export function resolveSessionUserId() {
  const user = state.user
  const token = state.token
  if (user?.id != null && user.id !== '') {
    const n = Number(user.id)
    if (Number.isFinite(n)) return n
  }
  if (token && typeof token === 'string' && token.startsWith('jwt-token-')) {
    const n = Number(token.slice('jwt-token-'.length))
    if (Number.isFinite(n)) return n
  }
  return undefined
}

export function useAuthStore() {
  const isLoggedIn = computed(() => !!state.user)
  const user = computed(() => state.user)
  const token = computed(() => state.token)
  const isAdmin = computed(() => state.user?.role === 'admin')

  function login(userData, accessToken) {
    const token = accessToken || 'mock-token'
    state.user = attachIdFromToken(userData, token)
    state.token = token
    save(state.user)
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
