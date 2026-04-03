<template>
  <div class="irun-home home-page">
    <!-- Thanh ưu đãi / thông tin nhanh (kiểu banner trên cùng) -->
    <div class="irun-promo-bar" role="region" aria-label="Thông tin giao hàng">
      <div class="irun-promo-inner">
        Miễn phí giao từ 3.000.000₫ · Đổi trả trong 14 ngày · Thanh toán an toàn
      </div>
    </div>

    <!-- Hero full-width nền tối + lookbook chồng ảnh -->
    <section class="home-hero-dark" aria-label="Giới thiệu">
      <div class="home-hero-dark-inner">
        <div class="home-hero-copy">
          <p class="home-hero-eyebrow">Kesn Store</p>
          <h1 class="home-hero-title">JUST YOUR SHOES.</h1>
          <p class="home-hero-sub">
            Giày không chỉ để đi – là để thể hiện chính mình. Chọn đôi giày phù hợp cho từng bước chạy và phong cách của bạn.
          </p>
          <div class="home-hero-actions">
            <button type="button" class="home-btn home-btn--hero-solid" @click="$router.push('/product')">
              Mua sắm ngay
            </button>
            <button type="button" class="home-btn home-btn--hero-outline" @click="$router.push('/faq')">
              Tư vấn size
            </button>
          </div>
        </div>
        <div class="home-hero-lookbook">
          <a href="#" class="home-lookbook-img home-lookbook-img--a" @click.prevent="$router.push('/product')">
            <img :src="p1" alt="Kesn Store" class="home-lookbook-photo" />
          </a>
          <a href="#" class="home-lookbook-img home-lookbook-img--b" @click.prevent="$router.push('/product')">
            <img :src="p2" alt="Kesn Store" class="home-lookbook-photo" />
          </a>
        </div>
      </div>
    </section>

    <!-- Dải uy tín 3 cột (icon + text) -->
    <section class="home-trust-icons" role="region" aria-label="Cam kết dịch vụ">
      <div class="home-trust-icons-inner">
        <div class="home-trust-cell">
          <span class="home-trust-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M3 7h18v10H3z" />
              <path d="M7 7V5a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v2" />
              <path d="M7 12h4" />
            </svg>
          </span>
          <div class="home-trust-text">
            <strong>Giao hàng</strong>
            <p>Miễn phí từ 3.000.000₫ · Toàn quốc</p>
          </div>
        </div>
        <div class="home-trust-cell">
          <span class="home-trust-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 22s7-4.5 7-11a7 7 0 1 0-14 0c0 6.5 7 11 7 11z" />
              <circle cx="12" cy="11" r="2.5" />
            </svg>
          </span>
          <div class="home-trust-text">
            <strong>Hỗ trợ</strong>
            <p>Tư vấn size · Đổi trả trong 14 ngày</p>
          </div>
        </div>
        <div class="home-trust-cell">
          <span class="home-trust-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="4" y="9" width="16" height="11" rx="1" />
              <path d="M8 9V7a4 4 0 0 1 8 0v2" />
            </svg>
          </span>
          <div class="home-trust-text">
            <strong>Thanh toán</strong>
            <p>Bảo mật · Nhiều phương thức</p>
          </div>
        </div>
      </div>
    </section>

    <main class="home-main">
      <!-- Hàng mới & nổi bật — từ API -->
      <section class="home-section home-section--band">
        <header class="section-head">
          <div class="section-head-text">
            <p class="section-eyebrow">Mới nhất</p>
            <h2 class="section-title">Hàng mới &amp; nổi bật</h2>
          </div>
          <button type="button" class="section-link-all" @click="$router.push('/product')">
            Xem tất cả
            <span class="section-link-arrow" aria-hidden="true">→</span>
          </button>
        </header>

        <p v-if="featuredLoading" class="home-loading">Đang tải sản phẩm…</p>
        <p v-else-if="featuredError" class="home-error">{{ featuredError }}</p>
        <div v-else class="home-product-grid">
          <article
            v-for="item in featuredProducts"
            :key="item.id"
            class="home-product-card"
            tabindex="0"
            role="link"
            @click="goProduct(item.id)"
            @keydown.enter="goProduct(item.id)"
          >
            <div class="home-product-media">
              <img
                :src="item.image || PLACEHOLDER_IMG"
                :alt="item.name"
                loading="lazy"
                @error="(e) => (e.target.src = PLACEHOLDER_IMG)"
              />
            </div>
            <div class="home-product-body">
              <p class="home-product-brand">{{ item.brand || '—' }}</p>
              <h3 class="home-product-name">{{ item.name }}</h3>
              <p class="home-product-price">{{ formatPrice(item.price) }}</p>
            </div>
          </article>
        </div>
      </section>

      <!-- Spotlight: Training & Đá banh -->
      <section class="home-spotlight" aria-label="Danh mục nổi bật">
        <article
          class="home-spotlight-card"
          @click="$router.push({ path: '/product', query: { category: 'Gym' } })"
        >
          <div class="home-spotlight-media">
            <img :src="t4" alt="Training & Gym" />
          </div>
          <div class="home-spotlight-body">
            <p class="home-spotlight-eyebrow">Training</p>
            <h3 class="home-spotlight-title">Gym &amp; chạy bộ trong nhà</h3>
            <p class="home-spotlight-desc">Giày đệm êm, bám sàn tốt cho buổi tập của bạn.</p>
            <span class="home-spotlight-cta">Khám phá <span aria-hidden="true">→</span></span>
          </div>
        </article>
        <article
          class="home-spotlight-card"
          @click="$router.push({ path: '/product', query: { category: 'Đá banh' } })"
        >
          <div class="home-spotlight-media">
            <img :src="t2" alt="Đá banh" />
          </div>
          <div class="home-spotlight-body">
            <p class="home-spotlight-eyebrow">Đá banh</p>
            <h3 class="home-spotlight-title">Sân cỏ &amp; futsal</h3>
            <p class="home-spotlight-desc">Đế đinh và đế TF cho từng loại mặt sân.</p>
            <span class="home-spotlight-cta">Khám phá <span aria-hidden="true">→</span></span>
          </div>
        </article>
      </section>

      <!-- Thể thao -->
      <section class="home-section">
        <header class="section-head">
          <div class="section-head-text">
            <p class="section-eyebrow">Danh mục</p>
            <h2 class="section-title">Thể thao</h2>
          </div>
          <button type="button" class="section-link-all" @click="$router.push('/product')">
            Xem tất cả
            <span class="section-link-arrow" aria-hidden="true">→</span>
          </button>
        </header>

        <div class="home-grid home-grid--4">
          <article
            class="home-tile"
            @click="$router.push({ path: '/product', query: { category: 'Basketball' } })"
          >
            <div class="home-tile-media">
              <img :src="t1" alt="Basketball" />
            </div>
            <h3 class="home-tile-title">Basketball</h3>
          </article>
          <article
            class="home-tile"
            @click="$router.push({ path: '/product', query: { category: 'Đá banh' } })"
          >
            <div class="home-tile-media">
              <img :src="t2" alt="Football" />
            </div>
            <h3 class="home-tile-title">Football</h3>
          </article>
          <article
            class="home-tile"
            @click="$router.push({ path: '/product', query: { category: 'Baseball' } })"
          >
            <div class="home-tile-media">
              <img :src="t3" alt="Baseball" />
            </div>
            <h3 class="home-tile-title">Baseball</h3>
          </article>
          <article
            class="home-tile"
            @click="$router.push({ path: '/product', query: { category: 'Gym' } })"
          >
            <div class="home-tile-media">
              <img :src="t4" alt="Athletics running" />
            </div>
            <h3 class="home-tile-title">Athletics running</h3>
          </article>
        </div>
      </section>

      <!-- Mua theo thương hiệu -->
      <section class="home-section home-section--band">
        <header class="section-head">
          <div class="section-head-text">
            <p class="section-eyebrow">Thương hiệu</p>
            <h2 class="section-title">Mua theo thương hiệu</h2>
          </div>
          <button type="button" class="section-link-all" @click="$router.push('/product')">
            Xem tất cả
            <span class="section-link-arrow" aria-hidden="true">→</span>
          </button>
        </header>

        <div class="home-grid home-grid--4">
          <article
            class="home-tile home-tile--brand"
            @click="$router.push({ path: '/product', query: { brand: 'Nike' } })"
          >
            <div class="home-tile-media">
              <img :src="ig5" alt="Nike" />
            </div>
            <h3 class="home-tile-title">Nike</h3>
          </article>
          <article
            class="home-tile home-tile--brand"
            @click="$router.push({ path: '/product', query: { brand: 'Adidas' } })"
          >
            <div class="home-tile-media">
              <img :src="ig6" alt="Adidas" />
            </div>
            <h3 class="home-tile-title">Adidas</h3>
          </article>
          <article
            class="home-tile home-tile--brand"
            @click="$router.push({ path: '/product', query: { brand: 'Balenciaga' } })"
          >
            <div class="home-tile-media">
              <img :src="ig7" alt="Balenciaga" />
            </div>
            <h3 class="home-tile-title">Balenciaga</h3>
          </article>
          <article class="home-tile home-tile--brand" @click="$router.push('/product')">
            <div class="home-tile-media">
              <img :src="ig8" alt="Nhiều mẫu khác" />
            </div>
            <h3 class="home-tile-title">Nhiều mẫu khác</h3>
          </article>
        </div>
      </section>

      <!-- Banner -->
      <section class="home-section home-section--video">
        <header class="section-head">
          <div class="section-head-text">
            <p class="section-eyebrow">Không bỏ lỡ</p>
            <h2 class="section-title accent">Đừng bỏ lỡ những mẫu mới nhất</h2>
          </div>
        </header>

        <div class="home-video-banner">
          <img :src="banner" alt="Banner" />
        </div>
      </section>

      <!-- Bộ sưu tập: 3 sản phẩm từ API + hiệu ứng -->
      <section class="home-section home-collection-section" aria-labelledby="collection-heading">
        <header class="section-head">
          <div class="section-head-text">
            <p class="section-eyebrow">Bộ sưu tập</p>
            <h2 id="collection-heading" class="section-title">Nổi bật</h2>
          </div>
          <button type="button" class="section-link-all" @click="$router.push('/product')">
            Xem tất cả
            <span class="section-link-arrow" aria-hidden="true">→</span>
          </button>
        </header>

        <p v-if="featuredLoading" class="home-loading">Đang tải sản phẩm…</p>
        <p v-else-if="featuredError" class="home-error">{{ featuredError }}</p>
        <p v-else-if="collectionProducts.length === 0" class="home-collection-empty">
          Chưa có sản phẩm cho bộ sưu tập. <button type="button" class="home-link-inline" @click="$router.push('/product')">Xem cửa hàng</button>
        </p>
        <div v-else class="home-collection-grid">
          <article
            v-for="(item, idx) in collectionProducts"
            :key="'col-' + item.id"
            class="home-collection-card"
            :class="'home-collection-card--' + (idx + 1)"
            tabindex="0"
            role="link"
            @click="goProduct(item.id)"
            @keydown.enter="goProduct(item.id)"
          >
            <div class="home-collection-card-inner">
              <div class="home-collection-media">
                <img
                  :src="item.image || PLACEHOLDER_IMG"
                  :alt="item.name"
                  loading="lazy"
                  @error="(e) => (e.target.src = PLACEHOLDER_IMG)"
                />
              </div>
              <div class="home-collection-body">
                <p class="home-collection-brand">{{ item.brand || '—' }}</p>
                <h3 class="home-collection-name">{{ item.name }}</h3>
                <p class="home-collection-price">{{ formatPrice(item.price) }}</p>
                <p class="home-collection-cat">{{ item.category || '—' }}</p>
              </div>
            </div>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import banner from './image/banner.png'
