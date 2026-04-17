<template>
  <div class="users-page">
    <header class="page-head d-flex justify-content-between align-items-center mb-4">
      <div class="page-head__text">
        <h1 class="h3 fw-bold mb-1">Quản lý người dùng</h1>
        <p class="text-secondary small mb-0">Xem, lọc và chỉnh sửa hồ sơ cùng vai trò tài khoản</p>
      </div>
      <div class="page-head__actions d-flex gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm d-flex align-items-center gap-2 px-3" :disabled="loading" @click="load">
          <i class="bi bi-arrow-clockwise" :class="{ 'spinner-border spinner-border-sm border-0': isRefreshing }"></i>
          Làm mới
        </button>
        <button
          type="button"
          class="btn btn-primary btn-sm d-flex align-items-center gap-2 px-4 shadow-sm"
          :disabled="loading || filteredSorted.length === 0"
          @click="exportCsv"
        >
          <i class="bi bi-download"></i>
          Xuất dữ liệu
        </button>
      </div>
    </header>

    <section v-if="!loading || isRefreshing" class="kpi-grid mb-4" aria-label="Tổng quan người dùng">
      <article class="kpi-card shadow-sm border-0 h-100">
        <span class="kpi-label">Tổng tài khoản</span>
        <strong class="kpi-value">{{ stats.total }}</strong>
      </article>
      <article class="kpi-card kpi-card--blue shadow-sm border-0 h-100">
        <span class="kpi-label">Quản trị viên</span>
        <strong class="kpi-value">{{ stats.admin }}</strong>
      </article>
      <article class="kpi-card kpi-card--green shadow-sm border-0 h-100">
        <span class="kpi-label">Khách hàng</span>
        <strong class="kpi-value">{{ stats.customer }}</strong>
      </article>
    </section>

    <section v-if="users.length" class="toolbar card shadow-sm border-0 p-3 mb-4 d-block">
      <div class="row g-3 align-items-center">
        <div class="col-md-5">
          <div class="position-relative">
            <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 text-secondary"></i>
            <input
              v-model="searchQuery"
              type="search"
              class="form-control border-0 bg-light ps-5 py-2"
              placeholder="Tìm ID, email, họ tên..."
              autocomplete="off"
            />
          </div>
        </div>
        <div class="col-md-4">
          <div class="d-flex flex-wrap gap-2" role="group">
            <button
              v-for="f in roleFilters"
              :key="f.value"
              type="button"
              class="btn btn-sm px-3 rounded-pill fw-bold"
              :class="roleFilter === f.value ? 'btn-dark shadow-sm' : 'btn-light text-secondary border-0'"
              @click="roleFilter = f.value; page = 1"
            >
              {{ f.label }}
              <span v-if="f.value !== 'all'" class="badge bg-secondary ms-1">{{ countByRole(f.value) }}</span>
            </button>
          </div>
        </div>
        <div class="col-md-3 text-md-end">
          <div class="d-inline-flex align-items-center gap-2">
            <span class="small text-secondary fw-bold text-nowrap">Sắp xếp:</span>
            <select v-model="sortKey" class="form-select form-select-sm border-0 bg-light w-auto fw-medium">
              <option value="id_asc">ID tăng dần</option>
              <option value="id_desc">ID giảm dần</option>
              <option value="name_asc">Tên A → Z</option>
              <option value="email_asc">Email A → Z</option>
            </select>
          </div>
        </div>
      </div>
    </section>

    <section v-if="loading" class="skeleton-panel" aria-busy="true">
      <div class="sk-line sk-line--title" />
      <div v-for="n in 6" :key="n" class="sk-row" />
    </section>

    <div v-else-if="users.length" class="table-panel">
      <div class="table-panel__meta">
        <span>Hiển thị <strong>{{ pageStart }}–{{ pageEnd }}</strong> / {{ filteredSorted.length }} người</span>
        <div class="page-size">
          <label for="ups">Số dòng</label>
          <select id="ups" v-model.number="pageSize" class="sort-select sort-select--sm" @change="page = 1">
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
              <th class="th-id">ID</th>
              <th>Email</th>
              <th>Họ tên</th>
              <th>SĐT</th>
              <th>Vai trò</th>
              <th class="th-act">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in pagedUsers" :key="u.id" class="data-row">
              <td class="mono">#{{ u.id }}</td>
              <td>
                <div class="cell-email">{{ u.email }}</div>
              </td>
              <td class="cell-strong">{{ u.fullName || '—' }}</td>
              <td class="mono cell-phone">{{ u.phone || '—' }}</td>
              <td>
                <span class="role-badge" :class="normalizeRole(u.role)">{{ roleLabel(u.role) }}</span>
              </td>
              <td>
                <button type="button" class="btn-detail" @click="openDetail(u)">Chi tiết</button>
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
      <span class="empty-ico" aria-hidden="true">👤</span>
      Chưa có người dùng.
    </p>

    <div v-if="toast.message" class="toast" :class="toast.type" role="status">
      {{ toast.message }}
    </div>

    <Teleport to="body">
      <div v-if="selected" class="modal-overlay" @click.self="closeModal">
        <div class="modal modal--wide">
          <header class="modal-head">
            <div>
              <h3>Người dùng <span class="mono">#{{ selected.id }}</span></h3>
              <p class="modal-sub mono">{{ selected.email }}</p>
            </div>
            <span class="role-badge role-badge--lg" :class="normalizeRole(selected.role)">
              {{ roleLabel(selected.role) }}
            </span>
          </header>

          <div class="modal-body">
            <section class="info-block">
              <h4>Hồ sơ</h4>
              <label class="field">
                <span class="field-label">Họ và tên</span>
                <input v-model="editFullName" type="text" class="field-input" autocomplete="name" />
              </label>
              <label class="field">
                <span class="field-label">Số điện thoại</span>
                <input v-model="editPhone" type="text" class="field-input" autocomplete="tel" />
              </label>
              <div class="field-actions">
                <button type="button" class="btn-primary btn-primary--inline" :disabled="savingProfile" @click="saveProfile">
                  {{ savingProfile ? 'Đang lưu…' : 'Lưu hồ sơ' }}
                </button>
              </div>
            </section>

            <section class="info-block">
              <h4>Vai trò</h4>
              <p class="field-hint">Quản trị truy cập trang admin; khách hàng chỉ dùng cửa hàng.</p>
              <select v-model="editRole" class="field-input field-input--select">
                <option value="customer">Khách hàng</option>
                <option value="admin">Quản trị</option>
              </select>
              <div class="field-actions">
                <button type="button" class="btn-primary btn-primary--inline" :disabled="savingRole" @click="saveRole">
                  {{ savingRole ? 'Đang lưu…' : 'Lưu vai trò' }}
                </button>
              </div>
            </section>

            <section class="info-block info-block--copy">
              <h4>Liên hệ nhanh</h4>
              <div class="copy-row">
                <span class="copy-text">{{ selected.email }}</span>
                <button type="button" class="btn-mini" @click="copyText(selected.email, 'Đã sao chép email')">
                  Sao chép email
                </button>
              </div>
              <div v-if="selected.phone" class="copy-row">
                <span class="copy-text">{{ selected.phone }}</span>
                <button type="button" class="btn-mini" @click="copyText(selected.phone, 'Đã sao chép SĐT')">
                  Sao chép SĐT
                </button>
              </div>
            </section>
          </div>

          <footer class="modal-foot">
            <button type="button" class="btn-secondary" @click="closeModal">Đóng</button>
          </footer>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getUsers, updateUserProfile, updateUserRole } from '../../api/services/adminService'

