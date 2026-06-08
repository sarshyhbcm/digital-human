<template>
  <div class="achievement-page">
    <div class="header">
      <h1>成就殿堂</h1>
      <p class="subtitle">打卡景点解锁成就徽章，收集你的灵山之旅回忆</p>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>

    <template v-else>
      <!-- 统计 -->
      <div class="stats-bar">
        <div class="stat-item">
          <span class="stat-value">{{ unlockedCount }}</span>
          <span class="stat-label">已解锁</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-value">{{ totalCount }}</span>
          <span class="stat-label">总成就</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-value">{{ Math.round((unlockedCount / totalCount) * 100) }}%</span>
          <span class="stat-label">完成度</span>
        </div>
      </div>

      <el-progress
        :percentage="Math.round((unlockedCount / totalCount) * 100)"
        :stroke-width="10"
        color="#e6a23c"
        striped
        striped-flow
        class="progress-bar"
      />

      <!-- 成就卡片 -->
      <div class="achievement-grid">
        <div
          v-for="item in achievements"
          :key="item.id"
          class="achievement-card"
          :class="{ unlocked: item.unlocked }"
        >
          <div class="card-glow" v-if="item.unlocked" />
          <div class="card-icon" :class="{ locked: !item.unlocked }">
            {{ item.unlocked ? item.icon : '🔒' }}
          </div>
          <div class="card-body">
            <div class="card-name">{{ item.name }}</div>
            <div class="card-desc">{{ item.description || item.conditionDesc }}</div>
          </div>
          <div v-if="item.unlocked" class="card-badge">
            <el-tag type="success" size="small" effect="dark" round>已达成</el-tag>
          </div>
          <div v-else class="card-badge">
            <el-tag type="info" size="small" round>未解锁</el-tag>
          </div>
        </div>
      </div>

      <div v-if="achievements.length === 0" class="empty">
        <el-empty description="暂无成就" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyAchievements } from '../profile/api'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const achievements = ref([])

const unlockedCount = computed(() =>
  achievements.value.filter(a => a.unlocked).length
)
const totalCount = computed(() => achievements.value.length)

onMounted(async () => {
  try {
    achievements.value = await getMyAchievements()

    // 恢复滚动位置
    requestAnimationFrame(() => {
      const saved = sessionStorage.getItem('scroll_AchievementHall')
      if (saved) {
        sessionStorage.removeItem('scroll_AchievementHall')
        window.scrollTo(0, parseInt(saved))
      }
    })
  } catch (e) {
    if (e.message !== 'offline') {
      ElMessage.error('加载成就失败')
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.achievement-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.header h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.loading-container {
  padding: 40px;
}

/* 统计栏 */
.stats-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #e6a23c;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e4e7ed;
}

.progress-bar {
  margin-bottom: 32px;
}

/* 成就网格 */
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.achievement-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 14px;
  background: #f5f7fa;
  border: 2px solid transparent;
  transition: all 0.3s;
  overflow: hidden;
}

.achievement-card.unlocked {
  background: linear-gradient(135deg, #fff7e6, #fffbe6);
  border-color: #faecd8;
}

.achievement-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.card-glow {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(230, 162, 60, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.card-icon {
  font-size: 32px;
  width: 48px;
  text-align: center;
  flex-shrink: 0;
}

.card-icon.locked {
  filter: grayscale(1);
  opacity: 0.5;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 3px;
}

.card-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.card-badge {
  flex-shrink: 0;
}

.empty {
  padding: 60px;
}

@media (max-width: 640px) {
  .achievement-grid {
    grid-template-columns: 1fr;
  }
}
</style>
