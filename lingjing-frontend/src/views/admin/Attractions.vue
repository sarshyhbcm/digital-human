<template>
  <div class="attractions-page">
    <h2 class="page-title">景点管理</h2>

    <el-card shadow="hover">
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增景点
        </el-button>
        <el-radio-group v-model="areaFilter" @change="handleSearch">
          <el-radio-button value="">全部区域</el-radio-button>
          <el-radio-button value="灵山胜境">灵山胜境</el-radio-button>
          <el-radio-button value="拈花湾">拈花湾</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="keyword"
          placeholder="搜索景点名称..."
          clearable
          class="search-input"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <el-table :data="list" stripe style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              style="width: 48px; height: 36px; border-radius: 4px;"
              fit="cover"
            />
            <span v-else style="color: #aaa; font-size: 12px;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="area" label="区域" width="100">
          <template #default="{ row }">
            <el-tag :type="row.area === '灵山胜境' ? 'danger' : 'success'" size="small">
              {{ row.area }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑景点' : '新增景点'"
      width="720px"
      class="attraction-dialog"
    >
      <el-form :model="form" label-width="100px" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="景点名称">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属区域">
              <el-select v-model="form.area" style="width: 100%;">
                <el-option label="灵山胜境" value="灵山胜境" />
                <el-option label="拈花湾" value="拈花湾" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="简要介绍">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="详细介绍">
          <el-input v-model="form.detail" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="开放时间">
              <el-input v-model="form.openingHours" placeholder="如 08:00-17:00" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="游览时长">
              <el-input v-model="form.duration" placeholder="如 1-2小时" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标签">
              <el-input v-model="form.tags" placeholder="逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.statusBool" active-text="显示" inactive-text="隐藏" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 封面图 -->
        <el-divider content-position="left">封面图</el-divider>
        <div class="upload-area">
          <div class="cover-preview" v-if="form.coverImage">
            <el-image :src="form.coverImage" fit="cover" />
            <div class="cover-overlay">
              <el-button size="small" circle @click="triggerCoverUpload">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" circle type="danger" @click="form.coverImage = ''">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div v-else class="upload-placeholder" @click="triggerCoverUpload">
            <el-icon :size="28"><Plus /></el-icon>
            <span>点击上传封面</span>
          </div>
          <input
            ref="coverInput"
            type="file"
            accept="image/*"
            style="display:none"
            @change="handleCoverChange"
          />
        </div>

        <!-- 多张图片 -->
        <el-divider content-position="left">景点图集</el-divider>
        <div class="image-grid">
          <div v-for="(img, i) in imageList" :key="i" class="image-item">
            <el-image :src="img" fit="cover" />
            <div class="image-item-overlay">
              <el-button size="small" circle type="danger" @click="removeImage(i)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="image-add" @click="triggerImagesUpload">
            <el-icon :size="24"><Plus /></el-icon>
          </div>
          <input
            ref="imagesInput"
            type="file"
            accept="image/*"
            multiple
            style="display:none"
            @change="handleImagesChange"
          />
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import {
  getAdminAttractions, createAttraction, updateAttraction, deleteAttraction,
  uploadAttractionCover, uploadAttractionImages, removeAttractionImage
} from './api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const areaFilter = ref('')
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const coverInput = ref(null)
const imagesInput = ref(null)

const defaultForm = {
  id: null,
  name: '',
  description: '',
  detail: '',
  area: '灵山胜境',
  openingHours: '',
  duration: '',
  tags: '',
  sortOrder: 0,
  statusBool: true,
  coverImage: ''
}

const form = reactive({ ...defaultForm })
const imageList = ref([])

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (areaFilter.value) params.area = areaFilter.value
    if (keyword.value) params.keyword = keyword.value
    const result = await getAdminAttractions(params)
    list.value = result.records
    total.value = result.total
  } catch (e) {
    ElMessage.error('加载景点列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    Object.assign(form, {
      ...row,
      statusBool: row.status === 1
    })
    imageList.value = parseImages(row.images)
  } else {
    isEdit.value = false
    Object.assign(form, { ...defaultForm })
    imageList.value = []
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description,
      detail: form.detail,
      area: form.area,
      openingHours: form.openingHours,
      duration: form.duration,
      tags: form.tags,
      sortOrder: form.sortOrder,
      status: form.statusBool ? 1 : 0,
      coverImage: form.coverImage,
      images: JSON.stringify(imageList.value)
    }
    if (isEdit.value) {
      await updateAttraction(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createAttraction(payload)
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
    await ElMessageBox.confirm('确定删除此景点？', '提示')
    await deleteAttraction(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function toggleStatus(row) {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateAttraction(row.id, { status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已显示' : '已隐藏')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 封面上传
function triggerCoverUpload() {
  coverInput.value?.click()
}

async function handleCoverChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!form.id) {
    ElMessage.warning('请先保存景点基本信息再上传封面')
    return
  }
  const formData = new FormData()
  formData.append('file', file)
  try {
    const url = await uploadAttractionCover(form.id, formData)
    form.coverImage = url
    ElMessage.success('封面上传成功')
  } catch (err) {
    ElMessage.error('封面上传失败')
  }
  coverInput.value.value = ''
}

// 多图上传
function triggerImagesUpload() {
  imagesInput.value?.click()
}

async function handleImagesChange(e) {
  const files = e.target.files
  if (!files || files.length === 0) return
  if (!form.id) {
    ElMessage.warning('请先保存景点基本信息再上传图片')
    return
  }
  const formData = new FormData()
  for (const f of files) {
    formData.append('files', f)
  }
  try {
    const imagesJson = await uploadAttractionImages(form.id, formData)
    imageList.value = parseImages(imagesJson)
    ElMessage.success(`上传 ${files.length} 张图片成功`)
  } catch (err) {
    ElMessage.error('图片上传失败')
  }
  imagesInput.value.value = ''
}

async function removeImage(index) {
  const url = imageList.value[index]
  if (!form.id) {
    imageList.value.splice(index, 1)
    return
  }
  try {
    const updated = await removeAttractionImage(form.id, { url })
    imageList.value = parseImages(updated.images)
    ElMessage.success('图片已删除')
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

function parseImages(json) {
  if (!json) return []
  try {
    const parsed = JSON.parse(json)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

onMounted(fetchData)
</script>

<style scoped>
.attractions-page { padding: 0; }

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 220px;
}

.pagination-wrap {
  margin-top: 20px;
  text-align: center;
}

/* 封面上传 */
.upload-area {
  display: flex;
  gap: 16px;
}

.cover-preview {
  position: relative;
  width: 200px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-subtle, #e4e7ed);
}

.cover-preview .el-image {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.cover-preview:hover .cover-overlay {
  opacity: 1;
}

.upload-placeholder {
  width: 200px;
  height: 150px;
  border: 2px dashed var(--border-subtle, #d9d9d9);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-placeholder:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

/* 多图网格 */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--border-subtle, #e4e7ed);
}

.image-item .el-image {
  width: 100%;
  height: 100%;
}

.image-item-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-item:hover .image-item-overlay {
  opacity: 1;
}

.image-add {
  width: 100px;
  height: 100px;
  border: 2px dashed var(--border-subtle, #d9d9d9);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;
}

.image-add:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
</style>
