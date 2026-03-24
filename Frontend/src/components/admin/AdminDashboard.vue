<template>
  <div class="admin-dashboard">
    <h1>Dashboard</h1>
    <p class="sub">Quản lý Kesn Store</p>

    <section v-if="loading" class="loading">Đang tải...</section>
    <section v-else-if="loadError" class="error">{{ loadError }}</section>

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
      <div class="stat-card">
        <span class="stat-value">{{ formatMoney(report.summary.totalRevenue) }}</span>
        <span class="stat-label">Tổng doanh thu (mọi đơn)</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ formatMoney(report.summary.deliveredRevenue) }}</span>
        <span class="stat-label">Doanh thu đã giao</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ report.summary.deliveredOrders }}</span>
        <span class="stat-label">Số đơn đã giao</span>
      </div>
    </div>

    <div class="block">
      <h2>Chào mừng Admin</h2>
      <p>Dữ liệu thống kê được lấy từ database qua API.</p>
    </div>

    <div v-if="!loading" class="report-grid">
      <section class="block">
        <h2>Doanh thu theo loại sản phẩm</h2>
        <table class="table" v-if="report.revenueByCategory.length">
          <thead>
            <tr>
              <th>Loại</th>
              <th>Số lượng</th>
              <th>Doanh thu</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in report.revenueByCategory" :key="row.category">
              <td>{{ row.category }}</td>
              <td>{{ row.quantity }}</td>
              <td>{{ formatMoney(row.revenue) }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">Chưa có dữ liệu.</p>
      </section>

      <section class="block">
        <h2>Doanh thu theo sản phẩm</h2>
        <table class="table" v-if="report.revenueByProduct.length">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Số lượng</th>
              <th>Doanh thu</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in report.revenueByProduct" :key="row.productName">
              <td>{{ row.productName }}</td>
              <td>{{ row.quantity }}</td>
              <td>{{ formatMoney(row.revenue) }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">Chưa có dữ liệu.</p>
      </section>

      <section class="block report-full">
        <h2>Doanh thu theo khách hàng</h2>
        <table class="table" v-if="report.revenueByCustomer.length">
          <thead>
            <tr>
              <th>Khách hàng</th>
              <th>Email</th>
              <th>Số đơn</th>
              <th>Doanh thu</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in report.revenueByCustomer" :key="row.customerKey">
              <td>{{ row.customerName || 'Khách vãng lai' }}</td>
              <td>{{ row.customerEmail || '—' }}</td>
              <td>{{ row.orders }}</td>
              <td>{{ formatMoney(row.revenue) }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">Chưa có dữ liệu.</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats, getRevenueReport } from '../../api/services/adminService'

const stats = ref({ products: 0, orders: 0, users: 0 })
const report = ref({
  summary: { totalOrders: 0, deliveredOrders: 0, totalRevenue: 0, deliveredRevenue: 0 },
  revenueByCategory: [],
  revenueByProduct: [],
  revenueByCustomer: [],
})
const loading = ref(true)
const loadError = ref('')

function formatMoney(n) {
  return Number(n || 0).toLocaleString('vi-VN') + ' VNĐ'
}

onMounted(async () => {
  try {
    loadError.value = ''
    stats.value = await getStats()
    report.value = await getRevenueReport(10)
  } catch (e) {
    const message = e.response?.data?.message || e.message || 'Không thể tải dữ liệu dashboard'
    loadError.value = `Lỗi tải dashboard: ${message}`
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

.error {
  padding: 16px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  border-radius: 12px;
  margin-bottom: 20px;
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

.report-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.report-full {
  grid-column: span 2;
}

.empty {
  color: #6b7280;
  font-size: 14px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.table th,
.table td {
  text-align: left;
  padding: 10px 8px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
}

.table th {
  color: #6b7280;
  font-weight: 700;
}

@media (max-width: 640px) {
  .stats {
    grid-template-columns: 1fr;
  }

  .report-grid {
    grid-template-columns: 1fr;
  }

  .report-full {
    grid-column: span 1;
  }
}
</style>
