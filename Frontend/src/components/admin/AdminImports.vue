<template>
  <div class="admin-imports">
    <div class="header d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="h3 fw-bold mb-1">Nhập hàng</h1>
        <p class="text-secondary small mb-0">Quản lý phiếu nhập và cập nhật tồn kho sản phẩm.</p>
      </div>
      <button class="btn btn-primary d-flex align-items-center gap-2 px-4 shadow-sm" @click="openCreate">
        <i class="bi bi-plus-lg"></i> Tạo phiếu nhập
      </button>
    </div>

    <div class="toolbar card shadow-sm border-0 p-3 mb-4">
      <div class="row g-2 align-items-center">
        <div class="col-md-6 col-lg-4">
          <div class="input-group">
            <span class="input-group-text bg-white border-end-0 text-secondary">
              <i class="bi bi-search"></i>
            </span>
            <input v-model.trim="keyword" class="form-control border-start-0 ps-0" placeholder="Tìm mã phiếu, NCC..." @keyup.enter="load(true)" />
          </div>
        </div>
        <div class="col-md-2">
          <button class="btn btn-dark w-100 fw-bold" @click="load(true)">Tìm kiếm</button>
        </div>
      </div>
    </div>

    <div class="table-card shadow-sm border-0" :class="{ 'is-refreshing': isRefreshing }">
      <div v-if="loading && !isRefreshing" class="loading-state text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="mt-3 text-secondary">Đang tải dữ liệu...</p>
      </div>

      <div v-else-if="receipts.length" class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="bg-light text-uppercase small fw-bold text-secondary">
            <tr>
              <th class="ps-4" width="60">STT</th>
              <th>Mã phiếu</th>
              <th>Nhà cung cấp</th>
              <th>Sản phẩm</th>
              <th>Người tạo</th>
              <th>Ngày nhập</th>
              <th>Tổng tiền</th>
              <th class="pe-4 text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(r, idx) in receipts" :key="r.id" class="align-middle">
              <td class="ps-4 text-secondary">#{{ idx + 1 }}</td>
              <td><span class="badge bg-light text-dark border fw-bold">{{ r.code }}</span></td>
              <td>{{ r.supplierName || '—' }}</td>
              <td :title="getProductSnapshot(r.items)">
                <div class="text-truncate" style="max-width: 200px">
                  {{ getProductSnapshot(r.items) }}
                </div>
              </td>
              <td><span class="text-secondary small">{{ r.createdBy || '—' }}</span></td>
              <td><span class="text-secondary small">{{ formatDate(r.createdAt) }}</span></td>
              <td><strong class="text-primary">{{ formatMoney(r.totalCost) }}</strong></td>
              <td class="pe-4 text-end">
                <button class="btn btn-outline-primary btn-sm me-1 border-0" @click="openEdit(r)" title="Sửa">
                  <i class="bi bi-pencil-square"></i>
                </button>
                <button class="btn btn-outline-danger btn-sm border-0" @click="removeReceipt(r)" title="Xóa">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="empty-state text-center py-5">
        <div class="display-1 text-light mb-3"><i class="bi bi-inbox"></i></div>
        <h5 class="text-secondary">Chưa có phiếu nhập nào.</h5>
      </div>
    </div>

    <div v-if="showForm" class="overlay" @click.self="closeForm">
      <div class="modal">
        <div class="modal-head">
          <h3>{{ editingId ? 'Cập nhật phiếu nhập' : 'Tạo phiếu nhập' }}</h3>
          <button class="icon-btn" @click="closeForm">✕</button>
        </div>

        <div class="grid">
          <label>
            Mã phiếu
            <input v-model.trim="form.code" placeholder="PN-2026-001" />
          </label>
          <label>
            Nhà cung cấp
            <input v-model.trim="form.supplierName" placeholder="Tên nhà cung cấp" />
          </label>
          <label>
            Người tạo
            <input v-model.trim="form.createdBy" placeholder="Admin tạo phiếu" disabled />
          </label>
          <label>
            Ghi chú
            <input v-model.trim="form.note" placeholder="Ghi chú thêm" />
          </label>
        </div>

        <div class="items-head">
          <h4>Sản phẩm nhập</h4>
          <button class="btn" @click="addItem">+ Thêm dòng</button>
        </div>

        <table class="table items">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th width="110">SL</th>
              <th width="150">Đơn giá nhập</th>
              <th width="150">Thành tiền</th>
              <th width="70"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(it, idx) in form.items" :key="idx">
              <td>
                <select v-model.number="it.productId">
                  <option :value="null">-- Chọn sản phẩm --</option>
                  <option v-for="p in products" :key="p.id" :value="p.id">
                    {{ p.name }} (Tồn: {{ p.stockQty || 0 }})
                  </option>
                </select>
              </td>
              <td><input v-model.number="it.quantity" type="number" min="1" /></td>
              <td><input v-model.number="it.unitCost" type="number" min="0" /></td>
              <td>{{ formatMoney(lineTotal(it)) }}</td>
              <td><button class="link danger" @click="removeItem(idx)">Xóa</button></td>
            </tr>
          </tbody>
        </table>

        <div class="total">Tổng cộng: <strong>{{ formatMoney(grandTotal) }}</strong></div>
        <p v-if="error" class="error">{{ error }}</p>
        <div class="actions">
          <button class="btn" @click="closeForm">Hủy</button>
          <button class="btn-primary" :disabled="saving" @click="save">
            {{ saving ? 'Đang lưu...' : 'Lưu phiếu nhập' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '../../authStore'
import {
  createImportReceipt,
  deleteImportReceipt,
  getImportReceiptDetail,
  getImportReceipts,
  getProducts,
  updateImportReceipt,
} from '../../adminService'

const auth = useAuthStore()
const loading = ref(false)
const isRefreshing = ref(false)
const saving = ref(false)
const receipts = ref([])
const products = ref([])
const keyword = ref('')
const error = ref('')

const showForm = ref(false)
const editingId = ref(null)
const form = reactive({
  code: '',
  supplierName: '',
  createdBy: '',
  note: '',
  items: [],
})

const grandTotal = computed(() => form.items.reduce((s, it) => s + lineTotal(it), 0))

function formatDate(v) {
  if (!v) return '—'
  return new Date(v).toLocaleString('vi-VN')
}

function formatMoney(v) {
  return `${Number(v || 0).toLocaleString('vi-VN')} đ`
}

function lineTotal(it) {
  return (Number(it.quantity) || 0) * (Number(it.unitCost) || 0)
}

function currentCreatorName() {
  const user = auth.state?.user
  return user?.fullName || user?.full_name || user?.email || ''
}

function getProductSnapshot(items) {
  if (!items || !items.length) return '—'
  return items.map(i => `${i.productName} (${i.quantity})`).join(', ')
}

function resetForm() {
  editingId.value = null
  error.value = ''
  form.code = ''
  form.supplierName = ''
  form.createdBy = currentCreatorName()
  form.note = ''
  form.items = [{ productId: null, quantity: 1, unitCost: 0 }]
}

function addItem() {
  form.items.push({ productId: null, quantity: 1, unitCost: 0 })
}

function removeItem(idx) {
  form.items.splice(idx, 1)
  if (!form.items.length) addItem()
}

function openCreate() {
  resetForm()
  showForm.value = true
}

async function openEdit(row) {
  resetForm()
  editingId.value = row.id
  const detail = await getImportReceiptDetail(row.id)
  form.code = detail.code || ''
  form.supplierName = detail.supplierName || ''
  form.createdBy = detail.createdBy || ''
  form.note = detail.note || ''
  form.items = (detail.items || []).map((it) => ({
    productId: it.product?.id ?? null,
    quantity: it.quantity ?? 1,
    unitCost: it.unitCost ?? 0,
  }))
  if (!form.items.length) addItem()
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function load(refresh = false) {
  if (refresh) isRefreshing.value = true
  else loading.value = true
  
  try {
    receipts.value = await getImportReceipts({ keyword: keyword.value || undefined })
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

async function loadProducts() {
  const page = await getProducts({ page: 0, size: 1000 })
  products.value = page?.content || []
}

async function save() {
  error.value = ''
  if (!form.code) {
    error.value = 'Vui lòng nhập mã phiếu.'
    return
  }
  if (!form.items.length || form.items.some((x) => !x.productId || Number(x.quantity) <= 0)) {
    error.value = 'Danh sách sản phẩm nhập chưa hợp lệ.'
    return
  }
  saving.value = true
  const payload = {
    code: form.code,
    supplierName: form.supplierName || null,
    createdBy: form.createdBy || null,
    note: form.note || null,
    status: 'completed',
    items: form.items.map((x) => ({
      productId: Number(x.productId),
      quantity: Number(x.quantity),
      unitCost: Number(x.unitCost || 0),
    })),
  }
  try {
    if (editingId.value) await updateImportReceipt(editingId.value, payload)
    else await createImportReceipt(payload)
    showForm.value = false
    await Promise.all([load(), loadProducts()])
  } catch (e) {
    error.value = e?.response?.data?.message || 'Không thể lưu phiếu nhập.'
  } finally {
    saving.value = false
  }
}

async function removeReceipt(row) {
  if (!confirm(`Xóa phiếu nhập ${row.code}? Tồn kho sẽ bị trừ lại.`)) return
  try {
    await deleteImportReceipt(row.id)
    await Promise.all([load(), loadProducts()])
  } catch (e) {
    alert(e?.response?.data?.message || 'Không thể xóa phiếu nhập.')
  }
}

onMounted(async () => {
  resetForm()
  await Promise.all([load(), loadProducts()])
})
</script>

<style scoped>
/* ============================================================
   AdminImports — Scoped styles
   ============================================================ */

.admin-imports { max-width: 1200px; margin: 0 auto; }

/* Header */
.header {
  display: flex; align-items: flex-start; justify-content: space-between;
  flex-wrap: wrap; gap: 12px;
  margin-bottom: 24px; padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}
.header h1 { margin: 0; font-size: 1.55rem; font-weight: 800; color: #0f172a; letter-spacing: -.03em; }
.header p { margin: 5px 0 0; font-size: .9rem; color: #64748b; }

.btn-primary {
  padding: 9px 20px; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: .875rem; font-weight: 700;
  cursor: pointer; font-family: inherit;
  box-shadow: 0 4px 12px rgba(99,102,241,.28);
}
.btn-primary:hover { filter: brightness(1.08); transform: translateY(-1px); }

/* Toolbar */
.toolbar {
  display: flex; gap: 10px; flex-wrap: wrap;
  margin-bottom: 16px; padding: 13px 18px;
  background: #fff; border: 1px solid #e2e8f0; border-radius: 14px;
  box-shadow: 0 1px 3px rgba(15,23,42,.04);
}
.toolbar input {
  flex: 1 1 220px; padding: 9px 13px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: .875rem; font-family: inherit;
  color: #0f172a; background: #f8fafc; outline: none;
  transition: border-color .15s, box-shadow .15s;
}
.toolbar input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.15); background: #fff; }

/* Card wrapper */
.card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px;
  overflow: hidden; box-shadow: 0 2px 10px rgba(15,23,42,.06);
}

/* Table */
.table {
  width: 100%; border-collapse: collapse; font-size: .875rem;
}
.table th {
  text-align: left; padding: 13px 18px;
  font-size: .67rem; font-weight: 700; text-transform: uppercase;
  letter-spacing: .07em; color: #94a3b8; background: #f8fafc;
  border-bottom: 1.5px solid #e2e8f0; white-space: nowrap;
}
.table td { padding: 14px 18px; border-bottom: 1px solid #f1f5f9; color: #334155; vertical-align: middle; }
.table tbody tr:last-child td { border-bottom: none; }
.table tbody tr { transition: background .15s; }
.table tbody tr:hover td { background: #fafbff; }
.table .right { text-align: right; }

.table.items th, .table.items td { padding: 10px 14px; }
.table.items th { font-size: .65rem; }
.table.items td { color: #334155; }
.table.items select, .table.items input {
  width: 100%; padding: 7px 10px; border: 1.5px solid #e2e8f0;
  border-radius: 8px; font-size: .84rem; font-family: inherit;
  color: #0f172a; background: #f8fafc; outline: none;
}
.table.items select:focus,
.table.items input:focus { border-color: #6366f1; background: #fff; }

.state { text-align: center; padding: 40px; color: #94a3b8; font-size: .9rem; }

/* Action links in table */
.link {
  font-size: .8rem; font-weight: 600; cursor: pointer; padding: 5px 10px;
  border-radius: 7px; border: 1.5px solid #e2e8f0; background: #fff;
  color: #4f46e5; font-family: inherit; transition: all .15s; margin-right: 5px;
}
.link:hover { background: #eef2ff; border-color: #6366f1; }
.link.danger { color: #be123c; border-color: #fda4af; }
.link.danger:hover { background: #fff1f2; border-color: #fb7185; }

.btn { padding: 8px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0; background: #fff; color: #475569; font-size: .875rem; font-weight: 600; cursor: pointer; font-family: inherit; transition: all .15s; }
.btn:hover { background: #f1f5f9; }

/* Overlay / Modal */
.overlay {
  position: fixed; inset: 0; background: rgba(15,23,42,.52);
  backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center;
  z-index: 8000; padding: 20px;
  animation: ov-in .2s ease;
}
@keyframes ov-in { from { opacity: 0; } to { opacity: 1; } }

.modal {
  background: #fff; border-radius: 18px; padding: 0;
  max-width: 720px; width: 100%; max-height: min(92vh, 800px);
  overflow: hidden; display: flex; flex-direction: column;
  box-shadow: 0 24px 64px rgba(15,23,42,.2), 0 0 0 1px rgba(99,102,241,.07);
  animation: mo-in .3s cubic-bezier(.16,1,.3,1);
}
@keyframes mo-in { from { opacity: 0; transform: translateY(20px) scale(.96); } to { opacity: 1; transform: none; } }

.modal-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 22px 26px 18px; border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%); flex-shrink: 0;
}
.modal-head h3 { margin: 0; font-size: 1.1rem; font-weight: 800; color: #0f172a; }
.icon-btn { width: 32px; height: 32px; border-radius: 8px; background: #f1f5f9; border: none; font-size: 1rem; cursor: pointer; color: #64748b; transition: background .15s; }
.icon-btn:hover { background: #e2e8f0; }

.grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 14px;
  padding: 20px 26px; border-bottom: 1px solid #f1f5f9;
}
@media (max-width: 560px) { .grid { grid-template-columns: 1fr; } }
.grid label { display: flex; flex-direction: column; gap: 6px; font-size: .8125rem; font-weight: 600; color: #475569; }
.grid input {
  padding: 9px 12px; border: 1.5px solid #e2e8f0; border-radius: 9px;
  font-size: .875rem; font-family: inherit; color: #0f172a; outline: none;
  transition: border-color .15s, box-shadow .15s;
}
.grid input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.15); }
.grid input:disabled { background: #f8fafc; color: #94a3b8; cursor: not-allowed; }

.items-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 26px 10px; border-bottom: 1px solid #f1f5f9;
}
.items-head h4 { margin: 0; font-size: .84rem; font-weight: 700; color: #0f172a; }

.total {
  padding: 14px 26px; font-size: 1rem; font-weight: 600; color: #475569;
  border-top: 1px solid #f1f5f9; background: #f8fafc; flex-shrink: 0;
}
.total strong { color: #0f172a; font-weight: 900; font-variant-numeric: tabular-nums; font-size: 1.1rem; }

.error { color: #be123c; font-size: .84rem; font-weight: 600; padding: 8px 26px; }

.actions {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 14px 26px; border-top: 1px solid #f1f5f9;
  background: #f8fafc; flex-shrink: 0;
}
</style>

