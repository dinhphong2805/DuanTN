<template>
  <div class="search-page">
    <div class="search-promo" role="region" aria-label="Cam kết dịch vụ">
      <div class="search-promo-inner">
        Miễn phí giao từ 3.000.000₫ · Đổi trả trong 14 ngày · Thanh toán an toàn
      </div>
    </div>

    <header class="search-hero">
      <div class="search-hero-inner">
        <nav class="search-breadcrumb" aria-label="Breadcrumb">
          <button type="button" class="search-breadcrumb-link" @click="$router.push('/')">Trang chủ</button>
          <span class="search-breadcrumb-sep" aria-hidden="true">/</span>
          <span class="search-breadcrumb-current">Sản phẩm</span>
        </nav>
        <p class="search-eyebrow">Cửa hàng</p>
        <h1 class="search-title">Sản phẩm</h1>
        <p class="search-subtitle">
          Lựa chọn giày chính hãng theo thể thao, phong cách và ngân sách của bạn.
        </p>
      </div>
    </header>

    <section class="search-layout">
      <aside class="search-filters" aria-label="Bộ lọc">
        <div class="filters-card">
          <div class="filters-card-head">
            <p class="filters-card-title">Bộ lọc</p>
            <button
              v-if="hasActiveFilters"
              type="button"
              class="filters-reset"
              @click="clearFilters"
            >
              Xóa lọc
            </button>
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-sport">Thể thao</label>
            <select id="filter-sport" v-model="filterTheThao" class="filter-select">
            <option value="">Tất cả</option>
            <option value="Basketball">Basketball</option>
            <option value="Đá banh">Đá banh</option>
            <option value="Bóng bầu dục">Bóng bầu dục</option>
            <option value="Gym">Gym</option>
            <option value="Baseball">Baseball</option>
            <option value="Giày cao gót">Giày cao gót</option>
            <option value="Dép">Dép</option>
            <option value="Dép lười">Dép lười</option>
            <option value="Các loại khác">Các loại khác</option>
            </select>
          </div>

          <div class="filter-group">
            <label class="filter-label" for="filter-price">Chọn khoảng giá</label>
            <select id="filter-price" v-model="filterGia" class="filter-select">
            <option value="">Tất cả</option>
            <option value="0-1000000">Dưới 1.000.000 VNĐ</option>
            <option value="1000000-3000000">1.000.000 – 3.000.000 VNĐ</option>
            <option value="3000000-999999999">Trên 3.000.000 VNĐ</option>
            </select>
          </div>

          <div class="filter-group">
            <label class="filter-label" for="filter-brand">Thương hiệu</label>
            <select id="filter-brand" v-model="filterThuongHieu" class="filter-select">
            <option value="">Tất cả</option>
            <option value="Nike">Nike</option>
            <option value="Adidas">Adidas</option>
            <option value="Stussy">Stussy</option>
            <option value="Cross">Cross</option>
            </select>
          </div>
        </div>
      </aside>

      <main class="search-results">
        <div class="results-toolbar">
          <div class="results-search-wrap">
            <span class="results-search-icon" aria-hidden="true">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <circle cx="11" cy="11" r="7" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
            </span>
            <input
              v-model="filterSearch"
              type="search"
              placeholder="Tìm theo tên, thương hiệu, danh mục..."
              class="search-input"
              autocomplete="off"
            />
          </div>
          <div class="results-heading-row">
            <div class="results-heading-left">
              <h2 class="results-title">{{ filterSearch ? 'Kết quả tìm kiếm' : 'Tất cả sản phẩm' }}</h2>
              <p class="results-count">Hiển thị <strong>{{ displayedProducts.length }}</strong> sản phẩm</p>
            </div>
            <label class="sort-wrap">
              <span class="sort-label">Sắp xếp</span>
              <select v-model="sortBy" class="sort-select">
                <option value="default">Mới nhất</option>
                <option value="price-asc">Giá tăng dần</option>
                <option value="price-desc">Giá giảm dần</option>
              </select>
            </label>
          </div>
        </div>

        <p v-if="loading" class="results-loading">
          <span class="results-loading-dot" aria-hidden="true" />
          Đang tải sản phẩm…
        </p>
        <p v-else-if="loadError" class="results-error">{{ loadError }}</p>
        <div v-else-if="!displayedProducts.length" class="results-empty">
          <p class="results-empty-title">Không tìm thấy sản phẩm</p>
          <p class="results-empty-text">Thử đổi bộ lọc hoặc từ khóa tìm kiếm.</p>
          <button type="button" class="results-empty-btn" @click="clearFilters">Xóa bộ lọc</button>
        </div>
        <div v-else class="results-grid">
          <article
            v-for="item in displayedProducts"
            :key="item.id"
            class="product-card"
            tabindex="0"
            role="link"
            @click="goProduct(item.id)"
            @keydown.enter="goProduct(item.id)"
          >
            <div class="product-card-media">
              <img
                :src="item.image || PLACEHOLDER_IMG"
                :alt="item.name"
                class="product-image"
                loading="lazy"
                @error="$event.target.src = PLACEHOLDER_IMG"
              />
              <div class="product-card-overlay" aria-hidden="true">
                <span class="product-card-cta">Xem chi tiết</span>
              </div>
            </div>
            <div class="product-body">
              <p class="product-brand">{{ item.brand || '—' }}</p>
              <h3 class="product-name">{{ item.name }}</h3>
              <p class="product-meta">{{ item.category || '—' }}</p>
              <p class="product-price">{{ formatPrice(item.price) }}</p>
            </div>
          </article>
        </div>

        <section v-if="!loading && !loadError && recommendations.length" class="recommend-section">
          <div class="recommend-head">
            <h2 class="recommend-title">Gợi ý thêm</h2>
            <p class="recommend-desc">Các mẫu khác trong danh sách của bạn</p>
          </div>
          <div class="recommend-grid">
            <article
              v-for="item in recommendations"
              :key="'rec-' + item.id"
              class="product-card"
              tabindex="0"
              role="link"
              @click="goProduct(item.id)"
              @keydown.enter="goProduct(item.id)"
            >
              <div class="product-card-media">
                <img
                  :src="item.image || PLACEHOLDER_IMG"
                  :alt="item.name"
                  class="product-image"
                  loading="lazy"
                  @error="$event.target.src = PLACEHOLDER_IMG"
                />
                <div class="product-card-overlay" aria-hidden="true">
                  <span class="product-card-cta">Xem chi tiết</span>
                </div>
              </div>
              <div class="product-body">
                <p class="product-brand">{{ item.brand || '—' }}</p>
                <h3 class="product-name">{{ item.name }}</h3>
                <p class="product-meta">{{ item.category || '—' }}</p>
                <p class="product-price">{{ formatPrice(item.price) }}</p>
              </div>
            </article>
          </div>
        </section>
      </main>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductsFromApi } from '../api/services/productService'
