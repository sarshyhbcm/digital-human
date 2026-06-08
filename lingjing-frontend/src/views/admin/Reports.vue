<template>
  <div class="reports-page">
    <h2 class="page-title">游客感受度报告</h2>

    <!-- 概览卡片 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon positive"><el-icon :size="24"><SuccessFilled /></el-icon></div>
          <div class="stat-value">{{ positivePercent }}%</div>
          <div class="stat-label">正面评价占比</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon neutral"><el-icon :size="24"><InfoFilled /></el-icon></div>
          <div class="stat-value">{{ neutralPercent }}%</div>
          <div class="stat-label">中性评价占比</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon negative"><el-icon :size="24"><WarningFilled /></el-icon></div>
          <div class="stat-value">{{ negativePercent }}%</div>
          <div class="stat-label">负面评价占比</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>情感分布</template>
          <v-chart :option="sentimentOption" style="height: 320px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>关注点分布</template>
          <v-chart :option="intentOption" style="height: 320px;" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="12">
        <el-card shadow="never" class="hot-list-card">
          <template #header>
            <div class="card-header">
              <span class="hot-title">游客高频对话 · TOP 10</span>
              <el-tag type="info" size="small" class="hot-subtitle">高频问题</el-tag>
            </div>
          </template>
          <div v-if="topQuestions.length === 0" class="empty-chart">暂无数据</div>
          <div v-else class="hot-list">
            <div v-for="(q, i) in topQuestions" :key="i" class="hot-item" :class="{ 'top-three': i < 3 }">
              <span class="hot-rank" :class="'rank-' + (i + 1)">{{ rankLabel(i) }}</span>
              <span class="hot-text">{{ q.question }}</span>
              <span class="hot-heat" :class="'heat-' + (q.heat || 'wen')">{{ heatLabel(q.heat) }}</span>
              <span class="hot-count">{{ q.count }}</span>
              <div class="hot-bar-bg">
                <div class="hot-bar-fill" :style="{ width: heatPercent(q.count) + '%' }" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>服务建议</template>
          <div v-if="suggestions.length === 0" class="empty-chart">暂无足够数据生成建议</div>
          <div v-else>
            <div v-for="(s, i) in suggestions" :key="i" class="suggestion-item">
              <el-icon :color="s.iconColor" size="20" style="margin-right: 8px;">
                <WarningFilled v-if="s.type === 'warning'" />
                <SuccessFilled v-else-if="s.type === 'success'" />
                <InfoFilled v-else />
              </el-icon>
              <span>{{ s.text }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSentiment, getTopQuestions, getInteractionTypes } from './api'
import { WarningFilled, SuccessFilled, InfoFilled } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const sentimentData = ref([])
const topQuestions = ref([])
const interactionTypes = ref([])

const totalSentiment = computed(() => {
  return sentimentData.value.reduce((sum, item) => sum + (item.value || 0), 0) || 1
})

const positivePercent = computed(() => {
  const item = sentimentData.value.find(s => s.name === '正面')
  return Math.round((item?.value || 0) / totalSentiment.value * 100)
})

const neutralPercent = computed(() => {
  const item = sentimentData.value.find(s => s.name === '中性')
  return Math.round((item?.value || 0) / totalSentiment.value * 100)
})

const negativePercent = computed(() => {
  const item = sentimentData.value.find(s => s.name === '负面')
  return Math.round((item?.value || 0) / totalSentiment.value * 100)
})

const sentimentOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  color: ['#2e7df6', '#5a6a8a', '#ef4444'],
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: sentimentData.value,
    label: { show: true, formatter: '{b}: {d}%', color: '#e2e8f0' },
    labelLine: { lineStyle: { color: 'rgba(46,125,246,0.15)' } }
  }]
}))

const intentOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  color: ['#2e7df6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#5a6a8a', '#8b5cf6'],
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: interactionTypes.value.map(t => ({
      name: t.name || '其他',
      value: t.value || 0
    })),
    label: { show: true, formatter: '{b}: {d}%', color: '#e2e8f0' }
  }]
}))

const suggestions = computed(() => {
  const list = []
  const negItem = sentimentData.value.find(s => s.name === '负面')
  const negCount = negItem?.value || 0
  const total = totalSentiment.value

  if (total > 0) {
    const negRatio = negCount / total

    if (negRatio > 0.3) {
      list.push({ type: 'warning', iconColor: '#f56c6c', text: '⚠ 负面评价占比偏高（' + Math.round(negRatio * 100) + '%），建议检查服务质量和知识库准确性' })
    } else if (negRatio > 0.1) {
      list.push({ type: 'info', iconColor: '#e6a23c', text: '负面评价占比 ' + Math.round(negRatio * 100) + '%，建议关注高频问题中的薄弱环节' })
    } else {
      list.push({ type: 'success', iconColor: '#67c23a', text: '✅ 游客满意度良好，负面评价仅占 ' + Math.round(negRatio * 100) + '%' })
    }
  }

  if (topQuestions.value.length > 0) {
    list.push({ type: 'info', iconColor: '#409eff', text: '游客最关心的问题是「' + topQuestions.value[0]?.question + '」，建议优化相关知识库内容' })
  }

  if (interactionTypes.value.length > 0) {
    const topType = interactionTypes.value[0]
    list.push({ type: 'info', iconColor: '#409eff', text: '游客关注最多的方向是「' + topType.name + '」（' + topType.value + '次），可针对性丰富该领域知识' })
  }

  return list
})

