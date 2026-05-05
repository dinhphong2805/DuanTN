<template>
  <div class="admin-vouchers">
    <div class="header d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="h3 fw-bold mb-1">Mã giảm giá</h1>
        <p class="text-secondary small mb-0">Quản lý khuyến mãi và voucher khách hàng</p>
      </div>
      <div class="d-flex gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm d-flex align-items-center gap-2 px-3" @click="load(true)">
          <i class="bi bi-arrow-clockwise" :class="{ 'spinner-border spinner-border-sm border-0': isRefreshing }"></i>
          Làm mới
        </button>
        <button type="button" class="btn btn-primary d-flex align-items-center gap-2 px-4 shadow-sm" @click="openForm()">
          <i class="bi bi-plus-lg"></i> Thêm mã
        </button>
      </div>
    </div>

    <div class="table-card shadow-sm border-0" :class="{ 'is-refreshing': isRefreshing }">
      <section v-if="loading && !isRefreshing" class="loading-state text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="mt-3 text-secondary">Đang tải...</p>
      </section>

      <div v-else-if="vouchers.length" class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="bg-light text-uppercase small fw-bold text-secondary">
            <tr>
              <th class="ps-4">ID</th>
              <th>Mã Voucher</th>
              <th>Loại</th>
              <th>Giảm</th>
              <th>Đơn tối thiểu</th>
              <th>Trạng thái</th>
              <th class="pe-4 text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in vouchers" :key="v.id" class="align-middle">
              <td class="ps-4 text-secondary">#{{ v.id }}</td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <span class="badge bg-light text-dark border fw-bold font-monospace">{{ v.code }}</span>
                  <span v-if="v.isSignupDefault" class="badge bg-warning text-dark border-0 ultra-small">MỚI</span>
                </div>
              </td>
              <td>{{ v.type === 'fixed' ? 'Cố định' : 'Phần trăm' }}</td>
              <td>
                <span class="fw-bold text-primary">{{ v.type === 'fixed' ? formatPrice(v.discount) + ' ₫' : v.discount + '%' }}</span>
              </td>
              <td class="text-secondary">{{ formatPrice(v.minOrder || 0) }} ₫</td>
              <td>
                <span class="badge rounded-pill" :class="v.status === 'active' ? 'bg-success-subtle text-success border border-success' : 'bg-secondary-subtle text-secondary border border-secondary'">
                  {{ v.status === 'active' ? 'Hoạt động' : 'Đã khóa' }}
                </span>
              </td>
              <td class="pe-4 text-end">
                <button type="button" class="btn btn-outline-primary btn-sm me-1 border-0" @click="openForm(v)">
                  <i class="bi bi-pencil-square"></i>
                </button>
                <button type="button" class="btn btn-outline-danger btn-sm border-0" @click="confirmDelete(v)">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="empty-state text-center py-5">
        <div class="display-1 text-light mb-3"><i class="bi bi-ticket-perforated"></i></div>
        <h5 class="text-secondary">Chưa có mã giảm giá nào.</h5>
      </div>
    </div>

    <!-- Modal form -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal">
        <h3>{{ isEdit ? 'Sửa mã giảm giá' : 'Thêm mã giảm giá' }}</h3>
        <form @submit.prevent="saveVoucher">
          <div class="form-group">
            <label>Mã *</label>
            <input v-model="form.code" required placeholder="VD: KESN10" :disabled="isEdit" />
          </div>
          <div class="form-group row-group">
            <div>
                <label>Loại</label>
                <select v-model="form.type">
                  <option value="percent">Phần trăm (%)</option>
                  <option value="fixed">Cố định (VNĐ)</option>
                </select>
            </div>
            <div>
                <label>Trạng thái</label>
                <select v-model="form.status">
                  <option value="active">Hoạt động</option>
                  <option value="inactive">Khóa (Bảo trì)</option>
                </select>
            </div>
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
          <div class="form-group checkbox-group">
            <label>
              <input type="checkbox" v-model="form.isSignupDefault" />
              Auto-grant: Tặng tự động cho người dùng mới đăng ký
            </label>
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
import Swal from 'sweetalert2'
import { getVouchers, createVoucher, deleteVoucher, updateVoucher } from '../../api/services/adminService'

const vouchers = ref([])
const loading = ref(true)
const isRefreshing = ref(false)
const showForm = ref(false)
const saving = ref(false)
const formError = ref('')
const isEdit = ref(false)

