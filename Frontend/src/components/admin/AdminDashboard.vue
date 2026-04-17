<template>
  <div class="stats-page">
    <header class="page-head d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom">
      <div class="page-head__text">
        <h1 class="h3 fw-bold mb-1">Thống kê tổng quan</h1>
        <p class="text-secondary small mb-0">Theo dõi doanh thu, tăng trưởng và hiệu suất bán hàng</p>
      </div>
      <div class="page-head__actions d-flex gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm d-flex align-items-center gap-2 px-3" :disabled="loading" @click="loadData(true)">
          <i class="bi bi-arrow-clockwise" :class="{ 'spinner-border spinner-border-sm border-0' : isRefreshing }"></i>
          Làm mới dữ liệu
        </button>
      </div>
    </header>

    <section v-if="loading" class="skeleton-wrap" aria-busy="true">
      <div class="skeleton-cards" />
      <div class="skeleton-bar" />
      <div class="skeleton-split" />
    </section>

    <p v-else-if="loadError" class="msg-error">{{ loadError }}</p>

    <div v-else class="dash-content" :class="{ 'is-refreshing': isRefreshing }">
      <!-- 4 thẻ kỳ: hôm nay / tuần / tháng / năm -->
      <section class="period-grid" aria-label="Thống kê theo kỳ">
        <article
          v-for="card in periodCardDefs"
          :key="card.key"
          class="period-card"
          :class="card.theme"
        >
          <div class="period-card__title">
            <span class="period-card__ico" aria-hidden="true">{{ card.icon }}</span>
            {{ card.title }}
          </div>
          <div class="period-card__money">{{ formatMoney(periodNum(card.key, 'revenue')) }}</div>
          <div class="period-card__foot">
            <div>
              <span>Sản phẩm</span>
              <strong>{{ periodNum(card.key, 'itemsSold') }}</strong>
            </div>
            <div>
              <span>Đơn thành công</span>
              <strong>{{ periodNum(card.key, 'ordersSuccess') }}</strong>
            </div>
            <div>
              <span>Đơn hủy</span>
              <strong>{{ periodNum(card.key, 'ordersCancelled') }}</strong>
            </div>
            <div>
              <span>Đơn trả</span>
              <strong>{{ periodNum(card.key, 'ordersReturned') }}</strong>
            </div>
          </div>
        </article>
      </section>

      <!-- Bộ lọc -->
      <section class="filter-card card shadow-sm border-0 p-3 mb-4 d-block">
        <div class="d-flex flex-wrap align-items-center gap-3">
          <div class="fw-bold text-dark d-flex align-items-center gap-2">
            <i class="bi bi-funnel"></i> Bộ lọc
          </div>
          <div class="filter-presets d-flex flex-wrap gap-2 flex-grow-1">
            <button
              v-for="p in presetButtons"
              :key="p.value"
              type="button"
              class="btn btn-sm rounded-pill px-3 fw-bold"
              :class="filterPreset === p.value ? 'btn-dark shadow-sm' : 'btn-light text-secondary border'"
              @click="setPreset(p.value)"
            >
              {{ p.label }}
            </button>
          </div>
          <button type="button" class="btn btn-primary btn-sm px-4 fw-bold shadow-sm" @click="exportBestSellingCsv">
            <i class="bi bi-file-earmark-excel me-1"></i> Xuất EXCEL
          </button>
        </div>
        <div v-if="filterPreset === 'custom'" class="filter-custom">
          <label class="date-field">
            <span>Từ ngày</span>
            <input v-model="customFrom" type="date" class="date-input" />
          </label>
          <label class="date-field">
            <span>Đến ngày</span>
            <input v-model="customTo" type="date" class="date-input" />
          </label>
          <button type="button" class="btn-apply" @click="applyCustom">Áp dụng</button>
        </div>
      </section>

      <!-- Bảng bán chạy + pie -->
      <div class="split-row split-row--main">
        <section class="panel panel--table">
          <h3 class="section-title section-title--accent">
            Danh sách sản phẩm bán chạy theo {{ rangeLabelVi }}
          </h3>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Ảnh</th>
                  <th>Tên sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Giá tiền</th>
                  <th>Kích cỡ</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, i) in pagedBestSelling" :key="row.productName + i">
                  <td class="td-img">
                    <img v-if="imgSrc(row.imageUrl)" :src="imgSrc(row.imageUrl)" alt="" class="thumb" />
                    <span v-else class="thumb-placeholder">—</span>
                  </td>
                  <td>{{ row.productName }}</td>
                  <td>{{ row.quantity }}</td>
                  <td>{{ formatMoney(row.unitPrice) }}</td>
                  <td>{{ row.sizes || '—' }}</td>
                </tr>
                <tr v-if="!bestSelling.length">
                  <td colspan="5" class="td-empty">Chưa có dữ liệu trong khoảng đã chọn.</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="bestSelling.length" class="table-footer">
            <label class="page-size">
              Xem
              <select v-model.number="pageSize" class="page-size-select">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="15">15</option>
              </select>
              sản phẩm
            </label>
            <div class="pager">
              <button type="button" class="pager-btn" :disabled="bestPage <= 1" @click="bestPage--">‹</button>
              <span
                v-for="n in bestTotalPages"
                :key="n"
                class="pager-num"
                :class="{ current: n === bestPage }"
                role="button"
                tabindex="0"
                @click="bestPage = n"
                @keydown.enter.prevent="bestPage = n"
              >
                {{ n }}
              </span>
              <button
                type="button"
                class="pager-btn"
                :disabled="bestPage >= bestTotalPages"
                @click="bestPage++"
              >
                ›
              </button>
            </div>
          </div>
        </section>

        <section class="panel panel--chart">
          <h3 class="section-title">Biểu đồ trạng thái {{ rangeLabelVi }}</h3>
          <!-- Canvas cố định chiều cao; legend tách ra ngoài để không tràn đè hàng dưới -->
          <div class="chart-canvas-wrap">
            <Pie v-if="pieHasData" :data="pieChartData" :options="pieOptions" />
            <ul v-else class="pie-legend pie-legend--empty">
              <li v-for="s in orderStatusPie" :key="s.key" class="pie-legend__row">
                <span class="dot" :style="{ background: pieColor(s.key) }" />
                <span>{{ s.label }}</span>
                <span class="pct">{{ Number(s.percent).toFixed(2) }}%</span>
              </li>
            </ul>
          </div>
          <ul v-if="pieHasData" class="pie-legend pie-legend--below">
            <li v-for="s in orderStatusPie" :key="'l-' + s.key" class="pie-legend__row">
              <span class="dot" :style="{ background: pieColor(s.key) }" />
              <span>{{ s.label }}</span>
              <span class="pct">{{ Number(s.percent).toFixed(2) }}%</span>
            </li>
          </ul>
        </section>
      </div>

      <!-- Tồn kho thấp + tăng trưởng -->
      <div class="split-row split-row--bottom">
        <section class="panel panel--table">
          <div class="panel-head">
            <h3 class="section-title">Danh sách sản phẩm sắp hết hàng</h3>
            <select v-model.number="lowStockLimit" class="low-limit">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
          </div>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Ảnh</th>
                  <th>Tên sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Giá tiền</th>
                  <th>Kích cỡ</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, i) in lowStockDisplay" :key="'low-' + i">
                  <td class="td-img">
                    <img v-if="imgSrc(row.imageUrl)" :src="imgSrc(row.imageUrl)" alt="" class="thumb" />
                    <span v-else class="thumb-placeholder">—</span>
                  </td>
                  <td>{{ row.productName }}</td>
                  <td>{{ row.quantity }}</td>
                  <td>{{ formatMoney(row.unitPrice) }}</td>
                  <td>{{ row.sizes || '—' }}</td>
                </tr>
                <tr v-if="!lowStockDisplay.length">
                  <td colspan="5" class="td-empty td-empty--soft">Chưa có sản phẩm sắp hết hàng.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section class="growth-panel">
          <div class="growth-head">
            <h3>Tốc độ tăng trưởng của cửa hàng</h3>
            <button type="button" class="btn-refresh" title="Làm mới" @click="reload">↻</button>
          </div>
          <ul class="growth-list">
            <li v-for="(row, idx) in growthPanel" :key="idx" class="growth-row">
              <span class="growth-ico" aria-hidden="true">▮</span>
              <span class="growth-label">{{ row.label }}</span>
              <span class="growth-value">{{ formatGrowthValue(row) }}</span>
              <span class="growth-trend" :class="row.trendPct >= 0 ? 'up' : 'down'">
                {{ row.trendPct >= 0 ? '+' : '' }}{{ Number(row.trendPct).toFixed(2) }}%
              </span>
            </li>
          </ul>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js'
