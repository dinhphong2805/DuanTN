import axios from 'axios';

const API_URL = 'http://localhost:8080/api/admin';

const adminApi = axios.create({
    baseURL: API_URL
});

// --- Interceptor: Tự động lấy Token từ key 'kesn_token' ---
adminApi.interceptors.request.use((config) => {
    try {
        // Lấy token trực tiếp từ Local Storage dựa theo hình ảnh bạn cung cấp
        let token = localStorage.getItem('kesn_token');

        if (token) {
            // Xóa dấu ngoặc kép ở đầu và cuối chuỗi (nếu có do JSON.stringify sinh ra)
            token = token.replace(/^"(.*)"$/, '$1'); 
            
            // Đính kèm token vào Header
            config.headers.Authorization = `Bearer ${token}`;
            console.log("✅ Đã đính kèm Token thành công");
        } else {
            console.warn("⚠️ Không tìm thấy key 'kesn_token' trong LocalStorage");
        }
    } catch (error) {
        console.error("❌ Lỗi khi đọc Token:", error);
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

// --- Các hàm Export API ---
export const getProducts = () => adminApi.get('/products').then(res => res.data);
export const getBrandNames = () => adminApi.get('/brands/names').then(res => res.data);
export const getCategoryNames = () => adminApi.get('/categories/names').then(res => res.data);
export const createProduct = (p) => adminApi.post('/products', p).then(res => res.data);
export const updateProduct = (id, p) => adminApi.put(`/products/${id}`, p).then(res => res.data);
export const deleteProduct = (id) => adminApi.delete(`/products/${id}`).then(res => res.data);

// Hàm upload ảnh đã được tối ưu
export const uploadImage = (file) => {
    const fd = new FormData();
    fd.append('file', file); // Tên 'file' phải khớp với phía Backend yêu cầu
    
    // Axios sẽ tự động thêm Header Content-Type là multipart/form-data
    return adminApi.post('/upload', fd)
        .then(res => res.data)
        .catch(err => {
            console.error("Lỗi upload ảnh:", err.response?.data || err.message);
            throw err;
        });
};