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
            <transition name="fade-hero" mode="out-in">
              <img
                :key="displayImage"
                :src="displayImage"
                :alt="product.name"
                class="hero-img"
                @error="heroImgError = true"
                @click="lightboxOpen = true"
              />
            </transition>
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

          <div v-if="product.stockQty <= 0" class="out-of-stock-badge">
            <span class="oos-icon" aria-hidden="true">✕</span>
            Đã hết hàng
          </div>

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

          <button
            class="cta"
            type="button"
            @click="addToCart"
            :disabled="product.stockQty <= 0"
            :class="{ 'cta--disabled': product.stockQty <= 0 }"
          >
            {{ product.stockQty <= 0 ? 'Hết hàng' : 'Thêm vào giỏ' }}
          </button>
        </aside>
      </section>

      <section class="detail-body" v-if="product.description">
        <h2>Thông tin sản phẩm</h2>
        <p class="desc">{{ product.description }}</p>
      </section>

      <section class="reviews-section">
        <div class="reviews-header">
          <h2>Đánh giá sản phẩm</h2>
          <span v-if="reviews.length" class="reviews-count">{{ reviews.length }} đánh giá</span>
        </div>

        <!-- Danh sách review -->
        <div v-if="reviews.length" class="reviews-list">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-avatar">{{ (r.userName || 'A')[0].toUpperCase() }}</div>
            <div class="review-content">
              <div class="review-meta">
                <span class="review-user">{{ r.userName }}</span>
                <span class="review-stars">
                  <span v-for="i in 5" :key="i" :class="i <= r.rating ? 'star filled' : 'star'">★</span>
                </span>
                <span class="review-date">{{ formatDate(r.createdAt) }}</span>
              </div>
              <p class="review-comment">{{ r.comment }}</p>
            </div>
          </div>
        </div>
        <div v-else-if="!reviewLoading" class="reviews-empty">
          <p>Chưa có đánh giá nào. Hãy là người đầu tiên!</p>
        </div>

        <!-- Form viết review -->
        <div class="review-form">
          <h3>Viết đánh giá của bạn</h3>
          <p v-if="reviewError" class="review-error">{{ reviewError }}</p>
          <div class="form-row">
            <label>Đánh giá với tư cách: <strong>{{ auth.user.value?.fullName || auth.user.value?.email || auth.user.value?.username || 'Khách' }}</strong></label>
          </div>
          <div class="form-row">
            <label>Đánh giá</label>
            <div class="star-picker">
              <button
                v-for="i in 5"
                :key="i"
                type="button"
                class="star-pick"
                :class="{ active: i <= newReview.rating }"
                @click="newReview.rating = i"
              >★</button>
            </div>
          </div>
          <div class="form-group">
            <label for="review-comment">Bình luận</label>
            <textarea
              id="review-comment"
              v-model="newReview.comment"
              rows="3"
              placeholder="Chia sẻ trải nghiệm của bạn..."
            ></textarea>
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
            <div class="card-media">
              <img :src="item.image || placeholderImg" :alt="item.name" class="primary" @error="$event.target.src = placeholderImg" />
              <img v-if="item.image2" :src="item.image2" :alt="item.name" class="secondary" @error="$event.target.style.display = 'none'" />
            </div>
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

    <!-- Lightbox phóng to ảnh -->
    <teleport to="body">
      <transition name="lb-fade">
        <div
          v-if="lightboxOpen"
          class="lightbox-overlay"
          @click.self="lightboxOpen = false"
        >
          <button class="lb-close" type="button" @click="lightboxOpen = false" aria-label="Đóng">✕</button>
          <button
            v-if="product?.images?.length > 1"
            class="lb-nav lb-nav--left"
            type="button"
            @click="prevImage"
          >‹</button>
          <transition name="fade-hero" mode="out-in">
            <img
              :key="displayImage"
              :src="displayImage"
              :alt="product?.name"
              class="lb-img"
            />
          </transition>
          <button
            v-if="product?.images?.length > 1"
            class="lb-nav lb-nav--right"
            type="button"
            @click="nextImage"
          >›</button>
          <div class="lb-counter" v-if="product?.images?.length > 1">
            {{ activeIndex + 1 }} / {{ product.images.length }}
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import ShoeSizeGuideModal from './ShoeSizeGuideModal.vue'
import { useRoute, useRouter } from 'vue-router'
import { useCart } from '../cartStore'
import { useAuthStore } from '../authStore'
import { getProductByIdFromApi, getProductsFromApi, getReviews, addReview } from '../api/services/productService'
import { API_BASE_URL } from '../api/config'