const form = reactive({
  id: null,
  code: '',
  type: 'percent',
  discount: 0,
  minOrder: 0,
  status: 'active',
  isSignupDefault: false
})

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN')
}

async function load(refresh = false) {
  if (refresh) isRefreshing.value = true
  else loading.value = true
  
  try {
    vouchers.value = await getVouchers()
  } catch {
    vouchers.value = []
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function openForm(v = null) {
  formError.value = ''
  if (v) {
      isEdit.value = true
      form.id = v.id
      form.code = v.code
      form.type = v.type
      form.discount = v.discount
      form.minOrder = v.minOrder
      form.status = v.status || 'active'
      form.isSignupDefault = !!v.isSignupDefault
  } else {
      isEdit.value = false
      form.id = null
      form.code = ''
      form.type = 'percent'
      form.discount = 0
      form.minOrder = 0
      form.status = 'active'
      form.isSignupDefault = false
  }
  showForm.value = true
}

async function saveVoucher() {
  formError.value = ''
  saving.value = true
  try {
    const payload = {
      code: String(form.code).trim().toUpperCase(),
      type: form.type,
      discount: Number(form.discount) || 0,
      minOrder: Number(form.minOrder) || 0,
      status: form.status,
      isSignupDefault: form.isSignupDefault
    }
    
    if (isEdit.value) {
        await updateVoucher(form.id, payload)
    } else {
        await createVoucher(payload)
    }
    
    showForm.value = false
    await load()
  } catch (e) {
    formError.value = e.response?.data?.message || e.message || 'Có lỗi xảy ra'
  } finally {
    saving.value = false
  }
}

async function confirmDelete(v) {
  const result = await Swal.fire({
    title: 'Xóa mã giảm giá?',
    text: `Bạn có chắc chắn muốn xóa mã "${v.code}"?`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#e11d48',
    cancelButtonColor: '#94a3b8',
    confirmButtonText: 'Có, xóa!',
    cancelButtonText: 'Hủy'
  })

  if (result.isConfirmed) {
    deleteVoucher(v.id)
      .then(() => {
        Swal.fire({ title: 'Thành công!', text: 'Mã giảm giá đã bị xóa.', icon: 'success', timer: 1500, showConfirmButton: false })
        load()
      })
      .catch(() => Swal.fire({ title: 'Lỗi!', text: 'Không thể xóa mã giảm giá', icon: 'error', confirmButtonColor: '#4f46e5' }))
  }
}

onMounted(load)
</script>

<style scoped>
/* ============================================================
   AdminVouchers — Scoped styles
   ============================================================ */

.admin-vouchers { max-width: 1100px; margin: 0 auto; }

/* Header */
.header {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: 12px;
  margin-bottom: 24px; padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}
.header h1 { margin: 0; font-size: 1.55rem; font-weight: 800; color: #0f172a; letter-spacing: -.03em; }

.btn-add {
  padding: 9px 20px; border-radius: 12px; border: none;
  background: linear-gradient(135deg, #6366f1, #4f46e5); color: #fff;
  font-size: .875rem; font-weight: 700; cursor: pointer; font-family: inherit;
  box-shadow: 0 4px 14px rgba(99,102,241,.28); transition: filter .15s, transform .15s;
}
.btn-add:hover { filter: brightness(1.08); transform: translateY(-1px); }

/* Loading / Empty */
.loading, .empty {
  padding: 40px; text-align: center; color: #94a3b8;
  font-size: .9rem; background: #fff; border: 1px solid #e2e8f0;
  border-radius: 16px;
}

/* Table */
.table-wrap { overflow-x: auto; }
.table {
  width: 100%; border-collapse: collapse; font-size: .875rem;
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 10px rgba(15,23,42,.06);
}
.table thead tr { background: #f8fafc; }
.table th {
  text-align: left; padding: 13px 18px;
  font-size: .67rem; font-weight: 700; text-transform: uppercase;
  letter-spacing: .07em; color: #94a3b8;
  border-bottom: 1.5px solid #e2e8f0; white-space: nowrap;
}
.table td { padding: 13px 18px; border-bottom: 1px solid #f1f5f9; color: #334155; vertical-align: middle; }
.table tbody tr:last-child td { border-bottom: none; }
.table tbody tr { transition: background .15s; }
.table tbody tr:hover td { background: #fafbff; }

/* Status badge */
.status-active {
  display: inline-flex; align-items: center; padding: 4px 10px;
  border-radius: 999px; font-size: .7rem; font-weight: 700;
  background: #d1fae5; color: #047857;
}
.status-inactive {
  display: inline-flex; align-items: center; padding: 4px 10px;
  border-radius: 999px; font-size: .7rem; font-weight: 700;
  background: #f1f5f9; color: #64748b;
}
.badge-new {
  display: inline-flex; margin-left: 6px; padding: 2px 8px;
  border-radius: 999px; font-size: .65rem; font-weight: 700;
  background: #fef3c7; color: #92400e; vertical-align: middle;
}

/* Action buttons */
.btn-sm {
  padding: 6px 13px; border-radius: 8px; font-size: .78rem;
  font-weight: 600; cursor: pointer; font-family: inherit;
  border: 1.5px solid #e2e8f0; background: #fff; color: #4f46e5;
  transition: all .15s; margin-right: 6px;
}
.btn-sm:hover { background: #eef2ff; border-color: #6366f1; }
.btn-sm.danger { color: #be123c; border-color: #fda4af; }
.btn-sm.danger:hover { background: #fff1f2; border-color: #fb7185; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(15,23,42,.5);
  backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center;
  z-index: 8000; padding: 20px;
  animation: ov-in .2s ease;
}
@keyframes ov-in { from { opacity: 0; } to { opacity: 1; } }

.modal {
  background: #fff; border-radius: 18px; padding: 28px 30px;
  max-width: 480px; width: 100%;
  box-shadow: 0 24px 64px rgba(15,23,42,.2), 0 0 0 1px rgba(99,102,241,.07);
  animation: mo-in .3s cubic-bezier(.16,1,.3,1);
}
@keyframes mo-in { from { opacity: 0; transform: translateY(20px) scale(.96); } to { opacity: 1; transform: none; } }

.modal h3 { margin: 0 0 20px; font-size: 1.1rem; font-weight: 800; color: #0f172a; }

.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.form-group label { font-size: .8125rem; font-weight: 600; color: #475569; }
.form-group input,
.form-group select {
  padding: 9px 12px; border: 1.5px solid #e2e8f0; border-radius: 9px;
  font-size: .875rem; font-family: inherit; color: #0f172a; background: #f8fafc; outline: none;
  transition: border-color .15s, box-shadow .15s;
}
.form-group input:focus,
.form-group select:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.15); background: #fff; }
.form-group input:disabled { opacity: .5; cursor: not-allowed; }

.row-group { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.row-group > div { display: flex; flex-direction: column; gap: 6px; }
.row-group label { font-size: .8125rem; font-weight: 600; color: #475569; }

.hint { font-size: .76rem; color: #94a3b8; margin-top: 3px; }

.checkbox-group label {
  display: flex; align-items: center; gap: 9px;
  font-size: .875rem; font-weight: 500; color: #334155; cursor: pointer;
}
.checkbox-group input[type="checkbox"] {
  width: 16px; height: 16px; border-radius: 4px; flex-shrink: 0; padding: 0;
  accent-color: #6366f1; cursor: pointer;
}

.error { color: #be123c; font-size: .84rem; font-weight: 600; margin: -4px 0 10px; }

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.btn-cancel {
  padding: 9px 18px; border-radius: 10px;
  border: 1.5px solid #e2e8f0; background: #fff; color: #64748b;
  font-size: .875rem; font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-cancel:hover { background: #f1f5f9; }
.btn-save {
  padding: 9px 20px; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: .875rem; font-weight: 700;
  cursor: pointer; font-family: inherit;
  box-shadow: 0 4px 12px rgba(99,102,241,.28);
}
.btn-save:disabled { opacity: .5; cursor: not-allowed; }
.btn-save:hover:not(:disabled) { filter: brightness(1.08); }

/* ---- Smooth glass-fade effect cho data refresh ---- */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  transition: opacity 0.4s ease, filter 0.4s ease, transform 0.4s ease;
  will-change: opacity, filter, transform;
}

.table-card.is-refreshing {
  opacity: 0.5;
  filter: blur(2px);
  pointer-events: none;
  transform: scale(0.995);
}

.ultra-small { font-size: 0.6rem; padding: 2px 4px; vertical-align: middle; }
.text-end { text-align: right !important; }
</style>
