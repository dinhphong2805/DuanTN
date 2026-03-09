<template>
  <div class="orders-page">
    <h2>Lịch sử đơn hàng</h2>

    <section v-if="loading" class="loading">Đang tải...</section>

    <section v-else-if="orders.length === 0" class="empty">
      <p>Chưa có đơn hàng nào.</p>
      <router-link to="/product" class="link">Mua sắm ngay</router-link>
    </section>

    <section v-else class="orders-list">
      <article v-for="o in orders" :key="o.id" class="order-card">
        <div class="order-header">
          <span class="order-id">#{{ o.id }}</span>
          <span class="order-date">{{ formatDate(o.createdAt) }}</span>
          <span class="order-status" :class="o.status">{{ statusText(o.status) }}</span>
        </div>
        <div class="order-items">
          <div v-for="(item, i) in (o.items || [])" :key="i" class="order-item">
            <span>{{ item.productName || item.name }} x{{ item.quantity || item.qty }}</span>
            <span>{{ formatPrice((item.unitPrice || item.price || 0) * (item.quantity || item.qty || 1)) }} VNĐ</span>
          </div>
        </div>
        <div class="order-footer">
          <strong>Tổng: {{ formatPrice(o.total) }} VNĐ</strong>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../authStore'
import { getOrdersByUser } from '../api/services/orderService'

const auth = useAuthStore()
const orders = ref([])
const loading = ref(true)

function formatPrice(n) {
  return Number(n).toLocaleString('vi-VN')
}

function formatDate(d) {
  if (!d) return '—'
  const s = typeof d === 'string' ? d : (d.toString && d.toString())
  return s ? s.slice(0, 10) : '—'
}

function statusText(s) {
  const map = { pending: 'Chờ xử lý', shipping: 'Đang giao', delivered: 'Đã giao' }
  return map[s] || s
}

onMounted(async () => {
  loading.value = true
  try {
    const userId = auth.state?.user?.id
    if (userId) {
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
.orders-page h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 20px;
}

.loading,
.empty {
  text-align: center;
  padding: 32px;
  color: #6b7280;
}

.link {
  color: #111827;
  font-weight: 600;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
  background: #fff;
}

.order-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.order-id {
  font-weight: 700;
}

.order-date {
  font-size: 14px;
  color: #6b7280;
}

.order-status {
  margin-left: auto;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.order-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.order-status.shipping {
  background: #dbeafe;
  color: #1e40af;
}

.order-status.delivered {
  background: #d1fae5;
  color: #065f46;
}

.order-items {
  font-size: 14px;
  color: #374151;
  margin-bottom: 10px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
}

.order-footer {
  border-top: 1px solid #e5e7eb;
  padding-top: 10px;
  font-size: 14px;
}
</style>
