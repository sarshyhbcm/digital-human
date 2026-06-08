<template>
  <div
    id="app-container"
    :class="{ 'zen-theme': !route.path.startsWith('/admin') }"
  >
    <!-- 游客端导航 -->
    <nav v-if="!route.path.startsWith('/admin')" class="zen-nav">
      <div class="zen-nav-inner">
        <router-link to="/" class="zen-nav-brand">
          <span class="brand-char">灵</span>
          <span class="brand-title">灵境智游</span>
        </router-link>

        <div class="zen-nav-links">
          <router-link to="/attractions" class="zen-nav-item" active-class="is-active">
            <el-icon><MapLocation /></el-icon>
            <span>景点</span>
          </router-link>
          <router-link to="/chat" class="zen-nav-item" active-class="is-active">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能导游</span>
          </router-link>
          <router-link to="/routes" class="zen-nav-item" active-class="is-active">
            <el-icon><TrendCharts /></el-icon>
            <span>推荐路线</span>
          </router-link>
          <router-link v-if="authState.loggedIn" to="/my-routes" class="zen-nav-item" active-class="is-active">
            <el-icon><List /></el-icon>
            <span>我的路线</span>
          </router-link>
        </div>

        <div class="zen-nav-right">
          <template v-if="authState.loggedIn">
            <router-link to="/checkin" class="zen-nav-item" active-class="is-active">
              <el-icon><MapLocation /></el-icon>
              <span>打卡</span>
            </router-link>
            <router-link to="/achievements" class="zen-nav-item" active-class="is-active">
              <el-icon><TrophyBase /></el-icon>
              <span>成就</span>
            </router-link>
            <router-link v-if="authState.role === 'admin'" to="/admin/dashboard" class="zen-nav-item" active-class="is-active">
              <el-icon><Setting /></el-icon>
              <span>管理</span>
            </router-link>
            <router-link to="/profile" class="zen-nav-user" active-class="is-active">
              <img v-if="navAvatarUrl" :src="navAvatarUrl" class="nav-avatar" />
              <span v-else class="nav-avatar-letter">{{ authState.nickname.charAt(0) }}</span>
              <span class="nav-username">{{ authState.nickname }}</span>
              <span v-if="equippedBadge" class="nav-badge">{{ equippedBadge.name }}</span>
            </router-link>
            <button class="zen-nav-logout" @click="handleLogout" title="退出登录">
              <el-icon><SwitchButton /></el-icon>
            </button>
          </template>
          <router-link v-else to="/login" class="zen-nav-login">
            <el-icon><User /></el-icon>
            <span>登录</span>
          </router-link>
        </div>
      </div>
      <!-- 底部朱砂线 -->
      <div class="zen-nav-shadow" />
    </nav>

    <div class="main-content">
      <router-view />
    </div>

    <!-- 全局浮动数字人 -->
    <Live2DHuman
      v-if="showGlobalL2d"
      floating
      draggable
      interactive
      :size="180"
      expression="happy"
      @click-bubble="goToChat"
    />

    <!-- 全局登录提示 dialog -->
    <el-dialog
      v-model="showAuthDialog"
      width="400px"
      :close-on-click-modal="false"
      :show-close="false"
      class="auth-required-dialog"
    >
      <div class="auth-prompt-content">
        <div class="auth-prompt-seal">需</div>
        <h3 class="auth-prompt-title">此功能需登录后体验</h3>
        <p class="auth-prompt-desc">请先登录灵境智游账号，即可使用智能导游、路线规划、景点打卡、成就徽章等功能</p>
        <div class="auth-prompt-actions">
          <button class="auth-prompt-btn-primary" @click="goLogin">
            <el-icon><User /></el-icon>
            去登录
          </button>
          <button class="auth-prompt-btn-text" @click="cancelAuth">
            返回
          </button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, MapLocation, ChatDotRound, TrendCharts, List, TrophyBase, Setting, User, SwitchButton } from '@element-plus/icons-vue'
import Live2DHuman from './components/Live2DHuman.vue'
import { getMyAchievements } from './views/profile/api'
import { showAuthDialog, pendingRoute, closeAuthDialog } from './stores/authDialog'
import './assets/styles/zen-theme.css'

const route = useRoute()
const router = useRouter()

// 非聊天页、非管理后台、非登录注册页显示全局浮动数字人
const showGlobalL2d = computed(() => {
  const p = route.path
  return !p.startsWith('/chat') && !p.startsWith('/admin') && p !== '/login' && p !== '/register'
})

function goToChat() {
  router.push('/chat')
}

function parseToken() {
  const token = localStorage.getItem('token')
  if (!token) return { loggedIn: false, nickname: '', role: '' }
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    // 优先使用 localStorage 中的昵称（用户可能在编辑页修改过）
    const storedNickname = localStorage.getItem('nickname')
    return {
      loggedIn: true,
      nickname: storedNickname || payload.username || payload.nickname || '用户',
      role: payload.role || ''
    }
  } catch {
    return { loggedIn: false, nickname: '', role: '' }
  }
}

const authState = reactive(parseToken())
const navAvatarUrl = ref(localStorage.getItem('avatar') || '')

const equippedBadge = ref(null)

async function fetchEquippedBadge() {
  try {
    const result = await getMyAchievements()
    const equipped = (result || []).find(b => b.equipped)
    equippedBadge.value = equipped || null
  } catch {
    equippedBadge.value = null
  }
}

watch(
  () => route.path,
  () => {
    const state = parseToken()
    Object.assign(authState, state)
    navAvatarUrl.value = localStorage.getItem('avatar') || ''
    if (authState.loggedIn) fetchEquippedBadge()
  },
  { immediate: true }
)

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  localStorage.removeItem('nickname')
  localStorage.removeItem('avatar')
  authState.loggedIn = false
  authState.nickname = ''
  authState.role = ''
  router.push('/attractions')
}