const users = ref([])
const loading = ref(true)
const searchQuery = ref('')
const roleFilter = ref('all')
const sortKey = ref('id_asc')
const page = ref(1)
const pageSize = ref(12)

const selected = ref(null)
const editFullName = ref('')
const editPhone = ref('')
const editRole = ref('customer')
const savingProfile = ref(false)
const savingRole = ref(false)

const toast = ref({ message: '', type: 'ok' })
let toastTimer = null

const roleFilters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'admin', label: 'Quản trị' },
  { value: 'customer', label: 'Khách hàng' },
]

function showToast(msg, type = 'ok') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { message: msg, type }
  toastTimer = setTimeout(() => {
    toast.value = { message: '', type: 'ok' }
  }, 2800)
}

/** Chỉ admin vs khách; role cũ "staff" trong DB hiển thị/lọc như khách. */
function normalizeRole(r) {
  return (r || '').toLowerCase() === 'admin' ? 'admin' : 'customer'
}

function roleLabel(r) {
  return normalizeRole(r) === 'admin' ? 'Quản trị' : 'Khách hàng'
}

const stats = computed(() => {
  const list = users.value
  let admin = 0
  let customer = 0
  for (const u of list) {
    if (normalizeRole(u.role) === 'admin') admin++
    else customer++
  }
  return { total: list.length, admin, customer }
})

