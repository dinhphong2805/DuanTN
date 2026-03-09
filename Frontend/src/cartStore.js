import { computed, reactive } from 'vue'

const STORAGE_KEY = 'kesn_cart_v1'

function safeParse(json) {
  try {
    const val = JSON.parse(json)
    return Array.isArray(val) ? val : []
  } catch {
    return []
  }
}

const state = reactive({
  items: safeParse(localStorage.getItem(STORAGE_KEY) || '[]'),
})

function persist() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items))
}

function toNumber(priceText) {
  // "4.000.000 VNĐ" -> 4000000
  const digits = String(priceText || '').replace(/[^\d]/g, '')
  return digits ? Number(digits) : 0
}

export function useCart() {
  const total = computed(() =>
    state.items.reduce((sum, it) => sum + toNumber(it.price) * it.quantity, 0)
  )

  const totalText = computed(() =>
    total.value.toLocaleString('vi-VN') + ' VNĐ'
  )

  function addItem(payload) {
    const key = `${payload.id}_${payload.size ?? ''}`
    const existing = state.items.find((x) => x.key === key)
    if (existing) {
      existing.quantity += payload.quantity ?? 1
    } else {
      state.items.unshift({
        key,
        id: payload.id,
        name: payload.name,
        brand: payload.brand,
        price: payload.price,
        image: payload.image,
        size: payload.size ?? null,
        quantity: payload.quantity ?? 1,
      })
    }
    persist()
  }

  function removeItem(key) {
    state.items = state.items.filter((x) => x.key !== key)
    persist()
  }

  function setQuantity(key, qty) {
    const item = state.items.find((x) => x.key === key)
    if (!item) return
    const n = Number(qty)
    if (!Number.isFinite(n) || n < 1) return
    item.quantity = Math.floor(n)
    persist()
  }

  function clear() {
    state.items = []
    persist()
  }

  return {
    state,
    addItem,
    removeItem,
    setQuantity,
    clear,
    total,
    totalText,
  }
}

