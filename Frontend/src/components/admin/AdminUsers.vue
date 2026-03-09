<template>
  <div class="admin-users">
    <h1>Người dùng</h1>

    <section v-if="loading" class="loading">Đang tải...</section>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Họ tên</th>
            <th>SĐT</th>
            <th>Vai trò</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.fullName || '—' }}</td>
            <td>{{ u.phone || '—' }}</td>
            <td><span class="role-badge" :class="u.role">{{ u.role }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="!loading && users.length === 0" class="empty">Chưa có người dùng.</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers } from '../../api/services/adminService'

const users = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    users.value = await getUsers()
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-users h1 {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 20px;
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

.role-badge {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.role-badge.admin {
  background: #dbeafe;
  color: #1e40af;
}

.role-badge.staff {
  background: #e0e7ff;
  color: #3730a3;
}

.role-badge.customer {
  background: #d1fae5;
  color: #065f46;
}
</style>