function countByRole(role) {
  return users.value.filter((u) => normalizeRole(u.role) === role).length
}

const filteredSorted = computed(() => {
  let list = [...users.value]
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    list = list.filter((u) => {
      const id = String(u.id || '')
      const email = (u.email || '').toLowerCase()
      const name = (u.fullName || '').toLowerCase()
      const phone = (u.phone || '').toLowerCase()
      return id.includes(q) || email.includes(q) || name.includes(q) || phone.includes(q)
    })
  }
  if (roleFilter.value !== 'all') {
    list = list.filter((u) => normalizeRole(u.role) === roleFilter.value)
  }
  list.sort((a, b) => {
    switch (sortKey.value) {
      case 'id_desc':
        return Number(b.id) - Number(a.id)
      case 'name_asc':
        return (a.fullName || '').localeCompare(b.fullName || '', 'vi')
      case 'email_asc':
        return (a.email || '').localeCompare(b.email || '', 'vi')
      case 'id_asc':
      default:
        return Number(a.id) - Number(b.id)
    }
  })
  return list
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredSorted.value.length / pageSize.value)))

watch([filteredSorted, pageSize], () => {
  if (page.value > totalPages.value) page.value = totalPages.value
})

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredSorted.value.slice(start, start + pageSize.value)
})

const pageStart = computed(() => {
  if (filteredSorted.value.length === 0) return 0
  return (page.value - 1) * pageSize.value + 1
})

const pageEnd = computed(() => Math.min(page.value * pageSize.value, filteredSorted.value.length))

function mergeUser(updated) {
  const i = users.value.findIndex((u) => u.id === updated.id)
  if (i >= 0) users.value[i] = { ...users.value[i], ...updated }
  if (selected.value && selected.value.id === updated.id) {
    selected.value = { ...selected.value, ...updated }
  }
}

async function load() {
  loading.value = true
  try {
    users.value = await getUsers()
    page.value = 1
  } catch {
    users.value = []
    showToast('Không tải được danh sách người dùng', 'err')
  } finally {
    loading.value = false
  }
}

function openDetail(u) {
  selected.value = u
  editFullName.value = u.fullName || ''
  editPhone.value = u.phone || ''
  editRole.value = normalizeRole(u.role) === 'admin' ? 'admin' : 'customer'
}

function closeModal() {
  selected.value = null
}

async function saveProfile() {
  if (!selected.value) return
  savingProfile.value = true
  try {
    const data = await updateUserProfile(selected.value.id, {
      fullName: editFullName.value,
      phone: editPhone.value,
    })
    mergeUser(data)
    showToast('Đã cập nhật hồ sơ')
  } catch {
    showToast('Không lưu được hồ sơ', 'err')
  } finally {
    savingProfile.value = false
  }
}

