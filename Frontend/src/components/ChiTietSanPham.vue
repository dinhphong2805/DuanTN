<template>
  <div class="detail-page">
    <p v-if="loading" class="loading-msg">Đang tải...</p>
    <p v-else-if="notFound" class="not-found">Không tìm thấy sản phẩm. <router-link to="/product">Quay lại danh sách</router-link></p>

    <template v-else-if="product">
      <header class="detail-header">
        <div class="detail-breadcrumb">
          <span class="crumb" @click="$router.push('/')">Trang chủ</span>
          <span class="sep">/</span>
          <span class="crumb" @click="$router.push('/product')">Sản phẩm</span>
          <span class="sep">/</span>
          <span class="current">{{ product.name }}</span>
        </div>
      </header>

      <section class="detail-top">
        <div class="gallery" :class="{ 'gallery--single': product.images.length <= 1 }">
          <div class="thumbs" v-if="product.images.length > 1">
            <button
              v-for="(img, idx) in product.images"
              :key="idx"
              class="thumb"
              :class="{ active: idx === activeIndex }"
              type="button"
              @click="activeIndex = idx"
            >
              <img :src="img" alt="" />
            </button>
          </div>
          <div class="hero">
            <button
              v-if="product.images.length > 1"
              class="nav-btn nav-btn--left"
              type="button"
              @click="prevImage"
            >
              ‹
            </button>
            <img
              :src="displayImage"
              :alt="product.name"
              @error="heroImgError = true"
            />
            <button
              v-if="product.images.length > 1"
              class="nav-btn nav-btn--right"
              type="button"
              @click="nextImage"
            >
              ›
            </button>
          </div>
        </div>

        <aside class="panel">
          <p class="brand">{{ product.brand || '—' }}</p>
          <h1 class="name">{{ product.name }}</h1>
          <p class="meta">{{ product.category || '—' }}</p>
          <p class="price">{{ formatPrice(product.price) }}</p>

          <div class="size">
            <div class="size-head">
              <h2>Kích thước</h2>
              <div class="size-head-actions">
                <p class="size-note">Chọn size</p>
                <button
                  type="button"
                  class="size-chart-link"
                  @click="sizeGuideOpen = true"
                >
                  Bảng size &amp; cách đo
                </button>
              </div>
            </div>
            <div class="size-grid">
              <button
                v-for="s in product.sizes"
                :key="s"
                type="button"
                class="size-btn"
                :class="{ selected: s === selectedSize }"
                @click="selectedSize = s"
              >
                {{ s }}
              </button>
            </div>
          </div>

          <button class="cta" type="button" @click="addToCart">Thêm vào giỏ</button>
        </aside>
      </section>

      <section class="detail-body" v-if="product.description">
        <h2>Thông tin sản phẩm</h2>
        <p class="desc">{{ product.description }}</p>
      </section>

      <section class="reviews-section">
      <h2>Đánh giá</h2>
      <div v-if="reviews.length" class="reviews-list">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-meta">
            <span class="review-user">{{ r.userName }}</span>
            <span class="review-rating">★ {{ r.rating }}/5</span>
            <span class="review-date">{{ r.createdAt }}</span>
          </div>
          <p class="review-comment">{{ r.comment }}</p>
        </div>
      </div>
      <div class="review-form">
        <h3>Viết đánh giá</h3>
        <div class="form-row">
          <label>Điểm:</label>
          <select v-model="newReview.rating" class="rating-select">
            <option :value="1">1 sao</option>
            <option :value="2">2 sao</option>
            <option :value="3">3 sao</option>
            <option :value="4">4 sao</option>
            <option :value="5">5 sao</option>
          </select>
        </div>
        <div class="form-group">
          <label>Bình luận</label>
          <textarea v-model="newReview.comment" rows="3" placeholder="Chia sẻ trải nghiệm của bạn"></textarea>
        </div>
        <button type="button" class="btn-review" :disabled="reviewLoading" @click="submitReview">
          {{ reviewLoading ? 'Đang gửi...' : 'Gửi đánh giá' }}
        </button>
      </div>
    </section>

      <section class="recommend" v-if="recommendations.length">
        <h2>Gợi ý cho bạn</h2>
        <div class="grid">
          <article
            v-for="item in recommendations"
            :key="item.id"
            class="card"
            @click="$router.push(`/product/${item.id}`)"
          >
            <img :src="item.image || placeholderImg" :alt="item.name" @error="$event.target.src = placeholderImg" />
            <div class="card-body">
              <p class="card-brand">{{ item.brand || '—' }}</p>
              <p class="card-name">{{ item.name }}</p>
              <p class="card-price">{{ formatPrice(item.price) }}</p>
            </div>
          </article>
        </div>
      </section>
    </template>

    <ShoeSizeGuideModal v-model="sizeGuideOpen" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import ShoeSizeGuideModal from './ShoeSizeGuideModal.vue'
