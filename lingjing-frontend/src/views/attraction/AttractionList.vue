<template>
  <div class="attraction-list">
    <!-- 顶部标题 -->
    <div class="header">
      <h1>探索灵山胜境</h1>
      <p class="subtitle">灵山胜境 &amp; 拈花湾禅意小镇 — 共22处景点等你发现</p>
    </div>

    <!-- 区域筛选标签 -->
    <div class="filter-bar">
      <el-radio-group v-model="area" @change="handleAreaChange">
        <el-radio-button value="">全部景点</el-radio-button>
        <el-radio-button value="灵山胜境">灵山胜境</el-radio-button>
        <el-radio-button value="拈花湾">拈花湾</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 景点卡片网格 -->
    <div v-else class="card-grid">
      <el-card
        v-for="item in list"
        :key="item.id"
        class="attraction-card"
        shadow="hover"
        @click="goDetail(item.id)"
      >
        <div class="card-content">
          <div class="card-header">
            <span class="card-name">{{ item.name }}</span>
            <el-tag :type="item.area === '灵山胜境' ? 'danger' : 'success'" size="small">
              {{ item.area }}
            </el-tag>
          </div>
          <p class="card-desc">{{ item.description }}</p>
          <div class="card-footer">
            <span class="info-item">
              <el-icon><Clock /></el-icon>
              {{ item.duration || '—' }}
            </span>
            <span class="info-item">
              <el-icon><Timer /></el-icon>
              {{ item.openingHours || '—' }}
            </span>
          </div>
          <div class="card-tags" v-if="item.tags">
            <el-tag
              v-for="tag in item.tags.split(',')"
              :key="tag"
              size="small"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无景点数据" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAttractions } from './api'
import { ElMessage } from 'element-plus'

const router = useRouter()

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const area = ref('')

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (area.value) params.area = area.value
    const result = await getAttractions(params)
    list.value = result.records
    total.value = result.total
    // 恢复滚动位置（等渲染完成后再滚）
    restoreScroll()
  } catch (e) {
    console.error('加载景点列表失败:', e)
    ElMessage.error('加载景点列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function restoreScroll() {
  requestAnimationFrame(() => {
    const savedPos = sessionStorage.getItem('scroll_AttractionList')
    if (savedPos) {
      sessionStorage.removeItem('scroll_AttractionList')
      window.scrollTo(0, parseInt(savedPos))
    }
  })
}

function handleAreaChange() {
  page.value = 1
  fetchData()
}

function handlePageChange(val) {
  page.value = val
  fetchData()
}

function goDetail(id) {
  router.push(`/attractions/${id}`)
}

onMounted(fetchData)
</script>

<style scoped>
.attraction-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.filter-bar {
  text-align: center;
  margin-bottom: 30px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.attraction-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.attraction-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.card-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.info-item {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-item {
  font-size: 12px;
}

.loading-container {
  padding: 40px;
}

.pagination-wrapper {
  text-align: center;
  margin-top: 30px;
}
</style>
