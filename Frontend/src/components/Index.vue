<template>
  <div class="irun-home home-page">

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
          <!-- Dynamic Banner Slider in Hero -->
          <TransitionGroup v-if="activeBanners.length > 0" name="hero-slide-fade">
            <div
              v-for="(b, idx) in activeBanners"
              v-show="currentBannerIdx === idx"
              :key="b.id"
              class="home-hero-slide"
            >
              <a :href="b.linkUrl || '#'" @click.prevent="b.linkUrl ? null : $router.push('/product')">
                <img :src="toDisplayUrl(b.imageUrl)" :alt="b.title" class="home-hero-photo" />
              </a>
            </div>
          </TransitionGroup>

          <!-- Fallback (nếu không có banner nào trong DB) -->
          <template v-else>
            <a href="#" class="home-lookbook-img home-lookbook-img--a" @click.prevent="$router.push('/product')">
              <img :src="p1" alt="Kesn Store" class="home-lookbook-photo" />
            </a>
            <a href="#" class="home-lookbook-img home-lookbook-img--b" @click.prevent="$router.push('/product')">
              <img :src="p2" alt="Kesn Store" class="home-lookbook-photo" />
            </a>
          </template>
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
        <header class="section-head section-head--featured">
          <div class="section-head-text">
            <p class="section-eyebrow">Mới nhất</p>
            <h2 class="section-title">Hàng mới &amp; nổi bật</h2>
          </div>
          <div class="section-head-actions">
            <div
              v-if="!featuredLoading && !featuredError && featuredList.length > 4"
              class="home-rail-nav"
              role="group"
              aria-label="Chuyển nhóm sản phẩm (4 sản phẩm mỗi lần)"
            >
              <button
                type="button"
                class="rail-btn"
                aria-label="Nhóm sản phẩm trước"
                :disabled="featuredPageStart <= 0"
                @click="featuredPrev"
              >
                ‹
              </button>
              <button
                type="button"
                class="rail-btn"
                aria-label="Nhóm sản phẩm sau"
                :disabled="featuredPageStart >= featuredPageMaxStart"
                @click="featuredNext"
              >
                ›
              </button>
            </div>
            <button type="button" class="section-link-all" @click="$router.push('/product')">
              Xem tất cả
              <span class="section-link-arrow" aria-hidden="true">→</span>
            </button>
          </div>
        </header>

        <p v-if="featuredLoading" class="home-loading">Đang tải sản phẩm…</p>
        <p v-else-if="featuredError" class="home-error">{{ featuredError }}</p>
        <p v-else-if="featuredList.length === 0" class="home-loading">Chưa có sản phẩm nổi bật.</p>
        <div v-else class="home-featured-page" role="region" aria-label="Bốn sản phẩm mới nhất mỗi trang">
          <div class="home-featured-viewport">
            <Transition :name="featuredPageTransition" mode="out-in">
              <div :key="featuredPageStart" class="home-featured-grid">
                <article
                  v-for="item in featuredPageItems"
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
            </Transition>
          </div>
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
            @click="$router.push({ path: '/product', query: { category: 'Bóng bầu dục' } })"
          >
            <div class="home-tile-media">
              <img :src="t3" alt="Bóng bầu dục" />
            </div>
            <h3 class="home-tile-title">Bóng bầu dục</h3>
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
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
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
import client from '../api/client'
import { getProductsFromApi } from '../api/services/productService'

const router = useRouter()

const FEATURED_PER_PAGE = 4
const FEATURED_MAX_ITEMS = 40

const featuredPageStart = ref(0)
/** 1 = sang nhóm sau, -1 = nhóm trước — dùng cho hướng animation */
const featuredNavDir = ref(1)

const featuredPageTransition = computed(() =>
  featuredNavDir.value >= 0 ? 'home-featured-next' : 'home-featured-prev'
)

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

const featuredList = computed(() =>
  featuredRaw.value.slice(0, FEATURED_MAX_ITEMS).map((p) => ({
    id: p.id,
    image: getPrimaryImage(p.imageUrl),
    brand: p.brand,
    name: p.name,
    price: p.price,
  }))
)

const featuredPageMaxStart = computed(() =>
  Math.max(0, featuredList.value.length - FEATURED_PER_PAGE)
)

const featuredPageItems = computed(() => {
  const start = featuredPageStart.value
  return featuredList.value.slice(start, start + FEATURED_PER_PAGE)
})

function featuredPrev() {
  featuredNavDir.value = -1
  featuredPageStart.value = Math.max(0, featuredPageStart.value - FEATURED_PER_PAGE)
}

function featuredNext() {
  featuredNavDir.value = 1
  const maxS = featuredPageMaxStart.value
  featuredPageStart.value = Math.min(maxS, featuredPageStart.value + FEATURED_PER_PAGE)
}

watch(featuredList, () => {
  const maxS = Math.max(0, featuredList.value.length - FEATURED_PER_PAGE)
  if (featuredPageStart.value > maxS) featuredPageStart.value = maxS
})

/** Ưu tiên 1 SP / danh mục — thiếu thì lấy thêm từ danh sách */
const COLLECTION_CATS = ['Gym', 'Đá banh', 'Basketball']

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

const activeBanners = ref([])
const currentBannerIdx = ref(0)
let bannerTimer = null

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
})

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

  // Load banners
  try {
    const res = await client.get('banners')
    activeBanners.value = res.data || []
    if (activeBanners.value.length > 1) {
      bannerTimer = setInterval(() => {
        currentBannerIdx.value = (currentBannerIdx.value + 1) % activeBanners.value.length
      }, 2500)
    }
  } catch (e) {
    console.error('Không tải được banner:', e)
  }
})
</script>

<style src="../assets/css/home.css" scoped></style>