import { API_BASE_URL } from '../api/config'

const route = useRoute()
const router = useRouter()

function goProduct(id) {
  router.push(`/product/${id}`)
}
const loading = ref(true)
const loadError = ref('')
const productsRaw = ref([])
const filterTheThao = ref('')
const filterGia = ref('')
const filterThuongHieu = ref('')
const filterSearch = ref('')
const sortBy = ref('default')

function toDisplayUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const base = API_BASE_URL.replace(/\/api\/?$/, '') || 'http://localhost:8080'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return base + url
  const filename = url.replace(/^.*[/\\]/, '')
  if (filename && /\.(jpe?g|png|gif|webp)$/i.test(filename)) {
    return base + '/uploads/' + filename
  }
  return base + '/uploads/' + (url.replace(/^.*[/\\]/, '') || '')
}

function parseImageUrls(raw) {
  if (!raw) return []
  const txt = String(raw).trim()
  if (!txt) return []
  if (txt.startsWith('[')) {
    try {
      const arr = JSON.parse(txt)
      if (Array.isArray(arr)) return arr.filter(Boolean).map(String)
    } catch {}
  }
  if (txt.includes('|')) return txt.split('|').map(s => s.trim()).filter(Boolean)
  return [txt]
}

function getPrimaryImage(raw) {
  const urls = parseImageUrls(raw)
  return urls.length ? toDisplayUrl(urls[0]) : ''
}