async function loadData() {
  try {
    const [se, q, types] = await Promise.all([
      getSentiment(),
      getTopQuestions(),
      getInteractionTypes()
    ])
    sentimentData.value = se || []
    topQuestions.value = q || []
    interactionTypes.value = types || []
  } catch (e) {
    console.error('加载报告数据失败', e)
  }
}

function rankLabel(i) {
  if (i === 0) return 'TOP 1'
  if (i === 1) return 'TOP 2'
  if (i === 2) return 'TOP 3'
  return '#' + (i + 1)
}

function heatPercent(count) {
  if (!topQuestions.value.length) return 0
  const max = Math.max(...topQuestions.value.map(q => q.count || 0))
  return max ? (count / max) * 100 : 0
}

const HEAT_MAP = { bao: '爆', re: '热', huo: '火', wen: '温' }
function heatLabel(key) {
  return HEAT_MAP[key] || '温'
}

onMounted(loadData)
</script>

<style scoped>
.reports-page { padding: 0; }

.stat-card {
  text-align: left;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 4px 0;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}
.stat-icon.positive { background: rgba(46,125,246,0.1); color: var(--gold); }
.stat-icon.neutral { background: rgba(90,106,138,0.1); color: var(--text-muted); }
.stat-icon.negative { background: rgba(239,68,68,0.08); color: var(--vermillion); }

.stat-value {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-value.positive { color: var(--gold); }
.stat-value.neutral { color: var(--text-muted); }
.stat-value.negative { color: var(--vermillion); }
.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
  letter-spacing: 0.5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* ===== 热搜榜 ===== */
.hot-list-card :deep(.el-card__body) {
  padding: 8px 16px 12px;
}
.hot-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
  background: linear-gradient(135deg, var(--gold), var(--gold-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hot-subtitle {
  letter-spacing: 0.5px;
}
.hot-list {
  display: flex;
  flex-direction: column;
}
.hot-item {
  display: flex;
  align-items: center;
  padding: 8px 4px;
  border-bottom: 1px solid rgba(46,125,246,0.06);
  transition: background 0.2s;
  border-radius: 4px;
  gap: 8px;
}
.hot-item:hover {
  background: rgba(46,125,246,0.04);
}
.hot-item:last-child {
  border-bottom: none;
}
.hot-rank {
  width: 42px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  border-radius: 4px;
  flex-shrink: 0;
  letter-spacing: 0.5px;
  font-family: var(--font-display);
}
.hot-rank.rank-1 {
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff;
  box-shadow: 0 2px 8px rgba(245,158,11,0.3);
}
.hot-rank.rank-2 {
  background: linear-gradient(135deg, #94a3b8, #64748b);
  color: #fff;
  box-shadow: 0 2px 8px rgba(100,116,139,0.25);
}
.hot-rank.rank-3 {
  background: linear-gradient(135deg, #d97706, #b45309);
  color: #fff;
  box-shadow: 0 2px 8px rgba(185,83,9,0.25);
}
.hot-rank:not(.rank-1):not(.rank-2):not(.rank-3) {
  background: transparent;
  color: var(--text-muted);
  font-weight: 500;
}
.hot-text {
  flex: 1;
  font-size: 13px;
  color: var(--text-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}
.hot-text::before {
  content: '·';
  color: var(--text-muted);
  margin-right: 4px;
}
.hot-item.top-three .hot-text {
  font-weight: 600;
}
.hot-heat {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 600;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}
.hot-heat.heat-bao {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}
.hot-heat.heat-re {
  background: #fff7ed;
  color: #ea580c;
  border: 1px solid #fed7aa;
}
.hot-heat.heat-huo {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}
.hot-heat.heat-wen {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}
.hot-count {
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
  min-width: 20px;
  text-align: right;
}
.hot-count::after {
  content: '次';
  font-size: 10px;
  margin-left: 1px;
}
.hot-bar-bg {
  width: 48px;
  height: 3px;
  background: rgba(46,125,246,0.08);
  border-radius: 2px;
  flex-shrink: 0;
  overflow: hidden;
}
.hot-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--gold), var(--gold-light));
  border-radius: 2px;
  transition: width 0.6s ease;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-light);
  font-size: 14px;
  color: var(--text-ink);
  line-height: 1.6;
}
.suggestion-item:last-child { border-bottom: none; }
</style>
