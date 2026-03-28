<template>
  <div class="orders-page">
    <header class="page-head">
      <div class="page-head__text">
        <h1>Quản lý đơn hàng</h1>
        <p class="page-sub">Theo dõi, lọc và cập nhật trạng thái giao hàng</p>
      </div>
      <div class="page-head__actions">
        <button type="button" class="btn-ghost" :disabled="loading" @click="load">
          <span class="btn-ico" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99" />
            </svg>
          </span>
          Làm mới
        </button>
        <button
          type="button"
          class="btn-primary"
          :disabled="loading || filteredSorted.length === 0"
          @click="exportCsv"
        >
          Xuất CSV
        </button>
      </div>
    </header>

    <!-- KPI -->
    <section v-if="!loading" class="kpi-grid" aria-label="Tổng quan đơn hàng">
      <article class="kpi-card">
        <span class="kpi-label">Tổng đơn</span>
        <strong class="kpi-value">{{ stats.total }}</strong>
      </article>
      <article class="kpi-card kpi-card--amber">
        <span class="kpi-label">Chờ xử lý</span>
        <strong class="kpi-value">{{ stats.pending }}</strong>
      </article>
      <article class="kpi-card kpi-card--blue">
        <span class="kpi-label">Đang giao</span>
        <strong class="kpi-value">{{ stats.shipping }}</strong>
      </article>
      <article class="kpi-card kpi-card--green">
        <span class="kpi-label">Đã giao</span>
        <strong class="kpi-value">{{ stats.delivered }}</strong>
      </article>
      <article class="kpi-card kpi-card--slate">
        <span class="kpi-label">Doanh thu (đã giao)</span>
        <strong class="kpi-value kpi-value--money">{{ formatPrice(stats.deliveredRevenue) }} ₫</strong>
      </article>
    </section>

    <!-- Toolbar -->
    <section v-if="!loading && orders.length" class="toolbar">
      <div class="toolbar__search">
        <span class="search-ico" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="11" cy="11" r="7" />
            <path d="M21 21l-4.35-4.35" stroke-linecap="round" />
          </svg>
        </span>
        <input
          v-model="searchQuery"
          type="search"
          class="search-input"
          placeholder="Tìm theo mã đơn, tên, email, SĐT…"
          autocomplete="off"
        />
      </div>
      <div class="toolbar__filters" role="group" aria-label="Lọc trạng thái">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          type="button"
          class="chip"
          :class="{ active: statusFilter === f.value }"
          @click="statusFilter = f.value; page = 1"
        >
          {{ f.label }}
          <span v-if="f.value !== 'all'" class="chip-count">{{ countByStatus(f.value) }}</span>
        </button>
      </div>
      <div class="toolbar__sort">
        <label class="sort-label" for="order-sort">Sắp xếp</label>
        <select id="order-sort" v-model="sortKey" class="sort-select">
          <option value="date_desc">Mới nhất</option>
          <option value="date_asc">Cũ nhất</option>
          <option value="total_desc">Giá cao → thấp</option>
          <option value="total_asc">Giá thấp → cao</option>
        </select>
      </div>
    </section>

    <!-- Loading -->
    <section v-if="loading" class="skeleton-panel" aria-busy="true">
      <div class="sk-line sk-line--title" />
      <div class="sk-table">
        <div v-for="n in 6" :key="n" class="sk-row" />
      </div>
    </section>

    <!-- Table -->
    <div v-else-if="orders.length" class="table-panel">
      <div class="table-panel__meta">
        <span>Hiển thị <strong>{{ pageStart }}–{{ pageEnd }}</strong> / {{ filteredSorted.length }} đơn</span>
        <div class="page-size">
          <label for="ps">Số dòng</label>
          <select id="ps" v-model.number="pageSize" class="sort-select sort-select--sm" @change="page = 1">
            <option :value="8">8</option>
            <option :value="12">12</option>
            <option :value="24">24</option>
          </select>
        </div>
      </div>
      <div class="table-scroll">
        <table class="data-table">
          <thead>
            <tr>
              <th class="th-id">Mã</th>
              <th>Khách hàng</th>
              <th>Liên hệ</th>
              <th>Ngày đặt</th>
              <th>Trạng thái</th>
              <th class="th-num">Tổng</th>
              <th class="th-act">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in pagedOrders" :key="o.id" class="data-row">
              <td class="mono">#{{ o.id }}</td>
              <td>
                <div class="cell-name">{{ o.customerName || '—' }}</div>
                <div v-if="o.customerEmail" class="cell-muted">{{ o.customerEmail }}</div>
              </td>
              <td class="cell-phone">{{ o.customerPhone || '—' }}</td>
              <td class="cell-date">{{ formatDateTime(o.createdAt) }}</td>
              <td>
                <span class="status-badge" :class="o.status">{{ statusText(o.status) }}</span>
                <select
                  v-model="o.status"
                  class="status-select"
                  :class="o.status"
                  @change="updateStatus(o)"
                >
                  <option value="pending">Chờ xử lý</option>
                  <option value="shipping">Đang giao</option>
                  <option value="delivered">Đã giao</option>
                </select>
              </td>
              <td class="th-num mono strong">{{ formatPrice(o.total) }} ₫</td>
              <td>
                <button type="button" class="btn-detail" @click="openDetail(o)">Chi tiết</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <nav v-if="totalPages > 1" class="pager" aria-label="Phân trang">
        <button type="button" class="pager-btn" :disabled="page <= 1" @click="page--">Trước</button>
        <span class="pager-info">Trang {{ page }} / {{ totalPages }}</span>
        <button type="button" class="pager-btn" :disabled="page >= totalPages" @click="page++">Sau</button>
      </nav>
    </div>

    <p v-else class="empty-state">
      <span class="empty-ico" aria-hidden="true">📦</span>
      Chưa có đơn hàng nào.
    </p>

    <!-- Toast -->
    <div v-if="toast.message" class="toast" :class="toast.type" role="status">
      {{ toast.message }}
    </div>

    <!-- Modal chi tiết -->
    <Teleport to="body">
      <div v-if="selectedOrder" class="modal-overlay" @click.self="selectedOrder = null">
        <div class="modal modal--wide">
          <header class="modal-head">
            <div>
              <h3>Đơn hàng <span class="mono">#{{ selectedOrder.id }}</span></h3>
              <p class="modal-sub">{{ formatDateTime(selectedOrder.createdAt) }}</p>
            </div>
            <span class="status-badge status-badge--lg" :class="selectedOrder.status">
              {{ statusText(selectedOrder.status) }}
            </span>
          </header>

          <div class="modal-grid">
            <section class="info-block">
              <h4>Khách hàng</h4>
              <p><strong>Tên:</strong> {{ selectedOrder.customerName || '—' }}</p>
              <p class="copy-row">
                <span><strong>Email:</strong> {{ selectedOrder.customerEmail || '—' }}</span>
                <button
                  v-if="selectedOrder.customerEmail"
                  type="button"
                  class="btn-mini"
                  @click="copyText(selectedOrder.customerEmail, 'Đã sao chép email')"
                >
                  Sao chép
                </button>
              </p>
              <p class="copy-row">
                <span><strong>SĐT:</strong> {{ selectedOrder.customerPhone || '—' }}</span>
                <button
                  v-if="selectedOrder.customerPhone"
                  type="button"
                  class="btn-mini"
                  @click="copyText(selectedOrder.customerPhone, 'Đã sao chép SĐT')"
                >
                  Sao chép
                </button>
              </p>
              <p><strong>Địa chỉ:</strong> {{ selectedOrder.address || '—' }}</p>
            </section>
            <section class="info-block info-block--total">
              <h4>Thanh toán</h4>
              <p class="total-big">{{ formatPrice(selectedOrder.total) }} <span class="currency">VNĐ</span></p>
            </section>
          </div>

          <section class="items-block">
            <h4>Sản phẩm ({{ (selectedOrder.items || []).length }})</h4>
            <table class="items-table">
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th class="num">SL</th>
                  <th class="num">Đơn giá</th>
                  <th class="num">Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(it, i) in (selectedOrder.items || [])" :key="i">
                  <td>{{ it.productName }}</td>
                  <td class="num">{{ it.quantity }}</td>
                  <td class="num mono">{{ formatPrice(it.unitPrice || 0) }}</td>
                  <td class="num mono strong">{{ formatPrice((it.unitPrice || 0) * (it.quantity || 1)) }}</td>
                </tr>
              </tbody>
            </table>
          </section>

          <footer class="modal-foot">
            <button type="button" class="btn-secondary" @click="selectedOrder = null">Đóng</button>
          </footer>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getOrders, updateOrderStatus } from '../../api/services/adminService'

