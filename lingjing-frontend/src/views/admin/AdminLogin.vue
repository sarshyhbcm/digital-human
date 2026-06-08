<template>
  <div class="zen-theme admin-login-page">
    <!-- 返回游客端入口 — 右上角 -->
    <router-link to="/login" class="back-entry">
      <el-icon><ArrowLeft /></el-icon>
      返回用户登录
    </router-link>

    <!-- 背景装饰 -->
    <div class="login-bg">
      <svg class="bg-mountains" viewBox="0 0 1440 400" preserveAspectRatio="xMidYMax meet">
        <path d="M0,200 L120,130 L240,180 L360,80 L480,150 L560,60 L680,130 L800,40 L920,110 L1040,70 L1160,140 L1280,90 L1380,160 L1440,140 L1440,400 L0,400 Z" fill="rgba(44,44,44,0.04)" />
        <path d="M0,260 L100,190 L200,230 L320,150 L440,210 L540,130 L660,200 L780,120 L900,190 L1020,160 L1140,220 L1260,170 L1360,230 L1440,210 L1440,400 L0,400 Z" fill="rgba(44,44,44,0.025)" />
      </svg>
    </div>

    <div class="login-container">
      <!-- 左侧品牌 -->
      <div class="login-brand">
        <div class="brand-decoration">
          <div class="deco-ring">
            <div class="ring-inner" />
            <div class="ring-dot top" />
            <div class="ring-dot bottom" />
            <div class="ring-dot left" />
            <div class="ring-dot right" />
          </div>
        </div>
        <div class="brand-text">
          <h1 class="brand-title">灵境智游</h1>
          <p class="brand-subtitle">后台管理</p>
          <p class="brand-desc">LINGJING ADMIN</p>
        </div>
      </div>

      <!-- 登录卡片 -->
      <div class="login-card-wrap">
        <div class="login-card">
          <div class="card-top-deco" />

          <div class="card-header">
            <div class="card-header-seal">管</div>
            <h2>管理员登录</h2>
            <p class="card-subtitle">AUTHORIZED ACCESS ONLY</p>
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
                placeholder="管理员用户名"
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
                {{ loading ? '验证中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="card-footer-text">
            灵山胜境 · 拈花湾
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, getCurrentUser } from '../auth/api'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowLeft } from '@element-plus/icons-vue'
import '../../assets/styles/zen-theme.css'

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
    const res = await login({ ...form })
    if (res.role !== 'admin') {
      ElMessage.error('该账号不是管理员，无法登录管理后台')
      return
    }
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
    router.push('/admin/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: var(--paper-warm);
  overflow: hidden;
}

/* 返回入口 */
.back-entry {
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
.back-entry:hover {
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
  gap: 64px;
  position: relative;
  z-index: 1;
}

/* 左侧品牌 */
.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.brand-decoration {
  width: 180px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.deco-ring {
  position: relative;
  width: 160px;
  height: 160px;
}

.ring-inner {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px solid rgba(168, 160, 149, 0.2);
}
.ring-inner::before {
  content: '';
  position: absolute;
  inset: 24px;
  border-radius: 50%;
  border: 1px solid rgba(168, 160, 149, 0.12);
}
.ring-inner::after {
  content: '';
  position: absolute;
  inset: 48px;
  border-radius: 50%;
  border: 1px solid rgba(168, 160, 149, 0.08);
}

.ring-dot {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--ink-mist);
  opacity: 0.3;
}
.ring-dot.top { top: -3px; left: 50%; transform: translateX(-50%); }
.ring-dot.bottom { bottom: -3px; left: 50%; transform: translateX(-50%); }
.ring-dot.left { left: -3px; top: 50%; transform: translateY(-50%); }
.ring-dot.right { right: -3px; top: 50%; transform: translateY(-50%); }

.brand-text {
  text-align: center;
}

.brand-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--ink-black);
  letter-spacing: 6px;
  margin: 0;
}

.brand-subtitle {
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--ink-mist);
  letter-spacing: 4px;
  margin: 8px 0 0;
}

.brand-desc {
  font-size: 11px;
  color: var(--ink-mist);
  margin: 6px 0 0;
  letter-spacing: 3px;
  text-transform: uppercase;
  opacity: 0.6;
}

/* 登录卡片 */
.login-card-wrap {
  position: relative;
}

.login-card {
  width: 380px;
  background: var(--paper-dark);
  border-radius: 12px;
  padding: 40px;
  border: 1px solid var(--ink-faint);
  box-shadow: var(--shadow-soft);
  position: relative;
  overflow: hidden;
}

.card-top-deco {
  position: absolute;
  top: 0;
  left: 40px;
  right: 40px;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--ink-mist), transparent);
  border-radius: 0 0 3px 3px;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header-seal {
  width: 42px;
  height: 42px;
  margin: 0 auto 14px;
  background: var(--ink-dark);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--paper-warm);
}

.card-header h2 {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-black);
  margin: 0;
  letter-spacing: 4px;
}

.card-subtitle {
  font-size: 11px;
  color: var(--ink-mist);
  margin: 8px 0 0;
  letter-spacing: 3px;
  font-family: var(--font-display);
  text-transform: uppercase;
  opacity: 0.6;
}

/* 表单 */
.login-form {
  margin-bottom: 8px;
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

.card-footer-text {
  text-align: center;
  font-size: 12px;
  color: var(--ink-mist);
  letter-spacing: 2px;
  position: relative;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--ink-faint);
}

@media (max-width: 768px) {
  .login-brand {
    display: none;
  }
  .login-card {
    width: 340px;
    margin: 0 16px;
  }
  .back-entry {
    top: 16px;
    right: 16px;
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
