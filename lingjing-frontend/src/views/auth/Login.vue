<template>
  <div class="login-page">
    <!-- 管理员入口 — 右上角 -->
    <router-link to="/admin/login" class="admin-entry">
      <el-icon><Setting /></el-icon>
      管理员登录
    </router-link>

    <!-- 背景装饰 -->
    <div class="login-bg">
      <svg class="bg-mountains" viewBox="0 0 1440 400" preserveAspectRatio="xMidYMax meet">
        <path d="M0,200 L120,130 L240,180 L360,80 L480,150 L560,60 L680,130 L800,40 L920,110 L1040,70 L1160,140 L1280,90 L1380,160 L1440,140 L1440,400 L0,400 Z" fill="rgba(44,44,44,0.04)" />
        <path d="M0,260 L100,190 L200,230 L320,150 L440,210 L540,130 L660,200 L780,120 L900,190 L1020,160 L1140,220 L1260,170 L1360,230 L1440,210 L1440,400 L0,400 Z" fill="rgba(44,44,44,0.025)" />
      </svg>
    </div>

    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-deco">
        <div class="deco-vert-line" />
        <div class="deco-text">
          <span>灵</span>
          <span>山</span>
          <span>胜</span>
          <span>境</span>
        </div>
        <div class="deco-vert-line" />
      </div>

      <!-- 登录卡片 -->
      <div class="login-card">
        <div class="card-top-deco" />

        <div class="card-brand">
          <div class="brand-seal">灵</div>
          <h2 class="brand-title">欢迎回来</h2>
          <p class="brand-desc">登录灵境智游，开启心灵之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名 / 手机号"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              native-type="submit"
              :loading="loading"
              size="large"
              class="login-btn"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="switch-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>

        <div class="card-footer-text">
          灵山胜境 · 拈花湾
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login as loginApi, getCurrentUser } from './api'
import { ElMessage } from 'element-plus'
import { User, Lock, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi({ ...form })
    localStorage.setItem('token', res.token)
    localStorage.setItem('role', res.role)
    localStorage.setItem('username', res.username)

    // 登录后拉取用户完整信息（昵称、头像等），写入 localStorage
    try {
      const user = await getCurrentUser()
      if (user.nickname) localStorage.setItem('nickname', user.nickname)
      if (user.avatar) localStorage.setItem('avatar', user.avatar)
    } catch (_) { /* 静默失败，不影响登录 */ }

    ElMessage.success('登录成功')
    if (res.role === 'admin') {
      router.push('/admin/dashboard')
    } else {
      // 优先跳转到 redirect 参数指定的页面（从登录拦截 dialog 跳转过来）
      const redirect = route.query.redirect
      router.push(redirect || '/attractions')
    }
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: var(--paper-warm);
  overflow: hidden;
}

/* 管理员入口 */
.admin-entry {
  position: absolute;
  top: 24px;
  right: 24px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  font-size: 13px;
  color: var(--ink-mist);
  text-decoration: none;
  border: 1px solid var(--ink-faint);
  border-radius: 6px;
  transition: all 0.25s ease;
  z-index: 2;
}
.admin-entry:hover {
  color: var(--ink-dark);
  border-color: var(--ink-mist);
  background: rgba(168, 160, 149, 0.06);
}

/* 背景远山 */
.login-bg {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 260px;
  pointer-events: none;
  z-index: 0;
}
.bg-mountains {
  width: 100%;
  height: 100%;
}

/* 主容器 */
.login-container {
  display: flex;
  align-items: center;
  gap: 40px;
  position: relative;
  z-index: 1;
}

/* 左侧装饰 */
.login-deco {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.deco-vert-line {
  width: 1px;
  flex: 1;
  min-height: 40px;
  background: linear-gradient(180deg, transparent, var(--ink-mist), transparent);
}
.deco-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--ink-mist);
  letter-spacing: 2px;
  writing-mode: vertical-rl;
}

/* 登录卡片 */
.login-card {
  width: 400px;
  background: var(--paper-dark);
  border-radius: 12px;
  padding: 44px 40px 32px;
  border: 1px solid var(--ink-faint);
  box-shadow: var(--shadow-soft);
  position: relative;
  overflow: hidden;
}

.card-top-deco {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--ink-mist), transparent);
  border-radius: 0 0 3px 3px;
}

/* 品牌区 */
.card-brand {
  text-align: center;
  margin-bottom: 32px;
}

.brand-seal {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  background: var(--vermillion);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 2px 10px rgba(194, 58, 43, 0.18);
}

.brand-title {
  font-family: var(--font-display);
  font-size: 24px;
  color: var(--ink-black);
  margin: 0 0 8px;
  letter-spacing: 4px;
  font-weight: 700;
}

.brand-desc {
  font-size: 14px;
  color: var(--ink-mist);
  margin: 0;
  letter-spacing: 1px;
}

/* 表单 */
.login-form {
  margin-bottom: 4px;
}

.login-form :deep(.el-input__wrapper) {
  background: var(--paper-warm);
  box-shadow: 0 0 0 1px var(--ink-faint) inset;
  border-radius: 8px;
  padding: 4px 16px;
  transition: all 0.2s ease;
}
.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--ink-mist) inset;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--ink-dark) inset;
  background: #fff;
}
.login-form :deep(.el-input__inner) {
  color: var(--ink-black);
  font-size: 15px;
}
.login-form :deep(.el-input__prefix) {
  color: var(--ink-mist);
}
.login-form :deep(.el-input__prefix-inner) {
  margin-right: 8px;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-family: var(--font-display);
  letter-spacing: 6px;
  border-radius: 8px;
  background: var(--ink-dark);
  border: none;
  color: var(--paper-warm);
  transition: all 0.25s ease;
}
.login-btn:hover {
  background: var(--ink-black);
  box-shadow: 0 4px 16px rgba(44, 44, 44, 0.15);
  transform: translateY(-1px);
}

.switch-link {
  text-align: center;
  font-size: 14px;
  color: var(--ink-mist);
  margin: 0 0 20px;
}
.switch-link a {
  color: var(--vermillion);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.switch-link a:hover {
  color: var(--vermillion-light);
}

.card-footer-text {
  text-align: center;
  font-size: 12px;
  color: var(--ink-mist);
  letter-spacing: 2px;
  position: relative;
}
.card-footer-text::before,
.card-footer-text::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 60px;
  height: 1px;
  background: var(--ink-faint);
}
.card-footer-text::before {
  right: calc(100% + 16px);
}
.card-footer-text::after {
  left: calc(100% + 16px);
}

@media (max-width: 640px) {
  .login-deco {
    display: none;
  }
  .login-card {
    width: 340px;
    margin: 0 16px;
    padding: 36px 28px 28px;
  }
  .admin-entry {
    top: 16px;
    right: 16px;
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
