<template>
  <div class="order-detail-page">
    <div class="order-detail-inner">
      <nav class="breadcrumb" aria-label="Điều hướng">
        <router-link class="breadcrumb-link" to="/profile?tab=orders">
          <span class="breadcrumb-icon" aria-hidden="true">‹</span>
          Lịch sử đơn hàng
        </router-link>
        <span class="breadcrumb-sep" aria-hidden="true">/</span>
        <span class="breadcrumb-current">Đơn #{{ orderIdDisplay }}</span>
      </nav>

      <p class="page-eyebrow">Chi tiết đơn hàng</p>

      <section v-if="loading" class="state state--loading">
        <div class="spinner" aria-hidden="true" />
        <p>Đang tải thông tin đơn hàng…</p>
      </section>

      <section v-else-if="error" class="state state--error">
        <div class="state-icon state-icon--warn" aria-hidden="true">!</div>
        <p class="state-msg">{{ error }}</p>
        <router-link to="/profile?tab=orders" class="btn btn--primary">Quay lại danh sách</router-link>
      </section>

      <template v-else-if="order">
        <header class="hero-panel">
          <div class="hero-main">
            <div class="hero-titles">
              <h1 class="hero-title">
                Đơn <span class="order-num">#{{ order.id }}</span>
              </h1>
              <p class="hero-meta">
                <span class="meta-label">Đặt lúc</span>
                <time :datetime="order.createdAt">{{ formatDateTime(order.createdAt) }}</time>
              </p>
            </div>
            <span class="status-pill" :class="order.status">{{ statusText(order.status) }}</span>
          </div>
          <div v-if="order.status === 'cancelled' && order.cancelReason" class="cancel-banner">
            <strong>Đơn hàng đã hủy:</strong> {{ order.cancelReason }}
          </div>
        </header>

        <section v-if="order.status !== 'cancelled'" class="panel panel--timeline" aria-label="Tiến trình đơn hàng">
          <h2 class="panel-title">Tiến trình</h2>
          <div class="timeline">
            <div class="timeline-rail" aria-hidden="true" />
            <div
              v-for="(step, i) in timelineSteps"
              :key="step.key"
              class="timeline-step"
              :class="{ done: step.done, current: step.current }"
            >
              <span class="timeline-dot-wrap">
                <span class="timeline-dot" />
              </span>
              <span class="timeline-label">{{ step.label }}</span>
            </div>
          </div>
        </section>

        <div class="two-col">
          <section class="panel">
            <h2 class="panel-title">Giao hàng &amp; liên hệ</h2>
            <dl class="info-grid">
              <div class="info-cell">
                <dt>Người nhận</dt>
                <dd>{{ order.customerName || '—' }}</dd>
              </div>
              <div class="info-cell">
                <dt>Email</dt>
                <dd>{{ order.customerEmail || '—' }}</dd>
              </div>
              <div class="info-cell">
                <dt>Điện thoại</dt>
                <dd>{{ order.customerPhone || '—' }}</dd>
              </div>
              <div class="info-cell info-cell--wide">
                <dt>Địa chỉ giao hàng</dt>
                <dd>{{ order.address || '—' }}</dd>
              </div>
            </dl>
          </section>

          <section class="panel panel--items">
            <h2 class="panel-title">
              Sản phẩm
              <span class="panel-count">{{ (order.items || []).length }}</span>
            </h2>
            <div class="table-wrap">
              <div class="table-head" role="row">
                <span>Sản phẩm</span>
                <span class="t-center">SL</span>
                <span class="t-right">Đơn giá</span>
                <span class="t-right">Thành tiền</span>
              </div>
              <ul class="table-body">
                <li v-for="(item, i) in (order.items || [])" :key="i" class="table-row" role="row">
                  <span class="cell-name">{{ item.productName || item.name || 'Sản phẩm' }}</span>
                  <span class="cell-qty t-center">{{ item.quantity || item.qty || 1 }}</span>
                  <span class="cell-price t-right tabular">{{ formatPrice(item.unitPrice || item.price || 0) }} đ</span>
                  <span class="cell-sub t-right tabular strong">{{ formatPrice(lineTotal(item)) }} đ</span>
                </li>
              </ul>
            </div>
            <div class="total-strip">
              <span class="total-label">Tổng thanh toán</span>
              <span class="total-value tabular">{{ formatPrice(order.total) }} <small>VNĐ</small></span>
            </div>
          </section>
        </div>

        <footer class="action-bar">
          <button v-if="order.status !== 'delivered' && order.status !== 'shipping' && order.status !== 'cancelled'" type="button" class="btn btn--danger" @click="openCancelModal">Hủy đơn hàng</button>
          <router-link to="/profile?tab=orders" class="btn btn--ghost">Danh sách đơn</router-link>
          <router-link to="/product" class="btn btn--primary">Tiếp tục mua sắm</router-link>
        </footer>
      </template>

      <!-- Modal Hủy Đơn Hàng -->
      <Teleport to="body">
        <div v-if="showCancelModal" class="modal-overlay" @click.self="showCancelModal = false">
          <div class="modal modal--cancel">
            <header class="modal-head">
              <h3>Xác nhận hủy đơn hàng</h3>
              <p class="modal-sub">Mã đơn: #{{ order?.id }}</p>
            </header>
            
            <div class="modal-body">
              <div class="cancel-summary">
                <h4>Thông tin đơn</h4>
                <ul class="summary-items">
                  <li v-for="(item, i) in (order?.items || [])" :key="i">
                    - {{ item.productName || item.name }}: <span class="summary-qty">x{{ item.quantity || item.qty }}</span>
                  </li>
                </ul>
                <p class="summary-total"><strong>Tổng tiền:</strong> {{ formatPrice(order?.total) }} đ</p>
              </div>

              <div class="cancel-form">
                <label for="cancel-reason">Lý do hủy đơn <span class="req">*</span></label>
                <textarea 
                  id="cancel-reason" 
                  v-model="cancelReasonInput" 
                  rows="3" 
                  placeholder="Vui lòng nhập lý do bạn muốn hủy đơn hàng này..." 
                  class="reason-input"
                ></textarea>
                <p v-if="modalError && !showConfirmModal" class="req" style="margin: 4px 0 0; font-size: 13px;">{{ modalError }}</p>
              </div>
            </div>

            <footer class="modal-foot">
              <button type="button" class="btn btn--ghost" @click="showCancelModal = false">Đóng</button>
              <button type="button" class="btn btn--danger" @click="confirmCancel">Xác nhận hủy</button>
            </footer>
          </div>
        </div>
      </Teleport>

      <!-- Modal Xác Nhận Cuối Cùng -->
      <Teleport to="body">
        <div v-if="showConfirmModal" class="modal-overlay" @click.self="showConfirmModal = false">
          <div class="modal modal--small">
            <header class="modal-head">
              <h3>Xác nhận hủy</h3>
            </header>
            <div class="modal-body">
              <p style="margin:0; font-size: 15px; color: #334155;">Bạn có chắc chắn muốn hủy đơn hàng này không? Quá trình này không thể hoàn tác.</p>
              <p v-if="modalError && showConfirmModal" class="req" style="margin: 10px 0 0; font-size: 13px;">{{ modalError }}</p>
            </div>
            <footer class="modal-foot">
              <button type="button" class="btn btn--ghost" :disabled="isCancelling" @click="showConfirmModal = false">Quay lại</button>
              <button type="button" class="btn btn--danger" :disabled="isCancelling" @click="submitCancel">{{ isCancelling ? 'Đang hủy...' : 'Đồng ý hủy' }}</button>
            </footer>
          </div>
        </div>
      </Teleport>

      <!-- Toast Popup -->
      <div v-if="toastMsg" class="toast-popup">
        {{ toastMsg }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { resolveSessionUserId } from '../authStore'
import { getOrderDetail, cancelOrder } from '../api/services/orderService'

const route = useRoute()

const order = ref(null)
const loading = ref(true)
const error = ref('')

const showCancelModal = ref(false)
const cancelReasonInput = ref('')
const showConfirmModal = ref(false)
const modalError = ref('')
const isCancelling = ref(false)
const toastMsg = ref('')

let toastTimer = null
function showToast(msg) {
  if (toastTimer) clearTimeout(toastTimer)
  toastMsg.value = msg
  toastTimer = setTimeout(() => {
    toastMsg.value = ''
  }, 3000)
}

const orderIdDisplay = computed(() => route.params.id || '—')

function formatPrice(n) {
  return Number(n).toLocaleString('vi-VN')
}

function formatDateTime(d) {
  if (!d) return '—'
  let date
  if (typeof d === 'string') {
    date = new Date(d)
  } else if (Array.isArray(d)) {
    return d.join('-')
  } else {
    date = new Date(d)
  }
  if (Number.isNaN(date.getTime())) return String(d)
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function statusText(s) {
  const map = {
    pending: 'Chờ xử lý',
    paid: 'Đã thanh toán',
    shipping: 'Đang giao',
    delivered: 'Đã giao',
    cancelled: 'Đã hủy'
  }
  return map[s] || s || '—'
}

function lineTotal(item) {
  const q = item.quantity || item.qty || 1
  const p = Number(item.unitPrice || item.price || 0)
  return p * q
}

const timelineSteps = computed(() => {
  const s = order.value?.status || 'pending'
  const rank = { pending: 2, paid: 2, shipping: 3, delivered: 4 }
  const r = rank[s] ?? 2
  const steps = [
    { key: 'placed', label: 'Đã đặt' },
    { key: 'proc', label: 'Xử lý' },
    { key: 'ship', label: 'Giao hàng' },
    { key: 'done', label: 'Hoàn tất' },
  ]
  return steps.map((st, i) => {
    const stepNum = i + 1
    const done = stepNum < r || (stepNum === r && s === 'delivered')
    const current = stepNum === r && s !== 'delivered'
    return { ...st, done, current }
  })
})

async function load() {
  error.value = ''
  order.value = null
  const id = Number(route.params.id)
  const userId = resolveSessionUserId()
  if (!Number.isFinite(userId) || !Number.isFinite(id)) {
    loading.value = false
    error.value = 'Không tìm thấy đơn hàng hoặc bạn chưa đăng nhập. Hãy đăng nhập lại.'
    return
  }
  loading.value = true
  try {
    const data = await getOrderDetail(id, userId)
    order.value = data
  } catch (e) {
    const st = e.response?.status
    if (st === 404) {
      error.value = 'Không tìm thấy đơn hàng hoặc đơn không thuộc tài khoản của bạn.'
    } else if (st === 400) {
      error.value = 'Thiếu thông tin tài khoản. Vui lòng đăng xuất và đăng nhập lại.'
    } else {
      const msg = e.response?.data?.message
      error.value = msg
        ? String(msg)
        : 'Không tải được chi tiết đơn. Kiểm tra backend đang chạy và thử lại.'
    }
  } finally {
    loading.value = false
  }
}

function openCancelModal() {
  cancelReasonInput.value = ''
  modalError.value = ''
  showCancelModal.value = true
}

function confirmCancel() {
  modalError.value = ''
  if (!cancelReasonInput.value.trim()) {
    modalError.value = 'Vui lòng nhập lý do hủy đơn.';
    return;
  }
  showConfirmModal.value = true
}

async function submitCancel() {
  modalError.value = ''
  isCancelling.value = true
  const id = Number(route.params.id)
  const userId = resolveSessionUserId()
  try {
    await cancelOrder(id, cancelReasonInput.value.trim(), userId)
    showToast('Đã hủy đơn hàng thành công.')
    showConfirmModal.value = false
    showCancelModal.value = false
    await load() // reload data
  } catch (e) {
    const msg = e.response?.data?.message || 'Không thể hủy đơn hàng lúc này.'
    modalError.value = msg
  } finally {
    isCancelling.value = false
  }
}


watch(
  () => route.params.id,
  () => load(),
  { immediate: true }
)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.order-detail-page {
  font-family: 'Inter', -apple-system, system-ui, sans-serif;
  background: linear-gradient(-45deg, #0f172a, #000000, #312e81, #0f172a);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  min-height: calc(100vh - 100px);
  color: #fff;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.order-detail-inner {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 20px 88px;
}

.page-eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 8px;
}

.breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 20px;
}

.breadcrumb-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
  text-decoration: none;
  padding: 6px 0;
  transition: color 0.15s;
}

