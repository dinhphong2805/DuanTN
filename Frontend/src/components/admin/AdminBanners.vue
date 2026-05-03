<template>
  <div class="admin-banners">

    <!-- Page Header -->
    <div class="page-head">
      <div>
        <h1>Quản lý Banner</h1>
        <p class="page-sub">Thêm, sắp xếp và bật/tắt ảnh banner trang chủ</p>
      </div>
      <div class="page-head__actions">
        <button class="btn-ghost" @click="load(true)" :disabled="isRefreshing">
          <i class="bi bi-arrow-clockwise" :class="{ 'spin': isRefreshing }"></i>
          Làm mới
        </button>
        <button class="btn-primary" @click="openCreate">
          <i class="bi bi-plus-lg"></i> Thêm banner
        </button>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading && !isRefreshing" class="skeleton-panel">
      <div class="sk-line sk-line--title"></div>
      <div class="banner-grid">
        <div v-for="i in 4" :key="i" class="sk-banner-card">
          <div class="sk-img"></div>
          <div class="sk-line" style="margin-top:12px;width:70%"></div>
          <div class="sk-line" style="margin-top:8px;width:40%"></div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!banners.length" class="empty-state">
      <span class="empty-ico">🖼️</span>
      Chưa có banner nào. Hãy thêm banner đầu tiên!
    </div>

    <!-- Banner grid -->
    <div v-else class="banner-grid" :class="{ 'is-refreshing': isRefreshing }">
      <div
        v-for="(b, idx) in banners"
        :key="b.id"
        class="banner-card"
        :class="{ 'banner-card--inactive': !b.active }"
      >
        <!-- Image preview -->
        <div class="banner-img-wrap">
          <img :src="toDisplayUrl(b.imageUrl)" :alt="b.title" class="banner-img" loading="lazy" />
          <div class="banner-overlay">
            <button class="banner-overlay-btn" @click="openEdit(b)" title="Chỉnh sửa">
              <i class="bi bi-pencil-fill"></i>
            </button>
            <button class="banner-overlay-btn danger" @click="confirmDelete(b)" title="Xóa">
              <i class="bi bi-trash-fill"></i>
            </button>
          </div>
          <div v-if="!b.active" class="banner-inactive-badge">Ẩn</div>
        </div>

        <!-- Info -->
        <div class="banner-info">
          <div class="banner-info__row">
            <span class="banner-order">#{{ idx + 1 }}</span>
            <span class="banner-title">{{ b.title }}</span>
          </div>
          <div v-if="b.linkUrl" class="banner-link">
            <i class="bi bi-link-45deg"></i>
            <span class="text-truncate">{{ b.linkUrl }}</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="banner-actions">
          <!-- Toggle active -->
          <button
            class="banner-toggle"
            :class="b.active ? 'banner-toggle--on' : 'banner-toggle--off'"
            @click="toggleActive(b)"
            :disabled="toggling === b.id"
          >
            <span class="banner-toggle__track">
              <span class="banner-toggle__thumb"></span>
            </span>
            {{ b.active ? 'Đang hiển thị' : 'Đã ẩn' }}
          </button>

          <!-- Sort order buttons -->
          <div class="banner-sort-btns">
            <button
              class="sort-btn"
              :disabled="idx === 0"
              @click="moveUp(idx)"
              title="Lên trên"
            ><i class="bi bi-chevron-up"></i></button>
            <button
              class="sort-btn"
              :disabled="idx === banners.length - 1"
              @click="moveDown(idx)"
              title="Xuống dưới"
            ><i class="bi bi-chevron-down"></i></button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== ADD / EDIT MODAL ===== -->
    <Transition name="modal-fade">
      <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
        <div class="modal modal--wide">
          <div class="modal-head">
            <div>
              <h3>{{ editingId ? 'Chỉnh sửa banner' : 'Thêm banner mới' }}</h3>
              <p class="modal-sub">{{ editingId ? `Banner #${editingId}` : 'Tải ảnh lên và điền thông tin' }}</p>
            </div>
            <button class="icon-close-btn" @click="closeForm">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <div class="modal-body">
            <!-- Image upload -->
            <div class="upload-zone" @click="$refs.fileInput.click()" @dragover.prevent @drop.prevent="onDrop">
              <input ref="fileInput" type="file" accept="image/*" multiple class="hidden-input" @change="onFileChange" />
              <div v-if="previewUrl || form.imageUrl" class="upload-preview">
                <img :src="toDisplayUrl(previewUrl || form.imageUrl)" alt="preview" class="preview-img" />
                <div class="preview-badge">
                  <i class="bi bi-camera-fill"></i> Đổi ảnh
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <div class="upload-icon">🖼️</div>
                <p class="upload-hint"><strong>Kéo thả hoặc click</strong> để chọn 1 hoặc nhiều ảnh</p>
                <p class="upload-sub">PNG, JPG, WEBP — khuyến nghị 1920×600px</p>
              </div>
              <div v-if="uploading" class="upload-progress">
                <div class="upload-spinner"></div>
                <span>Đang tải lên...</span>
              </div>
            </div>

            <!-- Fields -->
            <div class="field">
              <label class="field-label">Tiêu đề banner <span class="req">*</span></label>
              <input v-model="form.title" class="field-input" placeholder="VD: Khuyến mãi hè 2026" maxlength="120" />
            </div>

            <div class="field">
              <label class="field-label">URL hình ảnh</label>
              <input v-model="form.imageUrl" class="field-input" placeholder="https://... (hoặc tải lên ở trên)" />
            </div>

            <div class="field">
              <label class="field-label">Link khi click (tuỳ chọn)</label>
              <input v-model="form.linkUrl" class="field-input" placeholder="https://example.com/sale" />
            </div>

            <div class="field-row">
              <div class="field">
                <label class="field-label">Thứ tự hiển thị</label>
                <input v-model.number="form.sortOrder" type="number" min="0" class="field-input" style="width:100px" />
                <p class="field-hint">Số nhỏ hơn hiển thị trước</p>
              </div>
              <div class="field">
                <label class="field-label">Trạng thái</label>
                <div class="toggle-wrap">
                  <button
                    type="button"
                    class="banner-toggle"
                    :class="form.active ? 'banner-toggle--on' : 'banner-toggle--off'"
                    @click="form.active = !form.active"
                  >
                    <span class="banner-toggle__track">
                      <span class="banner-toggle__thumb"></span>
                    </span>
                    {{ form.active ? 'Hiển thị' : 'Ẩn' }}
                  </button>
                </div>
              </div>
            </div>

            <p v-if="formError" class="form-error">{{ formError }}</p>
          </div>

          <div class="modal-foot">
            <button class="btn-ghost" @click="closeForm">Hủy</button>
            <button class="btn-primary" @click="saveBanner" :disabled="saving || uploading">
              <span v-if="saving">Đang lưu...</span>
              <span v-else>{{ editingId ? 'Cập nhật' : 'Thêm banner' }}</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Toast -->
    <Transition name="toast-slide">
      <div v-if="toast.message" class="toast" :class="toast.type">{{ toast.message }}</div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getBanners, createBanner, updateBanner,
  toggleBanner, deleteBanner, uploadImage,
} from '../../api/services/adminService'
import { API_BASE_URL } from '../../api/config'

function toDisplayUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const base = API_BASE_URL.replace(/\/api\/?$/, '') || 'http://localhost:8080'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return base + url
  const filename = url.replace(/^.*[/\\]/, '')
  if (filename && /\.(jpe?g|png|gif|webp)$/i.test(filename)) {
    return base + '/uploads/' + filename
  }
  return base + '/uploads/' + (url.replace(/^.*[/\\]/, '') || '')
}

const banners     = ref([])
const loading     = ref(true)
const isRefreshing = ref(false)
const toggling    = ref(null)
const saving      = ref(false)
const uploading   = ref(false)
const showForm    = ref(false)
const editingId   = ref(null)
const previewUrl  = ref('')
const formError   = ref('')
const fileInput   = ref(null)

const toast = ref({ message: '', type: 'ok' })
let toastTimer = null

const form = reactive({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  active: true,
})

function showToast(msg, type = 'ok') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { message: msg, type }
  toastTimer = setTimeout(() => { toast.value = { message: '', type: 'ok' } }, 2800)
}

async function load(refresh = false) {
  if (refresh) isRefreshing.value = true
  else loading.value = true
  try {
    banners.value = await getBanners()
  } catch {
    showToast('Không tải được danh sách banner', 'err')
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function openCreate() {
  editingId.value = null
  previewUrl.value = ''
  formError.value = ''
  form.title = ''
  form.imageUrl = ''
  form.linkUrl = ''
  form.sortOrder = banners.value.length
  form.active = true
  showForm.value = true
}

function openEdit(b) {
  editingId.value = b.id
  previewUrl.value = ''
  formError.value = ''
  form.title = b.title
  form.imageUrl = b.imageUrl
  form.linkUrl = b.linkUrl || ''
  form.sortOrder = b.sortOrder ?? 0
  form.active = b.active ?? true
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function onFileChange(e) {
  const files = Array.from(e.target.files || [])
  if (!files.length) return
  await handleFiles(files)
}

async function onDrop(e) {
  const files = Array.from(e.dataTransfer.files || []).filter(f => f.type.startsWith('image/'))
  if (!files.length) return
  await handleFiles(files)
}

async function handleFiles(files) {
  // Nếu đang edit thì chỉ nhận 1 ảnh
  if (editingId.value && files.length > 1) {
    showToast('Chỉ được chọn 1 ảnh khi đang chỉnh sửa', 'err')
    return
  }

  // Nếu chọn 1 ảnh thì load preview như bình thường
  if (files.length === 1) {
    await doUpload(files[0])
    return
  }

  // Upload siêu tốc nhiều ảnh cùng lúc
  uploading.value = true
  let successCount = 0
  for (let i = 0; i < files.length; i++) {
    try {
      const url = await uploadImage(files[i])
      if (url) {
        await createBanner({
          title: `Banner mới ${banners.value.length + successCount + 1}`,
          imageUrl: url,
          linkUrl: null,
          sortOrder: banners.value.length + successCount,
          active: true
        })
        successCount++
      }
    } catch (err) {
      console.error(err)
    }
  }
  uploading.value = false
  if (fileInput.value) fileInput.value.value = ''

  if (successCount > 0) {
    showToast(`Đã thêm nhanh ${successCount} banner!`)
    showForm.value = false
    await load()
  } else {
    showToast('Tải ảnh thất bại', 'err')
  }
}

async function doUpload(file) {
  uploading.value = true
  previewUrl.value = URL.createObjectURL(file)
  try {
    const url = await uploadImage(file)
    if (url) form.imageUrl = url
    else showToast('Tải ảnh thất bại', 'err')
  } catch {
    showToast('Lỗi upload ảnh', 'err')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

async function saveBanner() {
  formError.value = ''
  if (!form.title.trim()) { formError.value = 'Vui lòng nhập tiêu đề banner.'; return }
  if (!form.imageUrl.trim()) { formError.value = 'Vui lòng nhập hoặc tải lên ảnh banner.'; return }
  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      imageUrl: form.imageUrl.trim(),
      linkUrl: form.linkUrl.trim() || null,
      sortOrder: form.sortOrder,
      active: form.active,
    }
    if (editingId.value) {
      await updateBanner(editingId.value, payload)
      showToast('Đã cập nhật banner')
    } else {
      await createBanner(payload)
      showToast('Đã thêm banner mới')
    }
    showForm.value = false
    await load()
  } catch (e) {
    formError.value = e.response?.data?.message || 'Có lỗi xảy ra'
  } finally {
    saving.value = false
  }
}

async function toggleActive(b) {
  toggling.value = b.id
  try {
    const updated = await toggleBanner(b.id)
    b.active = updated.active
    showToast(updated.active ? 'Banner đã bật' : 'Banner đã ẩn')
  } catch {
    showToast('Không thể thay đổi trạng thái', 'err')
  } finally {
    toggling.value = null
  }
}

async function confirmDelete(b) {
  if (!confirm(`Xóa banner "${b.title}"? Hành động này không thể khôi phục.`)) return
  try {
    await deleteBanner(b.id)
    banners.value = banners.value.filter(x => x.id !== b.id)
    showToast('Đã xóa banner')
  } catch {
    showToast('Không thể xóa banner', 'err')
  }
}

async function moveUp(idx) {
  if (idx === 0) return
  const arr = [...banners.value]
  ;[arr[idx - 1], arr[idx]] = [arr[idx], arr[idx - 1]]
  await reorder(arr)
}

async function moveDown(idx) {
  if (idx >= banners.value.length - 1) return
  const arr = [...banners.value]
  ;[arr[idx], arr[idx + 1]] = [arr[idx + 1], arr[idx]]
  await reorder(arr)
}

async function reorder(arr) {
  banners.value = arr
  try {
    await Promise.all(arr.map((b, i) => updateBanner(b.id, { sortOrder: i })))
  } catch {
    showToast('Không thể lưu thứ tự', 'err')
    await load()
  }
}

onMounted(load)
</script>

<style scoped>
.admin-banners { max-width: 1300px; margin: 0 auto; }

/* ---- Grid ---- */
.banner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  transition: opacity 0.3s ease, filter 0.3s ease;
}
.banner-grid.is-refreshing { opacity: 0.5; filter: blur(2px); pointer-events: none; }

/* ---- Card ---- */
.banner-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(15,23,42,.06);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  display: flex;
  flex-direction: column;
}
.banner-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(15,23,42,.1);
}
.banner-card--inactive { opacity: 0.6; }

