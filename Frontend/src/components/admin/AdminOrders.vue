<template>
  <div class="orders-page">
    <header class="page-head d-flex justify-content-between align-items-center mb-4">
      <div class="page-head__text">
        <h1 class="h3 fw-bold mb-1">Quản lý đơn hàng</h1>
        <p class="text-secondary small mb-0">Theo dõi, lọc và cập nhật trạng thái giao hàng</p>
      </div>
      <div class="page-head__actions d-flex gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm d-flex align-items-center gap-2 px-3" :disabled="loading" @click="load(true)">
          <i class="bi bi-arrow-clockwise" :class="{ 'spinner-border spinner-border-sm border-0': isRefreshing }"></i>
          Làm mới
        </button>
        <button
          type="button"
          class="btn btn-dark btn-sm d-flex align-items-center gap-2 px-3 shadow-sm"
          :disabled="loading || filteredSorted.length === 0"
          @click="exportCsv"
        >
          <i class="bi bi-download"></i>
          Xuất dữ liệu
        </button>
      </div>
    </header>

    <!-- KPI Section (Grid) -->
    <section v-if="!loading || isRefreshing" class="kpi-grid mb-4" aria-label="Tổng quan đơn hàng">
      <article class="kpi-card shadow-sm border-0">
        <span class="kpi-label">Tổng đơn</span>
        <strong class="kpi-value">{{ stats.total }}</strong>
      </article>

      <article class="kpi-card kpi-card--orange shadow-sm border-0">
        <span class="kpi-label">Chờ xử lý</span>
        <strong class="kpi-value">{{ stats.pending }}</strong>
      </article>

      <article class="kpi-card kpi-card--blue shadow-sm border-0">
        <span class="kpi-label">Đang giao</span>
        <strong class="kpi-value">{{ stats.shipping }}</strong>
      </article>

      <article class="kpi-card kpi-card--green shadow-sm border-0">
        <span class="kpi-label">Đã giao</span>
        <strong class="kpi-value">{{ stats.delivered }}</strong>
      </article>

      <article class="kpi-card kpi-card--purple shadow-sm border-0">
        <span class="kpi-label">Doanh thu giao thành công</span>
        <strong class="kpi-value kpi-value--money">{{ formatPrice(stats.deliveredRevenue) }} ₫</strong>
      </article>
    </section>

    <!-- Loading Skeleton -->
    <section v-if="loading && !isRefreshing" class="skeleton-panel mb-4">
      <div class="sk-line sk-line--title mb-4"></div>
      <div v-for="n in 5" :key="n" class="sk-row mb-2"></div>
    </section>

    <!-- Main Content Container with Glass-Fade -->
    <div v-else class="content-body" :class="{ 'is-refreshing': isRefreshing }">
      
      <!-- Filter/Search Toolbar -->
      <section v-if="orders.length" class="toolbar card shadow-sm border-0 p-3 mb-4 d-block">
        <div class="row g-3 align-items-center">
          <div class="col-md-4">
            <div class="position-relative">
              <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 text-secondary"></i>
              <input
                v-model="searchQuery"
                type="search"
                class="form-control border-0 bg-light ps-5 py-2"
                placeholder="Tìm mã đơn, khách hàng..."
                autocomplete="off"
              />
            </div>
          </div>
          <div class="col-md-5">
            <div class="d-flex flex-wrap gap-2" role="group">
              <button
                v-for="f in statusFilters"
                :key="f.value"
                type="button"
                class="btn btn-sm px-3 rounded-pill fw-bold text-nowrap"
                :class="statusFilter === f.value ? 'btn-dark shadow-sm' : 'btn-light text-secondary border-0'"
                @click="statusFilter = f.value; page = 1"
              >
                {{ f.label }}
              </button>
            </div>
          </div>
          <div class="col-md-3 text-md-end">
            <div class="d-inline-flex align-items-center gap-2">
              <span class="small text-secondary fw-bold text-nowrap">Sắp xếp:</span>
              <select v-model="sortKey" class="form-select form-select-sm border-0 bg-light w-auto fw-medium">
                <option value="date_desc">Mới nhất</option>
                <option value="date_asc">Cũ nhất</option>
                <option value="total_desc">Giá cao → thấp</option>
                <option value="total_asc">Giá thấp → cao</option>
              </select>
            </div>
          </div>
        </div>
      </section>

      <!-- Results Table -->
      <div v-if="orders.length" class="table-panel">
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
                  <div v-if="o.customerEmail" class="cell-muted small">{{ o.customerEmail }}</div>
                </td>
                <td class="cell-phone">{{ o.customerPhone || '—' }}</td>
                <td class="cell-date">{{ formatDateTime(o.createdAt) }}</td>
                <td>
                  <span class="status-badge" :class="o.status">{{ statusText(o.status) }}</span>
                  <select
                    v-model="o.status"
                    class="status-select mt-1"
                    :class="o.status"
                    @change="updateStatus(o)"
                  >
                    <option value="pending">Chờ xử lý</option>
                    <option value="shipping">Đang giao</option>
                    <option value="delivered">Đã giao</option>
                    <option value="cancelled" disabled>Đã hủy</option>
                  </select>
                </td>
                <td class="th-num mono strong text-primary">{{ formatPrice(o.total) }} ₫</td>
                <td>
                  <div class="d-flex gap-1 justify-content-end">
                    <button type="button" class="btn btn-sm btn-primary px-3 shadow-sm" @click="openDetail(o)">Chi tiết</button>
                    <button v-if="o.status !== 'delivered' && o.status !== 'cancelled' && o.status !== 'shipping'" type="button" class="btn btn-sm btn-outline-danger" @click="promptCancel(o)">Hủy</button>
                  </div>
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
    </div>

    <!-- Toast -->
    <div v-if="toast.message" class="toast" :class="toast.type" role="status">
      {{ toast.message }}
    </div>

    <!-- Modal chi tiết -->
    <Teleport to="body">
      <div v-if="selectedOrder" class="modal-overlay" @click.self="selectedOrder = null">
        <div class="modal modal--wide shadow-lg">
          <header class="modal-head">
            <div>
              <h3 class="mb-1">Đơn hàng <span class="mono text-primary">#{{ selectedOrder.id }}</span></h3>
              <p class="modal-sub mb-0">{{ formatDateTime(selectedOrder.createdAt) }}</p>
            </div>
            <span class="status-badge status-badge--lg" :class="selectedOrder.status">
              {{ statusText(selectedOrder.status) }}
            </span>
          </header>

          <div class="modal-grid mt-4">
            <div v-if="selectedOrder.status === 'cancelled' && selectedOrder.cancelReason" class="info-block info-cancel p-3 bg-danger bg-opacity-10 rounded border border-danger border-opacity-25 mb-3">
              <h5 class="text-danger fw-bold mb-2">Đơn hàng bị hủy</h5>
              <p class="mb-0"><strong>Lý do:</strong> {{ selectedOrder.cancelReason }}</p>
            </div>
            <div class="row g-4">
              <div class="col-md-7">
                <section class="info-block card border-0 bg-light p-3">
                  <h5 class="fw-bold mb-3">Khách hàng</h5>
                  <p class="mb-2"><strong>Tên:</strong> {{ selectedOrder.customerName || '—' }}</p>
                  <p class="mb-2 d-flex justify-content-between align-items-center">
                    <span><strong>Email:</strong> {{ selectedOrder.customerEmail || '—' }}</span>
                    <button v-if="selectedOrder.customerEmail" type="button" class="btn btn-xs btn-outline-primary" @click="copyText(selectedOrder.customerEmail, 'Đã sao chép email')">Sao chép</button>
                  </p>
                  <p class="mb-2 d-flex justify-content-between align-items-center">
                    <span><strong>SĐT:</strong> {{ selectedOrder.customerPhone || '—' }}</span>
                    <button v-if="selectedOrder.customerPhone" type="button" class="btn btn-xs btn-outline-primary" @click="copyText(selectedOrder.customerPhone, 'Đã sao chép SĐT')">Sao chép</button>
                  </p>
                  <p class="mb-0"><strong>Địa chỉ:</strong> {{ selectedOrder.address || '—' }}</p>
                </section>
              </div>
              <div class="col-md-5">
                <section class="info-block card border-0 bg-primary bg-opacity-10 p-3 h-100">
                  <h5 class="fw-bold mb-3">Thanh toán</h5>
                  <p class="display-6 fw-bold text-primary mb-0">{{ formatPrice(selectedOrder.total) }} <span class="fs-6 fw-normal">VNĐ</span></p>
                </section>
              </div>
            </div>
          </div>

          <section class="items-block mt-4">
            <h5 class="fw-bold mb-3">Sản phẩm ({{ (selectedOrder.items || []).length }})</h5>
            <div class="table-responsive rounded border">
              <table class="table table-hover mb-0 align-middle">
                <thead class="bg-light">
                  <tr>
                    <th>Sản phẩm</th>
                    <th class="text-center" width="80">SL</th>
                    <th class="text-end" width="140">Đơn giá</th>
                    <th class="text-end" width="150">Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(it, i) in (selectedOrder.items || [])" :key="i">
                    <td class="fw-medium">{{ it.productName }}</td>
                    <td class="text-center">{{ it.quantity }}</td>
                    <td class="text-end mono">{{ formatPrice(it.unitPrice || 0) }} ₫</td>
                    <td class="text-end mono fw-bold">{{ formatPrice((it.unitPrice || 0) * (it.quantity || 1)) }} ₫</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <footer class="modal-foot mt-4 pt-3 border-top d-flex justify-content-end">
            <button type="button" class="btn btn-secondary px-4" @click="selectedOrder = null">Đóng</button>
          </footer>
        </div>
      </div>
    </Teleport>

    <!-- Modal Hủy Đơn Hàng -->
    <Teleport to="body">
      <div v-if="showCancelModal" class="modal-overlay" @click.self="showCancelModal = false">
        <div class="modal modal--cancel shadow-lg">
          <header class="modal-head">
            <h3 class="fw-bold text-danger mb-1">Xác nhận hủy đơn hàng</h3>
            <p class="modal-sub mb-0 text-secondary">Mã đơn: #{{ orderToCancel?.id }}</p>
          </header>
          
          <div class="modal-body mt-4">
            <div class="bg-light p-3 rounded mb-4">
              <h6 class="fw-bold mb-2">Tóm tắt đơn hàng:</h6>
              <ul class="list-unstyled mb-2 small">
                <li v-for="(item, i) in (orderToCancel?.items || [])" :key="i" class="mb-1 text-secondary">
                  • {{ item.productName || item.name }}: <span class="fw-bold text-dark">x{{ item.quantity || item.qty }}</span>
                </li>
              </ul>
              <div class="border-top pt-2 mt-2">
                <strong>Tổng cộng:</strong> <span class="text-primary fw-bold">{{ formatPrice(orderToCancel?.total) }} đ</span>
              </div>
            </div>

            <div class="mb-3">
              <label for="cancel-reason" class="form-label fw-bold small">Lý do hủy đơn <span class="text-danger">*</span></label>
              <textarea 
                id="cancel-reason" 
                v-model="cancelReasonInput" 
                rows="3" 
                placeholder="Vui lòng nhập lý do cụ thể..." 
                class="form-control"
              ></textarea>
              <p v-if="modalError && !showConfirmModal" class="text-danger small mt-1">{{ modalError }}</p>
            </div>
          </div>

          <footer class="modal-foot mt-4 pt-3 border-top d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-light px-4" @click="showCancelModal = false">Bỏ qua</button>
            <button type="button" class="btn btn-danger px-4 shadow-sm" @click="confirmCancelAdmin">Tiếp tục hủy</button>
          </footer>
        </div>
      </div>
    </Teleport>

    <!-- Modal Xác Nhận Cuối Cùng -->
    <Teleport to="body">
      <div v-if="showConfirmModal" class="modal-overlay" @click.self="showConfirmModal = false">
        <div class="modal modal--small shadow-lg p-4 text-center">
            <i class="bi bi-exclamation-triangle display-4 text-warning mb-3 d-block"></i>
            <h4 class="fw-bold mb-3">Bạn có chắc chắn?</h4>
            <p class="text-secondary mb-4">Hành động hủy đơn hàng <strong>#{{ orderToCancel?.id }}</strong> không thể hoàn tác.</p>
            <p v-if="modalError && showConfirmModal" class="text-danger small mb-3">{{ modalError }}</p>
          <div class="d-flex justify-content-center gap-2">
            <button type="button" class="btn btn-light px-4" :disabled="isCancelling" @click="showConfirmModal = false">Quay lại</button>
            <button type="button" class="btn btn-danger px-4 shadow-sm" :disabled="isCancelling" @click="submitCancelAdmin">
              {{ isCancelling ? 'Đang giao dịch...' : 'Xác nhận hủy' }}
            </button>
          </div>
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
const isRefreshing = ref(false)
const selectedOrder = ref(null)
const searchQuery = ref('')
const statusFilter = ref('all')
const sortKey = ref('date_desc')
const page = ref(1)
const pageSize = ref(12)