import p1 from './image/p1.jpg'
import p2 from './image/p2.jpg'
import t1 from './image/t1.jpg'
import t2 from './image/t2.jpg'
import t3 from './image/t3.jpg'
import t4 from './image/t4.png'
import ig5 from './image/ig5.png'
import ig6 from './image/ig6.png'
import ig7 from './image/ig7.png'
import ig8 from './image/ig8.png'
import { API_BASE_URL } from '../api/config'
import { getProductsFromApi } from '../api/services/productService'

const router = useRouter()

const PLACEHOLDER_IMG =
  'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22300%22 height=%22200%22 viewBox=%220 0 300 200%22%3E%3Crect fill=%22%23f3f4f6%22 width=%22300%22 height=%22200%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 fill=%22%239ca3af%22 font-size=%2214%22 text-anchor=%22middle%22 dy=%22.3em%22%3EKh%C3%B4ng c%C3%B3 %E1%BA%A3nh%3C/text%3E%3C/svg%3E'

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
    } catch {
      /* ignore */
    }
  }
  if (txt.includes('|')) return txt.split('|').map((s) => s.trim()).filter(Boolean)
  return [txt]
}

function getPrimaryImage(raw) {
  const urls = parseImageUrls(raw)
  return urls.length ? toDisplayUrl(urls[0]) : ''
}