const placeholderImg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%23f3f4f6' width='400' height='400'/%3E%3Ctext x='50%25' y='50%25' fill='%239ca3af' font-size='16' text-anchor='middle' dy='.3em'%3EKhông có ảnh%3C/text%3E%3C/svg%3E"

const route = useRoute()
const router = useRouter()
const cart = useCart()
const auth = useAuthStore()

const product = ref(null)
const loading = ref(true)
const notFound = ref(false)
const recommendations = ref([])
const activeIndex = ref(0)
const heroImgError = ref(false)
const selectedSize = ref('38')
const sizeGuideOpen = ref(false)
const lightboxOpen = ref(false)

const displayImage = computed(() => {
  if (heroImgError.value) return placeholderImg
  const img = product.value?.images?.[activeIndex.value]
  return img || placeholderImg
})
const reviews = ref([])
const newReview = ref({ rating: 5, comment: '' })
const reviewLoading = ref(false)
const reviewError = ref('')

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
      stockQty: p.stockQty ?? 0,
    }
    activeIndex.value = 0
    heroImgError.value = false
    selectedSize.value = DEFAULT_SIZES[0]
    await Promise.all([loadRecommendations(), loadReviewsForProduct(p.id)])
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
        image: toDisplayUrl(parseImageUrls(p.imageUrl)[0]),
        image2: toDisplayUrl(parseImageUrls(p.imageUrl)[1]),
      }))
    recommendations.value = others
  } catch {
    recommendations.value = []
  }
}

watch(() => route.params.id, loadProduct, { immediate: false })
onMounted(loadProduct)

async function loadReviewsForProduct(productId) {
  try {
    reviews.value = await getReviews(productId)
  } catch {
    reviews.value = []
  }
}

