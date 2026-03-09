<template>
  <div class="admin-products">
    <div class="header">
      <h1>Sản phẩm</h1>
      <button type="button" class="btn-add" @click="openForm()">+ Thêm sản phẩm</button>
    </div>

    <section v-if="loading" class="loading">Đang tải...</section>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên</th>
            <th>Thương hiệu</th>
            <th>Giá</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.name }}</td>
            <td>{{ p.brand || '—' }}</td>
            <td>{{ formatPrice(p.price) }} VNĐ</td>
            <td>
              <button type="button" class="btn-sm" @click="openForm(p)">Sửa</button>
              <button type="button" class="btn-sm danger" @click="confirmDelete(p)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="!loading && products.length === 0" class="empty">Chưa có sản phẩm. Bấm "Thêm sản phẩm" để tạo mới.</p>

    <!-- Modal form -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal">
        <h3>{{ editingId ? 'Sửa sản phẩm' : 'Thêm sản phẩm' }}</h3>
        <form @submit.prevent="saveProduct">
          <div class="form-group">
            <label>Tên *</label>
            <input v-model="form.name" required placeholder="Tên sản phẩm" />
          </div>
          <div class="form-group">
            <label>Giá (VNĐ) *</label>
            <input v-model.number="form.price" type="number" required min="0" placeholder="0" />
          </div>
          <div class="form-group">
            <label>Thương hiệu</label>
            <input v-model="form.brand" placeholder="VD: Nike" />
          </div>
          <div class="form-group">
            <label>Danh mục</label>
            <input v-model="form.category" placeholder="VD: Basketball" />
          </div>
          <div class="form-group">
            <label>Mô tả</label>
            <textarea v-model="form.description" rows="3" placeholder="Mô tả sản phẩm"></textarea>
          </div>
          <div class="form-group">
            <label>Ảnh sản phẩm</label>
            <input
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              @change="onFileSelect"
            />
            <p v-if="form.imageUrl" class="img-preview">
              <img :src="imagePreviewUrl" alt="Preview" class="preview-thumb" />
              <span class="img-name">{{ form.imageUrl }}</span>
            </p>
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
import { getProducts, createProduct, updateProduct, deleteProduct, uploadImage } from '../../api/services/adminService'

const products = ref([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  name: '',
  price: 0,
  brand: '',
  category: '',
  description: '',
  imageUrl: '',
})
const imagePreviewUrl = ref('')

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN')
}

async function load() {
  loading.value = true
  try {
    products.value = await getProducts()
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

async function onFileSelect(e) {
  const file = e.target?.files?.[0]
  if (!file) return
  formError.value = ''
  try {
    const url = await uploadImage(file)
    if (url) {
      form.imageUrl = url
      imagePreviewUrl.value = url
    } else {
      formError.value = 'Upload thất bại'
    }
  } catch (err) {
    const d = err.response?.data
    formError.value = d?.error || d?.message || err.message || 'Không thể upload ảnh'
  }
}

function openForm(p = null) {
  formError.value = ''
  imagePreviewUrl.value = ''
  if (p) {
    editingId.value = p.id
    form.name = p.name || ''
    form.price = p.price || 0
    form.brand = p.brand || ''
    form.category = p.category || ''
    form.description = p.description || ''
    form.imageUrl = p.imageUrl || ''
    imagePreviewUrl.value = p.imageUrl ? (p.imageUrl.startsWith('http') ? p.imageUrl : 'http://localhost:8080' + (p.imageUrl.startsWith('/') ? '' : '/') + p.imageUrl) : ''
  } else {
    editingId.value = null
    form.name = ''
    form.price = 0
    form.brand = ''
    form.category = ''
    form.description = ''
    form.imageUrl = ''
  }
  if (form.imageUrl) imagePreviewUrl.value = form.imageUrl
  showForm.value = true
}

async function saveProduct() {
  formError.value = ''
  saving.value = true
  try {
    const payload = {
      name: form.name,
      price: Number(form.price) || 0,
      brand: form.brand || null,
      category: form.category || null,
      description: form.description || null,
      imageUrl: form.imageUrl || null,
    }
    if (editingId.value) {
      await updateProduct(editingId.value, payload)
    } else {
      await createProduct(payload)
    }
    showForm.value = false
    await load()
  } catch (e) {
    const d = e.response?.data
    formError.value = d?.error || d?.message || (d && typeof d === 'object' ? JSON.stringify(d) : null) || e.message || 'Có lỗi xảy ra'
  } finally {
    saving.value = false
  }
}

function confirmDelete(p) {
  if (!confirm(`Xóa sản phẩm "${p.name}"?`)) return
  deleteProduct(p.id).then(() => load()).catch(() => alert('Không thể xóa'))
}

onMounted(load)
</script>

<style scoped>
.admin-products .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-products h1 {
  font-size: 24px;
  font-weight: 800;
}

.btn-add {
  padding: 10px 18px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.loading, .empty {
  padding: 24px;
  color: #6b7280;
}

.table-wrap {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.table th {
  background: #f9fafb;
  font-weight: 700;
}

.btn-sm {
  padding: 6px 12px;
  margin-right: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.btn-sm.danger {
  color: #b91c1c;
  border-color: #fecaca;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal h3 {
  margin: 0 0 20px;
  font-size: 18px;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.img-preview {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.preview-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.img-name {
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
}

.error {
  color: #b91c1c;
  font-size: 14px;
  margin-bottom: 12px;
}

.modal-actions {
  display: flex;
  gap: 10px;
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