function formatPrice(n) {
  return (Number(n) || 0).toLocaleString('vi-VN') + ' VNĐ'
}

const featuredLoading = ref(true)
const featuredError = ref('')
const featuredRaw = ref([])

const featuredProducts = computed(() =>
  featuredRaw.value.slice(0, 8).map((p) => ({
    id: p.id,
    image: getPrimaryImage(p.imageUrl),
    brand: p.brand,
    name: p.name,
    price: p.price,
  }))
)

/** Ưu tiên 1 SP / danh mục: Giày cao gót, Dép, Dép lười — thiếu thì lấy thêm từ danh sách */
const COLLECTION_CATS = ['Giày cao gót', 'Dép', 'Dép lười']

const collectionProducts = computed(() => {
  const list = featuredRaw.value || []
  const sorted = [...list].sort((a, b) => (Number(b.id) || 0) - (Number(a.id) || 0))
  const picked = []
  const used = new Set()

  for (const cat of COLLECTION_CATS) {
    const p = sorted.find((x) => (x.category || '').trim() === cat && !used.has(x.id))
    if (p) {
      picked.push(p)
      used.add(p.id)
    }
  }
  for (const p of sorted) {
    if (picked.length >= 3) break
    if (!used.has(p.id)) {
      picked.push(p)
      used.add(p.id)
    }
  }

  return picked.slice(0, 3).map((p) => ({
    id: p.id,
    image: getPrimaryImage(p.imageUrl),
    brand: p.brand,
    name: p.name,
    price: p.price,
    category: p.category,
  }))
})