const orderToCancel = ref(null)
const showCancelModal = ref(false)
const cancelReasonInput = ref('')
const showConfirmModal = ref(false)
const modalError = ref('')
const isCancelling = ref(false)

const toast = ref({ message: '', type: 'ok' })
let toastTimer = null

const statusFilters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'pending', label: 'Chờ xử lý' },
  { value: 'shipping', label: 'Đang giao' },
  { value: 'delivered', label: 'Đã giao' },
  { value: 'cancelled', label: 'Đã hủy' },
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
  const map = { pending: 'Chờ xử lý', shipping: 'Đang giao', delivered: 'Đã giao', cancelled: 'Đã hủy' }
  return map[(s || '').toLowerCase()] || s || '—'
}

async function load(refresh = false) {
  if (refresh) isRefreshing.value = true
  else loading.value = true
  
  try {
    orders.value = await getOrders()
    page.value = 1
  } catch {
    orders.value = []
    showToast('Không tải được danh sách đơn hàng', 'err')
  } finally {
    loading.value = false
    isRefreshing.value = false
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

function promptCancel(o) {
  orderToCancel.value = o
  cancelReasonInput.value = ''
  modalError.value = ''
  showCancelModal.value = true
}

function confirmCancelAdmin() {
  modalError.value = ''
  if (!cancelReasonInput.value.trim()) {
    modalError.value = 'Vui lòng nhập lý do hủy đơn.'
    return
  }
  showConfirmModal.value = true
}

async function submitCancelAdmin() {
  modalError.value = ''
  isCancelling.value = true
  try {
    await updateOrderStatus(orderToCancel.value.id, 'cancelled', cancelReasonInput.value.trim())
    showToast('Đã hủy đơn #' + orderToCancel.value.id)
    showConfirmModal.value = false
    showCancelModal.value = false
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || 'Không thể hủy đơn'
    modalError.value = msg
  } finally {
    isCancelling.value = false
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

.kpi-card--orange {
  border-color: #fde68a;
  background: linear-gradient(160deg, #fffbeb 0%, #fff 60%);
}
.kpi-card--orange::before { background: linear-gradient(90deg, #f59e0b, #d97706); }

.kpi-card--blue {
     border-color: #bfdbfe;
     background: linear-gradient(160deg, #eff6ff 0%, #fff 60%);
}
.kpi-card--blue::before { background: linear-gradient(90deg, #3b82f6, #1d4ed8); }

.kpi-card--green {
     border-color: #a7f3d0;
     background: linear-gradient(160deg, #ecfdf5 0%, #fff 60%);
}
.kpi-card--green::before { background: linear-gradient(90deg, #10b981, #059669); }

.kpi-card--purple {
  border-color: #ddd6fe;
  background: linear-gradient(160deg, #f5f3ff 0%, #fff 60%);
}
.kpi-card--purple::before { background: linear-gradient(90deg, #8b5cf6, #7c3aed); }

.kpi-value--money { font-size: 1.1rem; }

.cell-name { font-weight: 600; color: #0f172a; }
.cell-muted { font-size: 0.8rem; color: #94a3b8; margin-top: 2px; }
.cell-date { white-space: nowrap; font-size: 0.8125rem; color: #64748b; }

.content-body {
  transition: opacity 0.4s ease, filter 0.4s ease, transform 0.4s ease;
  will-change: opacity, filter, transform;
}

.content-body.is-refreshing {
  opacity: 0.5;
  filter: blur(2px);
  pointer-events: none;
  transform: scale(0.995);
}

.mono { font-family: 'JetBrains Mono', monospace; }

.btn-xs {
  padding: 2px 8px;
  font-size: 0.75rem;
  border-radius: 4px;
}
</style>
