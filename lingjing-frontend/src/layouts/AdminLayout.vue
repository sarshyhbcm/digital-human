<template>
  <div class="sci-fi-theme admin-layout">
    <el-container style="min-height: 100vh;">
      <!-- 侧边栏 -->
      <el-aside width="220px" class="admin-sidebar">
        <div class="sidebar-brand">
          <div class="brand-icon">灵</div>
          <div class="brand-text">
            <span class="brand-title">灵境智游</span>
            <span class="brand-sub">MANAGEMENT</span>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          router
          class="sci-menu sci-menu-main"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据大屏</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/conversations">
            <el-icon><ChatDotRound /></el-icon>
            <span>对话日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/achievements">
            <el-icon><TrophyBase /></el-icon>
            <span>成就配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/knowledge">
            <el-icon><Reading /></el-icon>
            <span>知识库</span>
          </el-menu-item>
          <el-menu-item index="/admin/qrcodes">
            <el-icon><Goods /></el-icon>
            <span>二维码管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/digital-human">
            <el-icon><UserFilled /></el-icon>
            <span>数字人配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <el-icon><TrendCharts /></el-icon>
            <span>感受度报告</span>
          </el-menu-item>
          <el-menu-item index="/admin/hot-search">
            <el-icon><Star /></el-icon>
            <span>热搜管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/attractions">
            <el-icon><MapLocation /></el-icon>
            <span>景点管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/banners">
            <el-icon><PictureFilled /></el-icon>
            <span>轮播图管理</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-divider" />

        <el-menu router class="sci-menu">
          <el-menu-item index="/admin/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>前往游客端</span>
          </el-menu-item>
          <el-menu-item index="" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <svg class="sidebar-decoration" viewBox="0 0 220 120" preserveAspectRatio="xMidYMax meet">
            <line x1="0" y1="40" x2="220" y2="40" stroke="rgba(46,125,246,0.04)" stroke-width="0.5" />
            <line x1="0" y1="80" x2="220" y2="80" stroke="rgba(46,125,246,0.04)" stroke-width="0.5" />
            <line x1="55" y1="0" x2="55" y2="120" stroke="rgba(46,125,246,0.04)" stroke-width="0.5" />
            <line x1="110" y1="0" x2="110" y2="120" stroke="rgba(46,125,246,0.04)" stroke-width="0.5" />
            <line x1="165" y1="0" x2="165" y2="120" stroke="rgba(46,125,246,0.04)" stroke-width="0.5" />
            <path d="M30 95 Q55 50 80 95 Q105 50 130 95 Q155 50 180 95" stroke="rgba(46,125,246,0.06)" fill="none" stroke-width="1" />
            <path d="M20 105 Q50 65 80 105 Q110 65 140 105 Q170 65 200 105" stroke="rgba(46,125,246,0.04)" fill="none" stroke-width="0.8" />
            <circle cx="55" cy="30" r="2.5" fill="rgba(46,125,246,0.1)" />
            <circle cx="110" cy="30" r="2" fill="rgba(6,182,212,0.08)" />
            <circle cx="165" cy="30" r="3" fill="rgba(46,125,246,0.12)" />
            <circle cx="110" cy="70" r="2" fill="rgba(46,125,246,0.08)" />
            <line x1="55" y1="30" x2="110" y2="70" stroke="rgba(46,125,246,0.05)" stroke-width="0.8" />
            <line x1="110" y1="70" x2="165" y2="30" stroke="rgba(46,125,246,0.05)" stroke-width="0.8" />
            <circle cx="30" cy="20" r="1.5" fill="rgba(6,182,212,0.15)" />
            <circle cx="180" cy="90" r="1.5" fill="rgba(6,182,212,0.15)" />
          </svg>
        </div>
      </el-aside>

      <!-- 主区域 -->
      <el-main class="admin-main">
        <header class="admin-header">
          <div class="header-left">
            <span class="header-breadcrumb">{{ currentPageTitle }}</span>
          </div>
          <div class="header-right">
            <span class="header-user">
              <img v-if="avatarUrl" :src="avatarUrl" class="user-avatar-img" />
              <span v-else class="user-avatar">{{ username.charAt(0) }}</span>
              <span class="user-name">{{ displayName }}</span>
              <!-- 已佩戴徽章 -->
              <span v-if="equippedBadge" class="header-badge-equipped" :title="equippedBadge.conditionDesc">
                <span class="header-badge-name">{{ equippedBadge.name }}</span>
              </span>
            </span>
          </div>
        </header>
        <div class="admin-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMyAchievements } from '../views/auth/api'
import '../assets/styles/admin-theme.css'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  document.documentElement.classList.add('sci-fi-theme')
  refreshUserInfo()
  fetchBadges()
})
onUnmounted(() => {
  document.documentElement.classList.remove('sci-fi-theme')
})

// 路由切换时刷新用户信息（Profile.vue 可能已修改）
watch(() => route.path, () => {
  refreshUserInfo()
  fetchBadges()
})

function refreshUserInfo() {
  avatarUrl.value = localStorage.getItem('avatar') || ''
  const storedNickname = localStorage.getItem('nickname')
  displayName.value = storedNickname || localStorage.getItem('username') || '管理员'
}

