<template>
  <div class="admin-products">
    <div class="page-header">
      <div class="header-left">
        <h1>Quản lý sản phẩm</h1>
        <p class="subtitle">Hiển thị, bộ lọc và chỉnh sửa danh mục hàng hóa</p>
      </div>
      <button class="btn-primary-add" @click="openForm()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
        Thêm sản phẩm
      </button>
    </div>

    <div class="filter-card">
      <div class="filter-grid">
        <div class="filter-item search-box">
          <svg class="icon-search" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
          <input v-model="filters.keyword" placeholder="Tìm theo tên sản phẩm..." @keyup.enter="applyFilters" />
        </div>
        <div class="filter-item">
          <select v-model="filters.brand" @change="applyFilters" class="form-select">
            <option value="">Tất cả thương hiệu</option>
            <option v-for="b in brandList" :key="b" :value="b">{{ b }}</option>
          </select>
        </div>
        <div class="filter-item">
          <select v-model="filters.category" @change="applyFilters" class="form-select">
            <option value="">Tất cả danh mục</option>
            <option v-for="c in categoryList" :key="c.id" :value="c.name">{{ c.name }}</option>
          </select>
        </div>
        <div class="filter-item price-range">
          <input v-model.number="filters.minPrice" type="number" placeholder="Giá từ" @keyup.enter="applyFilters" />
          <span>-</span>
          <input v-model.number="filters.maxPrice" type="number" placeholder="Đến" @keyup.enter="applyFilters" />
        </div>
      </div>
      <div class="filter-actions">
        <button class="btn-clear" @click="clearFilters">Xóa bộ lọc</button>
        <button class="btn-apply" @click="applyFilters">Lọc dữ liệu</button>
      </div>
    </div>

    <div class="table-card">
      <div v-if="loading" class="loader-container">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">📂</div>
        <h3>Không tìm thấy sản phẩm</h3>
        <p>Hãy thử thay đổi điều kiện lọc hoặc thêm sản phẩm mới.</p>
      </div>

      <table v-else class="modern-table">
        <thead>
          <tr>
            <th width="80">ID</th>
            <th width="80">Ảnh</th>
            <th>Tên sản phẩm</th>
            <th>Phân loại</th>
            <th>Tồn kho</th>
            <th>Giá bán (VNĐ)</th>
            <th class="text-right">Thao tác</th>
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
            <td class="price-col">{{ formatPrice(p.price) }} ₫</td>
            <td class="action-col">
              <button class="action-btn edit" @click="openForm(p)">Sửa</button>
              <button class="action-btn delete" @click="confirmDelete(p)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="pagination">
        <span class="page-info">Trang {{ filters.page + 1 }} / {{ totalPages }} (Tổng {{ totalElements }})</span>
        <div class="page-controls">
          <button :disabled="filters.page === 0" @click="changePage(filters.page - 1)">&larr; Trước</button>
          
          <button 
            v-for="p in pageNumbers" 
            :key="p" 
            :class="{ active: filters.page === p }"
            @click="changePage(p)">
            {{ p + 1 }}
          </button>
          
          <button :disabled="filters.page >= totalPages - 1" @click="changePage(filters.page + 1)">Sau &rarr;</button>
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

async function load() {
  loading.value = true
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
  }
}

function applyFilters() {
  filters.page = 0; 
  load();
}

function clearFilters() {
  filters.keyword = ''; filters.brand = ''; filters.category = ''; 
  filters.minPrice = null; filters.maxPrice = null; filters.page = 0;
  load();
}