const PLACEHOLDER_IMG =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="500" viewBox="0 0 400 500"><defs><linearGradient id="p" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#f4f4f5"/><stop offset="100%" stop-color="#e4e4e7"/></linearGradient></defs><rect fill="url(#p)" width="400" height="500"/><path fill="none" stroke="#d4d4d8" stroke-width="1.2" d="M95 310c40-80 170-95 220-25l18 45c8 22-5 48-32 52H118c-28-4-38-35-23-72z"/><text x="200" y="420" text-anchor="middle" fill="#71717a" font-family="system-ui,sans-serif" font-size="12" font-weight="500">Ảnh minh họa</text></svg>'
  )

function formatPrice(n) {
  return (Number(n) || 0).toLocaleString('vi-VN') + ' VNĐ'
}

const results = computed(() => {
  let list = productsRaw.value

  if (filterSearch.value) {
    const q = filterSearch.value.trim().toLowerCase()
    list = list.filter((p) => {
      const name = (p.name || '').toLowerCase()
      const brand = (p.brand || '').toLowerCase()
      const category = (p.category || '').toLowerCase()
      return name.includes(q) || brand.includes(q) || category.includes(q)
    })
  }
  if (filterTheThao.value) {
    list = list.filter((p) => (p.category || '').trim() === filterTheThao.value)
  }
  if (filterGia.value) {
    const [min, max] = filterGia.value.split('-').map(Number)
    list = list.filter((p) => {
      const price = Number(p.price) || 0
      return price >= min && price <= max
    })
  }
  if (filterThuongHieu.value) {
    list = list.filter((p) => (p.brand || '').trim() === filterThuongHieu.value)
  }

  return list.map((p) => ({
    id: p.id,
    image: getPrimaryImage(p.imageUrl),
    brand: p.brand,
    name: p.name,
    category: p.category,
    price: p.price,
  }))
})

const displayedProducts = computed(() => {
  const arr = [...results.value]
  if (sortBy.value === 'price-asc') {
    arr.sort((a, b) => (Number(a.price) || 0) - (Number(b.price) || 0))
  } else if (sortBy.value === 'price-desc') {
    arr.sort((a, b) => (Number(b.price) || 0) - (Number(a.price) || 0))
  } else {
    arr.sort((a, b) => (Number(b.id) || 0) - (Number(a.id) || 0))
  }
  return arr
})

/** Gợi ý: các sản phẩm từ thứ 5 trở đi (tránh trùng với lưới chính). */
const recommendations = computed(() => {
  const list = displayedProducts.value
  if (list.length <= 4) return []
  return list.slice(4, 8)
})

const hasActiveFilters = computed(
  () =>
    !!(filterTheThao.value || filterGia.value || filterThuongHieu.value || filterSearch.value.trim())
)

function clearFilters() {
  filterTheThao.value = ''
  filterGia.value = ''
  filterThuongHieu.value = ''
  filterSearch.value = ''
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    productsRaw.value = await getProductsFromApi()
  } catch (e) {
    loadError.value = e.response?.data?.message || e.message || 'Không thể tải sản phẩm'
    productsRaw.value = []
  } finally {
    loading.value = false
  }
}

function applyQueryFilters() {
  const q = route.query
  if (q.category) filterTheThao.value = q.category
  if (q.brand) filterThuongHieu.value = q.brand
  if (q.search) filterSearch.value = q.search
}