.breadcrumb-link:hover {
  color: #fff;
}

.breadcrumb-icon {
  font-size: 18px;
  line-height: 1;
  margin-top: -1px;
}

.breadcrumb-sep {
  color: #cbd5e1;
  user-select: none;
}

.breadcrumb-current {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.state {
  text-align: center;
  padding: 56px 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.state--loading p {
  margin: 16px 0 0;
  font-size: 15px;
  color: #64748b;
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto;
  border: 3px solid #e2e8f0;
  border-top-color: #0f172a;
  border-radius: 50%;
  animation: spin 0.75s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.state--error .state-msg {
  margin: 0 0 20px;
  font-size: 15px;
  line-height: 1.55;
  color: #475569;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
}

.state-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  border-radius: 14px;
  font-size: 22px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.state-icon--warn {
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
}

.hero-panel {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 28px 28px 24px;
  margin-bottom: 20px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
}

.hero-main {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.hero-title {
  font-size: clamp(22px, 4vw, 28px);
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 10px;
  line-height: 1.2;
  background: linear-gradient(135deg, #fff, #a5b4fc, #fbcfe8);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shine 5s linear infinite;
}

.order-num {
  font-variant-numeric: tabular-nums;
  color: #fff;
  -webkit-text-fill-color: #fff;
}

.hero-meta {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
}

.meta-label {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
}

.status-pill {
  padding: 10px 16px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.status-pill.pending {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

.status-pill.paid {
  background: #eef2ff;
  color: #4338ca;
  border: 1px solid #c7d2fe;
}

.status-pill.shipping {
  background: #eff6ff;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.status-pill.delivered {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.status-pill.cancelled {
  background: #fff1f2;
  color: #be123c;
  border: 1px solid #ffe4e6;
}

.cancel-banner {
  margin-top: 16px;
  padding: 12px 16px;
  background: #fff1f2;
  border: 1px solid #ffe4e6;
  border-radius: 12px;
  color: #be123c;
  font-size: 14px;
}

.panel {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 24px 26px 26px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.panel--timeline {
  margin-bottom: 20px;
  padding-bottom: 28px;
}

.panel-title {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-count {
  font-size: 12px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 2px 10px;
  border-radius: 999px;
  letter-spacing: 0;
  text-transform: none;
}

.timeline {
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 8px 4px 0;
  max-width: 640px;
  margin: 0 auto;
}

.timeline-rail {
  position: absolute;
  left: 12%;
  right: 12%;
  top: 15px;
  height: 2px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 1px;
}

.timeline-step {
  position: relative;
  z-index: 1;
  flex: 1;
  text-align: center;
  min-width: 0;
  padding: 0 6px;
}

.timeline-dot-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}

.timeline-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.timeline-step.done .timeline-dot {
  background: #fff;
  border-color: #fff;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.timeline-step.current .timeline-dot {
  background: transparent;
  border-color: #fff;
  border-width: 3px;
  width: 16px;
  height: 16px;
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.6);
}

.timeline-label {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.35;
  display: block;
}

.timeline-step.done .timeline-label,
.timeline-step.current .timeline-label {
  color: #fff;
}

.two-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-grid {
  display: grid;
  gap: 20px 24px;
  margin: 0;
}

@media (min-width: 560px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.info-cell dt {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 6px;
}

.info-cell dd {
  margin: 0;
  font-size: 15px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
}

.info-cell--wide {
  grid-column: 1 / -1;
}

.table-wrap {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 4px;
}

.table-head {
  display: grid;
  grid-template-columns: 1fr 44px minmax(92px, auto) minmax(104px, auto);
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.7);
}

.table-body {
  list-style: none;
  margin: 0;
  padding: 0;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 44px minmax(92px, auto) minmax(104px, auto);
  gap: 12px;
  padding: 16px;
  align-items: center;
  font-size: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: background 0.12s;
}

.table-row:hover {
  background: rgba(255, 255, 255, 0.08);
}

.cell-name {
  font-weight: 600;
  color: #fff;
}

.t-center {
  text-align: center;
}

.t-right {
  text-align: right;
}

.tabular {
  font-variant-numeric: tabular-nums;
}

.strong {
  font-weight: 700;
  color: #fff;
}

.total-strip {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
  padding: 20px 4px 0;
  border-top: 2px solid rgba(255, 255, 255, 0.2);
}

.total-label {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.total-value {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: #fff;
}

.total-value small {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin-left: 4px;
}

.action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 32px;
  padding-top: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 24px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: transform 0.15s, box-shadow 0.15s, background 0.15s;
}

.btn:active {
  transform: scale(0.98);
}

.btn--ghost {
  background: #fff;
  color: #334155;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.btn--ghost:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn--primary {
  background: #0f172a;
  color: #fff;
  border: 1px solid #0f172a;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.25);
}

.btn--primary:hover {
  background: #020617;
  border-color: #020617;
}

.btn--danger {
  background: #fff;
  color: #be123c;
  border: 1px solid #fda4af;
  box-shadow: 0 1px 2px rgba(225, 29, 72, 0.05);
}

.btn--danger:hover:not(:disabled) {
  background: #fff1f2;
  border-color: #fb7185;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn:focus-visible {
  outline: 2px solid #0f172a;
  outline-offset: 2px;
}

/* Modal UI */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal {
  background: #fff;
  border-radius: 20px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal--small {
  max-width: 380px;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-head {
  padding: 24px 28px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.modal-head h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.modal-sub {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.modal-body {
  padding: 24px 28px;
  overflow-y: auto;
  max-height: calc(100vh - 200px);
}

.cancel-summary {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-bottom: 20px;
}

.cancel-summary h4 {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  text-transform: uppercase;
}

.summary-items {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
  font-size: 14px;
  color: #334155;
}

.summary-items li {
  margin-bottom: 6px;
  line-height: 1.4;
}

.summary-qty {
  color: #94a3b8;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.summary-total {
  margin: 0;
  padding-top: 12px;
  border-top: 1px dashed #cbd5e1;
  font-size: 15px;
  color: #0f172a;
}

.cancel-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cancel-form label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.req {
  color: #e11d48;
}

.reason-input {
  width: 100%;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-family: inherit;
  font-size: 14px;
  color: #0f172a;
  resize: vertical;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.reason-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.modal-foot {
  padding: 20px 28px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.toast-popup {
  position: fixed;
  bottom: 24px;
  right: 24px;
  background: #0f172a;
  color: #fff;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.2);
  z-index: 10000;
  animation: toastIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes toastIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 640px) {
  .table-head {
    display: none;
  }
  
  .toast-popup {
    left: 20px;
    right: 20px;
    bottom: 20px;
    text-align: center;
  }

  .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 18px 16px;
  }

  .cell-qty::before {
    content: 'Số lượng: ';
    color: #94a3b8;
    font-weight: 600;
    font-size: 12px;
  }

  .cell-price::before {
    content: 'Đơn giá: ';
    color: #94a3b8;
    font-weight: 600;
    font-size: 12px;
  }

  .cell-sub::before {
    content: 'Thành tiền: ';
    color: #94a3b8;
    font-weight: 600;
    font-size: 12px;
  }

  .t-center,
  .t-right {
    text-align: left;
  }

  .timeline-rail {
    left: 8%;
    right: 8%;
  }

  .timeline-label {
    font-size: 10px;
  }
}
</style>