import { useRoute, useRouter } from 'vue-router'
import { useCart } from '../cartStore'
import { getProductByIdFromApi, getProductsFromApi } from '../api/services/productService'
import { API_BASE_URL } from '../api/config'

const placeholderImg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%23f3f4f6' width='400' height='400'/%3E%3Ctext x='50%25' y='50%25' fill='%239ca3af' font-size='16' text-anchor='middle' dy='.3em'%3EKhông có ảnh%3C/text%3E%3C/svg%3E"

const route = useRoute()
const router = useRouter()
const cart = useCart()

const product = ref(null)
const loading = ref(true)
const notFound = ref(false)
const recommendations = ref([])
const activeIndex = ref(0)
const heroImgError = ref(false)
const selectedSize = ref('38')
const sizeGuideOpen = ref(false)

const displayImage = computed(() => {
  if (heroImgError.value) return placeholderImg
  const img = product.value?.images?.[activeIndex.value]
  return img || placeholderImg
})
const reviews = ref([])
const newReview = ref({ rating: 5, comment: '' })
const reviewLoading = ref(false)

const DEFAULT_SIZES = ['36', '37', '38', '39', '40', '41', '42', '43', '44']

function toDisplayUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const base = API_BASE_URL.replace(/\/api\/?$/, '') || 'http://localhost:8080'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // Đường dẫn web: /uploads/xxx
  if (url.startsWith('/')) return base + url
  // Đường dẫn file (Windows/Unix): lấy tên file và dùng /uploads/filename
  const filename = url.replace(/^.*[/\\]/, '')
  if (filename && /\.(jpe?g|png|gif|webp)$/i.test(filename)) {
    return base + '/uploads/' + filename
  }
  return base + '/uploads/' + url.replace(/^.*[/\\]/, '') || ''
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

function formatPrice(n) {
  return (Number(n) || 0).toLocaleString('vi-VN') + ' VNĐ'
}