async function saveRole() {
  if (!selected.value) return
  savingRole.value = true
  const prev = normalizeRole(selected.value.role) === 'admin' ? 'admin' : 'customer'
  try {
    const data = await updateUserRole(selected.value.id, editRole.value)
    mergeUser(data)
    editRole.value = normalizeRole(data.role) === 'admin' ? 'admin' : 'customer'
    showToast('Đã cập nhật vai trò')
  } catch (e) {
    editRole.value = prev
    const msg = e.response?.data?.message || 'Không đổi được vai trò'
    showToast(msg, 'err')
  } finally {
    savingRole.value = false
  }
}

async function copyText(text, okMsg) {
  try {
    await navigator.clipboard.writeText(text)
    showToast(okMsg)
  } catch {
    showToast('Không sao chép được', 'err')
  }
}

function exportCsv() {
  const rows = filteredSorted.value
  const headers = ['ID', 'Email', 'Họ tên', 'SĐT', 'Vai trò']
  const lines = [headers.join(',')]
  for (const u of rows) {
    lines.push(
      [u.id, csvCell(u.email), csvCell(u.fullName), csvCell(u.phone), csvCell(roleLabel(u.role))].join(',')
    )
  }
  const bom = '\uFEFF'
  const blob = new Blob([bom + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `nguoi-dung-kesn-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
  showToast(`Đã xuất CSV (${rows.length} dòng)`)
}

function csvCell(v) {
  if (v == null || v === '') return '""'
  const s = String(v).replace(/"/g, '""')
  return `"${s}"`
}

onMounted(load)
</script>

<style scoped>
.users-page {
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

.btn-primary--inline {
  padding: 8px 16px;
  font-size: 0.8125rem;
}

.btn-primary:hover:not(:disabled) {
  filter: brightness(1.06);
}

.btn-primary:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

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
}

.kpi-card--blue {
  border-color: #bfdbfe;
  background: linear-gradient(180deg, #eff6ff 0%, #fff 100%);
}

.kpi-card--green {
  border-color: #a7f3d0;
  background: linear-gradient(180deg, #ecfdf5 0%, #fff 100%);
}

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

.th-act {
  width: 110px;
  text-align: right;
}

.mono {
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, 'Cascadia Code', monospace;
}

.cell-email {
  font-weight: 500;
  color: #0f172a;
  word-break: break-all;
}

.cell-strong {
  font-weight: 600;
  color: #0f172a;
}

.cell-phone {
  white-space: nowrap;
}

.role-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.role-badge.admin {
  background: #dbeafe;
  color: #1e40af;
}

.role-badge.customer {
  background: #d1fae5;
  color: #065f46;
}

.role-badge--lg {
  font-size: 0.75rem;
  padding: 6px 14px;
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
  max-width: 560px;
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

.modal-body {
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;
}

.info-block {
  margin-bottom: 22px;
}

.info-block:last-child {
  margin-bottom: 0;
}

.info-block h4 {
  margin: 0 0 10px;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #94a3b8;
}

.field-hint {
  margin: 0 0 10px;
  font-size: 0.8125rem;
  color: #64748b;
  line-height: 1.45;
}

.field {
  display: block;
  margin-bottom: 14px;
}

.field-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.field-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.9rem;
  color: #0f172a;
  box-sizing: border-box;
}

.field-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.field-input--select {
  cursor: pointer;
}

.field-actions {
  margin-top: 8px;
}

.copy-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.copy-text {
  font-size: 0.875rem;
  color: #334155;
  word-break: break-all;
}

.btn-mini {
  padding: 6px 12px;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #4f46e5;
  border-radius: 8px;
  white-space: nowrap;
}

.btn-mini:hover {
  background: #eef2ff;
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