const orders = ref([])
const loading = ref(true)
const selectedOrder = ref(null)
const searchQuery = ref('')
const statusFilter = ref('all')
const sortKey = ref('date_desc')
const page = ref(1)
const pageSize = ref(12)

const toast = ref({ message: '', type: 'ok' })
let toastTimer = null

const statusFilters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'pending', label: 'Chờ xử lý' },
  { value: 'shipping', label: 'Đang giao' },
  { value: 'delivered', label: 'Đã giao' },
]

function showToast(msg, type = 'ok') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { message: msg, type }
  toastTimer = setTimeout(() => {
    toast.value = { message: '', type: 'ok' }
  }, 2800)
}

function timeMs(d) {
  if (!d) return 0
  const t = new Date(typeof d === 'string' ? d : d).getTime()
  return Number.isNaN(t) ? 0 : t
}

const stats = computed(() => {
  const list = orders.value
  let pending = 0
  let shipping = 0
  let delivered = 0
  let deliveredRevenue = 0
  for (const o of list) {
    const s = (o.status || '').toLowerCase()
    if (s === 'pending') pending++
    else if (s === 'shipping') shipping++
    else if (s === 'delivered') {
      delivered++
      deliveredRevenue += Number(o.total || 0)
    }
  }
  return {
    total: list.length,
    pending,
    shipping,
    delivered,
    deliveredRevenue,
  }
})

