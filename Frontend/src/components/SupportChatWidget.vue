<template>
  <div v-if="visible" class="support-wrap">
    <Transition name="panel">
      <aside
        v-if="open"
        class="support-panel"
        role="dialog"
        aria-label="Chat hỗ trợ Kesn"
        aria-modal="true"
      >
        <header class="support-head">
          <div class="support-head__title">
            <span class="support-dot" aria-hidden="true" />
            <div>
              <strong>Hỗ trợ Kesn</strong>
              <span class="support-sub">{{ enabled ? 'Trợ lý AI' : 'Tạm tắt' }}</span>
            </div>
          </div>
          <button type="button" class="icon-close" aria-label="Đóng chat" @click="open = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" />
            </svg>
          </button>
        </header>

        <div ref="scrollRef" class="support-messages">
          <p v-if="!enabled" class="banner-off">
            Chat AI chưa bật trên server. Liên hệ quản trị hoặc dùng trang
            <router-link to="/contact">Liên hệ</router-link>.
          </p>
          <template v-else>
            <div v-if="messages.length === 0" class="bubble-row bubble-row--bot">
              <div class="bubble bubble--bot">
                Xin chào! Mình là trợ lý Kesn Store. Bạn cần hỗ trợ về đặt hàng, size giày hay giao hàng?
              </div>
            </div>
            <div
              v-for="(m, i) in messages"
              :key="i"
              class="bubble-row"
              :class="m.role === 'user' ? 'bubble-row--user' : 'bubble-row--bot'"
            >
              <div class="bubble" :class="m.role === 'user' ? 'bubble--user' : 'bubble--bot'">
                {{ m.text }}
              </div>
            </div>
            <div v-if="sending" class="bubble-row bubble-row--bot">
              <div class="bubble bubble--bot bubble--typing">Đang trả lời…</div>
            </div>
          </template>
        </div>

        <form class="support-input" @submit.prevent="send">
          <input
            v-model="draft"
            type="text"
            class="input"
            :placeholder="enabled ? 'Nhập câu hỏi…' : 'Chat đang tắt'"
            :disabled="!enabled || sending"
            maxlength="2000"
            autocomplete="off"
          />
          <button type="submit" class="send" :disabled="!enabled || sending || !draft.trim()">
            Gửi
          </button>
        </form>
      </aside>
    </Transition>

    <button
      type="button"
      class="fab"
      :class="{ 'fab--open': open }"
      aria-label="Mở chat hỗ trợ"
      @click="toggle"
    >
      <svg v-if="!open" class="fab-ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
        <path d="M21 11.5a8.38 8.38 0 01-.9 3.8 8.5 8.5 0 01-7.6 4.7 8.38 8.38 0 01-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 01-.9-3.8 8.5 8.5 0 014.7-7.6 8.38 8.38 0 013.8-.9h.5a8.48 8.48 0 018 8.5z" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      <svg v-else class="fab-ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" />
      </svg>
    </button>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { sendSupportChat, getSupportChatStatus } from '../api/services/supportChatService'

const route = useRoute()
const open = ref(false)
const draft = ref('')
const sending = ref(false)
const enabled = ref(true)
/** Chỉ lưu hội thoại thật (user/model) — lời chào tĩnh ở template */
const messages = ref([])
const scrollRef = ref(null)

const visible = computed(() => !route.path.startsWith('/admin'))

async function checkStatus() {
  try {
    const s = await getSupportChatStatus()
    enabled.value = !!s.enabled
  } catch {
    enabled.value = false
  }
}

function toggle() {
  open.value = !open.value
  if (open.value) checkStatus()
}

async function send() {
  const t = draft.value.trim()
  if (!t || !enabled.value || sending.value) return
  draft.value = ''
  messages.value.push({ role: 'user', text: t })
  sending.value = true
  await scrollToBottom()
  try {
    const payload = messages.value.map((m) => ({ role: m.role, text: m.text }))
    const { reply } = await sendSupportChat(payload)
    messages.value.push({ role: 'model', text: reply || 'Không có phản hồi.' })
  } catch (e) {
    const msg = e.response?.data?.message || 'Không gửi được tin. Thử lại sau.'
    messages.value.push({ role: 'model', text: msg })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(
  () => messages.value.length,
  () => scrollToBottom()
)

onMounted(() => {
  checkStatus()
})
</script>

<style scoped>
.support-wrap {
  position: fixed;
  z-index: 9998;
  right: 20px;
  bottom: 20px;
  font-family: inherit;
}

.support-panel {
  position: absolute;
  right: 0;
  bottom: 72px;
  width: min(100vw - 32px, 380px);
  height: min(520px, calc(100vh - 120px));
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.22);
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-enter-active,
.panel-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.panel-enter-from,
.panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

.support-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
}

.support-head__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.support-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #4ade80;
  box-shadow: 0 0 0 3px rgba(74, 222, 128, 0.35);
}

.support-head strong {
  display: block;
  font-size: 0.95rem;
}

.support-sub {
  font-size: 0.72rem;
  opacity: 0.85;
}

.icon-close {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.icon-close svg {
  width: 18px;
  height: 18px;
}

.support-messages {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: #f8fafc;
}

.banner-off {
  margin: 0;
  padding: 12px;
  font-size: 0.85rem;
  color: #64748b;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
  line-height: 1.5;
}

.banner-off a {
  color: #c2410c;
  font-weight: 600;
}

.bubble-row {
  display: flex;
  margin-bottom: 10px;
}

.bubble-row--user {
  justify-content: flex-end;
}

.bubble-row--bot {
  justify-content: flex-start;
}

.bubble {
  max-width: 88%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 0.875rem;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble--user {
  background: #4f46e5;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.bubble--bot {
  background: #fff;
  color: #334155;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 4px;
}

.bubble--typing {
  color: #64748b;
  font-style: italic;
}

.support-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #e2e8f0;
  background: #fff;
}

.input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.875rem;
  outline: none;
}

.input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.send {
  padding: 10px 16px;
  border: none;
  border-radius: 12px;
  background: #0f172a;
  color: #fff;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
}

.send:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 10px 30px rgba(79, 70, 229, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.fab:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 36px rgba(79, 70, 229, 0.5);
}

.fab--open {
  background: #334155;
  box-shadow: 0 8px 24px rgba(51, 65, 85, 0.4);
}

.fab-ico {
  width: 26px;
  height: 26px;
}

@media (max-width: 480px) {
  .support-wrap {
    right: 12px;
    bottom: 12px;
  }

  .support-panel {
    bottom: 68px;
    width: calc(100vw - 24px);
    height: min(70vh, 480px);
  }
}
</style>
