<template>
  <div class="search-page">
    <header class="search-header">
      <div class="search-breadcrumb">
        <span class="search-breadcrumb-home" @click="$router.push('/')">Trang chủ</span>
        <span class="search-breadcrumb-sep">/</span>
        <span class="search-breadcrumb-current">Sản phẩm</span>
      </div>
      <h1 class="search-title">Sản phẩm</h1>
      <p class="search-subtitle">
        Khám phá các sản phẩm phù hợp với bạn.
      </p>
    </header>

    <section class="search-layout">
      <!-- Bộ lọc -->
      <aside class="search-filters">
        <div class="filter-group">
          <h2 class="filter-title">Thể thao</h2>
          <select v-model="filterTheThao" class="filter-select">
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
          <h2 class="filter-title">Đế giày</h2>
          <select v-model="filterDeGiay" class="filter-select">
            <option value="">Tất cả</option>
            <option value="Mềm">Mềm</option>
            <option value="Cứng">Cứng</option>
            <option value="Siêu nhẹ">Siêu nhẹ</option>
          </select>
        </div>

        <div class="filter-group">
          <h2 class="filter-title">Chọn khoảng giá</h2>
          <select v-model="filterGia" class="filter-select">
            <option value="">Tất cả</option>
            <option value="0-1000000">Dưới 1.000.000 VNĐ</option>
            <option value="1000000-3000000">1.000.000 – 3.000.000 VNĐ</option>
            <option value="3000000-999999999">Trên 3.000.000 VNĐ</option>
          </select>
        </div>

        <div class="filter-group">
          <h2 class="filter-title">Thương hiệu</h2>
          <select v-model="filterThuongHieu" class="filter-select">
            <option value="">Tất cả</option>
            <option value="Nike">Nike</option>
            <option value="Adidas">Adidas</option>
            <option value="Stussy">Stussy</option>
            <option value="Cross">Cross</option>
          </select>
        </div>
      </aside>

      <!-- Kết quả -->
      <main class="search-results">
        <header class="results-header">
          <div class="results-search-row">
            <input
              v-model="filterSearch"
              type="search"
              placeholder="Tìm theo tên, thương hiệu, danh mục..."
              class="search-input"
            />
          </div>
          <h2 class="results-title">{{ filterSearch ? 'Kết quả tìm kiếm' : 'Tất cả sản phẩm' }}</h2>
          <p class="results-count">Hiển thị {{ results.length }} sản phẩm</p>
        </header>

        <p v-if="loading" class="results-loading">Đang tải...</p>
        <p v-else-if="loadError" class="results-error">{{ loadError }}</p>
        <div v-else class="results-grid">
          <article
            v-for="item in results"
            :key="item.id"
            class="product-card"
            @click="$router.push(`/product/${item.id}`)"
          >
            <img :src="item.image || PLACEHOLDER_IMG" :alt="item.name" class="product-image" @error="$event.target.src = PLACEHOLDER_IMG" />
            <div class="product-body">
              <p class="product-brand">{{ item.brand || '—' }}</p>
              <h3 class="product-name">{{ item.name }}</h3>
              <p class="product-meta">
                {{ item.category || '—' }}
              </p>
              <p class="product-price">{{ formatPrice(item.price) }}</p>
            </div>
          </article>
        </div>

        <!-- Gợi ý -->
        <section class="recommend-section">
          <h2 class="recommend-title">Bạn cũng có thể thích</h2>
          <div class="recommend-grid">
            <article
              v-for="item in recommendations"
              :key="item.id"
              class="product-card"
            >
              <img :src="item.image || PLACEHOLDER_IMG" :alt="item.name" class="product-image" @error="$event.target.src = PLACEHOLDER_IMG" />
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
import { useRoute } from 'vue-router'
import { getProductsFromApi } from '../api/services/productService'
import { API_BASE_URL } from '../api/config'

const route = useRoute()
const loading = ref(true)
const loadError = ref('')
const productsRaw = ref([])
const filterTheThao = ref('')
const filterDeGiay = ref('')
const filterGia = ref('')
const filterThuongHieu = ref('')
const filterSearch = ref('')

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

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22300%22 height=%22200%22 viewBox=%220 0 300 200%22%3E%3Crect fill=%22%23f3f4f6%22 width=%22300%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 fill=%22%239ca3af%22 font-size=%2214%22 text-anchor=%22middle%22 dy=%22.3em%22%3EKh%C3%B4ng c%C3%B3 %E1%BA%A3nh%3C/text%3E%3C/svg%3E"

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

const recommendations = computed(() => results.value.slice(0, 4))

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
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 16px 64px;
}

.search-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 32px;
}

.search-breadcrumb {
  font-size: 14px;
  color: #6b7280;
}

.search-breadcrumb-home {
  cursor: pointer;
}

.search-breadcrumb-home:hover {
  text-decoration: underline;
}

.search-breadcrumb-sep {
  margin: 0 4px;
}

.search-title {
  font-size: 28px;
  font-weight: 700;
}

.search-subtitle {
  font-size: 14px;
  color: #4b5563;
}

.search-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 32px;
}

.search-filters {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
}

.filter-select {
  margin-top: 4px;
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background-color: #ffffff;
  font-size: 14px;
  color: #111827;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  margin: 4px 4px 0 0;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background-color: #ffffff;
  font-size: 13px;
  cursor: pointer;
  color: #4b5563;
}

.filter-chip--active {
  background-color: #111827;
  color: #ffffff;
  border-color: #111827;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.results-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.results-search-row {
  margin-bottom: 12px;
}

.results-search-row .search-input {
  width: 100%;
  max-width: 400px;
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
}

.results-search-row .search-input:focus {
  outline: none;
  border-color: #111827;
}

.results-title {
  font-size: 20px;
  font-weight: 600;
}

.results-count {
  font-size: 13px;
  color: #6b7280;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.product-card {
  border-radius: 18px;
  overflow: hidden;
  background-color: #f9fafb;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-brand {
  font-size: 12px;
  color: #6b7280;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
}

.product-meta {
  font-size: 13px;
  color: #6b7280;
}

.product-price {
  font-size: 14px;
  font-weight: 600;
  margin-top: 4px;
}

.results-loading,
.results-error {
  padding: 24px;
  text-align: center;
  color: #6b7280;
}

.results-error {
  color: #dc2626;
}

.recommend-section {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-title {
  font-size: 18px;
  font-weight: 600;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1024px) {
  .search-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .results-grid,
  .recommend-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .results-grid,
  .recommend-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