function goProduct(id) {
  router.push(`/product/${id}`)
}

onMounted(async () => {
  featuredLoading.value = true
  featuredError.value = ''
  try {
    const list = await getProductsFromApi()
    const sorted = [...(list || [])].sort((a, b) => (Number(b.id) || 0) - (Number(a.id) || 0))
    featuredRaw.value = sorted
  } catch (e) {
    featuredError.value = e.response?.data?.message || e.message || 'Không thể tải sản phẩm'
    featuredRaw.value = []
  } finally {
    featuredLoading.value = false
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800&display=swap');

.irun-home {
  --surface-page: #ffffff;
  --surface-muted: #fafafa;
  --border: rgba(0, 0, 0, 0.08);
  --ink: #0a0a0a;
  --muted: #6b7280;
  --accent: #dc2626;
  --ease: cubic-bezier(0.4, 0, 0.2, 1);
  --dur: 0.35s;

  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  background: var(--surface-page);
  color: var(--ink);
  -webkit-font-smoothing: antialiased;
}

.irun-promo-bar {
  background: #111827;
  color: #f9fafb;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  text-align: center;
  padding: 10px 16px;
}

.irun-promo-inner {
  max-width: 1280px;
  margin: 0 auto;
}

/* Hero nền đen full-width — tương tự storefront chạy bộ */
.home-hero-dark {
  background: linear-gradient(165deg, #0a0a0a 0%, #171717 45%, #0a0a0a 100%);
  color: #fafafa;
  padding: 48px 24px 48px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
}

.home-hero-dark-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: clamp(24px, 4vw, 48px);
  align-items: stretch;
  min-height: clamp(420px, 52vh, 560px);
}

.home-hero-copy {
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 8px 0;
  min-height: 0;
}

.home-hero-eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.5);
  margin: 0 0 14px;
}

.home-hero-title {
  font-size: clamp(2.1rem, 4.5vw, 3.15rem);
  font-weight: 800;
  letter-spacing: -0.035em;
  line-height: 1.05;
  margin: 0 0 16px;
  color: #fff;
}

.home-hero-sub {
  font-size: 15px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.72);
  margin: 0 0 28px;
  max-width: 34rem;
}

