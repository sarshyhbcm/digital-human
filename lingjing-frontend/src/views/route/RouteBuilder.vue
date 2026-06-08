<template>
  <div class="route-builder">
    <div class="header">
      <div class="header-top">
        <el-button text @click="$router.back()">← 返回</el-button>
      </div>
      <h1>自定义路线</h1>
      <p class="subtitle">选择你想去的景点，灵宝帮你规划最优游览顺序</p>
    </div>

    <!-- 步骤指示器 -->
    <div class="steps">
      <div class="step" :class="{ active: step === 1, done: step > 1 }">
        <span class="step-num">1</span>
        <span class="step-label">选择景点</span>
      </div>
      <div class="step-line" :class="{ active: step > 1 }" />
      <div class="step" :class="{ active: step === 2 }">
        <span class="step-num">2</span>
        <span class="step-label">优化路线</span>
      </div>
    </div>

    <!-- 步骤1：选择景点 -->
    <template v-if="step === 1">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select v-model="areaFilter" placeholder="区域筛选" size="small" clearable style="width: 130px;">
          <el-option label="灵山胜境" value="灵山胜境" />
          <el-option label="拈花湾" value="拈花湾" />
        </el-select>
        <el-select v-model="tagFilter" placeholder="标签筛选" size="small" clearable style="width: 130px;">
          <el-option v-for="t in allTags" :key="t" :label="t" :value="t" />
        </el-select>
        <el-checkbox v-model="selectAll" :indeterminate="isIndeterminate" @change="handleSelectAll">
          全选
        </el-checkbox>
      </div>

      <!-- 景点列表 -->
      <div class="attraction-section">
        <h2 class="section-title">
          灵山胜境
          <span class="section-count">{{ lingshanSelected }}/{{ lingshanList.length }}</span>
        </h2>
        <div class="attraction-grid">
          <div
            v-for="a in lingshanList"
            :key="a.id"
            class="selectable-card"
            :class="{ selected: selectedIds.has(a.id) }"
            @click="toggleAttraction(a)"
          >
            <div class="card-check">
              <el-icon v-if="selectedIds.has(a.id)" color="#409eff"><Check /></el-icon>
              <span v-else class="uncheck">+</span>
            </div>
            <div class="card-body">
              <div class="card-name">{{ a.name }}</div>
              <div class="card-tags">
                <el-tag v-for="t in tagList(a.tags)" :key="t" size="small" effect="plain">{{ t }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="attraction-section">
        <h2 class="section-title">
          拈花湾
          <span class="section-count">{{ nianhuaSelected }}/{{ nianhuaList.length }}</span>
        </h2>
        <div class="attraction-grid">
          <div
            v-for="a in nianhuaList"
            :key="a.id"
            class="selectable-card"
            :class="{ selected: selectedIds.has(a.id) }"
            @click="toggleAttraction(a)"
          >
            <div class="card-check">
              <el-icon v-if="selectedIds.has(a.id)" color="#409eff"><Check /></el-icon>
              <span v-else class="uncheck">+</span>
            </div>
            <div class="card-body">
              <div class="card-name">{{ a.name }}</div>
              <div class="card-tags">
                <el-tag v-for="t in tagList(a.tags)" :key="t" size="small" effect="plain">{{ t }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredAttractions.length === 0" class="empty">
        <el-empty description="无匹配景点" />
      </div>

      <!-- 底部操作栏 -->
      <div class="bottom-bar">
        <span class="selected-count">已选择 {{ selectedIds.size }} / {{ attractions.length }} 个景点</span>
        <el-button
          type="primary"
          size="large"
          :disabled="selectedIds.size < 2"
          :loading="optimizing"
          @click="doOptimize"
        >
          <el-icon><Opportunity /></el-icon>
          生成最优路线
        </el-button>
      </div>
    </template>

    <!-- 步骤2：优化结果 -->
    <template v-if="step === 2 && result">
      <div class="result-header">
        <el-button text @click="reset">← 重新选择</el-button>
      </div>

      <!-- 统计卡片 -->
      <el-card class="stats-card" shadow="never">
        <div class="stats">
          <div class="stat-item">
            <span class="stat-value">{{ result.attractions.length }}</span>
            <span class="stat-label">景点数</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">{{ (result.totalDistanceMeters / 1000).toFixed(1) }}</span>
            <span class="stat-label">总路程(km)</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">{{ formatMinutes(result.estimatedMinutes) }}</span>
            <span class="stat-label">预计用时</span>
          </div>
        </div>
        <div class="stats-actions">
          <el-button type="primary" @click="showSaveDialog = true">
            <el-icon><Check /></el-icon>
            保存路线
          </el-button>
        </div>
      </el-card>

      <!-- 路线时间线 -->
      <div class="timeline">
        <!-- 起点 -->
        <div class="timeline-node start">
          <div class="node-dot start-dot">
            <el-icon><Flag /></el-icon>
          </div>
          <div class="node-content">
            <div class="node-name">景区入口</div>
            <div class="node-meta">从这里出发</div>
          </div>
        </div>

        <!-- 景点 -->
        <div
          v-for="(a, index) in result.attractions"
          :key="a.id"
          class="timeline-node"
          :class="{ last: index === result.attractions.length - 1 }"
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
            <div v-if="a.description" class="node-desc">{{ a.description }}</div>
            <!-- 到下一站的距离 -->
            <div v-if="index < result.segments.length" class="node-distance">
              <el-icon><Right /></el-icon>
              步行 {{ result.segments[index].distanceMeters }} 米至下一站
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 保存路线对话框 -->
    <el-dialog v-model="showSaveDialog" title="保存路线" width="400px">
      <el-form @submit.prevent="doSave">
        <el-form-item label="路线名称">
          <el-input v-model="routeName" placeholder="给这条路线起个名字" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSaveDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAttractions } from '../attraction/api'
import { optimizeCustomRoute, saveUserRoute, updateUserRoute } from './api'
import { Check, Opportunity, Flag, Right } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const attractions = ref([])
const selectedIds = ref(new Set())
const areaFilter = ref('')
const tagFilter = ref('')
const selectAll = ref(false)
const step = ref(1)
const optimizing = ref(false)
const result = ref(null)
const showSaveDialog = ref(false)
const routeName = ref('')
const saving = ref(false)
const editingRouteId = ref(null)

const allTags = computed(() => {
  const tags = new Set()
  for (const a of attractions.value) {
    if (a.tags) {
      a.tags.split(/[,，]/).forEach(t => {
        const trimmed = t.trim()
        if (trimmed) tags.add(trimmed)
      })
    }
  }
  return [...tags].sort()
})

const filteredAttractions = computed(() => {
  let list = attractions.value
  if (areaFilter.value) {
    list = list.filter(a => a.area === areaFilter.value)
  }
  if (tagFilter.value) {
    list = list.filter(a => a.tags && a.tags.includes(tagFilter.value))
  }
  return list
})

const lingshanList = computed(() =>
  filteredAttractions.value.filter(a => a.area === '灵山胜境')
)
const nianhuaList = computed(() =>
  filteredAttractions.value.filter(a => a.area !== '灵山胜境')
)
const lingshanSelected = computed(() =>
  lingshanList.value.filter(a => selectedIds.value.has(a.id)).length
)
const nianhuaSelected = computed(() =>
  nianhuaList.value.filter(a => selectedIds.value.has(a.id)).length
)

const isIndeterminate = computed(() => {
  const filtered = filteredAttractions.value
  const selected = filtered.filter(a => selectedIds.value.has(a.id)).length
  return selected > 0 && selected < filtered.length
})

function tagList(tags) {
  if (!tags) return []
  return tags.split(/[,，]/).map(t => t.trim()).filter(Boolean)
}

function toggleAttraction(a) {
  const s = new Set(selectedIds.value)
  if (s.has(a.id)) {
    s.delete(a.id)
  } else {
    s.add(a.id)
  }
  selectedIds.value = s
  updateSelectAll()
}

function handleSelectAll(val) {
  const s = new Set(selectedIds.value)
  for (const a of filteredAttractions.value) {
    if (val) {
      s.add(a.id)
    } else {
      s.delete(a.id)
    }
  }
  selectedIds.value = s
  updateSelectAll()
}

function updateSelectAll() {
  const filtered = filteredAttractions.value
  const selected = filtered.filter(a => selectedIds.value.has(a.id)).length
  if (selected === 0) selectAll.value = false
  else if (selected === filtered.length) selectAll.value = true
  // else indeterminate — handled by isIndeterminate
}

async function doOptimize() {
  if (selectedIds.value.size < 2) {
    ElMessage.warning('请至少选择2个景点')
    return
  }
  optimizing.value = true
  try {
    result.value = await optimizeCustomRoute([...selectedIds.value])
    step.value = 2
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (e) {
    ElMessage.error('路线优化失败')
  } finally {
    optimizing.value = false
  }
}

function formatMinutes(min) {
  const h = Math.floor(min / 60)
  const m = min % 60
  if (h > 0) return `${h}小时${m > 0 ? m + '分钟' : ''}`
  return `${m}分钟`
}

function reset() {
  step.value = 1
  result.value = null
  selectedIds.value = new Set()
  selectAll.value = false
}

async function doSave() {
  if (!routeName.value.trim()) {
    ElMessage.warning('请输入路线名称')
    return
  }
  saving.value = true
  const data = {
    name: routeName.value.trim(),
    attractionIds: result.value.attractions.map(a => a.id),
    totalDistanceMeters: result.value.totalDistanceMeters,
    estimatedMinutes: result.value.estimatedMinutes
  }
  try {
    if (editingRouteId.value) {
      await updateUserRoute(editingRouteId.value, data)
      ElMessage.success('路线更新成功')
    } else {
      await saveUserRoute(data)
      ElMessage.success('路线保存成功')
    }
    showSaveDialog.value = false
    routeName.value = ''
    router.push('/my-routes')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getAttractions({ page: 1, size: 100 })
    attractions.value = (res.records || []).sort((a, b) => a.sortOrder - b.sortOrder)

    // 编辑模式：从我的路线跳转过来，预选已有景点
    if (route.query.edit) {
      if (route.query.routeId) {
        editingRouteId.value = Number(route.query.routeId)
      }
      if (route.query.ids) {
        const ids = route.query.ids.split(',').map(Number)
        selectedIds.value = new Set(ids)
      }
      if (route.query.name) {
        routeName.value = decodeURIComponent(route.query.name)
      }
    }
  } catch (e) {
    ElMessage.error('加载景点失败')
  }
})
</script>

<style scoped>
.route-builder {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.header-top {
  text-align: left;
  margin-bottom: 8px;
}

.header h1 {
  font-family: var(--font-display);
  font-size: 28px;
  color: var(--ink-black);
  margin: 0 0 8px;
  letter-spacing: 6px;
}

.subtitle {
  color: var(--ink-light);
  font-size: 14px;
  margin: 0;
}

/* 步骤指示器 — 水墨/朱砂 */
.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 32px;
}

.step {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--ink-faint);
  color: var(--ink-mist);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  transition: all 0.3s;
}

