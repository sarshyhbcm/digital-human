<template>
  <div class="register-page">
    <!-- 返回登录 — 右上角 -->
    <router-link to="/login" class="back-entry">
      <el-icon><ArrowLeft /></el-icon>
      返回登录
    </router-link>

    <!-- 背景装饰 -->
    <div class="register-bg">
      <svg class="bg-mountains" viewBox="0 0 1440 400" preserveAspectRatio="xMidYMax meet">
        <path d="M0,200 L120,130 L240,180 L360,80 L480,150 L560,60 L680,130 L800,40 L920,110 L1040,70 L1160,140 L1280,90 L1380,160 L1440,140 L1440,400 L0,400 Z" fill="rgba(44,44,44,0.04)" />
        <path d="M0,260 L100,190 L200,230 L320,150 L440,210 L540,130 L660,200 L780,120 L900,190 L1020,160 L1140,220 L1260,170 L1360,230 L1440,210 L1440,400 L0,400 Z" fill="rgba(44,44,44,0.025)" />
      </svg>
    </div>

    <div class="register-container">
      <!-- 左侧装饰 -->
      <div class="register-deco">
        <div class="deco-vert-line" />
        <div class="deco-text">
          <span>灵</span>
          <span>山</span>
          <span>胜</span>
          <span>境</span>
        </div>
        <div class="deco-vert-line" />
      </div>

      <!-- 注册卡片 -->
      <div class="register-card">
        <div class="card-top-deco" />

        <div class="card-brand">
          <div class="brand-seal">缘</div>
          <h2 class="brand-title">创建账号</h2>
          <p class="brand-desc">注册灵境智游，开启心灵之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名（3-50字符）"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码（至少6位）"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="昵称（选填）"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              native-type="submit"
              :loading="loading"
              size="large"
              class="register-btn"
            >
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="switch-link">
          已有账号？<router-link to="/login">立即登录</router-link>
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
import { useRouter } from 'vue-router'
import { register } from './api'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
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
.register-bg {
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
.register-container {
  display: flex;
  align-items: center;
  gap: 40px;
  position: relative;
  z-index: 1;
}

/* 左侧装饰 */
.register-deco {
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

/* 注册卡片 */
.register-card {
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
.register-form {
  margin-bottom: 4px;
}

.register-form :deep(.el-input__wrapper) {
  background: var(--paper-warm);
  box-shadow: 0 0 0 1px var(--ink-faint) inset;
  border-radius: 8px;
  padding: 4px 16px;
  transition: all 0.2s ease;
}
.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--ink-mist) inset;
}
.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--ink-dark) inset;
  background: #fff;
}
.register-form :deep(.el-input__inner) {
  color: var(--ink-black);
  font-size: 15px;
}
.register-form :deep(.el-input__prefix) {
  color: var(--ink-mist);
}
.register-form :deep(.el-input__prefix-inner) {
  margin-right: 8px;
}

.register-btn {
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
.register-btn:hover {
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
  .register-deco {
    display: none;
  }
  .register-card {
    width: 340px;
    margin: 0 16px;
    padding: 36px 28px 28px;
  }
  .back-entry {
    top: 16px;
    right: 16px;
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