function changePage(p) {
  filters.page = p;
  load();
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
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.admin-products {
  font-family: 'Inter', sans-serif;
  padding: 32px;
  background-color: #f4f4f6;
  min-height: 100vh;
  color: #111827;
}

/* --- HEADER --- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left h1 { font-size: 28px; font-weight: 800; margin: 0; letter-spacing: -0.5px; }
.subtitle { color: #6b7280; font-size: 15px; margin: 4px 0 0; }

.btn-primary-add {
  display: flex; align-items: center; gap: 8px;
  background: #111827; color: white;
  padding: 12px 20px; border: none; border-radius: 12px;
  font-weight: 600; font-size: 15px; cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: all 0.2s;
}
.btn-primary-add:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.15); }
.btn-primary-add svg { width: 18px; height: 18px; }

/* --- FILTER BAR --- */
.filter-card {
  background: white; border-radius: 16px; padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02); margin-bottom: 24px;
}
.filter-grid {
  display: grid; grid-template-columns: 2fr 1fr 1fr 1.5fr; gap: 16px; margin-bottom: 16px;
}
.filter-item input, .form-select {
  width: 100%; padding: 12px 16px; background: #f9fafb;
  border: 1px solid #e5e7eb; border-radius: 10px; font-size: 14px;
  transition: all 0.2s; box-sizing: border-box; color: #111827;
}
.filter-item input:focus, .form-select:focus { outline: none; border-color: #111827; background: #fff; }
.search-box { position: relative; }
.search-box input { padding-left: 44px; }
.icon-search { position: absolute; left: 14px; top: 12px; width: 18px; height: 18px; color: #9ca3af; }
.price-range { display: flex; align-items: center; gap: 8px; }

.filter-actions { display: flex; justify-content: flex-end; gap: 12px; }
.btn-clear { background: transparent; border: none; color: #6b7280; font-weight: 600; cursor: pointer; padding: 10px 16px; }
.btn-apply { background: #f3f4f6; border: 1px solid #d1d5db; border-radius: 10px; font-weight: 600; padding: 10px 24px; cursor: pointer; }
.btn-apply:hover { background: #e5e7eb; }

/* --- TABLE --- */
.table-card {
  background: white; border-radius: 16px; overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
}
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table th { background: #f9fafb; padding: 16px; text-align: left; font-size: 13px; color: #6b7280; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e5e7eb; }
.modern-table td { padding: 16px; border-bottom: 1px solid #f3f4f6; vertical-align: middle; }
.modern-table tbody tr:hover { background: #fcfcfd; }

.id-col { font-weight: 600; color: #6b7280; }
.img-thumb { width: 48px; height: 48px; border-radius: 8px; background: #f3f4f6; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.img-thumb img { width: 100%; height: 100%; object-fit: cover; }
.no-img { font-size: 11px; color: #9ca3af; }

.name-col strong { font-size: 15px; color: #111827; }
.badges { display: flex; gap: 6px; flex-wrap: wrap; }
.badge { padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.badge.brand { background: #e0e7ff; color: #3730a3; }
.badge.category { background: #fce7f3; color: #9d174d; }
.price-col { font-weight: 700; color: #111827; }

.text-right { text-align: right !important; }
.action-col { text-align: right; }
.action-btn { background: transparent; border: none; font-weight: 600; font-size: 14px; cursor: pointer; padding: 6px 10px; border-radius: 6px; transition: background 0.2s; }
.action-btn.edit { color: #0284c7; margin-right: 8px; }
.action-btn.edit:hover { background: #e0f2fe; }
.action-btn.delete { color: #e11d48; }
.action-btn.delete:hover { background: #ffe4e6; }

/* --- PAGINATION --- */
.pagination { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: #fff; border-top: 1px solid #e5e7eb; }
.page-info { font-size: 14px; color: #6b7280; }
.page-controls { display: flex; gap: 6px; }
.page-controls button { padding: 8px 12px; border: 1px solid #e5e7eb; background: #fff; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 13px; color: #374151; }
.page-controls button:hover:not(:disabled) { background: #f3f4f6; }
.page-controls button.active { background: #111827; color: #fff; border-color: #111827; }
.page-controls button:disabled { opacity: 0.4; cursor: not-allowed; }

/* --- MODAL --- */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 9999; }
.modal { background: #fff; border-radius: 24px; width: 100%; max-width: 560px; box-shadow: 0 25px 50px rgba(0,0,0,0.25); overflow: hidden; display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { padding: 24px; border-bottom: 1px solid #f3f4f6; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 20px; font-weight: 800; }
.close-btn { background: #f3f4f6; border: none; width: 32px; height: 32px; border-radius: 50%; font-size: 20px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.close-btn:hover { background: #e5e7eb; }

.modal-body { padding: 24px; overflow-y: auto; }
.form-group { margin-bottom: 20px; }
.form-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 8px; color: #374151; }
.req { color: #e11d48; }
.form-group input, .form-group textarea, .form-select { width: 100%; padding: 12px 16px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; font-family: inherit; font-size: 14px; box-sizing: border-box; }
.form-group input:focus, .form-group textarea:focus, .form-select:focus { outline: none; border-color: #111827; background: #fff; }

.upload-area { border: 2px dashed #d1d5db; border-radius: 12px; padding: 24px; text-align: center; position: relative; background: #f9fafb; cursor: pointer; }
.upload-area input { position: absolute; inset: 0; opacity: 0; cursor: pointer; }
.upload-area p { margin: 0; font-size: 14px; font-weight: 500; color: #6b7280; }

.img-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; margin-top: 16px; }
.img-preview { position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; border: 1px solid #e5e7eb; }
.img-preview img { width: 100%; height: 100%; object-fit: cover; }
.img-preview button { position: absolute; top: 4px; right: 4px; background: rgba(0,0,0,0.6); color: white; border: none; width: 20px; height: 20px; border-radius: 50%; font-size: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; }

.modal-footer { padding: 20px 24px; border-top: 1px solid #f3f4f6; display: flex; justify-content: flex-end; gap: 12px; background: #f9fafb; }
.btn-cancel { padding: 12px 20px; background: white; border: 1px solid #d1d5db; border-radius: 12px; font-weight: 600; cursor: pointer; }
.btn-save { padding: 12px 24px; background: #111827; color: white; border: none; border-radius: 12px; font-weight: 600; cursor: pointer; }

/* Effects */
.fade-scale-enter-active, .fade-scale-leave-active { transition: opacity 0.3s, transform 0.3s; }
.fade-scale-enter-from, .fade-scale-leave-to { opacity: 0; transform: scale(0.95); }
.loader-container { padding: 60px; text-align: center; color: #6b7280; }
.spinner { width: 30px; height: 30px; border: 3px solid #f3f4f6; border-top-color: #111827; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 12px; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { padding: 60px; text-align: center; color: #6b7280; }
.empty-icon { font-size: 48px; margin-bottom: 16px; opacity: 0.5; }
</style>