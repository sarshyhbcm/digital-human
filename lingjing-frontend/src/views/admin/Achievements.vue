<template>
  <div class="achievements-page">
    <h2 class="page-title">成就配置</h2>

    <el-card shadow="hover">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="openDialog()">新增成就</el-button>
      </div>

      <el-table :data="achievements" stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="成就名称" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="conditionDesc" label="条件描述" width="150" />
        <el-table-column prop="conditionType" label="条件类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.conditionType === 'checkin_count' ? '打卡数' : '全部景点' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conditionValue" label="条件值" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑成就' : '新增成就'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="成就名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="图标URL" />
        </el-form-item>
        <el-form-item label="条件描述">
          <el-input v-model="form.conditionDesc" />
        </el-form-item>
        <el-form-item label="条件类型">
          <el-select v-model="form.conditionType">
            <el-option label="打卡数" value="checkin_count" />
            <el-option label="全部景点" value="all_attractions" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件值">
          <el-input-number v-model="form.conditionValue" :min="1" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getAchievements, createAchievement, updateAchievement, deleteAchievement } from './api'
import { ElMessage, ElMessageBox } from 'element-plus'

const achievements = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  description: '',
  icon: '',
  conditionDesc: '',
  conditionType: 'checkin_count',
  conditionValue: 1,
  sortOrder: 0
})

async function loadAchievements() {
  try {
    achievements.value = await getAchievements()
  } catch (e) {
    console.error('加载成就列表失败', e)
  }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.description = ''
    form.icon = ''
    form.conditionDesc = ''
    form.conditionType = 'checkin_count'
    form.conditionValue = 1
    form.sortOrder = 0
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (isEdit.value) {
      await updateAchievement(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createAchievement(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadAchievements()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除此成就？', '提示')
    await deleteAchievement(id)
    ElMessage.success('删除成功')
    loadAchievements()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadAchievements)
</script>

<style scoped>
.achievements-page { padding: 0; }
</style>
