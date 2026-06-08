<template>
  <div class="checkin-page">
    <div class="header">
      <h1>景点打卡</h1>
      <p class="subtitle">游览时打开定位，到景点附近即可打卡收集徽章</p>
    </div>

    <!-- 打卡进度 -->
    <el-card class="progress-card" v-if="!loading">
      <div class="progress-info">
        <span class="progress-label">打卡进度</span>
        <span class="progress-value">{{ checkInCount }} / {{ totalAttractions }}</span>
      </div>
      <el-progress
        :percentage="Math.round((checkInCount / totalAttractions) * 100)"
        :stroke-width="12"
        :color="progressColor"
        striped
        striped-flow
      />
      <div class="progress-actions">
        <el-button type="warning" plain @click="showShare = true" :disabled="checkInCount === 0">
          <el-icon><PictureFilled /></el-icon>
          生成分享图
        </el-button>
      </div>
      <div class="progress-areas">
        <div class="area-stat">
          <span class="area-dot lingshan" />
          <span>灵山胜境: {{ lingshanCount }}/{{ lingshanTotal }}</span>
        </div>
        <div class="area-stat">
          <span class="area-dot nianhua" />
          <span>拈花湾: {{ nianhuaCount }}/{{ nianhuaTotal }}</span>
        </div>
      </div>
    </el-card>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- 两个区域分别展示 -->
    <template v-else>
      <!-- 视图切换 -->
      <div class="view-toggle">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="map">🗺️ 地图视图</el-radio-button>
          <el-radio-button value="list">📋 列表视图</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 地图视图 -->
      <div v-show="viewMode === 'map'" class="map-section">
        <div v-if="!keyExists" class="map-placeholder">
          <el-empty description="地图未配置">
            <p style="color: #909399; font-size: 13px; margin-bottom: 12px;">
              请在 .env 中设置 VITE_AMAP_KEY
            </p>
            <el-button size="small" @click="viewMode = 'list'">切换到列表视图</el-button>
          </el-empty>
        </div>
        <AttractionMap
          v-else
          :attractions="attractions"
          @check-in="gpsCheckIn"
        />
      </div>

      <!-- 列表视图 -->
      <template v-if="viewMode === 'list'">
        <div class="area-section">
        <h2 class="area-title">
          <span class="area-indicator lingshan" />
          灵山胜境
        </h2>
        <div class="attraction-grid">
          <div
            v-for="item in lingshanAttractions"
            :key="item.id"
            class="attraction-card"
            :class="{ checked: item.checked }"
            @click="goAttraction(item.id)"
          >
            <div class="card-icon">
              <span v-if="item.checked" class="check-badge">✓</span>
              <span v-else class="uncheck-badge">☆</span>
            </div>
            <div class="card-body">
              <div class="card-name">{{ item.name }}</div>
              <div class="card-tags">{{ item.tags || '景点' }}</div>
              <el-button
                v-if="!item.checked"
                size="small"
                type="primary"
                plain
                :loading="gpsLoading === item.id"
                @click.stop="gpsCheckIn(item)"
                style="margin-top: 6px; margin-right: 6px;"
              >
                <el-icon style="margin-right: 4px;"><Opportunity /></el-icon>
                GPS打卡
              </el-button>
              <el-button
                v-if="!item.checked"
                size="small"
                type="warning"
                plain
                @click.stop="qrCheckIn(item)"
                style="margin-top: 6px;"
              >
                <el-icon style="margin-right: 4px;"><Camera /></el-icon>
                扫码打卡
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="area-section">
        <h2 class="area-title">
          <span class="area-indicator nianhua" />
          拈花湾
        </h2>
        <div class="attraction-grid">
          <div
            v-for="item in nianhuaAttractions"
            :key="item.id"
            class="attraction-card"
            :class="{ checked: item.checked }"
            @click="goAttraction(item.id)"
          >
            <div class="card-icon">
              <span v-if="item.checked" class="check-badge">✓</span>
              <span v-else class="uncheck-badge">☆</span>
            </div>
            <div class="card-body">
              <div class="card-name">{{ item.name }}</div>
              <div class="card-tags">{{ item.tags || '景点' }}</div>
              <el-button
                v-if="!item.checked"
                size="small"
                type="primary"
                plain
                :loading="gpsLoading === item.id"
                @click.stop="gpsCheckIn(item)"
                style="margin-top: 6px; margin-right: 6px;"
              >
                <el-icon style="margin-right: 4px;"><Opportunity /></el-icon>
                GPS打卡
              </el-button>
              <el-button
                v-if="!item.checked"
                size="small"
                type="warning"
                plain
                @click.stop="qrCheckIn(item)"
                style="margin-top: 6px;"
              >
                <el-icon style="margin-right: 4px;"><Camera /></el-icon>
                扫码打卡
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </template>
    </template>

    <div v-if="!loading && totalAttractions === 0" class="empty">
      <el-empty description="暂无景点数据" />
    </div>

    <!-- 分享图弹窗 -->
    <ShareImage
      v-model="showShare"
      :check-in-count="checkInCount"
      :total-count="totalAttractions"
      :lingshan-count="lingshanCount"
      :lingshan-total="lingshanTotal"
      :nianhua-count="nianhuaCount"
      :nianhua-total="nianhuaTotal"
      :checked-attractions="checkedAttractions"
    />

    <!-- 演示专用：模拟GPS打卡（评委展示用，可一键打卡） -->
    <el-card v-if="totalAttractions > 0" class="demo-card" shadow="none">
      <div class="demo-header" @click="showDemo = !showDemo">
        <span class="demo-title">🎯 演示辅助 — 模拟GPS打卡</span>
        <el-icon :class="{ rotated: showDemo }"><ArrowDown /></el-icon>
      </div>
      <template v-if="showDemo">
        <p class="demo-hint">点击按钮直接打卡（跳过GPS定位），配合 Chrome DevTools Sensors 可模拟真实定位流程</p>
        <div class="demo-list">
          <div
            v-for="item in unCheckedAttractions"
            :key="item.id"
            class="demo-item"
          >
            <span class="demo-name">{{ item.name }}</span>
            <el-tag size="small" type="info">{{ item.area }}</el-tag>
            <el-button
              size="small"
              type="success"
              plain
              :loading="demoLoading === item.id"
              @click="demoCheckIn(item)"
            >模拟打卡</el-button>
          </div>
        </div>
        <div v-if="unCheckedAttractions.length === 0" class="demo-done">
          🎉 全部景点已打卡完成！
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getAttractions } from '../attraction/api'
import { checkIn, getMyCheckIns, getCheckInCount } from '../attraction/checkin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { PictureFilled, Opportunity, ArrowDown, Camera } from '@element-plus/icons-vue'
import ShareImage from '../../components/ShareImage.vue'
import AttractionMap from '../../components/AttractionMap.vue'

