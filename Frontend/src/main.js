import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 1. Bootstrap & Icons
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

// 2. Custom Pro Max CSS
import './style.css'
import './assets/css/css_admin/admin_style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.mount('#app')
