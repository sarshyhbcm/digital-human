<template>
  <div class="hot-search-page">
    <h2 class="page-title">热搜管理</h2>

    <el-card shadow="hover">
      <div style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
        <el-button type="primary" @click="openDialog()">新增热搜</el-button>
        <el-tag type="info" effect="plain">
          展示在游客端聊天页的欢迎屏
        </el-tag>
      </div>

      <el-table :data="list" stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="question" label="热搜问题" min-width="260" />
        <el-table-column prop="heatLabel" label="热度标签" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="heatTagType(row.heatLabel)" size="small" effect="dark">
              {{ heatLabelText(row.heatLabel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="启用状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              @change="toggleEnabled(row)"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑热搜' : '新增热搜'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="热搜问题">
          <el-input v-model="form.question" placeholder="例如：灵山大佛有多高？" />
        </el-form-item>
        <el-form-item label="热度标签">
          <el-select v-model="form.heatLabel" style="width: 100%;">
            <el-option label="🔥 爆" value="bao" />
            <el-option label="🔥 热" value="re" />
            <el-option label="🔥 火" value="huo" />
            <el-option label="🔥 温" value="wen" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
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
import { getHotSearches, createHotSearch, updateHotSearch, deleteHotSearch } from './api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  question: '',
  heatLabel: 'wen',
  sortOrder: 0,
  enabled: true
})

const HEAT_MAP = { bao: '爆', re: '热', huo: '火', wen: '温' }

function heatLabelText(key) {
  return HEAT_MAP[key] || '温'
}

function heatTagType(key) {
  if (key === 'bao') return 'danger'
  if (key === 're') return 'warning'
  if (key === 'huo') return ''
  return 'info'
}

async function loadList() {
  try {
    list.value = await getHotSearches()
  } catch (e) {
    console.error('加载热搜列表失败', e)
  }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    form.id = null
    form.question = ''
    form.heatLabel = 'wen'
    form.sortOrder = 0
    form.enabled = true
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.question.trim()) {
    ElMessage.warning('请输入热搜问题')
    return
  }
  try {
    if (isEdit.value) {
      await updateHotSearch(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createHotSearch(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function toggleEnabled(row) {
  try {
    await updateHotSearch(row.id, { ...row, enabled: !row.enabled })
    ElMessage.success(row.enabled ? '已禁用' : '已启用')
    loadList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除此热搜？', '提示')
    await deleteHotSearch(id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.hot-search-page { padding: 0; }
</style>
