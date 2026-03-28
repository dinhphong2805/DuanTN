<template>
  <div class="stats-page">
    <header class="page-head">
      <h1>Thống kê</h1>
      <p class="page-sub">Tổng quan doanh thu và đơn hàng Kesn Store</p>
    </header>

    <section v-if="loading" class="skeleton-wrap" aria-busy="true">
      <div class="skeleton-cards" />
      <div class="skeleton-bar" />
      <div class="skeleton-split" />
    </section>

    <p v-else-if="loadError" class="msg-error">{{ loadError }}</p>

    <template v-else>
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
      <section class="filter-card">
        <div class="filter-card__row">
          <h2 class="filter-title">Bộ lọc</h2>
          <div class="filter-presets" role="group" aria-label="Chọn khoảng thời gian">
            <button
              v-for="p in presetButtons"
              :key="p.value"
              type="button"
              class="preset-btn"
              :class="{ active: filterPreset === p.value }"
              @click="setPreset(p.value)"
            >
              {{ p.label }}
            </button>
          </div>
          <button type="button" class="btn-export" @click="exportBestSellingCsv">EXPORT TO EXCEL</button>
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
    </template>
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

async function load() {
  loading.value = true
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
  }
}

function reload() {
  load()
}

function setPreset(v) {
  filterPreset.value = v
  if (v !== 'custom') {
    load()
  } else if (customFrom.value && customTo.value) {
    load()
  }
}

function applyCustom() {
  if (!customFrom.value || !customTo.value) return
  load()
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
.stats-page {
  max-width: 1320px;
  margin: 0 auto;
}

.page-head h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}

.page-sub {
  margin: 6px 0 20px;
  color: #64748b;
  font-size: 14px;
}

.msg-error {
  padding: 14px 18px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  border-radius: 12px;
}

.skeleton-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-cards {
  min-height: 200px;
  border-radius: 12px;
  background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%);
  background-size: 200% 100%;
  animation: sh 1.2s ease-in-out infinite;
}

.skeleton-bar {
  height: 72px;
  border-radius: 12px;
  background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%);
  background-size: 200% 100%;
  animation: sh 1.2s ease-in-out infinite;
}

.skeleton-split {
  height: 320px;
  border-radius: 12px;
  background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%);
  background-size: 200% 100%;
  animation: sh 1.2s ease-in-out infinite;
}

@keyframes sh {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.period-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .period-grid {
    grid-template-columns: 1fr;
  }
}

.period-card {
  border-radius: 12px;
  padding: 18px 20px;
  color: #fff;
  text-align: center;
}

.period-card--teal {
  background: #0d9488;
}
.period-card--orange {
  background: #fb923c;
}
.period-card--blue {
  background: #2563eb;
}
.period-card--green {
  background: #059669;
}

.period-card__title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  opacity: 0.95;
}

.period-card__ico {
  font-size: 16px;
}

.period-card__money {
  font-size: 26px;
  font-weight: 800;
  margin: 12px 0 16px;
  letter-spacing: -0.02em;
}

.period-card__foot {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  font-size: 11px;
  border-top: 1px solid rgba(255, 255, 255, 0.25);
  padding-top: 12px;
}

.period-card__foot span {
  display: block;
  opacity: 0.85;
  margin-bottom: 4px;
}

.period-card__foot strong {
  font-size: 15px;
  font-weight: 700;
}

.filter-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 18px;
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.filter-card__row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.filter-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  min-width: 72px;
}

.filter-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.preset-btn {
  padding: 8px 14px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  background: #fff;
  font-size: 12px;
  font-weight: 700;
  color: #111827;
  cursor: pointer;
}

.preset-btn.active {
  background: #f97316;
  border-color: #f97316;
  color: #fff;
}

.btn-export {
  margin-left: auto;
  padding: 8px 14px;
  border-radius: 6px;
  border: 1px solid #16a34a;
  background: #fff;
  color: #16a34a;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}

.btn-export:hover {
  background: #f0fdf4;
}

.filter-custom {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #f3f4f6;
}

.date-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #374151;
}

.date-input {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 13px;
}

.btn-apply {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.split-row {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
  align-items: start;
}

.split-row--main {
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
}

.split-row--bottom {
  grid-template-columns: minmax(0, 2fr) minmax(260px, 1fr);
}

@media (max-width: 1024px) {
  .split-row--main,
  .split-row--bottom {
    grid-template-columns: 1fr;
  }
}

.panel {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  min-width: 0;
}

.panel--chart {
  display: flex;
  flex-direction: column;
}

.section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: #b91c1c;
}

.section-title--accent {
  color: #2563eb;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-head .section-title {
  margin: 0;
  color: #111827;
}

.low-limit {
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  font-size: 13px;
}

.table-wrap {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table thead {
  background: #f97316;
  color: #fff;
}

.data-table th,
.data-table td {
  padding: 10px 8px;
  text-align: left;
  border-bottom: 1px solid #f3f4f6;
}

.data-table th {
  font-weight: 700;
}

.td-img {
  width: 56px;
}

.thumb {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.thumb-placeholder {
  color: #9ca3af;
}

.td-empty {
  text-align: center;
  color: #6b7280;
  padding: 28px !important;
}

.td-empty--soft {
  color: #9ca3af;
}

.table-footer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  font-size: 13px;
  color: #4b5563;
}

.page-size-select {
  margin: 0 6px;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
}

.pager {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pager-btn {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background: #fff;
  cursor: pointer;
}

.pager-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pager-num {
  min-width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
}

.pager-num.current {
  background: #f97316;
  color: #fff;
  border-color: #f97316;
}

.chart-canvas-wrap {
  position: relative;
  height: 220px;
  width: 100%;
  flex-shrink: 0;
}

.pie-legend {
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: 12px;
}

.pie-legend--below {
  margin-top: 14px;
  flex-shrink: 0;
}

.pie-legend--empty {
  margin: 0;
  height: 100%;
  overflow: auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.pie-legend__row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.pie-legend .dot {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  flex-shrink: 0;
}

.pie-legend .pct {
  margin-left: auto;
  font-weight: 600;
  color: #374151;
}

.growth-panel {
  background: #18181b;
  border-radius: 12px;
  padding: 16px 18px;
  color: #fafafa;
  min-width: 0;
}

.growth-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.growth-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
}

.btn-refresh {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: #fff;
  color: #f97316;
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
}

.growth-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.growth-row {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 10px;
  align-items: center;
  background: #27272a;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 13px;
}

.growth-ico {
  color: #a1a1aa;
  font-size: 10px;
}

.growth-label {
  color: #e4e4e7;
}

.growth-value {
  font-weight: 700;
  text-align: right;
}

.growth-trend {
  font-weight: 700;
  min-width: 64px;
  text-align: right;
}

.growth-trend.up {
  color: #4ade80;
}

.growth-trend.down {
  color: #f87171;
}
</style>
