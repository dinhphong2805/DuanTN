/**
 * Cấu hình API - đổi baseURL khi chạy Spring Boot
 * VD: http://localhost:8080/api
 */
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