async function loadProduct() {
  const id = route.params.id
  if (!id) {
    notFound.value = true
    loading.value = false
    return
  }
  loading.value = true
  notFound.value = false
  product.value = null
  try {
    const p = await getProductByIdFromApi(id)
    if (!p) {
      notFound.value = true
      return
    }
    const images = parseImageUrls(p.imageUrl).map(toDisplayUrl).filter(Boolean)
    product.value = {
      id: p.id,
      name: p.name,
      price: p.price,
      brand: p.brand,
      category: p.category,
      description: p.description,
      images,
      sizes: DEFAULT_SIZES,
    }
    activeIndex.value = 0
    heroImgError.value = false
    selectedSize.value = DEFAULT_SIZES[0]
    await loadRecommendations()
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

function prevImage() {
  if (!product.value?.images?.length) return
  const total = product.value.images.length
  activeIndex.value = (activeIndex.value - 1 + total) % total
}

function nextImage() {
  if (!product.value?.images?.length) return
  const total = product.value.images.length
  activeIndex.value = (activeIndex.value + 1) % total
}

async function loadRecommendations() {
  try {
    const list = await getProductsFromApi()
    const others = list
      .filter((p) => p.id !== product.value?.id)
      .slice(0, 4)
      .map((p) => ({
        id: p.id,
        name: p.name,
        price: p.price,
        brand: p.brand,
        category: p.category,
        image: toDisplayUrl(p.imageUrl),
      }))
    recommendations.value = others
  } catch {
    recommendations.value = []
  }
}

watch(() => route.params.id, loadProduct, { immediate: false })
onMounted(loadProduct)

async function submitReview() {
  const comment = newReview.value.comment?.trim()
  if (!comment) return
  reviewLoading.value = true
  try {
    reviews.value = [
      { id: Date.now(), userName: 'Bạn', rating: newReview.value.rating, comment, createdAt: new Date().toISOString().slice(0, 10) },
      ...reviews.value,
    ]
    newReview.value = { rating: 5, comment: '' }
  } finally {
    reviewLoading.value = false
  }
}

function addToCart() {
  if (!product.value) return
  cart.addItem({
    id: product.value.id,
    name: product.value.name,
    brand: product.value.brand,
    price: product.value.price,
    image: product.value.images[0] || placeholderImg,
    size: selectedSize.value,
    quantity: 1,
  })
  router.push('/cart')
}
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 72px;
}

.loading-msg,
.not-found {
  padding: 48px 16px;
  text-align: center;
  color: #6b7280;
}

.not-found a {
  color: #111827;
  text-decoration: underline;
}

.detail-breadcrumb {
  font-size: 14px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.crumb {
  cursor: pointer;
}

.crumb:hover {
  text-decoration: underline;
}

.sep {
  opacity: 0.7;
}

.current {
  color: #111827;
  font-weight: 600;
}

.detail-top {
  margin-top: 16px;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.gallery {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 16px;
}

.gallery--single {
  grid-template-columns: 1fr;
}

.thumbs {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thumb {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 12px;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
}

.thumb.active {
  border-color: #111827;
  box-shadow: 0 0 0 2px rgba(17, 24, 39, 0.15);
}

.thumb img {
  width: 100%;
  height: 68px;
  object-fit: cover;
  display: block;
}

.hero {
  border-radius: 18px;
  overflow: hidden;
  background: #f3f4f6;
  position: relative;
}

.hero img {
  width: 100%;
  height: 520px;
  object-fit: cover;
  display: block;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 38px;
  height: 38px;
  border-radius: 999px;
  border: none;
  background: rgba(17, 24, 39, 0.65);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  z-index: 2;
}

.nav-btn--left {
  left: 10px;
}

.nav-btn--right {
  right: 10px;
}

.panel {
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.brand {
  font-size: 13px;
  color: #6b7280;
}

.name {
  font-size: 24px;
  font-weight: 800;
}

.meta {
  font-size: 14px;
  color: #6b7280;
}

.price {
  font-size: 18px;
  font-weight: 700;
  margin-top: 6px;
}

.size {
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.size-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.size-head h2 {
  font-size: 16px;
  font-weight: 700;
}

.size-head-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  text-align: right;
}

.size-note {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.size-chart-link {
  border: none;
  background: none;
  padding: 0;
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.size-chart-link:hover {
  color: #1d4ed8;
}

.size-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.size-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 12px;
  padding: 10px 0;
  cursor: pointer;
  font-weight: 600;
}

.size-btn.selected {
  border-color: #111827;
  background: #111827;
  color: #fff;
}

.cta {
  margin-top: 10px;
  width: 100%;
  border: none;
  border-radius: 14px;
  padding: 12px 14px;
  background: #111827;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.reviews-section {
  margin-top: 28px;
  border-top: 1px solid #e5e7eb;
  padding-top: 22px;
}

.reviews-section h2 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.review-item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
  background: #f9fafb;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.review-user {
  font-weight: 700;
}

.review-rating {
  color: #d97706;
}

.review-date {
  font-size: 12px;
  color: #6b7280;
}

.review-comment {
  font-size: 14px;
  color: #374151;
  margin: 0;
}

.review-form {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 18px;
  background: #fff;
}

.review-form h3 {
  font-size: 15px;
  font-weight: 700;
  margin: 0 0 12px;
}

.review-form .form-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.rating-select {
  padding: 6px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.review-form textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
}

.btn-review {
  margin-top: 10px;
  padding: 8px 16px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.btn-review:disabled {
  opacity: 0.7;
}

.detail-body {
  margin-top: 28px;
  border-top: 1px solid #e5e7eb;
  padding-top: 22px;
}

.detail-body h2 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 10px;
}

.desc {
  color: #374151;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-line;
}

.recommend {
  margin-top: 28px;
}

.recommend h2 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.card {
  cursor: pointer;
  border-radius: 18px;
  overflow: hidden;
  background: #f9fafb;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.card img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.card-body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-brand {
  font-size: 12px;
  color: #6b7280;
}

.card-name {
  font-size: 14px;
  font-weight: 700;
}

.card-price {
  font-size: 13px;
  font-weight: 700;
}

@media (max-width: 1024px) {
  .detail-top {
    grid-template-columns: minmax(0, 1fr);
  }
  .hero img {
    height: 440px;
  }
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .gallery {
    grid-template-columns: minmax(0, 1fr);
  }
  .thumbs {
    flex-direction: row;
    overflow: auto;
    padding-bottom: 6px;
  }
  .thumb {
    min-width: 88px;
  }
  .hero img {
    height: 360px;
  }
  .size-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

