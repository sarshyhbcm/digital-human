<template>
  <div class="banners-page">
    <h2 class="page-title">轮播图管理</h2>

    <el-card shadow="hover">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增轮播图
        </el-button>
      </div>

      <el-table :data="list" stripe style="width: 100%;" v-loading="loading">
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              fit="cover"
              style="width: 100px; height: 56px; border-radius: 4px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="toggleStatus(row)"
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

      <el-empty v-if="!loading && list.length === 0" description="暂无轮播图，点击上方新增" />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="480px"
    >
      <el-form :model="form" label-width="80px" label-position="top">
        <el-form-item label="图片">
          <div class="banner-upload-area">
            <div v-if="form.imageUrl" class="banner-preview">
              <el-image :src="form.imageUrl" fit="cover" />
              <div class="banner-overlay">
                <el-button size="small" circle @click="triggerUpload">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </div>
            </div>
            <div v-else class="banner-placeholder" @click="triggerUpload">
              <el-icon :size="32"><Plus /></el-icon>
              <span>点击上传图片</span>
              <span class="hint">建议尺寸 1200×600</span>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display:none"
              @change="handleFileChange"
            />
          </div>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="用于 alt 文本" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="可选" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用">
              <el-switch v-model="form.statusBool" active-text="启用" inactive-text="禁用" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBanners, createBanner, updateBanner, deleteBanner, uploadBannerImage } from './api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const fileInput = ref(null)

const defaultForm = {
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  statusBool: true
}
const form = reactive({ ...defaultForm })

async function fetchData() {
  loading.value = true
  try {
    list.value = await getBanners()
  } catch (e) {
    ElMessage.error('加载轮播图失败')
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    Object.assign(form, { ...row, statusBool: row.status === 1 })
  } else {
    isEdit.value = false
    Object.assign(form, { ...defaultForm })
  }
  dialogVisible.value = true
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const url = await uploadBannerImage(formData)
    form.imageUrl = url
    ElMessage.success('图片上传成功')
  } catch (err) {
    ElMessage.error('图片上传失败')
  }
  fileInput.value.value = ''
}

async function handleSave() {
  saving.value = true
  try {
    if (!form.imageUrl) {
      ElMessage.warning('请上传图片')
      saving.value = false
      return
    }
    const payload = {
      title: form.title,
      imageUrl: form.imageUrl,
      linkUrl: form.linkUrl,
      sortOrder: form.sortOrder,
      status: form.statusBool ? 1 : 0
    }
    if (isEdit.value) {
      await updateBanner(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createBanner(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除此轮播图？', '提示')
    await deleteBanner(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function toggleStatus(row) {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateBanner(row.id, { status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.banners-page { padding: 0; }

.banner-upload-area {
  display: flex;
}

.banner-preview {
  position: relative;
  width: 100%;
  max-width: 360px;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-subtle, #e4e7ed);
}

.banner-preview .el-image {
  width: 100%;
  height: 100%;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.banner-preview:hover .banner-overlay {
  opacity: 1;
}

.banner-placeholder {
  width: 360px;
  height: 180px;
  border: 2px dashed var(--border-subtle, #d9d9d9);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;
}

.banner-placeholder:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.hint {
  font-size: 12px;
  color: #bbb;
}
</style>