function formatDate(val) {
  if (!val) return ''
  const d = new Date(val)
  if (isNaN(d)) return String(val).slice(0, 10)
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

async function submitReview() {
  reviewError.value = ''
  const userName = auth.user.value?.fullName || auth.user.value?.email || auth.user.value?.username || 'Khách'
  const comment = newReview.value.comment?.trim()
  if (!comment)  { reviewError.value = 'Vui lòng nhập bình luận'; return }
  reviewLoading.value = true
  try {
    const saved = await addReview(product.value.id, {
      userName,
      rating: newReview.value.rating,
      comment,
    })
    reviews.value = [saved, ...reviews.value]
    newReview.value = { rating: 5, comment: '' }
  } catch (e) {
    reviewError.value = e.response?.data?.message || 'Gửi đánh giá thất bại, vui lòng thử lại'
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
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800&display=swap');

.detail-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 28px 24px 80px;
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  color: #1a1a1a;
  background-color: #ffffff;
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
  background: #ffffff;
  border: 1px solid #f0f0f0;
  position: relative;
  height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero img {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
  display: block;
}

.hero-img {
  cursor: zoom-in;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 42px;
  height: 42px;
  border-radius: 999px;
  border: none;
  background: rgba(17, 24, 39, 0.7);
  color: #fff;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
  z-index: 2;
  opacity: 0;
  transition: opacity 0.25s ease, transform 0.25s ease, background 0.2s;
  backdrop-filter: blur(4px);
}

.hero:hover .nav-btn {
  opacity: 1;
}

.nav-btn:hover {
  background: rgba(17, 24, 39, 0.92);
  transform: translateY(-50%) scale(1.08);
}

.nav-btn--left {
  left: 12px;
}

.nav-btn--right {
  right: 12px;
}

/* ===== LIGHTBOX ===== */
.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
}

.lb-img {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 32px 80px rgba(0,0,0,0.6);
  user-select: none;
}

.lb-close {
  position: fixed;
  top: 20px;
  right: 24px;
  width: 44px;
  height: 44px;
  border-radius: 999px;
  border: none;
  background: rgba(255,255,255,0.12);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: background 0.2s, transform 0.2s;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lb-close:hover {
  background: rgba(255,255,255,0.22);
  transform: scale(1.1);
}

.lb-nav {
  position: fixed;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  border-radius: 999px;
  border: none;
  background: rgba(255,255,255,0.12);
  color: #fff;
  font-size: 32px;
  line-height: 1;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: background 0.2s, transform 0.2s;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lb-nav:hover {
  background: rgba(255,255,255,0.24);
  transform: translateY(-50%) scale(1.08);
}

.lb-nav--left {
  left: 20px;
}

.lb-nav--right {
  right: 20px;
}

.lb-counter {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  background: rgba(0,0,0,0.4);
  padding: 6px 16px;
  border-radius: 999px;
  backdrop-filter: blur(4px);
}

.lb-fade-enter-active,
.lb-fade-leave-active {
  transition: opacity 0.25s ease;
}

.lb-fade-enter-from,
.lb-fade-leave-to {
  opacity: 0;
}

.panel {
  border: 1px solid #f0f0f0;
  border-radius: 24px;
  padding: 32px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.3s ease;
}

.panel:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.brand {
  font-size: 13px;
  color: #6b7280;
}

.name {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #0a0a0a, #4f46e5);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shine 5s linear infinite;
}

.meta {
  font-size: 14px;
  color: #6b7280;
}

.price {
  font-size: 22px;
  font-weight: 800;
  margin-top: 6px;
  background: linear-gradient(to right, #000, #ec4899);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
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
  border-radius: 999px;
  padding: 16px;
  background: linear-gradient(45deg, #4f46e5, #ec4899);
  background-size: 200% auto;
  color: #fff;
  font-family: inherit;
  font-weight: 800;
  font-size: 14px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(236, 72, 153, 0.3);
  transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
}

.cta:hover:not(:disabled) {
  background-position: right center;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 16px 32px rgba(79, 70, 229, 0.4);
}

.cta--disabled,
.cta:disabled {
  background: #d1d5db;
  color: #9ca3af;
  box-shadow: none;
  cursor: not-allowed;
}

/* Badge hết hàng */
.out-of-stock-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 16px;
  background: #fef2f2;
  border: 1px solid rgba(239, 68, 68, 0.25);
  border-radius: 999px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.oos-icon {
  font-size: 11px;
  font-weight: 900;
}

.reviews-section {
  margin-top: 40px;
  border-top: 1px solid #e5e7eb;
  padding-top: 32px;
}

.reviews-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.reviews-header h2 {
  font-size: 20px;
  font-weight: 800;
  margin: 0;
}

.reviews-count {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 12px;
  border-radius: 999px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.review-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  border: 1px solid #f3f4f6;
  border-radius: 16px;
  background: #fafafa;
  transition: background 0.2s;
}

.review-item:hover {
  background: #f3f4f6;
}

.review-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #111827, #4f46e5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.review-content {
  flex: 1;
  min-width: 0;
}

.review-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.review-user {
  font-weight: 700;
  font-size: 15px;
  color: #111827;
}

.review-stars {
  display: flex;
  gap: 2px;
  font-size: 14px;
}

.star {
  color: #e5e7eb;
}

.star.filled {
  color: #f59e0b;
}

.review-date {
  font-size: 12px;
  color: #9ca3af;
  margin-left: auto;
}

.review-comment {
  font-size: 14px;
  line-height: 1.6;
  color: #374151;
  margin: 0;
  white-space: pre-line;
}

.reviews-empty {
  text-align: center;
  padding: 40px 20px;
  background: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 16px;
  color: #6b7280;
  font-size: 15px;
  margin-bottom: 32px;
}

.review-form {
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  padding: 24px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.review-form h3 {
  font-size: 18px;
  font-weight: 800;
  margin: 0 0 20px;
}

.review-error {
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;
  padding: 10px;
  background: #fef2f2;
  border-radius: 8px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.form-row label,
.form-group label {
  font-size: 13px;
  font-weight: 700;
  color: #374151;
}

.review-input,
.review-form textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.review-input:focus,
.review-form textarea:focus {
  outline: none;
  border-color: #111827;
  box-shadow: 0 0 0 3px rgba(17, 24, 39, 0.1);
}

.star-picker {
  display: flex;
  gap: 4px;
}

.star-pick {
  background: none;
  border: none;
  padding: 0;
  font-size: 24px;
  color: #e5e7eb;
  cursor: pointer;
  transition: transform 0.1s, color 0.2s;
}

.star-pick:hover {
  transform: scale(1.1);
}

.star-pick.active {
  color: #f59e0b;
}

.btn-review {
  margin-top: 8px;
  padding: 12px 24px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.btn-review:hover:not(:disabled) {
  background: #1f2937;
  transform: translateY(-1px);
}

.btn-review:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #9ca3af;
  margin-bottom: 16px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.card {
  cursor: pointer;
  border-radius: 2px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  transition: border-color 0.35s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.45s cubic-bezier(0.22, 1, 0.36, 1), transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.card:hover {
  border-color: rgba(0, 0, 0, 0.12);
  box-shadow: 0 20px 44px -14px rgba(15, 23, 42, 0.14);
  transform: translateY(-5px);
}

.card-media {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #fdfdfd;
}

.card img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  transition: opacity 0.5s ease, transform 0.65s ease;
}

.card img.secondary {
  position: absolute;
  top: 0;
  left: 0;
  opacity: 0;
  z-index: 1;
}

.card:hover img.primary {
  opacity: 0;
  transform: scale(1.05);
}

.card:hover img.secondary {
  opacity: 1;
  transform: scale(1.05);
}

.card-body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.card-brand {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #9ca3af;
}

.card-name {
  font-size: 14px;
  font-weight: 700;
}

.card-price {
  font-size: 13px;
  font-weight: 700;
}

.fade-hero-enter-active,
.fade-hero-leave-active {
  transition: opacity 0.3s ease;
}

.fade-hero-enter-from,
.fade-hero-leave-to {
  opacity: 0;
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

<!-- Global styles cho lightbox (teleport ra body, không dùng scoped) -->
<style>
.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
}

.lb-img {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.6);
  user-select: none;
}

.lb-close {
  position: fixed;
  top: 20px;
  right: 24px;
  width: 44px;
  height: 44px;
  border-radius: 999px;
  border: none;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: background 0.2s, transform 0.2s;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lb-close:hover {
  background: rgba(255, 255, 255, 0.22);
  transform: scale(1.1);
}

.lb-nav {
  position: fixed;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  border-radius: 999px;
  border: none;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 32px;
  line-height: 1;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: background 0.2s, transform 0.2s;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lb-nav:hover {
  background: rgba(255, 255, 255, 0.24);
  transform: translateY(-50%) scale(1.08);
}

.lb-nav--left { left: 20px; }
.lb-nav--right { right: 20px; }

.lb-counter {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  background: rgba(0, 0, 0, 0.4);
  padding: 6px 16px;
  border-radius: 999px;
  backdrop-filter: blur(4px);
}

.lb-fade-enter-active,
.lb-fade-leave-active {
  transition: opacity 0.25s ease;
}

.lb-fade-enter-from,
.lb-fade-leave-to {
  opacity: 0;
}
</style>

