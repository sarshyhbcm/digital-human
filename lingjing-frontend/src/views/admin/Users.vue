<template>
  <div class="users-page">
    <h2 class="page-title">用户管理</h2>

    <el-card shadow="hover">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称/手机号" clearable style="width: 300px;" @clear="search" @keyup.enter="search" />
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <el-table :data="users" stripe style="width: 100%; margin-top: 16px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'">{{ row.role === 'admin' ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-select v-model="row.role" size="small" style="width: 100px; margin-right: 8px;" @change="(val) => updateRole(row.id, val)">
              <el-option label="用户" value="user" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="loadUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, updateUserRole } from './api'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const users = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function loadUsers() {
  try {
    const res = await getUsers({ page: page.value, pageSize: pageSize.value, keyword: keyword.value })
    users.value = res.records
    total.value = res.total
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
}

function search() {
  page.value = 1
  loadUsers()
}

async function updateRole(id, role) {
  try {
    await updateUserRole(id, { role })
    ElMessage.success('角色更新成功')
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.users-page { padding: 0; }
</style>
