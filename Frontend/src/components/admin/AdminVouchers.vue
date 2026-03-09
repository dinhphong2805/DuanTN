<template>
  <div class="admin-vouchers">
    <div class="header">
      <h1>Mã giảm giá</h1>
      <button type="button" class="btn-add" @click="openForm()">+ Thêm mã</button>
    </div>

    <section v-if="loading" class="loading">Đang tải...</section>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã</th>
            <th>Loại</th>
            <th>Giảm</th>
            <th>Đơn tối thiểu</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="v in vouchers" :key="v.id">
            <td>{{ v.id }}</td>
            <td><strong>{{ v.code }}</strong></td>
            <td>{{ v.type === 'fixed' ? 'Cố định' : 'Phần trăm' }}</td>
            <td>
              {{ v.type === 'fixed' ? formatPrice(v.discount) + ' VNĐ' : v.discount + '%' }}
            </td>
            <td>{{ formatPrice(v.minOrder || 0) }} VNĐ</td>
            <td>
              <button type="button" class="btn-sm danger" @click="confirmDelete(v)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="!loading && vouchers.length === 0" class="empty">Chưa có mã giảm giá. Bấm "Thêm mã" để tạo mới.</p>

    <!-- Modal form -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal">
        <h3>Thêm mã giảm giá</h3>
        <form @submit.prevent="saveVoucher">
          <div class="form-group">
            <label>Mã *</label>
            <input v-model="form.code" required placeholder="VD: KESN10" />
          </div>
          <div class="form-group">
            <label>Loại</label>
            <select v-model="form.type">
              <option value="percent">Phần trăm (%)</option>
              <option value="fixed">Cố định (VNĐ)</option>
            </select>
          </div>
          <div class="form-group">
            <label>Giá trị giảm *</label>
            <input v-model.number="form.discount" type="number" required min="0" :placeholder="form.type === 'percent' ? 'VD: 10' : 'VD: 50000'" />
            <span class="hint">{{ form.type === 'percent' ? 'Nhập % (1-100)' : 'Nhập số tiền VNĐ' }}</span>
          </div>
          <div class="form-group">
            <label>Đơn tối thiểu (VNĐ)</label>
            <input v-model.number="form.minOrder" type="number" min="0" placeholder="0" />
          </div>
          <p v-if="formError" class="error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="showForm = false">Hủy</button>
            <button type="submit" class="btn-save" :disabled="saving">{{ saving ? 'Đang lưu...' : 'Lưu' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getVouchers, createVoucher, deleteVoucher } from '../../api/services/adminService'

const vouchers = ref([])
const loading = ref(true)
const showForm = ref(false)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  code: '',
  type: 'percent',
  discount: 0,
  minOrder: 0,
})

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN')
}

async function load() {
  loading.value = true
  try {
    vouchers.value = await getVouchers()
  } catch {
    vouchers.value = []
  } finally {
    loading.value = false
  }
}

function openForm() {
  formError.value = ''
  form.code = ''
  form.type = 'percent'
  form.discount = 0
  form.minOrder = 0
  showForm.value = true
}

async function saveVoucher() {
  formError.value = ''
  saving.value = true
  try {
    await createVoucher({
      code: String(form.code).trim().toUpperCase(),
      type: form.type,
      discount: Number(form.discount) || 0,
      minOrder: Number(form.minOrder) || 0,
    })
    showForm.value = false
    await load()
  } catch (e) {
    formError.value = e.response?.data?.message || e.message || 'Có lỗi xảy ra'
  } finally {
    saving.value = false
  }
}

function confirmDelete(v) {
  if (!confirm(`Xóa mã "${v.code}"?`)) return
  deleteVoucher(v.id).then(() => load()).catch(() => alert('Không thể xóa'))
}

onMounted(load)
</script>

<style scoped>
.admin-vouchers .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-vouchers h1 {
  font-size: 24px;
  font-weight: 800;
}

.btn-add {
  padding: 10px 18px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

.loading {
  padding: 24px;
  text-align: center;
  color: #6b7280;
}

.table-wrap {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.table th {
  background: #f9fafb;
  font-weight: 700;
  font-size: 13px;
}

.btn-sm {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  margin-right: 8px;
}

.btn-sm.danger {
  color: #b91c1c;
  border-color: #b91c1c;
}

.empty {
  padding: 24px;
  color: #6b7280;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 100%;
  max-width: 420px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal h3 {
  margin: 0 0 20px;
  font-size: 18px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 6px;
  font-size: 14px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
}

.hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.error {
  color: #b91c1c;
  font-size: 14px;
  margin-bottom: 12px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-cancel {
  padding: 10px 18px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  cursor: pointer;
}

.btn-save {
  padding: 10px 18px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}
</style>
