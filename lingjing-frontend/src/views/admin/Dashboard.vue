<template>
  <div class="dashboard">
    <h2 class="page-title">数据概览</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon users"><el-icon :size="24"><User /></el-icon></div>
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">注册用户</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon chats"><el-icon :size="24"><ChatDotRound /></el-icon></div>
          <div class="stat-value">{{ stats.conversationCount }}</div>
          <div class="stat-label">对话总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon messages"><el-icon :size="24"><Message /></el-icon></div>
          <div class="stat-value">{{ stats.messageCount }}</div>
          <div class="stat-label">消息总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon checkins"><el-icon :size="24"><Location /></el-icon></div>
          <div class="stat-value">{{ stats.checkInCount }}</div>
          <div class="stat-label">打卡总数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>近7日趋势</template>
          <v-chart :option="trendsOption" style="height: 300px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>情感分布</template>
          <v-chart :option="sentimentOption" style="height: 300px;" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>热门景点打卡排行</span>
              <el-button text type="primary" @click="showAttractionDetail = true">查看详情</el-button>
            </div>
          </template>
          <v-chart :option="attractionsOption" style="height: 300px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="hot-list-card">
          <template #header>
            <div class="card-header">
              <span class="hot-title">游客高频对话 · TOP 10</span>
              <el-tag size="small" type="info" effect="plain" class="hot-subtitle">高频问题</el-tag>
            </div>
          </template>
          <div v-if="topQuestions.length === 0" class="empty-chart">暂无数据</div>
          <div v-else class="hot-list">
            <div v-for="(q, i) in topQuestions" :key="i" class="hot-item" :class="{ 'top-three': i < 3 }">
              <!-- 排名 -->
              <span class="hot-rank" :class="'rank-' + (i + 1)">{{ rankLabel(i) }}</span>
              <!-- 问题 -->
              <span class="hot-text">{{ q.question }}</span>
              <!-- 热度标签 -->
              <span class="hot-heat" :class="'heat-' + (q.heat || 'wen')">{{ heatLabel(q.heat) }}</span>
              <!-- 次数 -->
              <span class="hot-count">{{ q.count }}</span>
              <!-- 热度条 -->
              <div class="hot-bar-bg">
                <div class="hot-bar-fill" :style="{ width: heatPercent(q.count) + '%' }" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门景点详情弹窗 -->
    <el-dialog v-model="showAttractionDetail" title="热门景点打卡排行" width="600px">
      <el-table :data="popularAttractions" stripe max-height="400px">
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="name" label="景点名称" min-width="150" />
        <el-table-column prop="area" label="区域" width="100" />
        <el-table-column prop="checkInCount" label="打卡次数" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.checkInCount }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getDashboardStats, getTrends, getPopularAttractions, getSentiment, getTopQuestions } from './api'
import { User, ChatDotRound, Message, Location } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

// 蓝调配色
const colors = {
  blue: '#2e7df6',
  blueLight: '#5b9af8',
  blueBg: 'rgba(46,125,246,0.12)',
  green: '#10b981',
  cyan: '#06b6d4',
  red: '#ef4444',
  textPrimary: '#e2e8f0',
  muted: '#5a6a8a'
}

const stats = ref({
  userCount: 0,
  conversationCount: 0,
  messageCount: 0,
  checkInCount: 0
})
const trends = ref([])
const popularAttractions = ref([])
const sentimentData = ref([])
const topQuestions = ref([])
const showAttractionDetail = ref(false)

const trendsOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 20, top: 20, bottom: 30 },
  xAxis: {
    type: 'category',
    data: trends.value.map(t => t.date),
    axisLine: { lineStyle: { color: colors.muted } },
    axisLabel: { color: colors.muted }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'rgba(46,125,246,0.06)' } },
    axisLabel: { color: colors.muted }
  },
  series: [{
    name: '对话数',
    type: 'line',
    data: trends.value.map(t => t.conversations),
    smooth: true,
    lineStyle: { color: colors.blue, width: 2 },
    itemStyle: { color: colors.blue },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: colors.blueBg },
          { offset: 1, color: 'rgba(46,125,246,0.01)' }
        ]
      }
    }
  }]
}))

const sentimentOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  color: [colors.green, colors.muted, colors.red],
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: sentimentData.value,
    label: {
      show: true,
      formatter: '{b}: {d}%',
      color: colors.textPrimary
    },
    labelLine: {
      lineStyle: { color: colors.muted }
    },
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0,0,0,0.1)'
      }
    }
  }]
}))

const attractionsOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 20, top: 20, bottom: 30 },
  xAxis: {
    type: 'category',
    data: popularAttractions.value.slice(0, 5).map(a => a.name),
    axisLine: { lineStyle: { color: colors.muted } },
    axisLabel: { color: colors.muted, rotate: 15 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'rgba(46,125,246,0.06)' } },
    axisLabel: { color: colors.muted }
  },
  series: [{
    type: 'bar',
    data: popularAttractions.value.slice(0, 5).map(a => ({
      value: a.checkInCount,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: colors.blueLight },
            { offset: 1, color: colors.blue }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      }
    })),
    barWidth: 36
  }]
}))

async function loadData() {
  try {
    const [s, t, a, se, q] = await Promise.all([
      getDashboardStats(),
      getTrends(),
      getPopularAttractions(),
      getSentiment(),
      getTopQuestions()
    ])
    stats.value = s
    trends.value = t
    popularAttractions.value = a
    sentimentData.value = se
    topQuestions.value = q
  } catch (e) {
    console.error('加载仪表盘数据失败', e)
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
.dashboard { padding: 0; }

.stat-cards { margin-bottom: 0; }
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
.stat-icon.users { background: rgba(46,125,246,0.1); color: var(--gold); }
.stat-icon.chats { background: rgba(6,182,212,0.1); color: var(--accent-cyan); }
.stat-icon.messages { background: rgba(90,106,138,0.1); color: var(--text-muted); }
.stat-icon.checkins { background: rgba(16,185,129,0.1); color: var(--jade); }

.stat-value {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--text-ink);
  line-height: 1.2;
}
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
</style>
