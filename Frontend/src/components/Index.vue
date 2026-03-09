<template>
  <div class="home-page">
    <main class="home-main">
      <!-- Hero -->
      <section class="home-hero">
        <div class="home-hero-images">
          <img :src="p1" alt="Hero left" class="home-hero-image" />
          <img :src="p2" alt="Hero right" class="home-hero-image" />
        </div>
        <div class="home-hero-text">
          <p class="home-hero-kesn">Kesn Store</p>
          <h1 class="home-hero-title">JUST YOUR SHOES.</h1>
          <p class="home-hero-sub">
            Giày không chỉ để đi – là để thể hiện chính mình
          </p>
        </div>
      </section>

      <!-- Thể thao -->
      <section class="home-section">
        <header class="home-section-header">
          <h2 class="home-section-title">Thể thao</h2>
          <p class="home-section-subtitle">Khám phá các dòng giày cho mọi môn thể thao</p>
        </header>

        <div class="home-grid home-grid--4">
          <article class="home-card" @click="$router.push({ path: '/product', query: { category: 'Basketball' } })">
            <img :src="t1" alt="Basketball" class="home-card-image" />
            <h3 class="home-card-title">Basketball</h3>
          </article>
          <article class="home-card" @click="$router.push({ path: '/product', query: { category: 'Đá banh' } })">
            <img :src="t2" alt="Football" class="home-card-image" />
            <h3 class="home-card-title">Football</h3>
          </article>
          <article class="home-card" @click="$router.push({ path: '/product', query: { category: 'Baseball' } })">
            <img :src="t3" alt="Baseball" class="home-card-image" />
            <h3 class="home-card-title">Baseball</h3>
          </article>
          <article class="home-card" @click="$router.push({ path: '/product', query: { category: 'Gym' } })">
            <img :src="t4" alt="Athletics running" class="home-card-image" />
            <h3 class="home-card-title">Athletics running</h3>
          </article>
        </div>
      </section>

      <!-- Mua theo thương hiệu -->
      <section class="home-section">
        <header class="home-section-header">
          <h2 class="home-section-title">Mua theo thương hiệu</h2>
          <p class="home-section-subtitle">Nike, Adidas, Balenciaga và nhiều hơn nữa</p>
        </header>

        <div class="home-grid home-grid--4">
          <article class="home-card home-card--brand" @click="$router.push({ path: '/product', query: { brand: 'Nike' } })">
            <img :src="ig5" alt="Nike" class="home-card-image" />
            <h3 class="home-card-title">Nike</h3>
          </article>
          <article class="home-card home-card--brand" @click="$router.push({ path: '/product', query: { brand: 'Adidas' } })">
            <img :src="ig6" alt="Adidas" class="home-card-image" />
            <h3 class="home-card-title">Adidas</h3>
          </article>
          <article class="home-card home-card--brand" @click="$router.push({ path: '/product', query: { brand: 'Balenciaga' } })">
            <img :src="ig7" alt="Balenciaga" class="home-card-image" />
            <h3 class="home-card-title">Balenciaga</h3>
          </article>
          <article class="home-card home-card--brand" @click="$router.push('/product')">
            <img :src="ig8" alt="Nhiều mẫu khác" class="home-card-image" />
            <h3 class="home-card-title">Nhiều mẫu khác</h3>
          </article>
        </div>
      </section>

      <!-- Video / Banner -->
      <section class="home-section home-section--video">
        <header class="home-section-header">
          <h2 class="home-section-title accent">Đừng bỏ lỡ những mẫu mới nhất</h2>
        </header>

        <div class="home-video-banner">
          <img :src="banner" alt="Banner" />
        </div>
      </section>

      <!-- Bộ sưu tập -->
      <section class="home-section">
        <header class="home-section-header">
          <h2 class="home-section-title">Bộ sưu tập nổi bật</h2>
        </header>

        <div class="home-grid home-grid--3">
          <article class="home-card" @click="$router.push({ path: '/product', query: { category: 'Giày cao gót' } })">
            <img :src="q1" alt="Giày cao gót" class="home-card-image" />
            <h3 class="home-card-title">Giày cao gót</h3>
          </article>
          <article class="home-card" @click="$router.push({ path: '/product', query: { category: 'Dép' } })">
            <img :src="q2" alt="Dép" class="home-card-image" />
            <h3 class="home-card-title">Dép</h3>
          </article>
          <article class="home-card" @click="$router.push({ path: '/product', query: { category: 'Dép lười' } })">
            <img :src="q3" alt="Dép lười" class="home-card-image" />
            <h3 class="home-card-title">Dép lười</h3>
          </article>
        </div>
      </section>
    </main>

    <!-- Footer -->
    <footer class="home-footer">
      <div class="home-footer-columns">
        <div class="home-footer-col">
          <h3>Sản phẩm</h3>
          <ul>
            <li>Giày</li>
            <li>Giày lười</li>
            <li>Dép</li>
            <li>Dép lười</li>
            <li>Cao gót</li>
            <li>Ủng</li>
          </ul>
        </div>
        <div class="home-footer-col">
          <h3>Thương hiệu</h3>
          <ul>
            <li>Puma</li>
            <li>Nike</li>
            <li>Stussy</li>
            <li>Balenciaga</li>
            <li>Christian Louboutin</li>
          </ul>
        </div>
        <div class="home-footer-col">
          <h3>Thông tin</h3>
          <ul></ul>
        </div>
        <div class="home-footer-col">
          <h3>Theo dõi chúng tôi</h3>
          <ul>
            <li>Instagram</li>
            <li>Facebook</li>
            <li>Telegram</li>
          </ul>
        </div>
      </div>
      <p class="home-footer-copy">© {{ new Date().getFullYear() }} Kesn Store. JUST YOUR SHOES.</p>
    </footer>
  </div>