function goLogin() {
  const redirect = pendingRoute.value ? pendingRoute.value.fullPath : '/chat'
  closeAuthDialog()
  router.push(`/login?redirect=${encodeURIComponent(redirect)}`)
}

function cancelAuth() {
  closeAuthDialog()
}
</script>

<style>
body {
  margin: 0;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

#app-container {
  min-height: 100vh;
}

.main-content {
  min-height: calc(100vh - 60px);
}

/* ============ 禅意导航栏 ============ */
.zen-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(245, 240, 232, 0.82);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: box-shadow 0.3s ease;
}

.zen-nav-shadow {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(168, 160, 149, 0.2) 15%,
    rgba(168, 160, 149, 0.2) 85%,
    transparent 100%
  );
}

.zen-nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 24px;
  gap: 8px;
}

/* Brand */
.zen-nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  margin-right: 16px;
  flex-shrink: 0;
}

.brand-char {
  width: 34px;
  height: 34px;
  background: linear-gradient(135deg, #3d3730, #6b6358);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 700;
  color: #f5f0e8;
  letter-spacing: 0;
}

.brand-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-black, #2c2c2c);
  letter-spacing: 3px;
}

/* Nav links */
.zen-nav-links {
  display: flex;
  align-items: center;
  gap: 2px;
}

.zen-nav-right {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
}

.zen-nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 0 14px;
  height: 44px;
  text-decoration: none;
  color: var(--ink-light, #6b6358);
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
  border-radius: 6px;
  transition: all 0.25s ease;
  position: relative;
  white-space: nowrap;
}

.zen-nav-item .el-icon {
  font-size: 16px;
  color: var(--ink-mist, #a8a095);
  transition: color 0.25s ease;
}

.zen-nav-item:hover {
  color: var(--ink-black, #2c2c2c);
  background: rgba(168, 160, 149, 0.08);
}

.zen-nav-item:hover .el-icon {
  color: var(--ink-light, #6b6358);
}

.zen-nav-item.is-active {
  color: var(--ink-black, #2c2c2c);
  font-weight: 600;
}

.zen-nav-item.is-active .el-icon {
  color: var(--vermillion, #c23a2b);
}

.zen-nav-item.is-active::after {
  content: '';
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  width: 18px;
  height: 2.5px;
  background: var(--vermillion, #c23a2b);
  border-radius: 2px;
  transition: width 0.25s ease;
}

.zen-nav-item.is-active:hover::after {
  width: 28px;
}

/* Login button */
.zen-nav-login {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 18px;
  height: 36px;
  text-decoration: none;
  color: var(--paper-warm, #f5f0e8);
  font-size: 13px;
  font-weight: 500;
  background: var(--ink-dark, #3d3730);
  border-radius: 8px;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.zen-nav-login:hover {
  background: var(--ink-black, #2c2c2c);
  box-shadow: 0 2px 12px rgba(44, 44, 44, 0.15);
}

/* User section */
.zen-nav-user {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  height: 40px;
  text-decoration: none;
  color: var(--ink-light, #6b6358);
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.zen-nav-user:hover {
  background: rgba(168, 160, 149, 0.08);
  color: var(--ink-black, #2c2c2c);
}

.zen-nav-user .nav-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(168, 160, 149, 0.2);
}

.zen-nav-user .nav-avatar-letter {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6b6358, #a8a095);
  color: #f5f0e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.zen-nav-user .nav-badge {
  margin-left: 2px;
  font-size: 11px;
  font-weight: 700;
  color: #b8860b;
  letter-spacing: 0.5px;
  padding: 1px 6px;
  border-radius: 3px;
  background: rgba(212, 160, 23, 0.1);
}

/* Logout */
.zen-nav-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  color: var(--ink-mist, #a8a095);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-size: 18px;
}

.zen-nav-logout:hover {
  color: var(--vermillion, #c23a2b);
  background: rgba(194, 58, 43, 0.06);
}

/* 全局登录提示 dialog */
:deep(.auth-required-dialog .el-dialog) {
  background: var(--paper-warm);
  border: 1px solid var(--ink-faint);
  border-radius: 12px;
  box-shadow: var(--shadow-float);
  padding: 0;
  overflow: hidden;
  width: 400px;
}
:deep(.auth-required-dialog .el-dialog__body) {
  padding: 40px 36px 32px;
}

.auth-prompt-content {
  text-align: center;
}

.auth-prompt-seal {
  width: 56px;
  height: 56px;
  margin: 0 auto 20px;
  background: var(--vermillion);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0;
  box-shadow: 0 2px 12px rgba(194, 58, 43, 0.2);
}

.auth-prompt-title {
  font-family: var(--font-display);
  font-size: 20px;
  color: var(--ink-black);
  margin: 0 0 12px;
  letter-spacing: 4px;
}

.auth-prompt-desc {
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink-light);
  margin: 0 0 28px;
}

.auth-prompt-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.auth-prompt-btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 48px;
  background: var(--ink-dark);
  color: var(--paper-warm);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 2px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all 0.25s ease;
}

.auth-prompt-btn-primary:hover {
  background: var(--ink-black);
  box-shadow: 0 4px 16px rgba(44, 44, 44, 0.15);
  transform: translateY(-1px);
}

.auth-prompt-btn-text {
  background: none;
  border: none;
  font-size: 13px;
  color: var(--ink-mist);
  cursor: pointer;
  padding: 4px 12px;
  transition: color 0.2s ease;
  font-family: var(--font-body);
}

.auth-prompt-btn-text:hover {
  color: var(--ink-light);
}
</style>
