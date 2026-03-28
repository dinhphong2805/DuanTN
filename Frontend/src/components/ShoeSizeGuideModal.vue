<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="size-guide-backdrop"
      role="presentation"
      @click.self="close"
    >
      <div
        class="size-guide-dialog"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="titleId"
      >
        <button type="button" class="size-guide-close" aria-label="Đóng" @click="close">
          ×
        </button>

        <div class="size-guide-layout">
          <div class="size-guide-instructions">
            <h2 :id="titleId" class="size-guide-title">{{ copy.title }}</h2>
            <p class="size-guide-subtitle">{{ copy.subtitle }}</p>
            <p class="size-guide-text">{{ copy.measureText }}</p>

            <div class="size-guide-illu" aria-hidden="true">
              <svg viewBox="0 0 200 100" class="foot-svg">
                <rect x="8" y="72" width="184" height="6" rx="1" fill="#e5e7eb" />
                <line x1="12" y1="72" x2="12" y2="84" stroke="#9ca3af" stroke-width="1" />
                <line x1="188" y1="72" x2="188" y2="84" stroke="#9ca3af" stroke-width="1" />
                <path
                  d="M28 68 Q32 40 48 32 Q70 22 95 28 Q118 34 132 48 Q142 58 148 72 L28 72 Z"
                  fill="#f3f4f6"
                  stroke="#111827"
                  stroke-width="1.2"
                />
                <line
                  x1="32"
                  y1="38"
                  x2="140"
                  y2="68"
                  stroke="#2563eb"
                  stroke-width="1.5"
                  stroke-dasharray="4 3"
                />
                <polygon points="32,38 38,34 38,42" fill="#2563eb" />
                <polygon points="140,68 134,64 134,72" fill="#2563eb" />
              </svg>
            </div>

            <div class="size-guide-notes">
              <p class="size-guide-notes-title">Lưu ý</p>
              <ul>
                <li v-for="(n, i) in copy.notes" :key="i">{{ n }}</li>
              </ul>
            </div>
          </div>

          <div class="size-guide-tables">
            <div
              v-for="sec in sections"
              :key="sec.label"
              class="size-guide-block"
            >
              <h3 class="size-guide-cat">{{ sec.label }}</h3>
              <table class="size-guide-table">
                <thead>
                  <tr>
                    <th>Size</th>
                    <th>Chiều dài chân</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, idx) in sec.rows" :key="sec.label + row.size + idx">
                    <td>
                      {{ row.size }}
                      <span v-if="row.note" class="size-guide-hint">{{ row.note }}</span>
                    </td>
                    <td>{{ row.cm }} cm</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { shoeSizeGuideSections, shoeSizeGuideCopy } from '../data/shoeSizeChart'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue'])

const copy = shoeSizeGuideCopy
const sections = shoeSizeGuideSections
const titleId = 'shoe-size-guide-title'

function close() {
  emit('update:modelValue', false)
}

function onKeydown(e) {
  if (e.key === 'Escape' && props.modelValue) {
    e.preventDefault()
    close()
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (typeof document === 'undefined') return
    document.body.style.overflow = open ? 'hidden' : ''
  },
  { immediate: true },
)

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<style scoped>
.size-guide-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 24px 16px 32px;
  overflow-y: auto;
  box-sizing: border-box;
}

.size-guide-dialog {
  position: relative;
  width: 100%;
  max-width: 920px;
  margin: auto;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.12);
  border: 1px solid #e5e7eb;
  padding: 22px 22px 26px;
  box-sizing: border-box;
}

.size-guide-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 999px;
  background: #f3f4f6;
  color: #374151;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  z-index: 2;
}

.size-guide-close:hover {
  background: #e5e7eb;
}

.size-guide-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr);
  gap: 24px;
  padding-top: 8px;
}

.size-guide-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 800;
  color: #111827;
  letter-spacing: -0.02em;
}

.size-guide-subtitle {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
}

.size-guide-text {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.55;
  color: #6b7280;
}

.size-guide-illu {
  margin-bottom: 16px;
  border-radius: 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  padding: 12px;
}

.foot-svg {
  width: 100%;
  max-height: 120px;
  display: block;
}

.size-guide-notes-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 700;
  color: #111827;
}

.size-guide-notes ul {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.5;
  color: #6b7280;
}

.size-guide-notes li + li {
  margin-top: 6px;
}

.size-guide-tables {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.size-guide-cat {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #111827;
}

.size-guide-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.size-guide-table th,
.size-guide-table td {
  border: 1px solid #e5e7eb;
  padding: 8px 10px;
  text-align: left;
}

.size-guide-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.size-guide-table td {
  color: #111827;
}

.size-guide-hint {
  display: block;
  font-size: 11px;
  font-weight: 500;
  color: #6b7280;
  margin-top: 2px;
}

@media (max-width: 720px) {
  .size-guide-layout {
    grid-template-columns: 1fr;
  }

  .size-guide-dialog {
    padding: 18px 16px 20px;
  }
}
</style>