import { getStatistics } from '../../api/services/adminService'
import { API_BASE_URL } from '../../api/config'

ChartJS.register(ArcElement, Tooltip, Legend)

const API_ORIGIN = API_BASE_URL.replace(/\/?api\/?$/i, '')

const loading = ref(true)
const isRefreshing = ref(false)
const loadError = ref('')
const periodCards = ref({})
const bestSelling = ref([])
const orderStatusPie = ref([])
const growthPanel = ref([])
const lowStock = ref([])

const filterPreset = ref('day')
const customFrom = ref('')
const customTo = ref('')
const pageSize = ref(5)
const bestPage = ref(1)
const lowStockLimit = ref(10)

const periodCardDefs = [
  { key: 'today', title: 'Hôm nay', icon: '📅', theme: 'period-card--teal' },
  { key: 'week', title: 'Tuần này', icon: '📚', theme: 'period-card--orange' },
  { key: 'month', title: 'Tháng này', icon: '📋', theme: 'period-card--blue' },
  { key: 'year', title: 'Năm nay', icon: '📊', theme: 'period-card--green' },
]

const presetButtons = [
  { value: 'day', label: 'NGÀY' },
  { value: 'week', label: 'TUẦN' },
  { value: 'month', label: 'THÁNG' },
  { value: 'year', label: 'NĂM' },
  { value: 'custom', label: 'TÙY CHỈNH' },
]

