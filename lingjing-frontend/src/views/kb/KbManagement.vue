<template>
  <div class="kb-page">
    <div class="header">
      <h1>知识库管理</h1>
      <p class="subtitle">管理景区知识文档，数字人将自动参考这些内容回答游客问题</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="showImportDialog = true">
        <el-icon><Upload /></el-icon>
        导入文档
      </el-button>
      <el-button @click="refreshList" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 文档列表 -->
    <el-table v-if="!loading" :data="documents" stripe style="width: 100%">
      <el-table-column prop="title" label="文档名称" min-width="200" />
      <el-table-column prop="fileType" label="类型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.fileType === 'docx' ? 'primary' : 'success'" size="small">
            {{ row.fileType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">已解析</el-tag>
          <el-tag v-else-if="row.status === 2" type="danger" size="small">解析失败</el-tag>
          <el-tag v-else type="warning" size="small">待解析</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="chunkCount" label="切片数" width="100" align="center" />
      <el-table-column prop="createdAt" label="导入时间" width="180" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-popconfirm title="确定删除此文档及其切片？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" size="small" :disabled="deleting">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-skeleton v-else :rows="4" animated />

    <el-empty v-if="!loading && documents.length === 0" description="暂无知识库文档" />

    <!-- 导入对话框 -->
    <el-dialog v-model="showImportDialog" title="导入文档" width="500px">
      <el-form label-position="top">
        <el-form-item label="文件路径">
          <el-input
            v-model="importPath"
            placeholder="输入文档的完整文件路径"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item>
          <div class="import-hint">
            <p>支持 .docx 和 .txt 文件</p>
            <p>示例：D:/资料/灵山胜境指南.docx</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getKbDocuments, importKbDocument, deleteKbDocument } from './api'
import { ElMessage } from 'element-plus'

const documents = ref([])
const loading = ref(true)
const deleting = ref(false)
const showImportDialog = ref(false)
const importPath = ref('')
const importing = ref(false)

async function refreshList() {
  loading.value = true
  try {
    documents.value = await getKbDocuments()
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleImport() {
  if (!importPath.value.trim()) {
    ElMessage.warning('请输入文件路径')
    return
  }
  importing.value = true
  try {
    await importKbDocument(importPath.value.trim())
    ElMessage.success('导入成功')
    showImportDialog.value = false
    importPath.value = ''
    await refreshList()
  } catch (e) {
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

async function handleDelete(id) {
  deleting.value = true
  try {
    await deleteKbDocument(id)
    ElMessage.success('已删除')
    await refreshList()
  } catch (e) {
    ElMessage.error('删除失败')
  } finally {
    deleting.value = false
  }
}

onMounted(refreshList)
</script>

<style scoped>
.kb-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 24px;
}

.header {
  margin-bottom: 24px;
}

.header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

.import-hint p {
  margin: 2px 0;
  color: #909399;
  font-size: 13px;
}
</style>