.step.active .step-num {
  background: var(--vermillion);
  color: #fff;
}

.step.done .step-num {
  background: var(--jade);
  color: #fff;
}

.step-label {
  font-size: 14px;
  color: var(--ink-mist);
}

.step.active .step-label {
  color: var(--vermillion);
  font-weight: 600;
}

.step-line {
  width: 60px;
  height: 2px;
  background: var(--ink-faint);
  margin: 0 12px;
  transition: background 0.3s;
}

.step-line.active {
  background: var(--jade);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-bar :deep(.el-select .el-input__wrapper) {
  background: var(--paper-dark);
  box-shadow: 0 0 0 1px var(--ink-faint) inset;
}
.filter-bar :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--ink-mist) inset;
}
.filter-bar :deep(.el-select .el-input__inner) {
  color: var(--ink-black);
}

/* 景点区域 */
.attraction-section {
  margin-bottom: 28px;
}

.section-title {
  font-family: var(--font-display);
  font-size: 17px;
  color: var(--ink-black);
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 2px;
}

.section-count {
  font-size: 13px;
  font-weight: 400;
  color: var(--ink-mist);
}

.attraction-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
}

.selectable-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--paper-dark);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.selectable-card:hover {
  border-color: var(--ink-mist);
  background: var(--paper-warm);
}

