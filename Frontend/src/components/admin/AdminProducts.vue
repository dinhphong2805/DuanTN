<template>
  <div class="admin-products">
    <header class="page-head d-flex justify-content-between align-items-center mb-4">
      <div class="page-head__text">
        <h1 class="h3 fw-bold mb-1">Quản lý sản phẩm</h1>
        <p class="text-secondary small mb-0">Hiển thị, bộ lọc và chỉnh sửa danh mục hàng hóa</p>
      </div>
      <div class="page-head__actions d-flex gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm d-flex align-items-center gap-2 px-3" :disabled="loading" @click="load(true)">
          <i class="bi bi-arrow-clockwise" :class="{ 'spinner-border spinner-border-sm border-0': isRefreshing }"></i>
          Làm mới
        </button>
        <button class="btn btn-primary btn-sm d-flex align-items-center gap-2 px-4 shadow-sm" @click="openForm()">
          <i class="bi bi-plus-lg"></i> Thêm sản phẩm
        </button>
      </div>
    </header>

    <div class="filter-card card shadow-sm border-0 mb-4 p-3">
      <div class="row g-3">
        <div class="col-md-4">
          <div class="position-relative">
            <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 text-secondary"></i>
            <input v-model="filters.keyword" class="form-control ps-5" placeholder="Tìm theo tên..." @keyup.enter="applyFilters" />
          </div>
        </div>
        <div class="col-md-2">
          <select v-model="filters.brand" @change="applyFilters" class="form-select bg-light border-0">
            <option value="">Thương hiệu</option>
            <option v-for="b in brandList" :key="b" :value="b">{{ b }}</option>
          </select>
        </div>
        <div class="col-md-2">
          <select v-model="filters.category" @change="applyFilters" class="form-select bg-light border-0">
            <option value="">Danh mục</option>
            <option v-for="c in categoryList" :key="c.id" :value="c.name">{{ c.name }}</option>
          </select>
        </div>
        <div class="col-md-3">
          <div class="input-group input-group-sm">
            <input v-model.number="filters.minPrice" type="number" class="form-control border-0 bg-light" placeholder="Min" @keyup.enter="applyFilters" />
            <span class="input-group-text bg-light border-0">-</span>
            <input v-model.number="filters.maxPrice" type="number" class="form-control border-0 bg-light" placeholder="Max" @keyup.enter="applyFilters" />
          </div>
        </div>
        <div class="col-md-1 d-flex gap-1 justify-content-end">
          <button class="btn btn-dark btn-sm rounded-circle px-2 py-2 shadow-sm" @click="applyFilters" title="Lọc">
            <i class="bi bi-funnel-fill"></i>
          </button>
          <button class="btn btn-outline-secondary btn-sm rounded-circle px-2 py-2" @click="clearFilters" title="Xóa lọc">
            <i class="bi bi-trash-fill"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="table-card" :class="{ 'is-refreshing': isRefreshing }">
      <div v-if="loading && !isRefreshing" class="loader-container">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon text-muted">📂</div>
        <h3>Không tìm thấy sản phẩm</h3>
        <p>Hãy thử thay đổi điều kiện lọc hoặc thêm sản phẩm mới.</p>
      </div>

      <div v-else class="table-responsive">
        <table class="modern-table mb-0">
          <thead>
            <tr>
              <th width="80">ID</th>
              <th width="80">Ảnh</th>
              <th>Tên sản phẩm</th>
              <th>Phân loại</th>
              <th>Tồn kho</th>
              <th>Giá bán (VNĐ)</th>
              <th class="text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.id">
              <td class="id-col">#{{ p.id }}</td>
              <td>
                <div class="img-thumb">
                  <img v-if="getFirstImage(p.imageUrl)" :src="toDisplayUrl(getFirstImage(p.imageUrl))" />
                  <span v-else class="no-img">N/A</span>
                </div>
              </td>
              <td class="name-col">
                <strong>{{ p.name }}</strong>
              </td>
              <td>
                <div class="badges">
                  <span class="badge brand" v-if="p.brand">{{ p.brand }}</span>
                  <span class="badge category" v-if="p.category">{{ p.category }}</span>
                </div>
              </td>
              <td>{{ p.stockQty ?? 0 }}</td>
              <td class="price-col text-primary fw-bold">{{ formatPrice(p.price) }} ₫</td>
              <td class="action-col text-end">
                <button class="action-btn edit" @click="openForm(p)">
                  <i class="bi bi-pencil-square me-1"></i> Sửa
                </button>
                <button class="action-btn delete" @click="confirmDelete(p)">
                  <i class="bi bi-trash me-1"></i> Xóa
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination-footer px-4 py-3 d-flex align-items-center justify-content-between border-top">
        <span class="page-info text-secondary">Trang {{ filters.page + 1 }} / {{ totalPages }} (Tổng {{ totalElements }})</span>
        <div class="page-controls d-flex gap-2">
          <button class="btn btn-outline-secondary btn-sm" :disabled="filters.page === 0" @click="changePage(filters.page - 1)">
            <i class="bi bi-chevron-left"></i>
          </button>
          
          <button 
            v-for="p in pageNumbers" 
            :key="p" 
            class="btn btn-sm"
            :class="filters.page === p ? 'btn-dark' : 'btn-outline-secondary'"
            @click="changePage(p)">
            {{ p + 1 }}
          </button>
          
          <button class="btn btn-outline-secondary btn-sm" :disabled="filters.page >= totalPages - 1" @click="changePage(filters.page + 1)">
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <Transition name="fade-scale">
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div class="modal">
          <div class="modal-header">
            <h3>{{ editingId ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới' }}</h3>
            <button class="close-btn" @click="showForm = false">&times;</button>
          </div>
          
          <form @submit.prevent="saveProduct" class="modal-body">
            <div class="form-group">
              <label>Tên sản phẩm <span class="req">*</span></label>
              <input v-model="form.name" required placeholder="Nhập tên chi tiết..." />
            </div>
            
            <div class="form-row-2">
              <div class="form-group">
                <label>Giá (VNĐ) <span class="req">*</span></label>
                <input v-model.number="form.price" type="number" required min="0" placeholder="0" />
              </div>
              <div class="form-group">
                <label>Tồn kho ban đầu</label>
                <input v-model.number="form.stockQty" type="number" min="0" placeholder="0" />
              </div>
              <div class="form-group">
                <label>Thương hiệu</label>
                <select v-model="form.brand" class="form-select">
                  <option value="">-- Chọn thương hiệu --</option>
                  <option v-for="b in brandList" :key="b" :value="b">{{ b }}</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label>Danh mục</label>
              <select v-model="form.categoryId" class="form-select">
                <option :value="null">-- Chọn danh mục --</option>
                <option v-for="c in categoryList" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>Mô tả chi tiết</label>
              <textarea v-model="form.description" rows="3" placeholder="Nhập mô tả sản phẩm"></textarea>
            </div>
            
            <div class="form-group">
              <label>Upload ảnh (Max 10)</label>
              <div class="upload-area">
                <input type="file" multiple accept="image/*" @change="onFileSelect" />
                <p>Kéo thả hoặc click để chọn ảnh</p>
              </div>
              <div v-if="form.images.length" class="img-grid">
                <div v-for="(img, idx) in form.images" :key="idx" class="img-preview">
                  <img :src="toDisplayUrl(img)" />
                  <button type="button" @click="removeImageAt(idx)">&times;</button>
                </div>
              </div>
            </div>
            
            <p v-if="formError" class="error-msg">{{ formError }}</p>
            
            <div class="modal-footer">
              <button type="button" class="btn-cancel" @click="showForm = false">Hủy</button>
              <button type="submit" class="btn-save" :disabled="saving">
                {{ saving ? 'Đang lưu...' : 'Lưu sản phẩm' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'

/* LƯU Ý: Nếu đường dẫn import này bị lỗi, bro nhớ sửa lại cho khớp với file adminService.js của bro nhé */
import { getProducts, createProduct, updateProduct, deleteProduct, uploadImage, getBrandNames, getCategories } from '../../adminService'
import { API_BASE_URL } from '../../api/config'

const products = ref([])
const loading = ref(true)
const isRefreshing = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')

// State cho Dropdown Category và Brand
const brandList = ref([])
const categoryList = ref([])

// State cho Phân trang & Lọc
const totalPages = ref(0)
const totalElements = ref(0)
const filters = reactive({
  keyword: '',
  brand: '',
  category: '',
  minPrice: null,
  maxPrice: null,
  page: 0,
  size: 10
})

const form = reactive({
  name: '', price: 0, stockQty: 0, brand: '', category: '', categoryId: null, description: '', images: [],
})

const pageNumbers = computed(() => {
  let pages = [];
  let start = Math.max(0, filters.page - 2);
  let end = Math.min(totalPages.value - 1, filters.page + 2);
  for (let i = start; i <= end; i++) pages.push(i);
  return pages;
})

function formatPrice(n) { return Number(n || 0).toLocaleString('vi-VN') }

function getFirstImage(imgStr) {
  const arr = parseImageUrls(imgStr);
  return arr.length > 0 ? arr[0] : null;
}

// ĐÃ SỬA: Hàm bọc giáp chống lỗi .startsWith is not a function
function toDisplayUrl(url) {
  if (!url) return '';
  
  // Rút trích đường dẫn nếu API trả về Object
  if (typeof url === 'object') {
      url = url.url || url.imageUrl || url.message || String(url);
  }
  
  if (typeof url !== 'string') return '';

  const base = API_BASE_URL.replace(/\/api\/?$/, '') || 'http://localhost:8080'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return base + url
  const filename = url.replace(/^.*[/\\]/, '')
  return filename ? `${base}/uploads/${filename}` : ''
}

function parseImageUrls(raw) {
  if (!raw) return []
  const txt = String(raw).trim()
  if (!txt) return []
  if (txt.startsWith('[')) { try { const arr = JSON.parse(txt); if (Array.isArray(arr)) return arr; } catch {} }
  if (txt.includes('|')) return txt.split('|').map(s => s.trim()).filter(Boolean)
  return [txt]
}

function serializeImageUrls(list) {
  return (list || []).filter(Boolean).slice(0, 10).join('|')
}

function dedupeSorted(list) {
  const arr = Array.isArray(list) ? list : []
  return [...new Set(arr.map((x) => String(x ?? '').trim()).filter(Boolean))].sort((a, b) =>
    a.localeCompare(b, 'vi')
  )
}

async function loadOptions() {
  try {
    const brands = await getBrandNames()
    const cats = await getCategories()
    brandList.value = dedupeSorted(brands)
    categoryList.value = cats || []
  } catch (error) {
    console.error("Lỗi tải danh mục/thương hiệu:", error)
  }
}

async function load(refresh = false) {
  if (refresh) isRefreshing.value = true
  else loading.value = true
  
  try {
    const res = await getProducts(filters)
    if (res && res.content !== undefined) {
      products.value = res.content
      totalPages.value = res.totalPages
      totalElements.value = res.totalElements
    } else {
      products.value = res || []
      totalPages.value = 1
    }
  } catch (err) {
    products.value = []
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function applyFilters() {
  filters.page = 0; 
  load(true);
}

function clearFilters() {
  filters.keyword = ''; filters.brand = ''; filters.category = ''; 
  filters.minPrice = null; filters.maxPrice = null; filters.page = 0;
  load(true);
}

function changePage(p) {
  filters.page = p;
  load(true);
}

// ĐÃ SỬA: Hàm bọc giáp chống lỗi nhận Object thay vì chuỗi URL khi upload
async function onFileSelect(e) {
  const files = Array.from(e.target?.files || [])
  if (!files.length) return
  if (form.images.length + files.length > 10) { formError.value = 'Tối đa 10 ảnh'; e.target.value = ''; return }
  try {
    for (const file of files) {
      const res = await uploadImage(file)
      // Ép kiểu chuẩn: Lấy đúng cái link URL dù API trả về chuỗi hay Object
      const imgUrl = typeof res === 'string' ? res : (res?.url || res?.imageUrl || res?.message || '');
      if (imgUrl) form.images.push(imgUrl)
    }
  } catch (err) { 
      formError.value = 'Upload thất bại' 
  } finally { 
      e.target.value = '' 
  }
}

function removeImageAt(idx) { form.images.splice(idx, 1) }

function openForm(p = null) {
  formError.value = ''
  if (p) {
    editingId.value = p.id
    form.name = p.name || ''; form.price = p.price || 0; form.stockQty = p.stockQty ?? 0; form.brand = p.brand || '';
    form.category = p.category || ''; form.categoryId = p.categoryId || null; form.description = p.description || '';
    form.images = parseImageUrls(p.imageUrl)
  } else {
    editingId.value = null; form.name = ''; form.price = 0; form.stockQty = 0; form.brand = ''; form.category = ''; form.categoryId = null; form.description = ''; form.images = []
  }
  showForm.value = true
}

async function saveProduct() {
  saving.value = true
  try {
    const payload = {
      name: form.name, price: Number(form.price) || 0, brand: form.brand || null,
      stockQty: Number(form.stockQty) || 0,
      description: form.description || null,
      imageUrl: serializeImageUrls(form.images) || null,
      categoryId: form.categoryId || null,
      category: form.category || null
    }
    
    if (form.categoryId) {
        const selectedCat = categoryList.value.find(c => c.id === form.categoryId);
        if (selectedCat) payload.category = selectedCat.name;
    }
    if (editingId.value) await updateProduct(editingId.value, payload)
    else await createProduct(payload)
    showForm.value = false
    await load()
  } catch (e) { formError.value = 'Lỗi lưu dữ liệu' } finally { saving.value = false }
}

function confirmDelete(p) {
  if (!confirm(`Xóa sản phẩm "${p.name}"?`)) return
  deleteProduct(p.id).then(() => load()).catch(() => alert('Không thể xóa'))
}

onMounted(() => {
  loadOptions()
  load()
})
</script>

<style scoped>
/* ============================================================
   AdminProducts — Scoped styles
   ============================================================ */

.admin-products { max-width: 1400px; margin: 0 auto; }

/* Page Header */
.page-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  flex-wrap: wrap; gap: 12px;
  margin-bottom: 24px; padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}
.header-left h1 {
  margin: 0; font-size: 1.6rem; font-weight: 800;
  color: #0f172a; letter-spacing: -.03em;
}
.subtitle { margin: 6px 0 0; font-size: .9rem; color: #64748b; }

.btn-primary-add {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 20px; border-radius: 12px; border: none;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: .875rem; font-weight: 700;
  cursor: pointer; font-family: inherit;
  box-shadow: 0 4px 14px rgba(99,102,241,.28);
  transition: filter .15s, transform .15s;
}
.btn-primary-add svg { width: 17px; height: 17px; flex-shrink: 0; }
.btn-primary-add:hover { filter: brightness(1.08); transform: translateY(-1px); }

/* Filter Card */
.filter-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px;
  padding: 18px 22px; margin-bottom: 18px;
  box-shadow: 0 1px 4px rgba(15,23,42,.05);
}
.filter-grid {
  display: grid; grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 12px; margin-bottom: 14px;
}
@media (max-width: 900px) { .filter-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 560px) { .filter-grid { grid-template-columns: 1fr; } }

.filter-item { position: relative; }
.filter-item input,
.form-select {
  width: 100%; padding: 9px 13px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: .875rem; font-family: inherit;
  color: #0f172a; background: #f8fafc; outline: none;
  transition: border-color .15s, box-shadow .15s;
}
.filter-item input:focus,
.form-select:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.15); background: #fff; }

.search-box { display: flex; align-items: center; }
.icon-search { position: absolute; left: 12px; width: 16px; height: 16px; color: #94a3b8; pointer-events: none; }
.search-box input { padding-left: 38px; }

.price-range { display: flex; align-items: center; gap: 8px; }
.price-range input { flex: 1; }
.price-range span { color: #94a3b8; font-weight: 600; flex-shrink: 0; }

.filter-actions { display: flex; gap: 10px; justify-content: flex-end; }
.btn-clear {
  padding: 8px 16px; border-radius: 10px;
  border: 1.5px solid #e2e8f0; background: #fff;
  font-size: .8125rem; font-weight: 600; color: #64748b;
  cursor: pointer; font-family: inherit; transition: all .15s;
}
.btn-clear:hover { background: #f1f5f9; }
.btn-apply {
  padding: 8px 18px; border-radius: 10px; border: none;
  background: #0f172a; color: #fff;
  font-size: .8125rem; font-weight: 700;
  cursor: pointer; font-family: inherit; transition: filter .15s;
}
.btn-apply:hover { filter: brightness(1.1); }

/* Table Card */
.table-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px;
  overflow: hidden; box-shadow: 0 2px 10px rgba(15,23,42,.06);
}

/* Loader */
.loader-container {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 12px; padding: 60px 24px;
  color: #94a3b8; font-size: .9rem;
}
.spinner {
  width: 36px; height: 36px; border-radius: 50%;
  border: 3px solid #f1f5f9; border-top-color: #6366f1;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Empty */
.empty-state {
  text-align: center; padding: 60px 24px; color: #94a3b8;
}
.empty-icon { font-size: 2.5rem; margin-bottom: 12px; }
.empty-state h3 { margin: 0 0 8px; color: #475569; font-size: 1rem; }
.empty-state p { margin: 0; font-size: .875rem; }

/* Table */
.modern-table { width: 100%; border-collapse: collapse; font-size: .875rem; }
.modern-table thead tr { background: #f8fafc; }
.modern-table th {
  text-align: left; padding: 13px 18px;
  font-size: .67rem; font-weight: 700; text-transform: uppercase;
  letter-spacing: .07em; color: #94a3b8;
  border-bottom: 1.5px solid #e2e8f0; white-space: nowrap;
}
.modern-table td {
  padding: 13px 18px; border-bottom: 1px solid #f1f5f9;
  color: #334155; vertical-align: middle;
}
.modern-table tbody tr { transition: background .15s; }
.modern-table tbody tr:hover td { background: #fafbff; }
.modern-table tbody tr:last-child td { border-bottom: none; }
.text-right { text-align: right !important; }

.id-col { color: #94a3b8; font-size: .8rem; font-weight: 700; }
.name-col strong { font-weight: 700; color: #0f172a; }
.price-col { font-weight: 700; font-variant-numeric: tabular-nums; color: #0f172a; }
.action-col { text-align: right; }

/* Product thumb */
.img-thumb {
  width: 48px; height: 48px; border-radius: 10px; overflow: hidden;
  border: 1px solid #e2e8f0; background: #f8fafc;
  display: flex; align-items: center; justify-content: center;
}
.img-thumb img { width: 100%; height: 100%; object-fit: cover; }
.no-img { font-size: .7rem; color: #94a3b8; }

/* Brand/Category badges */
.badges { display: flex; flex-wrap: wrap; gap: 5px; }
.badge {
  display: inline-flex; align-items: center;
  padding: 3px 9px; border-radius: 999px;
  font-size: .7rem; font-weight: 700;
}
.badge.brand    { background: #ede9fe; color: #7c3aed; }
.badge.category { background: #e0f2fe; color: #0369a1; }

/* Action buttons */
.action-btn {
  padding: 5px 12px; border-radius: 7px;
  font-size: .78rem; font-weight: 600; cursor: pointer;
  border: 1.5px solid; font-family: inherit; transition: all .15s;
  margin-left: 5px;
}
.action-btn.edit { border-color: #c7d2fe; color: #4f46e5; background: #fff; }
.action-btn.edit:hover { background: #eef2ff; border-color: #6366f1; }
.action-btn.delete { border-color: #fda4af; color: #be123c; background: #fff; }
.action-btn.delete:hover { background: #fff1f2; border-color: #fb7185; }

/* Pagination */
.pagination {
  display: flex; flex-wrap: wrap; align-items: center;
  justify-content: space-between; gap: 12px;
  padding: 14px 20px; border-top: 1px solid #f1f5f9;
}
.page-info { font-size: .84rem; color: #64748b; font-weight: 500; }
.page-controls { display: flex; gap: 5px; align-items: center; }
.page-controls button {
  min-width: 32px; height: 32px; padding: 0 8px; border-radius: 8px;
  border: 1.5px solid #e2e8f0; background: #fff;
  font-size: .84rem; font-weight: 600; color: #475569;
  cursor: pointer; font-family: inherit; transition: all .15s;
}
.page-controls button:hover:not(:disabled) { background: #f1f5f9; }
.page-controls button.active { background: #0f172a; border-color: #0f172a; color: #fff; }
.page-controls button:disabled { opacity: .35; cursor: not-allowed; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(15,23,42,.52);
  backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center;
  z-index: 8000; padding: 20px;
}
.fade-scale-enter-active { transition: opacity .2s ease, transform .3s cubic-bezier(.16,1,.3,1); }
.fade-scale-leave-active { transition: opacity .15s ease, transform .15s ease; }
.fade-scale-enter-from { opacity: 0; transform: scale(.95) translateY(16px); }
.fade-scale-leave-to   { opacity: 0; transform: scale(.97); }

.modal {
  background: #fff; border-radius: 18px; padding: 0;
  max-width: 620px; width: 100%; max-height: min(92vh, 820px);
  overflow: hidden; display: flex; flex-direction: column;
  box-shadow: 0 24px 64px rgba(15,23,42,.22), 0 0 0 1px rgba(99,102,241,.07);
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 22px 26px 18px; border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%); flex-shrink: 0;
}
.modal-header h3 { margin: 0; font-size: 1.1rem; font-weight: 800; color: #0f172a; }
.close-btn { width: 32px; height: 32px; border-radius: 8px; background: #f1f5f9; border: none; font-size: 1.1rem; cursor: pointer; color: #64748b; transition: background .15s; }
.close-btn:hover { background: #e2e8f0; }

.modal-body { padding: 22px 26px; overflow-y: auto; flex: 1; }

.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-group label { font-size: .8125rem; font-weight: 600; color: #475569; }
.req { color: #e11d48; }
.form-group input,
.form-group textarea,
.form-group .form-select {
  padding: 9px 12px; border: 1.5px solid #e2e8f0; border-radius: 9px;
  font-size: .875rem; font-family: inherit; color: #0f172a; outline: none;
  transition: border-color .15s, box-shadow .15s; background: #f8fafc;
}
.form-group input:focus,
.form-group textarea:focus,
.form-group .form-select:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.15); background: #fff; }
.form-group textarea { resize: vertical; }

.form-row-2 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px; }
@media (max-width: 560px) { .form-row-2 { grid-template-columns: 1fr; } }

/* Upload */
.upload-area {
  border: 2px dashed #cbd5e1; border-radius: 12px; padding: 20px;
  text-align: center; position: relative; background: #f8fafc;
  transition: border-color .15s, background .15s; cursor: pointer;
}
.upload-area:hover { border-color: #6366f1; background: #fafbff; }
.upload-area input[type="file"] { position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; }
.upload-area p { margin: 0; font-size: .84rem; color: #94a3b8; pointer-events: none; }

.img-grid { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 12px; }
.img-preview { position: relative; width: 72px; height: 72px; border-radius: 10px; overflow: hidden; border: 1.5px solid #e2e8f0; }
.img-preview img { width: 100%; height: 100%; object-fit: cover; }
.img-preview button {
  position: absolute; top: 3px; right: 3px;
  width: 20px; height: 20px; border-radius: 50%;
  background: rgba(15,23,42,.7); color: #fff; border: none;
  font-size: .75rem; cursor: pointer; display: flex; align-items: center; justify-content: center;
}

.error-msg { color: #be123c; font-size: .84rem; font-weight: 600; margin: -8px 0 8px; }

.modal-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 14px 26px; border-top: 1px solid #f1f5f9;
  background: #f8fafc; flex-shrink: 0;
}
.btn-cancel {
  padding: 9px 18px; border-radius: 10px;
  border: 1.5px solid #e2e8f0; background: #fff; color: #64748b;
  font-size: .875rem; font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-cancel:hover { background: #f1f5f9; }
.btn-save {
  padding: 9px 22px; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #6366f1, #4f46e5); color: #fff;
  font-size: .875rem; font-weight: 700; cursor: pointer; font-family: inherit;
  box-shadow: 0 4px 12px rgba(99,102,241,.28);
}
/* ---- Smooth glass-fade effect cho data refresh ---- */
.table-card {
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
</style>