const rangeLabelVi = computed(() => {
  const m = { day: 'ngày', week: 'tuần', month: 'tháng', year: 'năm', custom: 'tùy chỉnh' }
  return m[filterPreset.value] || 'ngày'
})

const PIE_COLORS = {
  pending: '#f59e0b',
  shipping: '#eab308',
  delivered: '#86efac',
  cancelled: '#ef4444',
  returned: '#a855f7',
  unknown: '#94a3b8',
}

function pieColor(key) {
  return PIE_COLORS[key] || '#cbd5e1'
}

const pieHasData = computed(() =>
  orderStatusPie.value.some((s) => Number(s.count) > 0),
)

const pieChartData = computed(() => ({
  labels: orderStatusPie.value.map((s) => s.label),
  datasets: [
    {
      data: orderStatusPie.value.map((s) => Number(s.count) || 0),
      backgroundColor: orderStatusPie.value.map((s) => pieColor(s.key)),
      borderWidth: 0,
    },
  ],
}))

const pieOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
  },
}

const bestTotalPages = computed(() => {
  const n = bestSelling.value.length
  if (!n) return 1
  return Math.max(1, Math.ceil(n / pageSize.value))
})

const pagedBestSelling = computed(() => {
  const start = (bestPage.value - 1) * pageSize.value
  return bestSelling.value.slice(start, start + pageSize.value)
})

const lowStockDisplay = computed(() => lowStock.value.slice(0, lowStockLimit.value))

watch([pageSize, bestSelling], () => {
  bestPage.value = 1
})

watch(bestTotalPages, (max) => {
  if (bestPage.value > max) bestPage.value = max
})

function periodNum(key, field) {
  const block = periodCards.value[key]
  if (!block) return 0
  const v = block[field]
  if (v == null) return 0
  return typeof v === 'string' ? Number(v) : v
}

function formatMoney(n) {
  const x = Number(n || 0)
  return x.toLocaleString('vi-VN') + ' đ'
}

function formatGrowthValue(row) {
  if (row.type === 'money') return formatMoney(row.value)
  return String(row.value ?? 0)
}