const activeMenu = computed(() => route.path)
const username = ref(localStorage.getItem('username') || '管理员')
const displayName = ref(localStorage.getItem('nickname') || username.value)
const avatarUrl = ref(localStorage.getItem('avatar') || '')

// 头部展示已佩戴的徽章
const unlockedBadges = ref([])
const equippedBadge = ref(null)

async function fetchBadges() {
  try {
    const result = await getMyAchievements()
    unlockedBadges.value = (result || []).filter(b => b.unlocked)
    equippedBadge.value = (result || []).find(b => b.equipped) || null
  } catch (e) {
    // 静默失败
  }
}

const pageTitles = {
  '/admin/dashboard': '数据大屏',
  '/admin/users': '用户管理',
  '/admin/conversations': '对话日志',
  '/admin/achievements': '成就配置',
  '/admin/knowledge': '知识库',
  '/admin/qrcodes': '二维码管理',
  '/admin/digital-human': '数字人配置',
  '/admin/reports': '感受度报告',
  '/admin/profile': '个人信息',
  '/admin/attractions': '景点管理',
  '/admin/banners': '轮播图管理'
}
const currentPageTitle = computed(() => pageTitles[route.path] || '管理后台')

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  font-family: var(--font-body);
  background: var(--bg-deep);
  min-height: 100vh;
}

/* ============ Sidebar ============ */
.admin-sidebar {
  background: var(--bg-sidebar);
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  z-index: 10;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(46, 125, 246, 0.08);
}

.sidebar-brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  border-bottom: 1px solid rgba(46, 125, 246, 0.08);
  flex-shrink: 0;
}

.brand-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-cyan));
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 0 12px rgba(46, 125, 246, 0.25);
}

.brand-text {
  display: flex;
  flex-direction: column;
}
.brand-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 2px;
  line-height: 1.2;
}
.brand-sub {
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 4px;
  line-height: 1.2;
  text-transform: uppercase;
  font-family: var(--font-display);
}

/* Menu */
.sci-menu {
  border-right: none !important;
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
/* 主菜单：按实际高度撑开，不滚动 */
.sci-menu-main {
  flex: 0 0 auto;
  overflow: visible;
}
.sci-menu.el-menu {
  background-color: transparent;
}
.sci-menu .el-menu-item {
  background-color: transparent !important;
  color: var(--text-secondary);
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 6px;
  font-size: 14px;
  letter-spacing: 0.5px;
  transition: all 0.2s ease;
}
.sci-menu .el-menu-item:hover {
  background-color: rgba(46, 125, 246, 0.08) !important;
  color: var(--text-primary);
}
.sci-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(46, 125, 246, 0.15), rgba(6, 182, 212, 0.05)) !important;
  color: var(--accent-blue) !important;
  position: relative;
}
.sci-menu .el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: linear-gradient(to bottom, var(--accent-blue), var(--accent-cyan));
  border-radius: 0 2px 2px 0;
  box-shadow: 0 0 8px rgba(46, 125, 246, 0.4);
}
.sci-menu .el-menu-item .el-icon {
  color: inherit;
  font-size: 18px;
  margin-right: 8px;
}

/* Sidebar footer */
.sidebar-footer {
  flex-shrink: 0;
}
.footer-menu {
  padding: 0;
}
.footer-menu .el-menu-item {
  font-size: 13px;
}
.sidebar-divider {
  height: 1px;
  background: rgba(46, 125, 246, 0.08);
  margin: 4px 16px;
}
.sidebar-decoration {
  display: block;
  width: 100%;
  height: 100px;
  margin-top: 8px;
  margin-bottom: 8px;
  pointer-events: none;
}

/* ============ Main area ============ */
.admin-main {
  margin-left: 220px;
  padding: 0;
  min-height: 100vh;
}

.admin-header {
  height: 64px;
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  position: sticky;
  top: 0;
  z-index: 5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-breadcrumb {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 1px;
  position: relative;
  padding-left: 16px;
}
.header-breadcrumb::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: linear-gradient(to bottom, var(--accent-blue), var(--accent-cyan));
  border-radius: 2px;
  box-shadow: 0 0 8px rgba(46, 125, 246, 0.3);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.header-user {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(46, 125, 246, 0.2);
}
.user-name {
  color: var(--text-secondary);
  font-size: 14px;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(46, 125, 246, 0.15), rgba(6, 182, 212, 0.1));
  border: 1px solid rgba(46, 125, 246, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--accent-cyan);
  font-weight: 500;
}

.admin-content {
  padding: 28px 32px;
}

/* ============ Page Transition ============ */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(18px) scale(0.97);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-14px) scale(0.97);
}

/* ============ Header Equipped Badge ============ */
.header-badge-equipped {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
  padding: 2px 10px;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.08), rgba(245, 215, 66, 0.04));
  border: 1px solid rgba(255, 215, 0, 0.15);
}
.header-badge-name {
  font-size: 12px;
  font-weight: 700;
  color: #d4a017;
  letter-spacing: 1px;
  text-shadow: 0 0 8px rgba(212, 160, 23, 0.25);
  line-height: 1;
}
</style>