const router = useRouter()
const loading = ref(true)
const viewMode = ref('map')
const attractions = ref([])
const checkedIds = ref(new Set())
const checkInCount = ref(0)
const totalAttractions = ref(0)
const showShare = ref(false)
const gpsLoading = ref(null)
const showDemo = ref(false)
const demoLoading = ref(null)

const keyExists = import.meta.env.VITE_AMAP_KEY

const unCheckedAttractions = computed(() =>
  attractions.value.filter(a => !a.checked)
)

const lingshanAttractions = computed(() =>
  attractions.value.filter(a => a.area === '灵山胜境')
)
const nianhuaAttractions = computed(() =>
  attractions.value.filter(a => a.area !== '灵山胜境')
)

const lingshanCount = computed(() =>
  lingshanAttractions.value.filter(a => a.checked).length
)
const lingshanTotal = computed(() => lingshanAttractions.value.length)
const nianhuaCount = computed(() =>
  nianhuaAttractions.value.filter(a => a.checked).length
)
const nianhuaTotal = computed(() => nianhuaAttractions.value.length)

const checkedAttractions = computed(() =>
  attractions.value.filter(a => a.checked).map(a => ({ name: a.name, area: a.area }))
)

function progressColor(percentage) {
  if (percentage < 30) return '#e6a23c'
  if (percentage < 70) return '#409eff'
  return '#67c23a'
}

async function gpsCheckIn(attraction) {
  if (gpsLoading.value) return

  try {
    await ElMessageBox.confirm(
      `使用GPS定位打卡「${attraction.name}」？\n请确保已允许浏览器获取位置信息。`,
      'GPS打卡',
      { confirmButtonText: '开始定位', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }

  gpsLoading.value = attraction.id
  try {
    const pos = await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: 10000
      })
    })
    await checkIn({
      attractionId: attraction.id,
      checkInType: 'gps',
      latitude: pos.coords.latitude,
      longitude: pos.coords.longitude
    })
    ElMessage.success(`✅ ${attraction.name} 打卡成功！`)
    // 刷新页面状态
    attraction.checked = true
    checkInCount.value++
    checkedIds.value.add(attraction.id)
  } catch (e) {
    if (e.code === 1) {
      ElMessage.error('定位权限被拒绝，请在浏览器设置中允许定位')
    } else if (e.code === 2) {
      ElMessage.error('定位失败，请检查GPS信号或使用二维码打卡')
    } else if (e.code === 3) {
      ElMessage.error('定位超时，请到开阔地带重试')
    } else {
      ElMessage.error(e.message || '定位失败')
    }
  } finally {
    gpsLoading.value = null
  }
}