function imgSrc(url) {
  if (!url) return ''
  const first = String(url).split('|')[0].trim()
  if (!first) return ''
  if (first.startsWith('http')) return first
  return API_ORIGIN + (first.startsWith('/') ? first : '/' + first)
}

function statsParams() {
  const p = { range: filterPreset.value }
  if (filterPreset.value === 'custom' && customFrom.value && customTo.value) {
    const from = new Date(customFrom.value + 'T00:00:00')
    const to = new Date(customTo.value + 'T23:59:59.999')
    p.from = from.toISOString()
    p.to = to.toISOString()
  }
  return p
}

async function load(refresh = false) {
  if (refresh) isRefreshing.value = true
  else loading.value = true
  
  loadError.value = ''
  try {
    const data = await getStatistics(statsParams())
    periodCards.value = data.periodCards || {}
    bestSelling.value = Array.isArray(data.bestSelling) ? data.bestSelling : []
    orderStatusPie.value = Array.isArray(data.orderStatusPie) ? data.orderStatusPie : []
    growthPanel.value = Array.isArray(data.growthPanel) ? data.growthPanel : []
    lowStock.value = Array.isArray(data.lowStock) ? data.lowStock : []
  } catch (e) {
    loadError.value =
      e.response?.data?.message || e.message || 'Không thể tải thống kê. Kiểm tra đăng nhập admin.'
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function handlePresetChange(val) {
  filterPreset.value = val
  load(true)
}

function reload() {
  load(true)
}

function setPreset(v) {
  filterPreset.value = v
  if (v !== 'custom') {
    load(true)
  } else if (customFrom.value && customTo.value) {
    load(true)
  }
}

function applyCustom() {
  if (!customFrom.value || !customTo.value) return
  load(true)
}

function exportBestSellingCsv() {
  const rows = [
    ['Ảnh', 'Tên sản phẩm', 'Số lượng', 'Giá tiền', 'Kích cỡ'].join(','),
    ...bestSelling.value.map((r) =>
      [
        `"${(r.imageUrl || '').replace(/"/g, '""')}"`,
        `"${String(r.productName || '').replace(/"/g, '""')}"`,
        r.quantity,
        r.unitPrice,
        `"${String(r.sizes || '').replace(/"/g, '""')}"`,
      ].join(','),
    ),
  ]
  const bom = '\uFEFF'
  const blob = new Blob([bom + rows.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `san-pham-ban-chay-${filterPreset.value}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
}

onMounted(() => {
  const t = new Date()
  customTo.value = t.toISOString().slice(0, 10)
  const f = new Date(t)
  f.setDate(f.getDate() - 7)
  customFrom.value = f.toISOString().slice(0, 10)
  load()
})
</script>

<style scoped>
/* ============================================================
   AdminDashboard Scoped — Cac class dac thu cua trang Thong ke
   Style chung (data-table, pager, pager-btn...) dung tu admin_style.css
   ============================================================ */

.stats-page { max-width: 1400px; margin: 0 auto; }

/* Skeleton */

.skeleton-wrap { display: flex; flex-direction: column; gap: 18px; }
.skeleton-cards, .skeleton-bar, .skeleton-split {
  border-radius: 16px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%; animation: sk 1.4s ease-in-out infinite;
}
.skeleton-cards { height: 140px; } .skeleton-bar { height: 90px; } .skeleton-split { height: 360px; }
@keyframes sk { 0% { background-position: 100% 0; } 100% { background-position: -100% 0; } }

.msg-error { padding: 20px 24px; border-radius: 12px; background: #fff1f2; color: #be123c; border: 1px solid #fecdd3; font-weight: 600; }

/* Period Cards */
.period-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-bottom: 22px; }
.period-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 18px; padding: 20px 22px; box-shadow: 0 2px 10px rgba(15,23,42,.06); position: relative; overflow: hidden; transition: transform .15s, box-shadow .15s; }
.period-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(15,23,42,.1); }
.period-card::before { content: ""; position: absolute; top: 0; left: 0; right: 0; height: 3px; border-radius: 18px 18px 0 0; background: linear-gradient(90deg, #6366f1, #4f46e5); }
.period-card--teal::before { background: linear-gradient(90deg, #14b8a6, #0d9488); }
.period-card--orange::before { background: linear-gradient(90deg, #f59e0b, #d97706); }
.period-card--blue::before { background: linear-gradient(90deg, #3b82f6, #1d4ed8); }
.period-card--green::before { background: linear-gradient(90deg, #10b981, #059669); }
.period-card--teal   { border-color: #99f6e4; background: linear-gradient(160deg, #f0fdf9 0%, #fff 60%); }
.period-card--orange { border-color: #fde68a; background: linear-gradient(160deg, #fffbeb 0%, #fff 60%); }
.period-card--blue   { border-color: #bfdbfe; background: linear-gradient(160deg, #eff6ff 0%, #fff 60%); }
.period-card--green  { border-color: #a7f3d0; background: linear-gradient(160deg, #ecfdf5 0%, #fff 60%); }
.period-card__title { font-size: .75rem; font-weight: 700; text-transform: uppercase; letter-spacing: .07em; color: #64748b; margin-bottom: 10px; display: flex; align-items: center; gap: 7px; }
.period-card__ico { font-size: 1rem; }
.period-card__money { font-size: 1.45rem; font-weight: 900; color: #0f172a; letter-spacing: -.03em; margin-bottom: 16px; line-height: 1; font-variant-numeric: tabular-nums; }
.period-card__foot { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; font-size: .78rem; }
.period-card__foot div { display: flex; flex-direction: column; gap: 2px; }
.period-card__foot span { color: #94a3b8; font-weight: 500; }
.period-card__foot strong { color: #0f172a; font-weight: 700; font-size: .9rem; font-variant-numeric: tabular-nums; }

/* Filter Card */
.filter-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 18px 22px; margin-bottom: 22px; box-shadow: 0 1px 4px rgba(15,23,42,.05); }
.filter-card__row { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; }
.filter-title { font-size: .9rem; font-weight: 800; color: #0f172a; letter-spacing: -.01em; margin: 0; white-space: nowrap; }
.filter-presets { display: flex; flex-wrap: wrap; gap: 6px; flex: 1; }
.preset-btn { padding: 7px 16px; border-radius: 999px; border: 1.5px solid #e2e8f0; background: #f8fafc; font-size: .78rem; font-weight: 700; color: #64748b; cursor: pointer; font-family: inherit; transition: all .15s; letter-spacing: .04em; }
.preset-btn:hover { background: #f1f5f9; border-color: #cbd5e1; color: #334155; }
.preset-btn.active { background: #0f172a; border-color: #0f172a; color: #fff; box-shadow: 0 2px 8px rgba(15,23,42,.22); }
.btn-export { padding: 8px 16px; border-radius: 10px; border: none; background: linear-gradient(135deg, #6366f1, #4f46e5); color: #fff; font-size: .75rem; font-weight: 800; cursor: pointer; letter-spacing: .05em; font-family: inherit; box-shadow: 0 4px 12px rgba(99,102,241,.3); transition: filter .15s, transform .15s; }
.btn-export:hover { filter: brightness(1.08); transform: translateY(-1px); }
.filter-custom { display: flex; flex-wrap: wrap; align-items: flex-end; gap: 12px; margin-top: 14px; padding-top: 14px; border-top: 1px solid #f1f5f9; }
.date-field { display: flex; flex-direction: column; gap: 5px; font-size: .82rem; font-weight: 600; color: #475569; }
.date-input { padding: 9px 12px; border: 1.5px solid #e2e8f0; border-radius: 9px; font-size: .875rem; font-family: inherit; color: #0f172a; outline: none; background: #f8fafc; transition: border-color .15s, box-shadow .15s; }
.date-input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.15); }
.btn-apply { padding: 9px 18px; border-radius: 10px; border: none; background: #6366f1; color: #fff; font-size: .875rem; font-weight: 700; cursor: pointer; font-family: inherit; transition: filter .15s; }
.btn-apply:hover { filter: brightness(1.08); }

/* Split Layout */
.split-row { display: grid; gap: 18px; margin-bottom: 18px; }
.split-row--main   { grid-template-columns: 1fr 340px; }
.split-row--bottom { grid-template-columns: 1fr 300px; }
@media (max-width: 1024px) { .split-row--main, .split-row--bottom { grid-template-columns: 1fr; } }

/* Panel */
.panel { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px 22px; box-shadow: 0 2px 8px rgba(15,23,42,.05); overflow: hidden; }
.section-title { font-size: .95rem; font-weight: 800; color: #0f172a; margin: 0 0 16px; letter-spacing: -.01em; }
.section-title--accent { padding-left: 12px; border-left: 3px solid #6366f1; }
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.table-wrap { overflow-x: auto; }
.thumb { width: 42px; height: 42px; object-fit: cover; border-radius: 8px; border: 1px solid #e2e8f0; }
.thumb-placeholder { display: inline-block; width: 42px; height: 42px; background: #f1f5f9; border-radius: 8px; text-align: center; line-height: 42px; color: #94a3b8; }
.td-img { width: 60px; }
.td-empty { text-align: center; padding: 24px 12px; color: #94a3b8; font-size: .875rem; }
.table-footer { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; padding-top: 14px; margin-top: 4px; border-top: 1px solid #f1f5f9; }
.page-size { display: flex; align-items: center; gap: 8px; font-size: .84rem; color: #475569; }
.page-size-select { padding: 5px 9px; border-radius: 8px; border: 1.5px solid #e2e8f0; font-size: .8125rem; color: #334155; background: #fff; cursor: pointer; }
.pager-num { width: 32px; height: 32px; border-radius: 8px; display: inline-flex; align-items: center; justify-content: center; font-size: .84rem; font-weight: 600; color: #64748b; cursor: pointer; transition: all .15s; }
.pager-num:hover { background: #f1f5f9; }
.pager-num.current { background: #0f172a; color: #fff; }
.low-limit { padding: 6px 10px; border-radius: 8px; border: 1.5px solid #e2e8f0; font-size: .8125rem; background: #f8fafc; cursor: pointer; }

/* Pie Chart */
.chart-canvas-wrap { height: 220px; display: flex; align-items: center; justify-content: center; }
.pie-legend { padding: 0; margin: 0; list-style: none; }
.pie-legend--below { padding-top: 14px; border-top: 1px solid #f1f5f9; margin-top: 12px; }
.pie-legend__row { display: flex; align-items: center; gap: 9px; padding: 6px 0; font-size: .84rem; border-bottom: 1px solid #f8fafc; }
.pie-legend__row:last-child { border-bottom: none; }
.dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.pct { margin-left: auto; font-weight: 700; font-variant-numeric: tabular-nums; color: #0f172a; }

/* Growth Panel */
.growth-panel { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px 22px; box-shadow: 0 2px 8px rgba(15,23,42,.05); }
.growth-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.growth-head h3 { font-size: .95rem; font-weight: 800; color: #0f172a; margin: 0; }
.btn-refresh { width: 32px; height: 32px; border-radius: 8px; background: #f1f5f9; border: none; font-size: 1.1rem; cursor: pointer; color: #475569; display: flex; align-items: center; justify-content: center; }
.btn-refresh:hover { background: #e2e8f0; }
.growth-list { padding: 0; margin: 0; list-style: none; }
.growth-row { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid #f1f5f9; font-size: .84rem; }
.growth-row:last-child { border-bottom: none; }
.growth-ico { color: #6366f1; font-size: .6rem; }
.growth-label { flex: 1; color: #475569; font-weight: 500; }
.growth-value { font-weight: 700; color: #0f172a; font-variant-numeric: tabular-nums; }
.growth-trend { font-size: .75rem; font-weight: 800; padding: 2px 7px; border-radius: 6px; }
.growth-trend.up { color: #047857; background: #d1fae5; }
.growth-trend.down { color: #be123c; background: #fee2e2; }

/* ---- Smooth glass-fade effect cho data refresh ---- */
.dash-content {
  transition: opacity 0.4s ease, filter 0.4s ease, transform 0.4s ease;
  will-change: opacity, filter, transform;
}

.dash-content.is-refreshing {
  opacity: 0.5;
  filter: blur(2px) grayscale(0.2);
  pointer-events: none;
  transform: scale(0.995);
}
</style>

