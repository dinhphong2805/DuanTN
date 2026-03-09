<template>
  <div class="admin-orders">
    <h1>Đơn hàng</h1>

    <section v-if="loading" class="loading">Đang tải...</section>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Khách hàng</th>
            <th>Ngày</th>
            <th>Trạng thái</th>
            <th>Tổng</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id">
            <td>#{{ o.id }}</td>
            <td>{{ o.customerName || o.customerEmail || '—' }}</td>
            <td>{{ formatDate(o.createdAt) }}</td>
            <td>
              <select v-model="o.status" @change="updateStatus(o)" class="status-select" :class="o.status">
                <option value="pending">Chờ xử lý</option>
                <option value="shipping">Đang giao</option>
                <option value="delivered">Đã giao</option>
              </select>
            </td>
            <td>{{ formatPrice(o.total) }} VNĐ</td>
            <td>
              <button type="button" class="btn-sm" @click="selectedOrder = o">Chi tiết</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="!loading && orders.length === 0" class="empty">Chưa có đơn hàng.</p>

    <!-- Modal chi tiết -->
    <div v-if="selectedOrder" class="modal-overlay" @click.self="selectedOrder = null">
      <div class="modal">
        <h3>Đơn hàng #{{ selectedOrder.id }}</h3>
        <div class="detail">
          <p><strong>Khách hàng:</strong> {{ selectedOrder.customerName }}</p>
          <p><strong>Email:</strong> {{ selectedOrder.customerEmail }}</p>
          <p><strong>SĐT:</strong> {{ selectedOrder.customerPhone }}</p>
          <p><strong>Địa chỉ:</strong> {{ selectedOrder.address }}</p>
          <p><strong>Trạng thái:</strong> {{ statusText(selectedOrder.status) }}</p>
          <p><strong>Tổng:</strong> {{ formatPrice(selectedOrder.total) }} VNĐ</p>
        </div>
        <div class="order-items">
          <h4>Sản phẩm</h4>
          <div v-for="(it, i) in (selectedOrder.items || [])" :key="i" class="item">
            {{ it.productName }} x{{ it.quantity }} — {{ formatPrice((it.unitPrice || 0) * (it.quantity || 1)) }} VNĐ
          </div>
        </div>
        <button type="button" class="btn-close" @click="selectedOrder = null">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders, updateOrderStatus } from '../../api/services/adminService'

const orders = ref([])
const loading = ref(true)
const selectedOrder = ref(null)

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN')
}

function formatDate(d) {
  if (!d) return '—'
  const s = typeof d === 'string' ? d : (d + '')
  return s.slice(0, 10)
}

function statusText(s) {
  const map = { pending: 'Chờ xử lý', shipping: 'Đang giao', delivered: 'Đã giao' }
  return map[s] || s
}

async function load() {
  loading.value = true
  try {
    orders.value = await getOrders()
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function updateStatus(o) {
  try {
    await updateOrderStatus(o.id, o.status)
  } catch {
    alert('Không thể cập nhật trạng thái')
  }
}

onMounted(load)
</script>

<style scoped>
.admin-orders h1 {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 20px;
}

.loading, .empty {
  padding: 24px;
  color: #6b7280;
}

.table-wrap {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.table th {
  background: #f9fafb;
  font-weight: 700;
}

.status-select {
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.status-select.pending { background: #fef3c7; color: #92400e; }
.status-select.shipping { background: #dbeafe; color: #1e40af; }
.status-select.delivered { background: #d1fae5; color: #065f46; }

.btn-sm {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  max-width: 480px;
  width: 90%;
}

.modal h3 { margin: 0 0 16px; }
.detail p { margin: 6px 0; font-size: 14px; }
.order-items { margin-top: 16px; }
.order-items h4 { margin: 0 0 8px; }
.item { padding: 4px 0; font-size: 14px; }
.btn-close {
  margin-top: 16px;
  padding: 8px 16px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>
