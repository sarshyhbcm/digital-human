<template>
  <div class="route-list">
    <div class="header">
      <h1>推荐路线</h1>
      <p class="subtitle">选择你的兴趣偏好，灵宝为你推荐最合适的游览路线</p>
      <el-button class="custom-btn" type="warning" plain @click="$router.push('/routes/custom')">
        自定义路线 →
      </el-button>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>

    <template v-else>
      <!-- 兴趣偏好选择 -->
      <div class="interest-section">
        <h2 class="section-title">
          选择兴趣偏好
          <el-select
            v-model="selectedPreference"
            placeholder="请选择你的兴趣偏好"
            size="large"
            clearable
            style="width: 240px; margin-left: 12px;"
            @change="onPreferenceChange"
          >
            <el-option
              v-for="tag in allTags"
              :key="tag"
              :label="tag"
              :value="tag"
            >
              <span>{{ tag }}</span>
            </el-option>
          </el-select>
          <el-tag v-if="selectedPreference" type="warning" effect="dark" closable @close="clearPreference" style="margin-left: 8px;">
            {{ selectedPreference }}
          </el-tag>
        </h2>
      </div>

      <!-- 已选择偏好：显示推荐排序结果 -->
      <template v-if="selectedPreference">
        <div class="section-title" style="margin-bottom: 20px;">
          为你推荐
        </div>
        <div class="route-cards">
          <div
            v-for="(item, index) in rankedRoutes"
            :key="item.route.id"
            class="route-card"
            :class="{ 'top-match': index === 0 }"
            :style="cardStyle(item.route.id, index === 0)"
            @click="goDetail(item.route.id)"
          >
            <div class="card-badge">{{ item.route.duration }}</div>
            <div v-if="index === 0" class="match-badge">最佳匹配</div>
            <h2>{{ item.route.name }}</h2>
            <p class="card-desc">{{ item.route.description }}</p>
            <div class="card-footer">
              <span class="attraction-count">
                <el-icon><Place /></el-icon>
                包含景点
              </span>
              <span v-if="item.matchedTags.length > 0" class="match-tags">
                匹配: {{ item.matchedTags.join(', ') }}
              </span>
              <el-button text type="primary" class="view-btn">查看详情 →</el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 未选择偏好：显示全部路线 -->
      <template v-else>
        <div class="route-cards">
          <div
            v-for="route in routes"
            :key="route.id"
            class="route-card"
            :style="cardStyle(route.id, false)"
            @click="goDetail(route.id)"
          >
            <div class="card-badge">{{ route.duration }}</div>
            <h2>{{ route.name }}</h2>
            <p class="card-desc">{{ route.description }}</p>
            <div class="card-footer">
              <span class="attraction-count">
                <el-icon><Place /></el-icon>
                包含景点
              </span>
              <el-button text type="primary" class="view-btn">查看详情 →</el-button>
            </div>
          </div>
        </div>
      </template>

      <div v-if="routes.length === 0" class="empty">
        <el-empty description="暂无推荐路线" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTourRoutes, getRecommendedRoutes } from './api'
import { Place } from '@element-plus/icons-vue'

const router = useRouter()
const routes = ref([])
const loading = ref(true)
const selectedPreference = ref('')
const rankedRoutes = ref([])

const allTags = computed(() => {
  const tags = new Set()
  for (const r of routes.value) {
    if (r.tags) {
      r.tags.split(/[,，]/).forEach(t => {
        const trimmed = t.trim()
        if (trimmed) tags.add(trimmed)
      })
    }
  }
  return [...tags].sort()
})

const cardGradients = [
  'linear-gradient(135deg, #f5f0e8, #ede4d6)',
  'linear-gradient(135deg, #edf3e8, #dde8d6)',
  'linear-gradient(135deg, #f5ece8, #ede0d6)',
  'linear-gradient(135deg, #e8f0ed, #d6e3de)',
  'linear-gradient(135deg, #f0e8e0, #e6d8cc)',
  'linear-gradient(135deg, #eef0f2, #dce0e4)'
]