</template>

<script>
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
import q1 from './image/q1.png'
import q2 from './image/q2.png'
import q3 from './image/q3.png'

export default {
  name: 'Index',
  data() {
    return {
      banner,
      p1,
      p2,
      t1,
      t2,
      t3,
      t4,
      ig5,
      ig6,
      ig7,
      ig8,
      q1,
      q2,
      q3,
    }
  },
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
}

.home-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 64px;
  padding: 40px 48px 80px;
}

.home-hero {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.home-hero-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  width: 100%;
}

.home-hero-image {
  width: 100%;
  height: auto;
  border-radius: 24px;
  object-fit: cover;
}

.home-hero-text {
  text-align: center;
  max-width: 100%;
}

.home-hero-kesn {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
}

.home-hero-title {
  font-size: 56px;
  font-weight: 900;
  font-style: italic;
  margin-bottom: 16px;
}

.home-hero-sub {
  font-size: 16px;
  color: #4b5563;
}

.home-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.home-section-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.home-section-title {
  font-size: 28px;
  font-weight: 700;
}

.home-section-title.accent {
  color: #ef4444;
}

.home-section-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.home-grid {
  display: grid;
  gap: 20px;
}

.home-grid--4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.home-grid--3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.home-card {
  border-radius: 18px;
  overflow: hidden;
  background-color: #f9fafb;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.home-card:hover {
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.12);
}

.home-card-placeholder {
  width: 100%;
  height: 220px;
  background-color: #e5e7eb;
}

.home-card-title {
  padding: 12px 16px 16px;
  font-size: 16px;
  font-weight: 600;
}

.home-card--brand .home-card-placeholder {
  height: 160px;
}

.home-section--video .home-video-banner img {
  width: 100%;
  border-radius: 24px;
  object-fit: cover;
  max-height: 420px;
}

.home-footer {
  border-top: 1px solid #e5e7eb;
  padding: 32px 48px 24px;
  background-color: #f3f4f6;
}

.home-footer-columns {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 32px;
  margin-bottom: 24px;
}

.home-footer-col h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.home-footer-col ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: #4b5563;
}

.home-footer-copy {
  font-size: 13px;
  color: #6b7280;
  text-align: center;
}

@media (max-width: 1024px) {
  .home-main {
    padding: 32px 20px 64px;
  }

  .home-hero-images {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .home-grid--4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .home-footer-columns {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .home-main {
    padding: 24px 16px 48px;
  }

  .home-hero-title {
    font-size: 36px;
  }

  .home-grid--3,
  .home-grid--4 {
    grid-template-columns: minmax(0, 1fr);
  }

  .home-footer-columns {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

