import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './authStore'
import Index from './components/Index.vue'
import DangNhap from './components/DangNhap.vue'
import DangKy from './components/DangKy.vue'
import QuenMatKhau from './components/QuenMatKhau.vue'
import ResetMatKhau from './components/ResetMatKhau.vue'
import SanPham from './components/SanPham.vue'
import ChiTietSanPham from './components/ChiTietSanPham.vue'
import GioHang from './components/GioHang.vue'
import Checkout from './components/Checkout.vue'
import Profile from './components/Profile.vue'
import FAQ from './components/FAQ.vue'
import Contact from './components/Contact.vue'
import AdminLayout from './components/admin/AdminLayout.vue'
import AdminDashboard from './components/admin/AdminDashboard.vue'
import AdminProducts from './components/admin/AdminProducts.vue'
import AdminOrders from './components/admin/AdminOrders.vue'
import AdminUsers from './components/admin/AdminUsers.vue'
import AdminVouchers from './components/admin/AdminVouchers.vue'

const routes = [
  { path: '/', name: 'Home', component: Index },
  { path: '/login', name: 'Login', component: DangNhap },
  { path: '/register', name: 'Register', component: DangKy },
  { path: '/forgot-password', name: 'ForgotPassword', component: QuenMatKhau },
  { path: '/reset-password', name: 'ResetPassword', component: ResetMatKhau },
  { path: '/product', name: 'Search', component: SanPham },
  { path: '/product/:id', name: 'ProductDetail', component: ChiTietSanPham },
  { path: '/cart', name: 'Cart', component: GioHang },
  { path: '/checkout', name: 'Checkout', component: Checkout },
  { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } },
  { path: '/faq', name: 'FAQ', component: FAQ },
  { path: '/contact', name: 'Contact', component: Contact },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: AdminDashboard },
      { path: 'products', name: 'AdminProducts', component: AdminProducts },
      { path: 'orders', name: 'AdminOrders', component: AdminOrders },
      { path: 'users', name: 'AdminUsers', component: AdminUsers },
      { path: 'vouchers', name: 'AdminVouchers', component: AdminVouchers },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  const user = auth.state?.user
  const loggedIn = !!user
  const admin = user?.role === 'admin' || user?.role === 'staff'
  if (to.meta.requiresAuth && !loggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.requiresAdmin && (!loggedIn || !admin)) {
    if (!loggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next('/')
    }
    return
  }
  next()
})

export default router

