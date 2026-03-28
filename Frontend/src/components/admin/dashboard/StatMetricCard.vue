<template>
  <article class="metric-card" :class="{ 'metric-card--accent': accent }">
    <p class="metric-label">{{ label }}</p>
    <p class="metric-value">{{ displayValue }}</p>
    <p v-if="trendText !== null" class="metric-trend" :class="trendClass">
      <span class="metric-trend-arrow">{{ trendArrow }}</span>
      {{ trendText }}
    </p>
    <p v-else-if="hint" class="metric-hint">{{ hint }}</p>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  /** % thay đổi so với kỳ trước (số, có thể âm) */
  trendPct: { type: Number, default: null },
  /** Hiển thị dạng điểm % (ví dụ chuyển đổi) thay vì % tăng trưởng */
  trendPoints: { type: Number, default: null },
  accent: { type: Boolean, default: false },
  hint: { type: String, default: '' },
})

const displayValue = computed(() => props.value)

const trendText = computed(() => {
  if (props.trendPoints != null && !Number.isNaN(props.trendPoints)) {
    const v = Number(props.trendPoints)
    const sign = v > 0 ? '+' : ''
    return `${sign}${v.toFixed(1)} điểm % so với kỳ trước`
  }
  if (props.trendPct == null || Number.isNaN(props.trendPct)) return null
  const v = Number(props.trendPct)
  const sign = v > 0 ? '+' : ''
  return `${sign}${v.toFixed(1)}% so với kỳ trước`
})

const trendClass = computed(() => {
  if (props.trendPoints != null) {
    if (props.trendPoints > 0.05) return 'metric-trend--up'
    if (props.trendPoints < -0.05) return 'metric-trend--down'
    return 'metric-trend--flat'
  }
  if (props.trendPct == null) return ''
  if (props.trendPct > 0.5) return 'metric-trend--up'
  if (props.trendPct < -0.5) return 'metric-trend--down'
  return 'metric-trend--flat'
})

const trendArrow = computed(() => {
  if (props.trendPoints != null) {
    if (props.trendPoints > 0.05) return '▲'
    if (props.trendPoints < -0.05) return '▼'
    return '◆'
  }
  if (props.trendPct == null) return ''
  if (props.trendPct > 0.5) return '▲'
  if (props.trendPct < -0.5) return '▼'
  return '◆'
})
</script>

<style scoped>
.metric-card {
  background: #fff;
  border-radius: 16px;
  padding: 22px 20px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06), 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(226, 232, 240, 0.9);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08), 0 16px 40px rgba(15, 23, 42, 0.08);
}

.metric-card--accent {
  background: linear-gradient(145deg, #0f172a 0%, #1e293b 100%);
  border-color: #0f172a;
}

.metric-card--accent .metric-label {
  color: #94a3b8;
}

.metric-card--accent .metric-value {
  color: #f8fafc;
}

.metric-card--accent .metric-trend {
  color: #cbd5e1;
}

.metric-label {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  letter-spacing: 0.02em;
}

.metric-value {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.metric-trend {
  margin: 10px 0 0;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.metric-trend-arrow {
  font-size: 10px;
}

.metric-trend--up {
  color: #059669;
}

.metric-trend--down {
  color: #dc2626;
}

.metric-trend--flat {
  color: #64748b;
}

.metric-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #94a3b8;
}
</style>
