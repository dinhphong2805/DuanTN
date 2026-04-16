<template>
  <div class="admin-categories">
    <div class="header">
      <h1>Quản lý danh mục</h1>
      <button class="btn-add" @click="openForm()">+ Cấu hình mới</button>
    </div>

    <section v-if="loading" class="loading">Đang tải dữ liệu...</section>
    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên danh mục</th>
            <th>Slug</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in categories" :key="c.id">
            <td>{{ c.id }}</td>
            <td><strong>{{ c.name }}</strong></td>
            <td>{{ c.slug }}</td>
            <td>
              <button class="btn-sm" @click="openForm(c)">Sửa</button>
              <button class="btn-sm danger" @click="confirmDelete(c)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
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
import client from '../../api/client'

const categories = ref([])
const loading = ref(true)

const showForm = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  id: null,
  name: ''
})

async function fetchCategories() {
  loading.value = true
  try {
    const { data } = await client.get('/admin/categories')
    categories.value = data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
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
  if (!confirm(`Xóa danh mục "${c.name}"?`)) return
  try {
    await client.delete(`/admin/categories/${c.id}`)
    await fetchCategories()
  } catch (err) {
    alert(err.response?.data?.message || 'Không thể xóa danh mục')
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.admin-categories .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.admin-categories h1 { font-size: 24px; font-weight: 800; }
.btn-add { padding: 10px 18px; background: #111827; color: #fff; border: none; border-radius: 10px; font-weight: 700; cursor: pointer; }
.loading { padding: 24px; text-align: center; color: #6b7280; }
.table-wrap { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #e5e7eb; }
.table th { background: #f9fafb; font-weight: 700; font-size: 13px; }
.btn-sm { padding: 6px 12px; border: 1px solid #e5e7eb; background: #fff; border-radius: 8px; font-size: 13px; cursor: pointer; margin-right: 8px; }
.btn-sm.danger { color: #b91c1c; border-color: #b91c1c; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 16px; padding: 24px; width: 100%; max-width: 420px; }
.modal h3 { margin: 0 0 20px; font-size: 18px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-weight: 600; margin-bottom: 8px; font-size: 14px; }
.form-group input { width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 8px; outline: none; }
.error { color: #dc2626; font-size: 13px; margin: 0 0 16px; }
.modal-actions { display: flex; gap: 12px; margin-top: 20px; }
.btn-cancel { padding: 10px 18px; border: 1px solid #e5e7eb; background: #fff; border-radius: 10px; cursor: pointer; }
.btn-save { padding: 10px 18px; background: #111827; color: #fff; border: none; border-radius: 10px; font-weight: 600; cursor: pointer; }
</style>
