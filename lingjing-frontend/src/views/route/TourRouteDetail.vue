<template>
  <div class="route-detail">
    <div class="back-bar">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回路线列表
      </el-button>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="routeData">
      <div class="route-header" :style="headerStyle">
        <div class="header-overlay">
          <h1>{{ routeData.name }}</h1>
          <div class="route-meta">
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ routeData.duration }}
            </span>
            <span class="meta-item">
              <el-icon><Place /></el-icon>
              {{ attractions.length }} 个景点
            </span>
          </div>
          <p class="route-desc">{{ routeData.description }}</p>
          <div class="header-actions">
            <el-button
              type="warning"
              :icon="Plus"
              :loading="saving"
              @click="handleSaveToMyRoutes"
            >
              加入我的路线
            </el-button>
          </div>
        </div>
      </div>

      <div class="attraction-list">
        <h2>游览路线</h2>
        <div class="timeline">
          <div
            v-for="(item, index) in attractions"
            :key="item.id"
            class="timeline-item"
            @click="goAttraction(item.id)"
          >
            <div class="timeline-dot">
              <span class="step-num">{{ index + 1 }}</span>
            </div>
            <div class="timeline-line" v-if="index < attractions.length - 1"></div>
            <div class="timeline-content">
              <div class="attraction-card">
                <div class="card-info">
                  <h3>{{ item.name }}</h3>
                  <p class="attraction-area">{{ item.area }}</p>
                  <p v-if="item.description" class="attraction-desc">{{ item.description }}</p>
                  <div v-if="item.tags" class="tags">
                    <el-tag
                      v-for="tag in parseTags(item.tags)"
                      :key="tag"
                      size="small"
                      class="tag"
                    >{{ tag }}</el-tag>
                  </div>
                </div>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-else-if="!loading" class="empty">
      <el-empty description="路线不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTourRouteDetail, saveTourRouteToMyRoutes } from './api'
import { ArrowLeft, Clock, Place, ArrowRight, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const vueRoute = useRoute()
const router = useRouter()

const routeData = ref(null)
const attractions = ref([])
const loading = ref(true)
const saving = ref(false)

const routeColors = {
  1: { bg: 'linear-gradient(135deg, #f5f0e8, #ede4d6)' },
  2: { bg: 'linear-gradient(135deg, #edf3e8, #dde8d6)' },
  3: { bg: 'linear-gradient(135deg, #f5ece8, #ede0d6)' },
  4: { bg: 'linear-gradient(135deg, #e8f0ed, #d6e3de)' },
  5: { bg: 'linear-gradient(135deg, #f0e8e0, #e6d8cc)' },
  6: { bg: 'linear-gradient(135deg, #eef0f2, #dce0e4)' },
  7: { bg: 'linear-gradient(135deg, #f0ece6, #e4dccf)' },
  8: { bg: 'linear-gradient(135deg, #eae5de, #ddd5c5)' }
}

const headerStyle = {
  background: routeColors[1].bg
}

function parseTags(tagsStr) {
  if (!tagsStr) return []
  return tagsStr.split(/[,，、]/).map(t => t.trim()).filter(Boolean)
}

function goBack() {
  router.back()
}

function goAttraction(id) {
  router.push(`/attractions/${id}`)
}

function handleSaveToMyRoutes() {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再保存路线')
    router.push('/login')
    return
  }
  saving.value = true
  saveTourRouteToMyRoutes(routeData.value.id)
    .then(() => {
      ElMessage.success('路线已保存到"我的路线"')
    })
    .catch(() => {
      ElMessage.error('保存失败')
    })
    .finally(() => {
      saving.value = false
    })
}

onMounted(async () => {
  try {
    const result = await getTourRouteDetail(vueRoute.params.id)
    routeData.value = result.route
    attractions.value = result.attractions || []
    // 根据路线ID动态设置头部颜色
    if (result.route && routeColors[result.route.id]) {
      headerStyle.background = routeColors[result.route.id].bg
    }
    // 恢复滚动位置
    requestAnimationFrame(() => {
      const key = `scroll_TourRouteDetail_${vueRoute.params.id}`
      const savedPos = sessionStorage.getItem(key)
      if (savedPos) {
        sessionStorage.removeItem(key)
        window.scrollTo(0, parseInt(savedPos))
      }
    })
  } catch (e) {
    // error
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.route-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.back-bar {
  padding: 16px 0;
}

.back-bar :deep(.el-button) {
  color: var(--ink-light);
}
.back-bar :deep(.el-button:hover) {
  color: var(--ink-black);
}

.loading-container {
  padding: 40px;
}

/* 头部 */
.route-header {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 32px;
  min-height: 200px;
  border: 1px solid var(--ink-faint);
}

.header-overlay {
  padding: 48px 40px;
}

.route-header h1 {
  font-family: var(--font-display);
  font-size: 28px;
  color: var(--ink-black);
  margin: 0 0 12px;
  letter-spacing: 4px;
}

.route-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--ink-light);
}

.route-desc {
  font-size: 15px;
  line-height: 1.8;
  color: var(--ink-light);
  margin: 0 0 20px;
  max-width: 600px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 景点时间线 */
.attraction-list h2 {
  font-family: var(--font-display);
  font-size: 20px;
  color: var(--ink-black);
  margin: 0 0 24px;
  letter-spacing: 3px;
}

.timeline {
  position: relative;
  padding-left: 40px;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;
  cursor: pointer;
}

.timeline-dot {
  position: absolute;
  left: -40px;
  top: 0;
  width: 32px;
  height: 32px;
  background: var(--vermillion);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.step-num {
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.timeline-line {
  position: absolute;
  left: -25px;
  top: 32px;
  width: 2px;
  height: calc(100% - 24px);
  background: var(--ink-faint);
}

.timeline-content {
  padding-left: 20px;
}

.attraction-card {
  background: var(--paper-dark);
  border: 1px solid var(--ink-faint);
  border-radius: 8px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: box-shadow 0.2s, transform 0.2s;
}

.attraction-card:hover {
  box-shadow: var(--shadow-warm);
  transform: translateX(4px);
}

.card-info h3 {
  font-family: var(--font-display);
  font-size: 16px;
  color: var(--ink-black);
  margin: 0 0 4px;
  letter-spacing: 1px;
}

.attraction-area {
  font-size: 13px;
  color: var(--ink-mist);
  margin: 0 0 6px;
}

.attraction-desc {
  font-size: 14px;
  color: var(--ink-light);
  line-height: 1.6;
  margin: 0 0 8px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag {
  margin-right: 4px;
}

.arrow {
  font-size: 18px;
  color: var(--ink-mist);
  flex-shrink: 0;
}

.empty {
  padding: 60px;
}
</style>
