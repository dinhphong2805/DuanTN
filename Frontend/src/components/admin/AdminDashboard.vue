<template>
  <div class="admin-dashboard">
    <h1>Dashboard</h1>
    <p class="sub">Quản lý Kesn Store</p>

    <section v-if="loading" class="loading">Đang tải...</section>

    <div v-else class="stats">
      <div class="stat-card">
        <span class="stat-value">{{ stats.products }}</span>
        <span class="stat-label">Sản phẩm</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.orders }}</span>
        <span class="stat-label">Đơn hàng</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.users }}</span>
        <span class="stat-label">Người dùng</span>
      </div>
    </div>

    <div class="block">
      <h2>Chào mừng Admin</h2>
      <p>Dữ liệu thống kê được lấy từ database qua API.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '../../api/services/adminService'

const stats = ref({ products: 0, orders: 0, users: 0 })
const loading = ref(true)

onMounted(async () => {
  try {
    stats.value = await getStats()
  } catch {
    stats.value = { products: 0, orders: 0, users: 0 }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.admin-dashboard h1 {
  font-size: 24px;
  font-weight: 800;
}

.sub {
  font-size: 14px;
  color: #6b7280;
  margin: 4px 0 20px;
}

.loading {
  padding: 24px;
  color: #6b7280;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 20px;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 800;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.block {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 24px;
}

.block h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 8px;
}

.block p {
  font-size: 14px;
  color: #4b5563;
  margin: 0;
}

@media (max-width: 640px) {
  .stats {
    grid-template-columns: 1fr;
  }
}
</style>
