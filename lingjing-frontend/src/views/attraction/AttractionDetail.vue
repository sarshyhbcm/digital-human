<template>
  <div class="attraction-detail">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 景点详情 -->
    <template v-else-if="detail">
      <!-- 标题区域 -->
      <div class="detail-header">
        <div class="header-info">
          <div class="header-top">
            <h1>{{ detail.name }}</h1>
            <div class="checkin-buttons" v-if="isLoggedIn">
              <template v-if="checkedIn">
                <el-button type="success" disabled>
                  <el-icon><Select /></el-icon>
                  已打卡
                </el-button>
              </template>
              <template v-else>
                <el-button
                  type="primary"
                  :loading="checkingIn"
                  @click="handleCheckIn"
                >
                  <el-icon><Opportunity /></el-icon>
                  GPS打卡
                </el-button>
                <el-button
                  type="warning"
                  plain
                  @click="handleQrCheckIn"
                >
                  <el-icon><Camera /></el-icon>
                  扫码打卡
                </el-button>
              </template>
            </div>
          </div>
          <div class="header-meta">
            <el-tag :type="detail.area === '灵山胜境' ? 'danger' : 'success'">
              {{ detail.area }}
            </el-tag>
            <span class="meta-item" v-if="detail.duration">
              <el-icon><Clock /></el-icon> 建议游览：{{ detail.duration }}
            </span>
            <span class="meta-item" v-if="detail.openingHours">
              <el-icon><Timer /></el-icon> {{ detail.openingHours }}
            </span>
          </div>
          <p class="brief">{{ detail.description }}</p>
        </div>
      </div>

      <!-- 封面图 -->
      <div v-if="detail.coverImage" class="cover-section">
        <el-image
          :src="detail.coverImage"
          fit="contain"
          class="detail-cover"
          :preview-src-list="allImages"
          preview-teleported
        />
      </div>

      <!-- 景点图集 -->
      <div v-if="galleryImages.length > 0" class="gallery-section">
        <h3 class="section-subtitle">景点图集</h3>
        <div class="gallery-grid">
          <el-image
            v-for="(img, i) in galleryImages"
            :key="i"
            :src="img"
            fit="cover"
            class="gallery-item"
            :preview-src-list="allImages"
            :initial-index="i"
            preview-teleported
          />
        </div>
      </div>

      <!-- 信息卡片 -->
      <div class="info-section">
        <!-- 详细介绍 -->
        <el-card v-if="detail.detail" class="info-card">
          <template #header>
            <span class="card-title">详细介绍</span>
          </template>
          <div class="rich-text">{{ detail.detail }}</div>
        </el-card>

        <!-- 基本信息 -->
        <el-card class="info-card">
          <template #header>
            <span class="card-title">基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="所属区域">{{ detail.area }}</el-descriptions-item>
            <el-descriptions-item label="建议游览时长">{{ detail.duration || '—' }}</el-descriptions-item>
            <el-descriptions-item label="开放时间">{{ detail.openingHours || '—' }}</el-descriptions-item>
            <el-descriptions-item label="标签">{{ detail.tags || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 位置信息 -->
        <el-card v-if="detail.latitude && detail.longitude" class="info-card">
          <template #header>
            <span class="card-title">位置信息</span>
          </template>
          <p>纬度：{{ detail.latitude }}</p>
          <p>经度：{{ detail.longitude }}</p>
        </el-card>
      </div>
    </template>

    <!-- 未找到 -->
    <el-empty v-else description="景点不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAttractionDetail } from './api'
import { checkIn, getCheckInStatus } from './checkin'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, Timer, Select, Opportunity, Camera } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const detail = ref(null)
const loading = ref(true)
const checkedIn = ref(false)
const checkingIn = ref(false)

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// 图片画廊
const galleryImages = computed(() => {
  if (!detail.value?.images) return []
  try {
    const parsed = JSON.parse(detail.value.images)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const allImages = computed(() => {
  const list = []
  if (detail.value?.coverImage) list.push(detail.value.coverImage)
  list.push(...galleryImages.value)
  return list.length > 0 ? list : undefined
})

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id
    detail.value = await getAttractionDetail(id)

    if (isLoggedIn.value) {
      try {
        const status = await getCheckInStatus(id)
        checkedIn.value = status.checkedIn
      } catch (e) {
        // not logged in
      }
    }
  } catch (e) {
    console.error('加载景点详情失败:', e)
  } finally {
    loading.value = false
  }
}

async function handleCheckIn() {
  checkingIn.value = true
  try {
    await checkIn({ attractionId: detail.value.id })
    checkedIn.value = true
    ElMessage.success('打卡成功！')
  } catch (e) {
    if (e.message && e.message.includes('已打卡')) {
      checkedIn.value = true
    } else {
      ElMessage.error('打卡失败，请重试')
    }
  } finally {
    checkingIn.value = false
  }
}

function handleQrCheckIn() {
  ElMessage.info(`扫码打卡：请扫描景点「${detail.value.name}」处的二维码完成打卡`)
}

function goBack() {
  router.back()
}

onMounted(fetchDetail)
</script>

<style scoped>
.attraction-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.back-bar {
  margin-bottom: 20px;
}

.detail-header {
  margin-bottom: 30px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}

.checkin-buttons {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.header-top h1 {
  font-size: 28px;
  color: #303133;
  margin: 0;
}

.header-info h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 12px 0;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 14px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.brief {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
}

.rich-text {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.loading-container {
  padding: 40px;
}

/* 封面图 */
.cover-section {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  background: #f8f8f8;
}

.detail-cover {
  width: 100%;
  display: block;
}

/* 景点图集 */
.gallery-section {
  margin-bottom: 24px;
}

.section-subtitle {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-left: 12px;
  border-left: 3px solid var(--el-color-primary, #409eff);
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.gallery-item {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.gallery-item:hover {
  transform: scale(1.03);
}
</style>
