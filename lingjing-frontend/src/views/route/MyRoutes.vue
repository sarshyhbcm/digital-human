<template>
  <div class="my-routes">
    <div class="header">
      <h1>{{ viewingRoute ? viewingRoute.name : '我的路线' }}</h1>
      <p class="subtitle">{{ viewingRoute ? '保存的路线详情' : '查看你保存的自定义游览路线' }}</p>
      <el-button v-if="viewingRoute" text @click="viewingRoute = null">← 返回列表</el-button>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 列表视图 -->
    <template v-else-if="!viewingRoute">
      <div v-if="routes.length === 0" class="empty">
        <el-empty description="还没有保存的路线">
          <el-button type="primary" @click="$router.push('/routes/custom')">去创建路线</el-button>
        </el-empty>
      </div>

      <div v-else class="route-list">
        <div
          v-for="route in routes"
          :key="route.id"
          class="route-card"
          @click="viewRoute(route.id)"
        >
          <div class="card-top">
            <h3>{{ route.name }}</h3>
            <el-tag size="small" effect="plain" type="info">{{ route.attractionCount }}个景点</el-tag>
          </div>
          <div class="card-stats">
            <div class="stat">
              <span class="stat-icon"><el-icon><Opportunity /></el-icon></span>
              <span>{{ (route.totalDistanceMeters / 1000).toFixed(1) }} km</span>
            </div>
            <div class="stat">
              <span class="stat-icon"><el-icon><Clock /></el-icon></span>
              <span>{{ formatMinutes(route.estimatedMinutes) }}</span>
            </div>
            <div class="stat date">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(route.createdAt) }}</span>
            </div>
          </div>
          <div class="card-actions" @click.stop>
            <el-button size="small" type="primary" text @click="viewRoute(route.id)">查看路线</el-button>
            <el-popconfirm title="确定删除这条路线？" @confirm="deleteRoute(route.id)">
              <template #reference>
                <el-button size="small" type="danger" text>删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </template>

    <!-- 详情视图 -->
    <template v-else>
      <el-card class="stats-card" shadow="never">
        <div class="stats">
          <div class="stat-item">
            <span class="stat-value">{{ routeDetail.attractionCount }}</span>
            <span class="stat-label">景点数</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">{{ (routeDetail.totalDistanceMeters / 1000).toFixed(1) }}</span>
            <span class="stat-label">总路程(km)</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">{{ formatMinutes(routeDetail.estimatedMinutes) }}</span>
            <span class="stat-label">预计用时</span>
          </div>
        </div>
        <div class="stats-actions">
          <el-button type="primary" plain @click="editRoute">
            <el-icon><Edit /></el-icon>
            编辑路线
          </el-button>
          <el-button type="warning" plain :loading="replanning" @click="replanRoute">
            <el-icon><Refresh /></el-icon>
            重新规划
          </el-button>
        </div>
      </el-card>

      <!-- 重新规划路线结果 -->
      <template v-if="replannedRoute">
        <div class="replanned-section">
          <div class="replanned-header">
            <h2>重新规划路线</h2>
            <p class="replanned-subtitle">基于未打卡景点重新规划</p>
          </div>
          <el-card class="stats-card" shadow="never">
            <div class="stats">
              <div class="stat-item">
                <span class="stat-value">{{ replannedRoute.attractions.length }}</span>
                <span class="stat-label">景点数</span>
              </div>
              <div class="stat-divider" />
              <div class="stat-item">
                <span class="stat-value">{{ (replannedRoute.totalDistanceMeters / 1000).toFixed(1) }}</span>
                <span class="stat-label">总路程(km)</span>
              </div>
              <div class="stat-divider" />
              <div class="stat-item">
                <span class="stat-value">{{ formatMinutes(replannedRoute.estimatedMinutes) }}</span>
                <span class="stat-label">预计用时</span>
              </div>
            </div>
          </el-card>
          <div class="timeline">
            <div class="timeline-node start">
              <div class="node-dot start-dot"><el-icon><Flag /></el-icon></div>
              <div class="node-content">
                <div class="node-name">景区入口</div>
                <div class="node-meta">从这里出发</div>
              </div>
            </div>
            <div
              v-for="(a, index) in replannedRoute.attractions"
              :key="'replan-' + a.id"
              class="timeline-node"
            >
              <div class="node-dot">
                <span>{{ index + 1 }}</span>
              </div>
              <div class="node-content">
                <div class="node-name">{{ a.name }}</div>
                <div class="node-meta">
                  <el-tag size="small" effect="plain" type="info">{{ a.area }}</el-tag>
                  <span v-if="a.duration" class="visit-time">建议停留 {{ a.duration }}</span>
                </div>
                <div v-if="index < replannedRoute.segments.length" class="node-distance">
                  <el-icon><Right /></el-icon>
                  步行 {{ replannedRoute.segments[index].distanceMeters }} 米至下一站
                </div>
              </div>
            </div>
          </div>
          <div class="replanned-actions">
            <el-button type="primary" @click="showReplanSaveDialog = true">
              <el-icon><Check /></el-icon>
              保存新路线
            </el-button>
            <el-button text @click="replannedRoute = null">收起</el-button>
          </div>
        </div>
      </template>

      <!-- 保存新路线对话框 -->
      <el-dialog v-model="showReplanSaveDialog" title="保存新路线" width="400px">
        <el-form @submit.prevent="saveReplannedRoute">
          <el-form-item label="路线名称">
            <el-input v-model="replannedName" placeholder="给路线起个名字" maxlength="50" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showReplanSaveDialog = false">取消</el-button>
          <el-button type="primary" :loading="savingReplanned" @click="saveReplannedRoute">保存</el-button>
        </template>
      </el-dialog>

      <div class="timeline">
        <div class="timeline-node start">
          <div class="node-dot start-dot"><el-icon><Flag /></el-icon></div>
          <div class="node-content">
            <div class="node-name">景区入口</div>
            <div class="node-meta">从这里出发</div>
          </div>
        </div>
        <div
          v-for="(a, index) in routeDetail.attractions"
          :key="a.id"
          class="timeline-node"
          :class="{ checked: checkedIds.has(a.id) }"
        >
          <div class="node-dot" :class="{ 'checked-dot': checkedIds.has(a.id) }">
            <span v-if="!checkedIds.has(a.id)">{{ index + 1 }}</span>
            <el-icon v-else><Select /></el-icon>
          </div>
          <div class="node-content" :class="{ 'checked-content': checkedIds.has(a.id) }">
            <div class="node-name">
              {{ a.name }}
              <el-tag v-if="checkedIds.has(a.id)" size="small" type="success" effect="dark" style="margin-left: 8px;">已打卡</el-tag>
            </div>
            <div class="node-meta">
              <el-tag size="small" effect="plain" type="info">{{ a.area }}</el-tag>
              <span v-if="a.duration" class="visit-time">建议停留 {{ a.duration }}</span>
            </div>
            <div v-if="a.description" class="node-desc">{{ a.description }}</div>
            <div class="node-actions">
              <el-button
                v-if="!checkedIds.has(a.id)"
                size="small"
                type="primary"
                plain
                :loading="gpsLoading === a.id"
                @click.stop="gpsCheckIn(a)"
              >
                <el-icon><Opportunity /></el-icon>
                GPS打卡
              </el-button>
              <el-button
                v-if="!checkedIds.has(a.id)"
                size="small"
                type="warning"
                plain
                @click.stop="qrCheckIn(a)"
              >
                <el-icon><Camera /></el-icon>
                扫码打卡
              </el-button>
            </div>
            <div v-if="index < routeDetail.segments.length" class="node-distance">
              <el-icon><Right /></el-icon>
              步行 {{ routeDetail.segments[index].distanceMeters }} 米至下一站
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserRoutes, getUserRouteDetail, deleteUserRoute, optimizeCustomRoute, saveUserRoute } from './api'
import { checkIn, getMyCheckIns } from '../attraction/checkin'
import { Clock, Calendar, Flag, Right, Opportunity, Select, Camera, Edit, Refresh, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const routes = ref([])
const loading = ref(true)
const viewingRoute = ref(null)
const routeDetail = ref(null)
const detailLoading = ref(false)
const checkedIds = ref(new Set())
const gpsLoading = ref(null)
const replannedRoute = ref(null)
const replanning = ref(false)
const savingReplanned = ref(false)
const showReplanSaveDialog = ref(false)
const replannedName = ref('')

function formatMinutes(min) {
  const h = Math.floor(min / 60)
  const m = min % 60
  if (h > 0) return `${h}小时${m > 0 ? m + '分钟' : ''}`
  return `${m}分钟`
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

async function viewRoute(id) {
  detailLoading.value = true
  try {
    const detail = await getUserRouteDetail(id)
    routeDetail.value = detail
    viewingRoute.value = { id, name: detail.name }
    // 获取已打卡状态
    const checkIns = await getMyCheckIns()
    checkedIds.value = new Set(checkIns.map(c => c.attractionId))
  } catch (e) {
    ElMessage.error('加载路线详情失败')
  } finally {
    detailLoading.value = false
  }
}

function editRoute() {
  const routeId = routeDetail.value.id
  const ids = routeDetail.value.attractions.map(a => a.id).join(',')
  const name = encodeURIComponent(routeDetail.value.name)
  router.push(`/routes/custom?edit=1&routeId=${routeId}&ids=${ids}&name=${name}`)
}

async function replanRoute() {
  const unchecked = routeDetail.value.attractions.filter(a => !checkedIds.value.has(a.id))
  if (unchecked.length < 2) {
    ElMessage.warning('未打卡景点不足2个，无法重新规划')
    return
  }
  try {
    await ElMessageBox.confirm(
      `当前路线有 ${unchecked.length} 个景点未打卡，是否根据未打卡景点重新规划路线？`,
      '重新规划路线',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  replanning.value = true
  try {
    const result = await optimizeCustomRoute(unchecked.map(a => a.id))
    replannedRoute.value = result
    replannedName.value = routeDetail.value.name + '（优化版）'
  } catch (e) {
    ElMessage.error('重新规划失败')
  } finally {
    replanning.value = false
  }
}

async function saveReplannedRoute() {
  if (!replannedRoute.value) return
  if (!replannedName.value.trim()) {
    ElMessage.warning('请输入路线名称')
    return
  }
  savingReplanned.value = true
  try {
    await saveUserRoute({
      name: replannedName.value.trim(),
      attractionIds: replannedRoute.value.attractions.map(a => a.id),
      totalDistanceMeters: replannedRoute.value.totalDistanceMeters,
      estimatedMinutes: replannedRoute.value.estimatedMinutes
    })
    ElMessage.success('新路线已保存')
    showReplanSaveDialog.value = false
    replannedRoute.value = null
    replannedName.value = ''
    viewingRoute.value = null
    routeDetail.value = null
    routes.value = await getUserRoutes()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingReplanned.value = false
  }
}

async function gpsCheckIn(attraction) {
  if (gpsLoading.value) return
  try {
    await ElMessageBox.confirm(
      `使用GPS定位打卡「${attraction.name}」？`,
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
    checkedIds.value = new Set([...checkedIds.value, attraction.id])
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

function qrCheckIn(attraction) {
  ElMessage.info(`扫码打卡：请扫描景点「${attraction.name}」处的二维码完成打卡`)
}

async function deleteRoute(id) {
  try {
    await deleteUserRoute(id)
    routes.value = routes.value.filter(r => r.id !== id)
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  try {
    routes.value = await getUserRoutes()
  } catch (e) {
    // offline
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.my-routes {
  max-width: 1000px;
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
  margin: 0 0 12px;
}

.loading-container {
  padding: 40px;
  max-width: 600px;
  margin: 0 auto;
}

.route-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.route-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s;
}

.route-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #409eff;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.card-top h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.card-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.stat-icon .el-icon {
  color: #409eff;
}

.stat.date {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.empty {
  padding: 60px;
}

/* 统计卡片 (detail) */
.stats-card {
  margin-bottom: 28px;
  border-radius: 12px;
}

.stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: #e4e7ed;
}

.stats-actions {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

/* 时间线 */
.timeline {
  position: relative;
  padding-left: 40px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 20px;
  top: 32px;
  bottom: 20px;
  width: 2px;
  background: #e4e7ed;
}

.timeline-node {
  position: relative;
  margin-bottom: 24px;
}

.timeline-node.checked .node-name {
  color: #67c23a;
}

.node-dot {
  position: absolute;
  left: -40px;
  top: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #ecf5ff;
  border: 2px solid #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #409eff;
  z-index: 1;
}

.node-dot.start-dot {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
  font-size: 18px;
}

.node-dot.checked-dot {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.node-content {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 16px 20px;
  margin-left: 16px;
  transition: transform 0.2s, background 0.3s, box-shadow 0.3s;
}

.node-content:hover {
  transform: translateX(4px);
}

.node-content.checked-content {
  background: linear-gradient(135deg, #f0f9eb, #ecf5ff);
  box-shadow: 0 0 0 1px #b7eb8f;
}

.node-actions {
  display: flex;
  gap: 8px;
  margin: 8px 0 4px;
}

.node-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.node-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.visit-time {
  font-size: 12px;
  color: #909399;
}

.node-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.node-distance {
  font-size: 12px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 640px) {
  .route-list {
    grid-template-columns: 1fr;
  }
  .stats {
    flex-direction: column;
    gap: 12px;
  }
  .stat-divider {
    width: 80%;
    height: 1px;
  }
}

/* 重新规划 */
.replanned-section {
  margin-top: 8px;
}

.replanned-header {
  text-align: center;
  margin-bottom: 20px;
}

.replanned-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 4px;
}

.replanned-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.replanned-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}
</style>
