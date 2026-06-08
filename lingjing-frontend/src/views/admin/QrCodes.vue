<template>
  <div class="qrcodes-page">
    <h2 class="page-title">二维码管理</h2>

    <el-card shadow="hover">
      <el-table :data="attractions" stripe style="width: 100%; margin-top: 16px;">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="景点名称" min-width="160" />
        <el-table-column prop="area" label="区域" width="100" />
        <el-table-column label="二维码" width="130" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.qrCode"
              :src="`/api/qrcodes/${row.id}/image`"
              style="width: 80px; height: 80px; display: block; margin: 4px auto; cursor: pointer;"
              fit="contain"
              @error="onImgError"
              @click="showPreview(row)"
            />
            <el-tag v-else type="info" size="small">未设置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button size="small" :loading="generatingId === row.id" @click="openGenerate(row)">
              {{ row.qrCode ? '重新生成' : '生成' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 生成二维码弹窗 -->
    <el-dialog v-model="previewVisible" title="生成二维码" width="420px" align-center>
      <div class="qr-preview">
        <div class="qr-name">{{ previewAttraction?.name }}</div>
        <img v-if="previewUrl" :src="previewUrl" class="qr-image" alt="二维码" />
        <el-skeleton v-else :rows="1" animated class="qr-skeleton" />
        <div class="qr-hint">扫码可查看景点信息</div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDownload" :loading="downloading">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getQrCodes, generateQrCode } from './api'
import { ElMessage } from 'element-plus'

const attractions = ref([])
const generatingId = ref(null)

const previewVisible = ref(false)
const previewAttraction = ref(null)
const previewUrl = ref('')
const downloading = ref(false)

async function loadData() {
  const res = await getQrCodes()
  attractions.value = res
}

async function openGenerate(row) {
  generatingId.value = row.id
  try {
    await generateQrCode(row.id)
    ElMessage.success('二维码已生成')
    await loadData()
    // 显示预览弹窗
    previewAttraction.value = row
    previewUrl.value = `/api/qrcodes/${row.id}/image?t=${Date.now()}`
    previewVisible.value = true
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    generatingId.value = null
  }
}

async function handleDownload() {
  if (!previewAttraction.value) return
  downloading.value = true
  try {
    const res = await fetch(`/api/qrcodes/${previewAttraction.value.id}/image`)
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${previewAttraction.value.name}_二维码.png`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    downloading.value = false
  }
}

function onImgError(e) {
  // 图片加载失败时静默处理（显示空）
}

function showPreview(row) {
  previewAttraction.value = row
  previewUrl.value = `/api/qrcodes/${row.id}/image?t=${Date.now()}`
  previewVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.qrcodes-page { padding: 0; }

.qr-preview { text-align: center; padding: 20px 0; }
.qr-name { font-family: var(--font-display); font-size: 16px; font-weight: 600; margin-bottom: 16px; color: var(--text-ink); }
.qr-image { width: 250px; height: 250px; display: block; margin: 0 auto; border-radius: 8px; }
.qr-skeleton { width: 250px; height: 250px; margin: 0 auto; }
.qr-hint { margin-top: 12px; font-size: 13px; color: var(--text-muted); }
</style>
