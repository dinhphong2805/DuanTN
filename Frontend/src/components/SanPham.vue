<template>
  <div class="search-page">
    

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
            <option value="Balenciaga">Balenciaga</option>
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

        <div v-if="totalPages > 1" class="pagination-wrap">
          <button 
            type="button" 
            class="pagination-btn" 
            :disabled="currentPage === 1"
            @click="currentPage--"
          >
            Trang trước
          </button>
          <span class="pagination-info">Trang {{ currentPage }} / {{ totalPages }}</span>
          <button 
            type="button" 
            class="pagination-btn" 
            :disabled="currentPage === totalPages"
            @click="currentPage++"
          >
            Trang sau
          </button>
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

// Pagination variables
const currentPage = ref(1)
const itemsPerPage = ref(12)

watch([filterTheThao, filterGia, filterThuongHieu, filterSearch, sortBy], () => {
  currentPage.value = 1
})

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

const sortedProducts = computed(() => {
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

const totalPages = computed(() => {
  return Math.ceil(sortedProducts.value.length / itemsPerPage.value) || 1
})

const displayedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return sortedProducts.value.slice(start, end)
})

/** Gợi ý: các sản phẩm từ thứ 5 trở đi (tránh trùng với lưới chính). */
const recommendations = computed(() => {
  const list = sortedProducts.value
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

<style src="../assets/css/product.css" scoped></style>


