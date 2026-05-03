<template>
  <footer class="app-footer irun-footer">
    <div class="irun-footer-cols app-footer-columns">
      <div class="app-footer-col">
        <h3>Thông tin</h3>
        <ul>
          <li><router-link to="/faq">FAQ</router-link></li>
          <li><router-link to="/contact">Liên hệ</router-link></li>
          <li><router-link to="/product">Sản phẩm</router-link></li>
        </ul>
      </div>
      <div class="app-footer-col">
        <h3>Danh mục</h3>
        <ul>
          <li><router-link to="/product">Tất cả giày</router-link></li>
          <li><router-link :to="{ path: '/product', query: { category: 'Gym' } }">Giày thể thao</router-link></li>
          <li><router-link :to="{ path: '/product', query: { category: 'Đá banh' } }">Đá banh</router-link></li>
          <li><router-link to="/cart">Giỏ hàng</router-link></li>
        </ul>
      </div>
      <div class="app-footer-col">
        <h3>Theo dõi</h3>
        <ul>
          <li><a href="#" @click.prevent>Instagram</a></li>
          <li>
            <a
              href="https://web.facebook.com/profile.php?id=61588954481444"
              target="_blank"
              rel="noopener noreferrer"
            >Facebook</a>
          </li>
          <li><a href="#" @click.prevent>Telegram</a></li>
        </ul>
      </div>
      <div class="app-footer-col">
        <h3>Kesn Store</h3>
        <p class="app-footer-blurb">
          Cửa hàng giày trực tuyến — JUST YOUR SHOES.
        </p>
      </div>
    </div>
    <p class="app-footer-copy">© {{ year }} Kesn Store. JUST YOUR SHOES.</p>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const year = computed(() => new Date().getFullYear())
</script>

<style scoped>
.app-footer.irun-footer {
  margin-top: auto;
  background: #0a0a0a;
  color: #e5e5e5;
  padding: 48px 24px 32px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.irun-footer-cols.app-footer-columns {
  max-width: 1280px;
  margin: 0 auto 28px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px;
}

.app-footer-col h3 {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
  margin: 0 0 16px;
}

.app-footer-col ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 14px;
}

.irun-footer-cols a,
.app-footer-col a {
  color: #d4d4d4;
  text-decoration: none;
  transition: color 0.2s ease;
}

.irun-footer-cols a:hover,
.app-footer-col a:hover {
  color: #fff;
}

.app-footer-blurb {
  font-size: 13px;
  line-height: 1.55;
  color: #a3a3a3;
  margin: 0;
}

.app-footer-copy {
  max-width: 1280px;
  margin: 0 auto;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: #737373;
  text-align: center;
  text-transform: uppercase;
}

@media (max-width: 1024px) {
  .irun-footer-cols.app-footer-columns {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .irun-footer-cols.app-footer-columns {
    grid-template-columns: 1fr;
  }
}
</style>