function countByStatus(st) {
  return orders.value.filter((o) => (o.status || '').toLowerCase() === st).length
}

const filteredSorted = computed(() => {
  let list = [...orders.value]
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    list = list.filter((o) => {
      const id = String(o.id || '')
      const name = (o.customerName || '').toLowerCase()
      const email = (o.customerEmail || '').toLowerCase()
      const phone = (o.customerPhone || '').toLowerCase()
      return id.includes(q) || name.includes(q) || email.includes(q) || phone.includes(q)
    })
  }
  if (statusFilter.value !== 'all') {
    list = list.filter((o) => (o.status || '').toLowerCase() === statusFilter.value)
  }
  list.sort((a, b) => {
    switch (sortKey.value) {
      case 'date_asc':
        return timeMs(a.createdAt) - timeMs(b.createdAt)
      case 'total_desc':
        return Number(b.total || 0) - Number(a.total || 0)
      case 'total_asc':
        return Number(a.total || 0) - Number(b.total || 0)
      case 'date_desc':
      default:
        return timeMs(b.createdAt) - timeMs(a.createdAt)
    }
  })
  return list
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredSorted.value.length / pageSize.value)))

watch([filteredSorted, pageSize], () => {
  if (page.value > totalPages.value) page.value = totalPages.value
})

const pagedOrders = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredSorted.value.slice(start, start + pageSize.value)
})

const pageStart = computed(() => {
  if (filteredSorted.value.length === 0) return 0
  return (page.value - 1) * pageSize.value + 1
})

const pageEnd = computed(() => {
  return Math.min(page.value * pageSize.value, filteredSorted.value.length)
})

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN')
}

function formatDateTime(d) {
  if (!d) return '—'
  try {
    const date = new Date(typeof d === 'string' ? d : d)
    if (Number.isNaN(date.getTime())) {
      const s = String(d)
      return s.length >= 10 ? s.slice(0, 10) : s
    }
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date)
  } catch {
    return '—'
  }
}

function statusText(s) {
  const map = { pending: 'Chờ xử lý', shipping: 'Đang giao', delivered: 'Đã giao' }
  return map[(s || '').toLowerCase()] || s || '—'
}

async function load() {
  loading.value = true
  try {
    orders.value = await getOrders()
    page.value = 1
  } catch {
    orders.value = []
    showToast('Không tải được danh sách đơn hàng', 'err')
  } finally {
    loading.value = false
  }
}

async function updateStatus(o) {
  try {
    await updateOrderStatus(o.id, o.status)
    showToast('Đã cập nhật trạng thái đơn #' + o.id)
  } catch {
    showToast('Không thể cập nhật trạng thái', 'err')
    await load()
  }
}

function openDetail(o) {
  selectedOrder.value = o
}

async function copyText(text, okMsg) {
  try {
    await navigator.clipboard.writeText(text)
    showToast(okMsg)
  } catch {
    showToast('Không sao chép được (trình duyệt chặn)', 'err')
  }
}