.selectable-card.selected {
  border-color: var(--vermillion);
  background: rgba(194, 58, 43, 0.04);
}

.card-check {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid var(--ink-mist);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-mist);
  margin-top: 2px;
  transition: all 0.2s;
}

.selected .card-check {
  border-color: var(--vermillion);
  background: var(--vermillion);
}

.selected .card-check .el-icon {
  color: #fff !important;
}

.uncheck {
  line-height: 1;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-black);
  margin-bottom: 4px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 底部栏 */
.bottom-bar {
  position: sticky;
  bottom: 0;
  background: rgba(237, 228, 214, 0.95);
  backdrop-filter: blur(8px);
  padding: 16px 24px;
  border-radius: 12px 12px 0 0;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}

.selected-count {
  font-size: 14px;
  color: var(--ink-light);
}

/* 结果页 */
.result-header {
  margin-bottom: 16px;
}

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
  color: var(--vermillion);
}

.stat-label {
  font-size: 13px;
  color: var(--ink-mist);
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: var(--ink-faint);
}

.stats-actions {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--ink-faint);
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
  background: var(--ink-faint);
}

.timeline-node {
  position: relative;
  margin-bottom: 24px;
}

.node-dot {
  position: absolute;
  left: -40px;
  top: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--paper-dark);
  border: 2px solid var(--ink-mist);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-light);
  z-index: 1;
}

.node-dot.start-dot {
  background: var(--jade);
  border-color: var(--jade);
  color: #fff;
  font-size: 18px;
}

.node-content {
  background: var(--paper-dark);
  border-radius: 8px;
  padding: 16px 20px;
  margin-left: 16px;
  transition: transform 0.2s;
}

.node-content:hover {
  transform: translateX(4px);
}

.node-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-black);
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
  color: var(--ink-mist);
}

.node-desc {
  font-size: 13px;
  color: var(--ink-light);
  line-height: 1.6;
  margin-bottom: 8px;
}

.node-distance {
  font-size: 12px;
  color: var(--vermillion);
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty {
  padding: 40px;
}

@media (max-width: 640px) {
  .attraction-grid {
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
</style>
