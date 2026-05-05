<template>
  <div class="admin-categories">
    <div class="header d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="h3 fw-bold mb-1">Quản lý danh mục</h1>
        <p class="text-secondary small mb-0">Phân loại sản phẩm cho cửa hàng</p>
      </div>
      <button class="btn btn-primary d-flex align-items-center gap-2 px-4 shadow-sm" @click="openForm()">
        <i class="bi bi-plus-lg"></i> Cấu hình mới
      </button>
    </div>

    <div class="table-card shadow-sm border-0" :class="{ 'is-refreshing': isRefreshing }">
      <section v-if="loading && !isRefreshing" class="loading-wrap text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="mt-3 text-secondary">Đang tải dữ liệu...</p>
      </section>
      
      <div v-else class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="bg-light text-uppercase small fw-bold text-secondary">
            <tr>
              <th class="ps-4">ID</th>
              <th>Tên danh mục</th>
              <th>Slug</th>
              <th class="pe-4 text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in categories" :key="c.id" class="align-middle">
              <td class="ps-4 text-secondary">#{{ c.id }}</td>
              <td><strong class="text-dark">{{ c.name }}</strong></td>
              <td class="text-secondary mono small">{{ c.slug }}</td>
              <td class="pe-4 text-end">
                <button class="btn btn-outline-primary btn-sm me-2 border-0" @click="openForm(c)">
                  <i class="bi bi-pencil-square"></i> Sửa
                </button>
                <button class="btn btn-outline-danger btn-sm border-0" @click="confirmDelete(c)">
                  <i class="bi bi-trash"></i> Xóa
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal form -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal">
        <h3>{{ isEdit ? 'Sửa danh mục' : 'Thêm mới danh mục' }}</h3>
        <form @submit.prevent="saveCategory">
          <div class="form-group">
            <label>Tên danh mục *</label>
            <input v-model="form.name" required placeholder="VD: Giày thể thao" />
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
import client from '../../api/client'

const categories = ref([])
const loading = ref(true)
const isRefreshing = ref(false)

const showForm = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  name: ''
})

async function fetchCategories(refresh = false) {
  if (refresh) isRefreshing.value = true
  else loading.value = true
  
  try {
    const { data } = await client.get('/admin/categories')
    categories.value = data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function openForm(c = null) {
  formError.value = ''
  if (c) {
    isEdit.value = true
    form.id = c.id
    form.name = c.name
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
  }
  showForm.value = true
}

async function saveCategory() {
  saving.value = true
  formError.value = ''
  try {
    if (isEdit.value) {
      await client.put(`/admin/categories/${form.id}`, { name: form.name.trim() })
    } else {
      await client.post('/admin/categories', { name: form.name.trim() })
    }
    showForm.value = false
    await fetchCategories()
  } catch (err) {
    formError.value = err.response?.data?.message || err.message || 'Lỗi lưu danh mục'
  } finally {
    saving.value = false
  }
}

async function confirmDelete(c) {
  const result = await Swal.fire({
    title: 'Xóa danh mục?',
    text: `Bạn có chắc muốn xóa "${c.name}"? Hành động này không thể hoàn tác.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#e11d48',
    cancelButtonColor: '#94a3b8',
    confirmButtonText: 'Có, xóa ngay!',
    cancelButtonText: 'Hủy bỏ'
  })
  
  if (!result.isConfirmed) return
  
  try {
    await client.delete(`/admin/categories/${c.id}`)
    Swal.fire({
      title: 'Đã xóa!',
      text: 'Danh mục đã được xóa thành công.',
      icon: 'success',
      timer: 1500,
      showConfirmButton: false
    })
    await fetchCategories()
  } catch (err) {
    Swal.fire({
      title: 'Lỗi',
      text: err.response?.data?.message || 'Không thể xóa danh mục',
      icon: 'error',
      confirmButtonColor: '#4f46e5'
    })
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
/* ============================================================
   AdminCategories — Scoped styles
   ============================================================ */

.admin-categories { max-width: 900px; margin: 0 auto; }

/* Header */
.header {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: 12px;
  margin-bottom: 24px; padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}
.header h1 {
  margin: 0; font-size: 1.55rem; font-weight: 800;
  color: #0f172a; letter-spacing: -.03em;
}

.btn-add {
  padding: 9px 20px; border-radius: 12px; border: none;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: .875rem; font-weight: 700;
  cursor: pointer; font-family: inherit;
  box-shadow: 0 4px 14px rgba(99,102,241,.28);
  transition: filter .15s, transform .15s;
}
.btn-add:hover { filter: brightness(1.08); transform: translateY(-1px); }

/* Loading */
.loading {
  padding: 40px; text-align: center; color: #94a3b8;
  font-size: .95rem; background: #fff; border: 1px solid #e2e8f0;
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
  border-bottom: 1.5px solid #e2e8f0;
}
.table td {
  padding: 14px 18px; border-bottom: 1px solid #f1f5f9;
  color: #334155; vertical-align: middle;
}
.table tbody tr:last-child td { border-bottom: none; }
.table tbody tr { transition: background .15s; }
.table tbody tr:hover td { background: #fafbff; }

/* Action buttons inside table */
.btn-sm {
  padding: 6px 13px; border-radius: 8px; font-size: .78rem;
  font-weight: 600; cursor: pointer; font-family: inherit;
  border: 1.5px solid #e2e8f0; background: #fff; color: #4f46e5;
  transition: all .15s; margin-right: 6px;
}
.btn-sm:hover { background: #eef2ff; border-color: #6366f1; }
.btn-sm.danger { color: #be123c; border-color: #fda4af; }
.btn-sm.danger:hover { background: #fff1f2; border-color: #fb7185; }

/* Modal overlay */
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
  max-width: 440px; width: 100%;
  box-shadow: 0 24px 64px rgba(15,23,42,.2), 0 0 0 1px rgba(99,102,241,.07);
  animation: mo-in .3s cubic-bezier(.16,1,.3,1);
}
@keyframes mo-in { from { opacity: 0; transform: translateY(20px) scale(.96); } to { opacity: 1; transform: none; } }

.modal h3 { margin: 0 0 20px; font-size: 1.1rem; font-weight: 800; color: #0f172a; }

.form-group { display: flex; flex-direction: column; gap: 7px; margin-bottom: 16px; }
.form-group label { font-size: .8125rem; font-weight: 600; color: #475569; }
.form-group input {
  padding: 10px 13px; border: 1.5px solid #e2e8f0; border-radius: 9px;
  font-size: .875rem; font-family: inherit; color: #0f172a; outline: none;
  transition: border-color .15s, box-shadow .15s;
}
.form-group input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.15); }

.error { color: #be123c; font-size: .84rem; font-weight: 600; margin: -8px 0 14px; }

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }

.btn-cancel {
  padding: 9px 18px; border-radius: 10px;
  border: 1.5px solid #e2e8f0; background: #fff; color: #64748b;
  font-size: .875rem; font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-cancel:hover { background: #f1f5f9; }
.btn-save {
  padding: 9px 22px; border-radius: 10px; border: none;
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

.text-end { text-align: right !important; }
.mono { font-family: 'JetBrains Mono', monospace; }
</style>