function exportCsv() {
  const rows = filteredSorted.value
  const headers = ['ID', 'Khách hàng', 'Email', 'SĐT', 'Ngày đặt', 'Trạng thái', 'Tổng (VNĐ)']
  const lines = [headers.join(',')]
  for (const o of rows) {
    const line = [
      o.id,
      csvCell(o.customerName),
      csvCell(o.customerEmail),
      csvCell(o.customerPhone),
      csvCell(formatDateTime(o.createdAt)),
      csvCell(statusText(o.status)),
      Number(o.total || 0),
    ]
    lines.push(line.join(','))
  }
  const bom = '\uFEFF'
  const blob = new Blob([bom + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `don-hang-kesn-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
  showToast('Đã xuất CSV (' + rows.length + ' đơn)')
}

function csvCell(v) {
  if (v == null || v === '') return '""'
  const s = String(v).replace(/"/g, '""')
  return `"${s}"`
}

onMounted(load)
</script>

<style scoped>
.orders-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.page-head h1 {
  margin: 0;
  font-size: 1.65rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.page-sub {
  margin: 6px 0 0;
  font-size: 0.95rem;
  color: #64748b;
}

.page-head__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.btn-ghost,
.btn-primary,
.btn-secondary,
.btn-detail,
.btn-mini {
  font-family: inherit;
  cursor: pointer;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 600;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
}

.btn-ghost:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-ico {
  display: flex;
  width: 18px;
  height: 18px;
}

.btn-ico svg {
  width: 18px;
  height: 18px;
}

.btn-primary {
  padding: 10px 18px;
  border: none;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: #fff;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.25);
}

.btn-primary:hover:not(:disabled) {
  filter: brightness(1.06);
}

.btn-primary:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* KPI */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.kpi-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px 18px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.kpi-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #64748b;
  margin-bottom: 6px;
}

.kpi-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
}

.kpi-value--money {
  font-size: 1.1rem;
}

.kpi-card--amber {
  border-color: #fde68a;
  background: linear-gradient(180deg, #fffbeb 0%, #fff 100%);
}

.kpi-card--blue {
  border-color: #bfdbfe;
  background: linear-gradient(180deg, #eff6ff 0%, #fff 100%);
}

.kpi-card--green {
  border-color: #a7f3d0;
  background: linear-gradient(180deg, #ecfdf5 0%, #fff 100%);
}

.kpi-card--slate {
  border-color: #cbd5e1;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
}

/* Toolbar */
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 20px;
  margin-bottom: 16px;
  padding: 14px 18px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

.toolbar__search {
  flex: 1 1 220px;
  position: relative;
  min-width: 200px;
}

.search-ico {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #94a3b8;
  pointer-events: none;
}

.search-ico svg {
  width: 18px;
  height: 18px;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 40px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.9rem;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.search-input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.toolbar__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 0.8125rem;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s;
}

.chip:hover {
  border-color: #cbd5e1;
  background: #f1f5f9;
}

.chip.active {
  background: #0f172a;
  border-color: #0f172a;
  color: #fff;
}

.chip-count {
  font-size: 0.7rem;
  opacity: 0.85;
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.15);
}

.chip.active .chip-count {
  background: rgba(255, 255, 255, 0.2);
}

.toolbar__sort {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
}

.sort-select {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  font-size: 0.875rem;
  font-weight: 500;
  color: #334155;
  background: #fff;
  cursor: pointer;
}

.sort-select--sm {
  padding: 6px 10px;
  font-size: 0.8125rem;
}

/* Skeleton */
.skeleton-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 20px;
}

.sk-line {
  height: 14px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: sh 1.2s ease-in-out infinite;
  border-radius: 6px;
}

.sk-line--title {
  width: 200px;
  height: 22px;
  margin-bottom: 20px;
}

.sk-row {
  height: 48px;
  margin-bottom: 10px;
  background: linear-gradient(90deg, #f8fafc 25%, #f1f5f9 50%, #f8fafc 75%);
  background-size: 200% 100%;
  animation: sh 1.2s ease-in-out infinite;
  border-radius: 8px;
}

@keyframes sh {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: -100% 0;
  }
}

/* Table panel */
.table-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.table-panel__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.875rem;
  color: #64748b;
}

.page-size {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size label {
  font-weight: 600;
  color: #475569;
}

.table-scroll {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.data-table th {
  text-align: left;
  padding: 14px 16px;
  font-weight: 700;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #64748b;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

.data-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
  color: #334155;
}

.data-row:hover td {
  background: #fafbfc;
}

.th-id {
  width: 72px;
}

.th-num {
  text-align: right;
}

.th-act {
  width: 110px;
  text-align: right;
}

.mono {
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, 'Cascadia Code', monospace;
}

.strong {
  font-weight: 700;
  color: #0f172a;
}

.cell-name {
  font-weight: 600;
  color: #0f172a;
}

.cell-muted {
  font-size: 0.8125rem;
  color: #94a3b8;
  margin-top: 2px;
}

.cell-phone {
  white-space: nowrap;
}

.cell-date {
  white-space: nowrap;
  color: #475569;
  font-size: 0.8125rem;
}

.status-badge {
  display: inline-block;
  margin-right: 8px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  vertical-align: middle;
}

.status-badge.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.shipping {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.delivered {
  background: #d1fae5;
  color: #065f46;
}

.status-badge--lg {
  font-size: 0.75rem;
  padding: 6px 14px;
}

.status-select {
  margin-top: 6px;
  display: block;
  max-width: 140px;
  padding: 6px 8px;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
}

.status-select.pending {
  background: #fffbeb;
  color: #92400e;
  border-color: #fde68a;
}

.status-select.shipping {
  background: #eff6ff;
  color: #1e40af;
  border-color: #bfdbfe;
}

.status-select.delivered {
  background: #ecfdf5;
  color: #065f46;
  border-color: #a7f3d0;
}

.btn-detail {
  padding: 8px 14px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  font-weight: 600;
}

.btn-detail:hover {
  border-color: #6366f1;
  color: #4f46e5;
  background: #eef2ff;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 14px;
  border-top: 1px solid #f1f5f9;
}

.pager-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  color: #334155;
}

.pager-btn:hover:not(:disabled) {
  background: #f8fafc;
}

.pager-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pager-info {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: #64748b;
  font-size: 1rem;
  background: #fff;
  border: 1px dashed #e2e8f0;
  border-radius: 14px;
}

.empty-ico {
  display: block;
  font-size: 2rem;
  margin-bottom: 12px;
}

/* Toast */
.toast {
  position: fixed;
  bottom: 28px;
  right: 28px;
  z-index: 2000;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.2);
  animation: toast-in 0.25s ease;
}

.toast.ok {
  background: #0f172a;
  color: #fff;
}

.toast.err {
  background: #b91c1c;
  color: #fff;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1500;
  padding: 20px;
}

.modal {
  background: #fff;
  border-radius: 18px;
  padding: 0;
  max-width: 520px;
  width: 100%;
  max-height: min(90vh, 720px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 80px rgba(15, 23, 42, 0.35);
}

.modal--wide {
  max-width: 640px;
}

.modal-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 22px 24px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
}

.modal-head h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 800;
  color: #0f172a;
}

.modal-sub {
  margin: 6px 0 0;
  font-size: 0.875rem;
  color: #64748b;
}

.modal-grid {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 20px;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

@media (max-width: 560px) {
  .modal-grid {
    grid-template-columns: 1fr;
  }
}

.info-block h4 {
  margin: 0 0 12px;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #94a3b8;
}

.info-block p {
  margin: 0 0 10px;
  font-size: 0.9rem;
  color: #334155;
  line-height: 1.5;
}

.copy-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.btn-mini {
  padding: 4px 10px;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #4f46e5;
  border-radius: 8px;
}

.btn-mini:hover {
  background: #eef2ff;
}

.info-block--total {
  text-align: right;
  align-self: start;
}

.total-big {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 800;
  color: #0f172a;
}

.total-big .currency {
  font-size: 0.9rem;
  font-weight: 600;
  color: #64748b;
}

.items-block {
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;
}

.items-block h4 {
  margin: 0 0 14px;
  font-size: 0.8rem;
  font-weight: 700;
  color: #475569;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.8125rem;
}

.items-table th,
.items-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f1f5f9;
  text-align: left;
}

.items-table th {
  font-weight: 700;
  color: #64748b;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.items-table .num {
  text-align: right;
}

.modal-foot {
  padding: 16px 24px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
}

.btn-secondary {
  padding: 10px 22px;
  border: none;
  border-radius: 10px;
  background: #0f172a;
  color: #fff;
  font-weight: 600;
}

.btn-secondary:hover {
  filter: brightness(1.08);
}
</style>