.home-hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.home-btn {
  font-family: inherit;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 15px 28px;
  border-radius: 0;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.home-btn--hero-solid {
  border: none;
  background: #fff;
  color: #0a0a0a;
}

.home-btn--hero-solid:hover {
  box-shadow: 0 12px 32px rgba(255, 255, 255, 0.18);
  transform: translateY(-2px);
}

.home-btn--hero-outline {
  background: transparent;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.45);
}

.home-btn--hero-outline:hover {
  border-color: #fff;
  background: rgba(255, 255, 255, 0.06);
  transform: translateY(-2px);
}

/* Lookbook: hai ảnh nghiêng chồng — cao theo cột trái, ảnh lớn hơn, overflow clip */
.home-hero-lookbook {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 380px;
  overflow: hidden;
  isolation: isolate;
  align-self: stretch;
}

.home-lookbook-img {
  position: absolute;
  display: block;
  overflow: hidden;
  right: auto;
  /* Ảnh lớn (~90% chiều cao vùng), tỷ lệ 3:4 — chồng + nghiêng trong khung */
  height: 90%;
  max-height: min(440px, 52vh);
  width: auto;
  aspect-ratio: 3 / 4;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: #141414;
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.48);
  border-radius: 2px;
  transform-origin: center center;
  transition: transform var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}

.home-lookbook-img:focus-visible {
  outline: 2px solid #fff;
  outline-offset: 3px;
}

/* Ảnh sau lệch vào trong để chồng lên ảnh trước (kiểu polaroid) */
.home-lookbook-img--a {
  left: 0;
  top: 50%;
  z-index: 1;
  transform: translateY(-50%) rotate(-4.5deg);
}

.home-lookbook-img--b {
  left: 28%;
  top: 50%;
  z-index: 2;
  transform: translateY(-50%) rotate(4deg);
}

.home-lookbook-img:hover {
  z-index: 3;
  box-shadow: 0 22px 52px rgba(0, 0, 0, 0.55);
}

.home-lookbook-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
  transition: transform 0.55s var(--ease);
}

.home-lookbook-img:hover .home-lookbook-photo {
  transform: scale(1.05);
}

/* Dải 3 cột uy tín */
.home-trust-icons {
  background: #fff;
  border-bottom: 1px solid var(--border);
}

.home-trust-icons-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 22px 24px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.home-trust-cell {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.home-trust-ico {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink);
  border: 1px solid var(--border);
}

.home-trust-ico svg {
  width: 22px;
  height: 22px;
}

.home-trust-text strong {
  display: block;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 4px;
  color: var(--ink);
}

.home-trust-text p {
  margin: 0;
  font-size: 12px;
  line-height: 1.45;
  color: var(--muted);
}

.home-main {
  flex: 1;
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px 24px 72px;
  display: flex;
  flex-direction: column;
  gap: 56px;
}

/* Hai khối spotlight */
.home-spotlight {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.home-spotlight-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  min-height: 200px;
  border: 1px solid var(--border);
  background: #fff;
  cursor: pointer;
  overflow: hidden;
  transition: transform var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}

.home-spotlight-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.1);
}

.home-spotlight-media {
  min-height: 200px;
  overflow: hidden;
  background: #f3f4f6;
}

.home-spotlight-media img {
  width: 100%;
  height: 100%;
  min-height: 200px;
  object-fit: cover;
  transition: transform 0.5s var(--ease);
}

.home-spotlight-card:hover .home-spotlight-media img {
  transform: scale(1.06);
}

.home-spotlight-body {
  padding: 20px 20px 22px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.home-spotlight-eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--muted);
  margin: 0 0 8px;
}

.home-spotlight-title {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0 0 8px;
  line-height: 1.25;
}

.home-spotlight-desc {
  font-size: 13px;
  line-height: 1.5;
  color: #4b5563;
  margin: 0 0 12px;
}

.home-spotlight-cta {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ink);
}

.home-spotlight-card:hover .home-spotlight-cta {
  text-decoration: underline;
}

