<template>
  <div class="charts-panel">
    <section class="chart-card">
      <header class="chart-head">
        <h3>Doanh thu theo ngày</h3>
        <p class="chart-sub">Đường xu hướng trong khoảng đã chọn</p>
      </header>
      <div v-if="hasTimeline" class="chart-canvas">
        <Line :data="lineData" :options="lineOptions" />
      </div>
      <p v-else class="chart-empty">Chưa có đơn trong khoảng thời gian này.</p>
    </section>

    <section class="chart-card">
      <header class="chart-head">
        <h3>Số đơn hàng theo ngày</h3>
        <p class="chart-sub">Tổng đơn tạo mỗi ngày</p>
      </header>
      <div v-if="hasTimeline" class="chart-canvas">
        <Bar :data="barData" :options="barOptions" />
      </div>
      <p v-else class="chart-empty">Chưa có đơn trong khoảng thời gian này.</p>
    </section>

    <section class="chart-card chart-card--wide">
      <header class="chart-head">
        <h3>Doanh thu theo danh mục</h3>
        <p class="chart-sub">Tỷ trọng theo loại sản phẩm (top)</p>
      </header>
      <div v-if="hasCategories" class="chart-canvas chart-canvas--pie">
        <Doughnut :data="pieData" :options="pieOptions" />
      </div>
      <p v-else class="chart-empty">Chưa có dữ liệu danh mục.</p>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Line, Bar, Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  ArcElement,
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  ArcElement,
)

const props = defineProps({
  timeline: { type: Array, default: () => [] },
  revenueByCategory: { type: Array, default: () => [] },
})

const PALETTE = ['#3b82f6', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#6366f1']

function shortLabel(isoDate) {
  if (!isoDate) return ''
  const [y, m, d] = String(isoDate).split('-')
  return d && m ? `${d}/${m}` : isoDate
}

function formatMoneyTooltip(value) {
  return `${Number(value).toLocaleString('vi-VN')} VNĐ`
}

const hasTimeline = computed(() => props.timeline && props.timeline.length > 0)

const labels = computed(() => props.timeline.map((t) => shortLabel(t.date)))

const hasCategories = computed(
  () => props.revenueByCategory && props.revenueByCategory.some((c) => Number(c.revenue) > 0),
)

const lineData = computed(() => ({
  labels: labels.value,
  datasets: [
    {
      label: 'Doanh thu (VNĐ)',
      data: props.timeline.map((t) => Number(t.revenue || 0)),
      borderColor: '#2563eb',
      backgroundColor: 'rgba(37, 99, 235, 0.12)',
      fill: true,
      tension: 0.35,
      pointRadius: 3,
      pointHoverRadius: 6,
      borderWidth: 2,
    },
  ],
}))

const lineOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: { display: true, position: 'top', labels: { usePointStyle: true, boxWidth: 8 } },
    tooltip: {
      callbacks: {
        label: (ctx) => formatMoneyTooltip(ctx.parsed.y),
      },
    },
  },
  scales: {
    x: { grid: { display: false }, ticks: { maxRotation: 45, minRotation: 0 } },
    y: {
      beginAtZero: true,
      ticks: {
        callback: (v) => (Number(v) >= 1e6 ? `${(v / 1e6).toFixed(1)}M` : `${(v / 1e3).toFixed(0)}k`),
      },
    },
  },
}))

const barData = computed(() => ({
  labels: labels.value,
  datasets: [
    {
      label: 'Số đơn',
      data: props.timeline.map((t) => Number(t.orders || 0)),
      backgroundColor: 'rgba(15, 23, 42, 0.75)',
      borderRadius: 6,
      borderSkipped: false,
    },
  ],
}))

const barOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: true, position: 'top', labels: { usePointStyle: true, boxWidth: 8 } },
  },
  scales: {
    x: { grid: { display: false } },
    y: { beginAtZero: true, ticks: { stepSize: 1 } },
  },
}))

const pieData = computed(() => {
  const rows = [...props.revenueByCategory]
    .filter((r) => Number(r.revenue) > 0)
    .sort((a, b) => Number(b.revenue) - Number(a.revenue))
  const top = rows.slice(0, 8)
  const rest = rows.slice(8)
  let labelsList = top.map((r) => r.category || '—')
  let dataList = top.map((r) => Number(r.revenue))
  if (rest.length) {
    const sumRest = rest.reduce((s, r) => s + Number(r.revenue), 0)
    labelsList = [...labelsList, 'Khác']
    dataList = [...dataList, sumRest]
  }
  const colors = labelsList.map((_, i) => PALETTE[i % PALETTE.length])
  return {
    labels: labelsList,
    datasets: [
      {
        data: dataList,
        backgroundColor: colors,
        borderWidth: 2,
        borderColor: '#fff',
        hoverOffset: 8,
      },
    ],
  }
})

const pieOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'right',
      labels: { usePointStyle: true, boxWidth: 10, padding: 12 },
    },
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const v = Number(ctx.raw ?? 0)
          const total = ctx.dataset.data.reduce((a, b) => a + Number(b), 0)
          const pct = total ? ((v / total) * 100).toFixed(1) : 0
          return `${ctx.label}: ${formatMoneyTooltip(v)} (${pct}%)`
        },
      },
    },
  },
}))
</script>

<style scoped>
.charts-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-top: 8px;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 22px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
  transition: box-shadow 0.2s ease;
}

.chart-card:hover {
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.07);
}

.chart-card--wide {
  grid-column: span 2;
}

.chart-head {
  margin-bottom: 16px;
}

.chart-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.chart-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.chart-canvas {
  position: relative;
  height: 280px;
  width: 100%;
}

.chart-canvas--pie {
  height: 320px;
  max-width: 640px;
  margin: 0 auto;
}

.chart-empty {
  margin: 0;
  padding: 48px 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

@media (max-width: 900px) {
  .charts-panel {
    grid-template-columns: 1fr;
  }

  .chart-card--wide {
    grid-column: span 1;
  }
}
</style>