watch(() => route.query, applyQueryFilters, { immediate: true })
onMounted(() => {
  load()
  applyQueryFilters()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800&display=swap');

.search-page {
  --sp-max: 1400px;
  --sp-ink: #0a0a0a;
  --sp-muted: #6b7280;
  --sp-line: rgba(0, 0, 0, 0.08);
  --sp-ease: cubic-bezier(0.22, 1, 0.36, 1);
  --sp-surface: #fafafa;

  max-width: var(--sp-max);
  margin: 0 auto;
  padding: 0 24px 56px;
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  color: var(--sp-ink);
  background: var(--sp-surface);
}

.search-promo {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: #0a0a0a;
  color: rgba(255, 255, 255, 0.82);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  text-align: center;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.search-promo-inner {
  max-width: var(--sp-max);
  margin: 0 auto;
}

.search-hero {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: linear-gradient(165deg, #111827 0%, #0a0a0a 48%, #0a0a0a 100%);
  color: #fff;
  padding: 36px 24px 40px;
  margin-bottom: 32px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.search-hero-inner {
  max-width: var(--sp-max);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.search-eyebrow {
  margin: 0;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
}

.search-breadcrumb {
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.04em;
  color: rgba(255, 255, 255, 0.45);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.search-breadcrumb-link {
  padding: 0;
  border: none;
  background: none;
  font: inherit;
  color: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: color 0.2s ease;
}

.search-breadcrumb-link:hover {
  color: #fff;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.search-breadcrumb-current {
  color: rgba(255, 255, 255, 0.92);
  font-weight: 600;
}

.search-title {
  font-size: clamp(28px, 3.2vw, 40px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.12;
  margin: 0;
}

.search-subtitle {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.55;
  color: rgba(255, 255, 255, 0.55);
  max-width: 520px;
  margin: 0;
  margin-top: 6px;
}

.search-layout {
  display: grid;
  grid-template-columns: 272px minmax(0, 1fr);
  gap: 36px;
  align-items: start;
}

.search-filters {
  position: sticky;
  top: 88px;
}

.filters-card {
  background: #ffffff;
  border: 1px solid var(--sp-line);
  border-radius: 16px;
  padding: 20px 18px 22px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
}

.filters-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.filters-card-title {
  margin: 0;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #9ca3af;
}

.filters-reset {
  padding: 6px 10px;
  border: 1px solid var(--sp-line);
  border-radius: 999px;
  background: #fafafa;
  font-family: inherit;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--sp-ink);
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.filters-reset:hover {
  background: #fff;
  border-color: #d1d5db;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group + .filter-group {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--sp-line);
}

.filter-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #374151;
}

.filter-select {
  width: 100%;
  padding: 11px 32px 11px 12px;
  border-radius: 10px;
  border: 1px solid var(--sp-line);
  background-color: #fafafa;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%236b7280' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  color: var(--sp-ink);
  cursor: pointer;
  appearance: none;
  transition: border-color 0.2s, background-color 0.2s, box-shadow 0.2s;
}

.filter-select:hover {
  border-color: #d1d5db;
  background-color: #fff;
}

.filter-select:focus {
  outline: none;
  border-color: var(--sp-ink);
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.06);
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 28px;
  min-width: 0;
}

.results-toolbar {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.results-search-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.results-search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 14px 18px 14px 46px;
  border: 1px solid var(--sp-line);
  border-radius: 999px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  color: var(--sp-ink);
  background: #fff;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.8) inset;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input::placeholder {
  color: #9ca3af;
}

.search-input:focus {
  outline: none;
  border-color: var(--sp-ink);
  box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.06);
}

.results-heading-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.results-heading-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.results-title {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.results-count {
  margin: 0;
  font-size: 12px;
  font-weight: 500;
  color: var(--sp-muted);
}

.sort-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.sort-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #9ca3af;
}

.sort-select {
  min-width: 180px;
  padding: 10px 32px 10px 14px;
  border-radius: 10px;
  border: 1px solid var(--sp-line);
  background-color: #fff;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%236b7280' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  font-family: inherit;
  font-size: 13px;
  font-weight: 600;
  color: var(--sp-ink);
  cursor: pointer;
  appearance: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.sort-select:focus {
  outline: none;
  border-color: var(--sp-ink);
  box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.06);
}

.results-count strong {
  font-weight: 700;
  color: var(--sp-ink);
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

/* Thẻ sản phẩm — hover storefront */
.product-card {
  position: relative;
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--sp-line);
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: border-color 0.35s var(--sp-ease), box-shadow 0.45s var(--sp-ease), transform 0.4s var(--sp-ease);
}

.product-card:hover,
.product-card:focus-visible {
  border-color: rgba(0, 0, 0, 0.1);
  box-shadow: 0 22px 48px -16px rgba(15, 23, 42, 0.18);
  transform: translateY(-6px);
}

.product-card:focus-visible {
  outline: 2px solid #2563eb;
  outline-offset: 2px;
}

.product-card-media {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4 / 5;
  background: linear-gradient(145deg, #f3f4f6, #fafafa);
}

.product-card-media::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    105deg,
    transparent 38%,
    rgba(255, 255, 255, 0.45) 48%,
    transparent 58%
  );
  transform: translateX(-120%);
  transition: transform 0.75s var(--sp-ease);
  pointer-events: none;
}

.product-card:hover .product-card-media::after {
  transform: translateX(120%);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.65s var(--sp-ease);
}

.product-card:hover .product-image {
  transform: scale(1.06);
}

.product-card-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 16px;
  background: linear-gradient(to top, rgba(10, 10, 10, 0.72) 0%, transparent 55%);
  opacity: 0;
  transition: opacity 0.35s var(--sp-ease);
  pointer-events: none;
}

.product-card:hover .product-card-overlay,
.product-card:focus-visible .product-card-overlay {
  opacity: 1;
}

.product-card-cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 18px;
  border-radius: 999px;
  background: #fff;
  color: #0a0a0a;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transform: translateY(8px);
  transition: transform 0.35s var(--sp-ease);
}

.product-card:hover .product-card-cta,
.product-card:focus-visible .product-card-cta {
  transform: translateY(0);
}

.product-body {
  padding: 16px 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.product-brand {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #9ca3af;
}

.product-name {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  font-size: 12px;
  font-weight: 500;
  color: var(--sp-muted);
}

.product-price {
  font-size: 14px;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin-top: 6px;
  color: var(--sp-ink);
}

.results-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 56px 24px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--sp-muted);
  background: #fff;
  border: 1px solid var(--sp-line);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.04);
}

