<template>
  <div class="orders-page">
    <header class="orders-head">
      <div>
        <h2 class="orders-title">Lịch sử đơn hàng</h2>
        <p class="orders-sub">Theo dõi và xem chi tiết các đơn đã đặt</p>
      </div>
      <span v-if="!loading && orders.length > 0" class="orders-badge">{{ orders.length }} đơn</span>
    </header>

    <section v-if="loading" class="state state--loading">
      <div class="skeleton-list">
        <div v-for="n in 3" :key="n" class="skeleton-card" />
      </div>
    </section>

    <section v-else-if="orders.length === 0" class="state state--empty">
      <div class="empty-visual" aria-hidden="true">
        <span class="empty-icon">📦</span>
      </div>
      <h3 class="empty-title">Chưa có đơn hàng</h3>
      <p class="empty-text">Khi bạn đặt mua, đơn sẽ hiển thị tại đây.</p>
      <router-link to="/product" class="btn-shop">Khám phá sản phẩm</router-link>
    </section>

    <section v-else class="orders-list">
      <router-link
        v-for="o in orders"
        :key="o.id"
        :to="{ name: 'OrderDetail', params: { id: String(o.id) } }"
        class="order-card"
      >
        <div class="order-card-accent" :class="o.status" aria-hidden="true" />
        <div class="order-card-body">
          <div class="order-top">
            <div class="order-ids">
              <span class="order-label">Mã đơn</span>
              <span class="order-id">#{{ o.id }}</span>
            </div>
            <span class="order-status" :class="o.status">{{ statusText(o.status) }}</span>
          </div>
          <p class="order-date">
            <span class="date-label">Ngày đặt</span>
            {{ formatDate(o.createdAt) }}
          </p>
          <ul v-if="(o.items || []).length" class="order-preview">
            <li v-for="(item, i) in previewItems(o)" :key="i" class="preview-line">
              <span class="preview-name">{{ item.productName || item.name }}</span>
              <span class="preview-qty">×{{ item.quantity || item.qty }}</span>
            </li>
            <li v-if="(o.items || []).length > 2" class="preview-more">
              +{{ (o.items || []).length - 2 }} sản phẩm khác
            </li>
          </ul>
          <div class="order-bottom">
            <div class="order-total">
              <span class="total-label">Tổng cộng</span>
              <strong class="total-num">{{ formatPrice(o.total) }} <span class="currency">VNĐ</span></strong>
            </div>
            <span class="order-cta">Xem chi tiết <span class="arrow">→</span></span>
          </div>
        </div>
      </router-link>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { resolveSessionUserId } from '../authStore'
import { getOrdersByUser } from '../api/services/orderService'

const orders = ref([])
const loading = ref(true)

function formatPrice(n) {
  return Number(n).toLocaleString('vi-VN')
}

function formatDate(d) {
  if (!d) return '—'
  const s = typeof d === 'string' ? d : d?.toString?.() || ''
  if (!s) return '—'
  try {
    const date = new Date(s)
    if (!Number.isNaN(date.getTime())) {
      return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      })
    }
  } catch {
    /* fall through */
  }
  return s.slice(0, 10)
}

function statusText(s) {
  const map = {
    pending: 'Chờ xử lý',
    paid: 'Đã thanh toán',
    shipping: 'Đang giao',
    delivered: 'Đã giao',
    cancelled: 'Đã hủy',
  }
  return map[s] || s
}

function previewItems(o) {
  const items = o.items || []
  return items.slice(0, 2)
}

onMounted(async () => {
  loading.value = true
  try {
    const userId = resolveSessionUserId()
    if (Number.isFinite(userId)) {
      orders.value = await getOrdersByUser(userId)
    } else {
      orders.value = []
    }
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.orders-page {
  font-family: 'Inter', -apple-system, system-ui, sans-serif;
  color: #0f172a;
}

.orders-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.orders-title {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0 0 6px;
}

.orders-sub {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.45;
}

.orders-badge {
  font-size: 12px;
  font-weight: 700;
  padding: 8px 14px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.state--loading {
  padding: 8px 0 16px;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.skeleton-card {
  height: 132px;
  border-radius: 16px;
  background: linear-gradient(90deg, #f1f5f9 0%, #e8ecf1 50%, #f1f5f9 100%);
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

@keyframes shimmer {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: -100% 0;
  }
}

.state--empty {
  text-align: center;
  padding: 48px 24px 52px;
  border-radius: 20px;
  background: linear-gradient(180deg, #fafbfc 0%, #f4f6f8 100%);
  border: 1px dashed #cbd5e1;
}

.empty-visual {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.empty-icon {
  font-size: 32px;
  line-height: 1;
  opacity: 0.85;
}

.empty-title {
  font-size: 18px;
  font-weight: 800;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.empty-text {
  margin: 0 0 24px;
  font-size: 14px;
  color: #64748b;
  max-width: 280px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.5;
}

.btn-shop {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 22px;
  border-radius: 14px;
  background: #0f172a;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid #0f172a;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.22);
  transition: background 0.15s, transform 0.15s;
}

.btn-shop:hover {
  background: #020617;
}

.btn-shop:active {
  transform: scale(0.98);
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  display: flex;
  position: relative;
  text-decoration: none;
  color: inherit;
  border-radius: 18px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 8px 28px rgba(15, 23, 42, 0.06);
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;
}

.order-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.1);
  transform: translateY(-1px);
}

.order-card-accent {
  width: 4px;
  flex-shrink: 0;
  background: #cbd5e1;
}

.order-card-accent.pending {
  background: linear-gradient(180deg, #fbbf24, #f59e0b);
}

.order-card-accent.paid {
  background: linear-gradient(180deg, #818cf8, #6366f1);
}

.order-card-accent.shipping {
  background: linear-gradient(180deg, #60a5fa, #3b82f6);
}

.order-card-accent.delivered {
  background: linear-gradient(180deg, #34d399, #10b981);
}

.order-card-accent.cancelled {
  background: linear-gradient(180deg, #fb7185, #e11d48);
}

.order-card-body {
  flex: 1;
  min-width: 0;
  padding: 18px 20px 16px;
}

.order-top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.order-ids {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #94a3b8;
}

.order-id {
  font-size: 18px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.order-status {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.order-status.pending {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

.order-status.paid {
  background: #eef2ff;
  color: #4338ca;
  border: 1px solid #c7d2fe;
}

.order-status.shipping {
  background: #eff6ff;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.order-status.delivered {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.order-status.cancelled {
  background: #fff1f2;
  color: #be123c;
  border: 1px solid #ffe4e6;
}

.order-date {
  margin: 0 0 14px;
  font-size: 13px;
  color: #64748b;
}

.date-label {
  font-weight: 600;
  color: #94a3b8;
  margin-right: 8px;
}

.order-preview {
  list-style: none;
  margin: 0 0 16px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
}

.preview-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  padding: 4px 0;
  color: #334155;
}

.preview-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-qty {
  flex-shrink: 0;
  color: #64748b;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.preview-more {
  font-size: 12px;
  color: #94a3b8;
  padding-top: 6px;
  margin-top: 4px;
  border-top: 1px solid #e2e8f0;
}

.order-bottom {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #e2e8f0;
}

.total-label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #94a3b8;
  margin-bottom: 4px;
}

.total-num {
  font-size: 17px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.currency {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-left: 2px;
}

.order-cta {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: color 0.15s;
}

.order-card:hover .order-cta {
  color: #0f172a;
}

.arrow {
  transition: transform 0.2s;
}

.order-card:hover .arrow {
  transform: translateX(3px);
}
</style>