/* ---- Image area ---- */
.banner-img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16/5;
  overflow: hidden;
  background: #f1f5f9;
}
.banner-img {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.banner-card:hover .banner-img { transform: scale(1.04); }

.banner-overlay {
  position: absolute; inset: 0;
  background: rgba(15,23,42,.45);
  backdrop-filter: blur(2px);
  display: flex; align-items: center; justify-content: center; gap: 10px;
  opacity: 0;
  transition: opacity 0.2s ease;
}
.banner-card:hover .banner-overlay { opacity: 1; }

.banner-overlay-btn {
  width: 40px; height: 40px;
  border-radius: 10px;
  background: rgba(255,255,255,.18);
  border: 1.5px solid rgba(255,255,255,.35);
  color: #fff; font-size: 0.9rem;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s;
}
.banner-overlay-btn:hover { background: rgba(255,255,255,.28); transform: scale(1.08); }
.banner-overlay-btn.danger:hover { background: rgba(239,68,68,.55); border-color: #f87171; }

.banner-inactive-badge {
  position: absolute; top: 8px; left: 8px;
  padding: 3px 10px; border-radius: 999px;
  background: #1e293b; color: #fff;
  font-size: 0.7rem; font-weight: 700;
  letter-spacing: .05em;
}

/* ---- Info ---- */
.banner-info {
  padding: 12px 14px 8px;
  flex: 1;
}
.banner-info__row {
  display: flex; align-items: center; gap: 8px; margin-bottom: 4px;
}
.banner-order {
  font-size: 0.7rem; font-weight: 800;
  color: #94a3b8; letter-spacing: .05em;
  flex-shrink: 0;
}
.banner-title {
  font-size: 0.9rem; font-weight: 700; color: #0f172a;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.banner-link {
  display: flex; align-items: center; gap: 4px;
  font-size: 0.75rem; color: #6366f1;
  overflow: hidden;
}
.banner-link span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ---- Card Actions ---- */
.banner-actions {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 14px;
  border-top: 1px solid #f1f5f9;
  gap: 8px;
}
.banner-sort-btns { display: flex; gap: 4px; }
.sort-btn {
  width: 30px; height: 30px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  color: #64748b; font-size: 0.75rem;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all .15s;
}
.sort-btn:hover:not(:disabled) { background: #e0e7ff; border-color: #6366f1; color: #4f46e5; }
.sort-btn:disabled { opacity: 0.3; cursor: not-allowed; }

/* ---- Toggle Switch ---- */
.banner-toggle {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 5px 10px 5px 5px;
  border-radius: 999px;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  font-size: 0.78rem; font-weight: 600;
  color: #64748b; cursor: pointer; font-family: inherit;
  transition: all .2s cubic-bezier(.16,1,.3,1);
}
.banner-toggle--on {
  background: #ecfdf5; border-color: #a7f3d0; color: #047857;
}
.banner-toggle--off {
  background: #f8fafc; border-color: #e2e8f0; color: #94a3b8;
}
.banner-toggle__track {
  width: 30px; height: 17px;
  border-radius: 999px;
  background: #cbd5e1;
  position: relative; flex-shrink: 0;
  transition: background .2s;
}
.banner-toggle--on .banner-toggle__track { background: #10b981; }
.banner-toggle__thumb {
  position: absolute;
  top: 2.5px; left: 2.5px;
  width: 12px; height: 12px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.2);
  transition: transform .2s cubic-bezier(.16,1,.3,1);
}
.banner-toggle--on .banner-toggle__thumb { transform: translateX(13px); }

/* ---- Skeleton ---- */
.sk-banner-card {
  background: #fff; border: 1px solid #e2e8f0;
  border-radius: 16px; overflow: hidden; padding-bottom: 14px;
}
.sk-img {
  width: 100%; aspect-ratio: 16/5;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

/* ---- Modal ---- */
.icon-close-btn {
  width: 34px; height: 34px;
  border-radius: 9px; background: #f1f5f9; border: none;
  font-size: 0.95rem; color: #64748b;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: background .15s;
}
.icon-close-btn:hover { background: #e2e8f0; }

/* Upload zone */
.upload-zone {
  width: 100%;
  aspect-ratio: 16/5;
  min-height: 140px;
  border: 2px dashed #cbd5e1;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  background: #f8fafc;
  transition: border-color .2s, background .2s;
  margin-bottom: 18px;
}
.upload-zone:hover { border-color: #6366f1; background: #f5f3ff; }
.hidden-input { display: none; }

.upload-preview { width: 100%; height: 100%; position: relative; }
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.preview-badge {
  position: absolute; inset: 0;
  background: rgba(15,23,42,.45);
  backdrop-filter: blur(2px);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 0.875rem; font-weight: 700; gap: 6px;
  opacity: 0; transition: opacity .2s;
}
.upload-zone:hover .preview-badge { opacity: 1; }

.upload-placeholder {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 100%; gap: 6px; padding: 20px;
}
.upload-icon { font-size: 2rem; }
.upload-hint { margin: 0; font-size: 0.9rem; color: #334155; text-align: center; }
.upload-sub { margin: 0; font-size: 0.78rem; color: #94a3b8; text-align: center; }

.upload-progress {
  position: absolute; inset: 0;
  background: rgba(255,255,255,.8);
  display: flex; align-items: center; justify-content: center; gap: 10px;
  font-size: 0.875rem; font-weight: 600; color: #6366f1;
}
.upload-spinner {
  width: 22px; height: 22px;
  border: 3px solid #e0e7ff;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}

.field-row { display: grid; grid-template-columns: auto 1fr; gap: 20px; align-items: start; }
.toggle-wrap { padding-top: 4px; }

.form-error {
  color: #be123c; font-size: .84rem; font-weight: 600;
  margin: 8px 0 0; padding: 10px 14px;
  background: #fff1f2; border-radius: 8px; border: 1px solid #ffe4e6;
}

/* ---- Transitions ---- */
.modal-fade-enter-active { animation: modal-in .28s cubic-bezier(.16,1,.3,1); }
.modal-fade-leave-active { animation: modal-in .18s reverse ease; }
@keyframes modal-in {
  from { opacity: 0; transform: translateY(16px) scale(.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}
.toast-slide-enter-active { animation: toast-in .3s cubic-bezier(.16,1,.3,1); }
.toast-slide-leave-active { animation: toast-in .2s reverse ease; }
@keyframes toast-in {
  from { opacity: 0; transform: translateY(16px) scale(.95); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes shimmer {
  0%   { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin .7s linear infinite; }
</style>