async function demoCheckIn(attraction) {
  demoLoading.value = attraction.id
  try {
    await checkIn({
      attractionId: attraction.id,
      checkInType: 'gps',
      latitude: 31.425,
      longitude: 120.095
    })
    ElMessage.success(`✅ ${attraction.name} 打卡成功！`)
    attraction.checked = true
    checkInCount.value++
    checkedIds.value.add(attraction.id)
  } catch (e) {
    ElMessage.error(e.message || '打卡失败')
  } finally {
    demoLoading.value = null
  }
}

function qrCheckIn(attraction) {
  ElMessage.info(`扫码打卡：请扫描景点「${attraction.name}」处的二维码完成打卡`)
}

function goAttraction(id) {
  router.push(`/attractions/${id}`)
}

onMounted(async () => {
  try {
    // 获取所有景点
    const result = await getAttractions({ page: 1, size: 100 })
    const records = result.records || []
    attractions.value = records.sort((a, b) => a.sortOrder - b.sortOrder)

    // 获取已打卡列表
    const checkIns = await getMyCheckIns()
    for (const ci of checkIns) {
      checkedIds.value.add(ci.attractionId)
    }

    // 标记打卡状态
    for (const a of attractions.value) {
      a.checked = checkedIds.value.has(a.id)
    }

    // 获取计数
    const countResult = await getCheckInCount()
    checkInCount.value = countResult.count || 0
    totalAttractions.value = attractions.value.length

    // 恢复滚动位置
    requestAnimationFrame(() => {
      const saved = sessionStorage.getItem('scroll_CheckInMap')
      if (saved) {
        sessionStorage.removeItem('scroll_CheckInMap')
        window.scrollTo(0, parseInt(saved))
      }
    })
  } catch (e) {
    if (e.message !== 'offline') {
      ElMessage.error('加载失败')
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.checkin-page {
  max-width: 960px;
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

/* 视图切换 */
.view-toggle {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.map-section {
  margin-left: -60px;
  margin-right: -60px;
  margin-bottom: 28px;
}

.map-section :deep(.map-container) {
  border-radius: 0;
}

.map-placeholder {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
}

/* 进度卡片 */
.progress-card {
  margin-bottom: 32px;
  border-radius: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.progress-value {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}

.progress-areas {
  display: flex;
  gap: 24px;
  margin-top: 10px;
}

.progress-actions {
  margin-top: 14px;
  display: flex;
  justify-content: center;
}

.area-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.area-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.area-dot.lingshan {
  background: #409eff;
}

.area-dot.nianhua {
  background: #67c23a;
}

/* 区域标题 */
.area-section {
  margin-bottom: 32px;
}

.area-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.area-indicator {
  width: 4px;
  height: 18px;
  border-radius: 2px;
}

.area-indicator.lingshan {
  background: #409eff;
}

.area-indicator.nianhua {
  background: #67c23a;
}

/* 景点网格 */
.attraction-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.attraction-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.25s;
  border: 2px solid transparent;
}

.attraction-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #e4e7ed;
}

.attraction-card.checked {
  background: linear-gradient(135deg, #ecf5ff, #f0f9eb);
  border-color: #a0cfff;
}

.card-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.checked .card-icon {
  background: #67c23a;
  color: white;
}

.card-icon .uncheck-badge {
  font-size: 22px;
  color: #c0c4cc;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-tags {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.empty {
  padding: 60px;
}

.demo-card {
  margin-top: 24px;
  border: 1px dashed #e6a23c;
  border-radius: 10px;
  background: #fdf6ec;
}
.demo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}
.demo-title {
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
}
.demo-header .el-icon {
  transition: transform 0.25s;
}
.demo-header .rotated {
  transform: rotate(180deg);
}
.demo-hint {
  font-size: 12px;
  color: #909399;
  margin: 8px 0 12px;
}
.demo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.demo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 8px;
  border-radius: 6px;
  background: #fff;
}
.demo-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}
.demo-done {
  text-align: center;
  padding: 16px;
  font-size: 14px;
  color: #67c23a;
  font-weight: 600;
}

@media (max-width: 640px) {
  .attraction-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