const featuredGradient = 'linear-gradient(135deg, #f5eddc, #e8d8b8)'

function cardStyle(id, isTopMatch) {
  if (isTopMatch) {
    return { background: featuredGradient }
  }
  const idx = (id - 1) % cardGradients.length
  return { background: cardGradients[idx] }
}

function goDetail(id) {
  router.push(`/routes/${id}`)
}

async function onPreferenceChange(value) {
  if (!value) {
    clearPreference()
    return
  }
  selectedPreference.value = value
  try {
    const result = await getRecommendedRoutes(value)
    rankedRoutes.value = result
  } catch (e) {
    rankedRoutes.value = routes.value.map(r => ({
      route: r,
      matchScore: 0,
      matchedTags: []
    }))
  }
}

function clearPreference() {
  selectedPreference.value = ''
  rankedRoutes.value = []
}

onMounted(async () => {
  try {
    routes.value = await getTourRoutes()
  } catch (e) {
    // offline
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.route-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-family: var(--font-display);
  font-size: 32px;
  color: var(--ink-black);
  margin: 0 0 8px;
  letter-spacing: 6px;
}

.subtitle {
  color: var(--ink-light);
  font-size: 15px;
  margin: 0 0 12px;
}

.custom-btn {
  margin-top: 4px;
}

.loading-container {
  padding: 40px;
  max-width: 600px;
  margin: 0 auto;
}

/* 兴趣偏好 */
.interest-section {
  margin-bottom: 36px;
}

.section-title {
  font-family: var(--font-display);
  font-size: 18px;
  color: var(--ink-black);
  margin: 0;
  display: flex;
  align-items: center;
  letter-spacing: 2px;
}

.interest-section .section-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.interest-section :deep(.el-select .el-input__wrapper) {
  background: var(--paper-dark);
  box-shadow: 0 0 0 1px var(--ink-faint) inset;
}
.interest-section :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--ink-mist) inset;
}
.interest-section :deep(.el-select .el-input__inner) {
  color: var(--ink-black);
}

/* 笺纸卡片 */
.route-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.route-card {
  border-radius: 12px;
  padding: 32px 28px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex;
  flex-direction: column;
  min-height: 280px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(107, 99, 88, 0.08);
  color: var(--ink-black);
}

.route-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-float);
}

.route-card.top-match {
  min-height: 320px;
  border-color: rgba(212, 160, 23, 0.2);
}

.route-card.top-match::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(212, 160, 23, 0.04) 0%, transparent 60%);
  animation: shimmer 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes shimmer {
  0%, 100% { transform: translate(-10%, -10%); }
  50% { transform: translate(10%, 10%); }
}

.card-badge {
  display: inline-block;
  background: rgba(107, 99, 88, 0.08);
  padding: 4px 14px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--ink-light);
  font-family: var(--font-display);
  letter-spacing: 1px;
  align-self: flex-start;
  margin-bottom: 20px;
}

.match-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--gold);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 4px;
  letter-spacing: 2px;
}

.route-card h2 {
  font-family: var(--font-display);
  font-size: 20px;
  color: var(--ink-black);
  margin: 0 0 12px;
  line-height: 1.3;
  letter-spacing: 2px;
}

.card-desc {
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink-light);
  flex: 1;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(107, 99, 88, 0.1);
  flex-wrap: wrap;
  gap: 4px;
}

.attraction-count {
  font-size: 13px;
  color: var(--ink-mist);
  display: flex;
  align-items: center;
  gap: 4px;
}

.match-tags {
  font-size: 12px;
  color: var(--ink-light);
  background: rgba(107, 99, 88, 0.06);
  padding: 2px 10px;
  border-radius: 4px;
}

.view-btn {
  color: var(--vermillion) !important;
  font-size: 13px;
}

.empty {
  padding: 60px;
}

@media (max-width: 900px) {
  .route-cards {
    grid-template-columns: 1fr;
  }
}
</style>
