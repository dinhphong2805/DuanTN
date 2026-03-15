// import axios from 'axios';

// const API_URL = 'http://localhost:8080/api/admin';

// const adminApi = axios.create({
//     baseURL: API_URL
// });

// // Interceptor: Tự động lấy Token từ Object 'auth'
// adminApi.interceptors.request.use((config) => {
//     try {
//         const authData = localStorage.getItem('auth');
//         if (authData) {
//             // Chuyển chuỗi JSON thành Object
//             const parsedAuth = JSON.parse(authData);
//             // Lấy token theo đúng cấu trúc Pinia: state -> user -> token
//             const token = parsedAuth.state?.user?.token;
git
//             if (token) {
//                 config.headers.Authorization = `Bearer ${token}`;
//                 console.log("✅ Đã đính kèm Token thành công");
//             } else {
//                 console.warn("⚠️ Tìm thấy 'auth' nhưng không thấy 'token' bên trong");
//             }
//         } else {
//             console.error("❌ Không tìm thấy key 'auth' trong LocalStorage");
//         }
//     } catch (error) {
//         console.error("❌ Lỗi khi đọc Token từ LocalStorage:", error);
//     }
//     return config;
// }, (error) => {
//     return Promise.reject(error);
// });

// // --- Các hàm Export ---
// export const getProducts = () => adminApi.get('/products').then(res => res.data);
// export const getBrandNames = () => adminApi.get('/brands/names').then(res => res.data);
// export const getCategoryNames = () => adminApi.get('/categories/names').then(res => res.data);
// export const createProduct = (p) => adminApi.post('/products', p).then(res => res.data);
// export const updateProduct = (id, p) => adminApi.put(`/products/${id}`, p).then(res => res.data);
// export const deleteProduct = (id) => adminApi.delete(`/products/${id}`).then(res => res.data);
// export const uploadImage = (file) => {
//     const fd = new FormData();
//     fd.append('file', file);
//     return adminApi.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } }).then(res => res.data);
// };