/* Section headers */
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.section-eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--muted);
  margin: 0 0 6px;
}

.section-title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0;
}

.section-title.accent {
  color: var(--accent);
}

.section-link-all {
  font-family: inherit;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--ink);
  background: none;
  border: none;
  border-bottom: 2px solid var(--ink);
  padding: 0 0 2px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.2s ease;
}

.section-link-all:hover {
  opacity: 0.7;
}

.section-link-arrow {
  display: inline-block;
  transition: transform 0.25s var(--ease);
}

.section-link-all:hover .section-link-arrow {
  transform: translateX(4px);
}

.home-section {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.home-section--band {
  padding: 28px 20px 32px;
  margin: 0 -20px;
  background: var(--surface-muted);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.home-loading,
.home-error {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

.home-error {
  color: #b91c1c;
}

/* Product grid from API */
.home-product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.home-product-card {
  position: relative;
  background: #fff;
  border: 1px solid var(--border);
  cursor: pointer;
  overflow: hidden;
  transition: transform var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}

.home-product-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    105deg,
    transparent 40%,
    rgba(255, 255, 255, 0.45) 50%,
    transparent 60%
  );
  transform: translateX(-100%);
  transition: transform 0.6s var(--ease);
  pointer-events: none;
}

.home-product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.12);
}

.home-product-card:hover::after {
  transform: translateX(100%);
}

.home-product-media {
  aspect-ratio: 4 / 5;
  overflow: hidden;
  background: #f3f4f6;
}

.home-product-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s var(--ease);
}

.home-product-card:hover .home-product-media img {
  transform: scale(1.06);
}

.home-product-body {
  padding: 14px 14px 18px;
}

.home-product-brand {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--muted);
  margin: 0 0 4px;
}

.home-product-name {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.35;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.home-product-price {
  font-size: 15px;
  font-weight: 800;
  margin: 0;
  color: var(--ink);
}

/* Category / brand tiles */
.home-grid {
  display: grid;
  gap: 16px;
}

.home-grid--4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.home-grid--3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.home-tile {
  position: relative;
  cursor: pointer;
  border: 1px solid var(--border);
  background: #fff;
  overflow: hidden;
  transition: transform var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}

.home-tile:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.1);
}

.home-tile-media {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #f3f4f6;
}

.home-tile-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s var(--ease);
}

.home-tile:hover .home-tile-media img {
  transform: scale(1.05);
}

.home-tile--brand .home-tile-media {
  aspect-ratio: 16 / 10;
}

.home-tile-title {
  padding: 12px 14px 16px;
  font-size: 14px;
  font-weight: 700;
  margin: 0;
}

/* Bộ sưu tập: sản phẩm API + nghiêng nhẹ + vào trang + hover */
.home-collection-empty {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

.home-link-inline {
  font: inherit;
  font-weight: 600;
  color: var(--ink);
  background: none;
  border: none;
  border-bottom: 1px solid var(--ink);
  padding: 0;
  cursor: pointer;
}

.home-link-inline:hover {
  opacity: 0.75;
}

.home-collection-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  perspective: 900px;
}

.home-collection-card {
  cursor: pointer;
  outline: none;
  animation: home-collection-enter 0.75s var(--ease) backwards;
}

.home-collection-card--1 {
  animation-delay: 0.06s;
}

.home-collection-card--2 {
  animation-delay: 0.16s;
}

.home-collection-card--3 {
  animation-delay: 0.26s;
}

@keyframes home-collection-enter {
  from {
    opacity: 0;
    transform: translateY(36px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.home-collection-card-inner {
  position: relative;
  height: 100%;
  border: 1px solid var(--border);
  background: #fff;
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.07);
  transition: transform 0.45s var(--ease), box-shadow 0.45s var(--ease);
  transform-style: preserve-3d;
}

.home-collection-card--1 .home-collection-card-inner {
  transform: rotate(-2.5deg);
}

.home-collection-card--2 .home-collection-card-inner {
  transform: rotate(0.5deg) translateY(4px);
}

.home-collection-card--3 .home-collection-card-inner {
  transform: rotate(2.5deg);
}

.home-collection-card:hover .home-collection-card-inner,
.home-collection-card:focus-visible .home-collection-card-inner {
  transform: translateY(-10px) rotate(0deg) translateZ(0);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.14);
}

.home-collection-media {
  position: relative;
  aspect-ratio: 4 / 5;
  overflow: hidden;
  background: #f3f4f6;
}

.home-collection-media::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    105deg,
    transparent 38%,
    rgba(255, 255, 255, 0.35) 48%,
    transparent 58%
  );
  transform: translateX(-120%);
  transition: transform 0.75s var(--ease);
  pointer-events: none;
}

