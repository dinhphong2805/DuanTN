<template>
  <div class="admin-imports">
    <div class="header">
      <div>
        <h1>Nhập hàng</h1>
        <p>Quản lý phiếu nhập và cập nhật tồn kho sản phẩm.</p>
      </div>
      <button class="btn-primary" @click="openCreate">+ Tạo phiếu nhập</button>
    </div>

    <div class="toolbar">
      <input v-model.trim="keyword" placeholder="Tìm theo mã phiếu, NCC, người tạo..." @keyup.enter="load" />
      <button class="btn" @click="load">Tìm</button>
    </div>

    <div class="card">
      <table v-if="!loading && receipts.length" class="table">
        <thead>
          <tr>
            <th>Mã phiếu</th>
            <th>Nhà cung cấp</th>
            <th>Người tạo</th>
            <th>Ngày tạo</th>
            <th>Tổng tiền</th>
            <th class="right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in receipts" :key="r.id">
            <td>{{ r.code }}</td>
            <td>{{ r.supplierName || '—' }}</td>
            <td>{{ r.createdBy || '—' }}</td>
            <td>{{ formatDate(r.createdAt) }}</td>
            <td>{{ formatMoney(r.totalCost) }}</td>
            <td class="right">
              <button class="link" @click="openEdit(r)">Sửa</button>
              <button class="link danger" @click="removeReceipt(r)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else-if="loading" class="state">Đang tải dữ liệu...</p>
      <p v-else class="state">Chưa có phiếu nhập nào.</p>
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
            <input v-model.trim="form.createdBy" placeholder="Admin tạo phiếu" />
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
import {
  createImportReceipt,
  deleteImportReceipt,
  getImportReceiptDetail,
  getImportReceipts,
  getProducts,
  updateImportReceipt,
} from '../../adminService'

const loading = ref(false)
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

function resetForm() {
  editingId.value = null
  error.value = ''
  form.code = ''
  form.supplierName = ''
  form.createdBy = ''
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

async function load() {
  loading.value = true
  try {
    receipts.value = await getImportReceipts({ keyword: keyword.value || undefined })
  } finally {
    loading.value = false
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
.admin-imports { padding: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header h1 { margin: 0; }
.header p { margin: 4px 0 0; color: #6b7280; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.toolbar input { flex: 1; }
input, select { border: 1px solid #d1d5db; border-radius: 8px; padding: 10px; }
.card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; overflow: hidden; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border-bottom: 1px solid #f1f5f9; padding: 10px; text-align: left; }
.table .right { text-align: right; }
.state { padding: 20px; color: #6b7280; }
.btn, .btn-primary { border: 1px solid #d1d5db; border-radius: 8px; padding: 10px 12px; background: #fff; cursor: pointer; }
.btn-primary { background: #111827; color: #fff; border-color: #111827; }
.link { border: none; background: transparent; cursor: pointer; color: #2563eb; }
.link.danger { color: #dc2626; }

.overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: grid; place-items: center; z-index: 30; }
.modal { width: min(980px, 95vw); max-height: 92vh; overflow: auto; background: #fff; border-radius: 14px; padding: 16px; }
.modal-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.icon-btn { border: none; background: #f3f4f6; width: 32px; height: 32px; border-radius: 999px; cursor: pointer; }
.grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-bottom: 10px; }
label { display: flex; flex-direction: column; gap: 6px; font-size: 14px; }
.items-head { display: flex; justify-content: space-between; align-items: center; margin: 8px 0; }
.items td input, .items td select { width: 100%; }
.total { text-align: right; margin-top: 8px; }
.actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 12px; }
.error { color: #dc2626; }
@media (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
</style>