.results-loading-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--sp-ink);
  animation: sp-pulse 1s ease-in-out infinite;
}

@keyframes sp-pulse {
  0%,
  100% {
    opacity: 0.35;
    transform: scale(0.9);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

.results-error {
  padding: 48px 24px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: #b91c1c;
  border: 1px solid rgba(185, 28, 28, 0.25);
  background: #fef2f2;
  border-radius: 16px;
}

.results-empty {
  padding: 56px 28px;
  text-align: center;
  background: #fff;
  border: 1px solid var(--sp-line);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.04);
}

.results-empty-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--sp-ink);
}

.results-empty-text {
  margin: 0 0 20px;
  font-size: 14px;
  color: var(--sp-muted);
  line-height: 1.5;
}

.results-empty-btn {
  padding: 12px 22px;
  border: none;
  border-radius: 999px;
  background: #0a0a0a;
  color: #fff;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}

.results-empty-btn:hover {
  background: #111827;
}

.results-empty-btn:active {
  transform: scale(0.98);
}

.recommend-section {
  margin-top: 8px;
  padding: 32px 0 0;
  border-top: 1px solid var(--sp-line);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.recommend-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recommend-title {
  margin: 0;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #9ca3af;
}

.recommend-desc {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--sp-muted);
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

@media (max-width: 1024px) {
  .search-filters {
    position: static;
  }

  .search-layout {
    grid-template-columns: minmax(0, 1fr);
    gap: 28px;
  }

  .filters-card {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 0 16px;
  }

  .filters-card-head {
    grid-column: 1 / -1;
    margin-bottom: 8px;
  }

  .filter-group + .filter-group {
    margin-top: 0;
    padding-top: 0;
    border-top: none;
  }

  .filter-group {
    min-width: 0;
  }

  .results-grid,
  .recommend-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .filters-card {
    grid-template-columns: 1fr;
  }

  .filter-group + .filter-group {
    margin-top: 14px;
    padding-top: 14px;
    border-top: 1px solid var(--sp-line);
  }
}

@media (max-width: 640px) {
  .search-page {
    padding: 0 16px 40px;
  }

  .search-hero {
    padding: 28px 16px 32px;
  }

  .results-grid,
  .recommend-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .results-heading-row {
    flex-direction: column;
    align-items: stretch;
  }

  .sort-wrap {
    align-items: stretch;
  }

  .sort-select {
    width: 100%;
    min-width: 0;
  }
}
</style>