.home-collection-card:hover .home-collection-media::after {
  transform: translateX(120%);
}

.home-collection-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.55s var(--ease);
}

.home-collection-card:hover .home-collection-media img {
  transform: scale(1.07);
}

.home-collection-body {
  padding: 14px 16px 18px;
}

.home-collection-brand {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--muted);
  margin: 0 0 6px;
}

.home-collection-name {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.home-collection-price {
  font-size: 15px;
  font-weight: 800;
  margin: 0 0 6px;
  color: var(--ink);
}

.home-collection-cat {
  font-size: 11px;
  color: var(--muted);
  margin: 0;
}

@media (prefers-reduced-motion: reduce) {
  .home-collection-card {
    animation: none;
  }

  .home-collection-card-inner,
  .home-collection-media img,
  .home-collection-media::after {
    transition: none;
  }

  .home-collection-card--1 .home-collection-card-inner,
  .home-collection-card--2 .home-collection-card-inner,
  .home-collection-card--3 .home-collection-card-inner {
    transform: none;
  }

  .home-collection-card:hover .home-collection-card-inner,
  .home-collection-card:focus-visible .home-collection-card-inner {
    transform: none;
  }

  .home-collection-card:hover .home-collection-media img {
    transform: none;
  }
}

.home-section--video .home-video-banner {
  border: 1px solid var(--border);
  overflow: hidden;
  transition: transform var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}

.home-section--video .home-video-banner:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.1);
}

.home-section--video .home-video-banner img {
  width: 100%;
  display: block;
  object-fit: cover;
  max-height: 420px;
}

@media (max-width: 1024px) {
  .home-hero-dark-inner {
    grid-template-columns: 1fr;
    gap: 32px;
    min-height: 0;
  }

  .home-hero-lookbook {
    height: clamp(320px, 42vh, 400px);
    min-height: 300px;
    max-height: 400px;
    max-width: min(480px, 100%);
    margin: 0 auto;
  }

  .home-lookbook-img {
    max-height: min(360px, 48vh);
    height: 90%;
  }

  .home-lookbook-img--b {
    left: 26%;
  }

  .home-trust-icons-inner {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .home-spotlight {
    grid-template-columns: 1fr;
  }

  .home-spotlight-card {
    grid-template-columns: 1fr 1fr;
  }

  .home-product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .home-grid--4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .home-collection-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

}

@media (max-width: 640px) {
  .home-main {
    padding: 28px 16px 56px;
  }

  .home-hero-dark {
    padding: 36px 16px 40px;
  }

  .home-hero-copy {
    text-align: center;
  }

  .home-hero-actions {
    justify-content: center;
  }

  .home-hero-sub {
    margin-left: auto;
    margin-right: auto;
  }

  .home-hero-lookbook {
    height: clamp(280px, 38vh, 340px);
    min-height: 260px;
    max-height: 340px;
    max-width: 100%;
  }

  .home-lookbook-img {
    max-height: min(300px, 44vh);
    height: 88%;
  }

  .home-lookbook-img--a {
    transform: translateY(-50%) rotate(-3.5deg);
  }

  .home-lookbook-img--b {
    left: 22%;
    transform: translateY(-50%) rotate(3.5deg);
  }

  .home-spotlight-card {
    grid-template-columns: 1fr;
  }

  .home-spotlight-media {
    min-height: 180px;
  }

  .home-product-grid {
    grid-template-columns: 1fr;
  }

  .home-grid--3,
  .home-grid--4 {
    grid-template-columns: 1fr;
  }

  .home-collection-grid {
    grid-template-columns: 1fr;
  }

  .irun-promo-bar {
    font-size: 9px;
    line-height: 1.5;
  }
}
</style>
