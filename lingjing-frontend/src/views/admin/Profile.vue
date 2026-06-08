<template>
  <div class="profile-page">
    <h2 class="page-title">个人信息</h2>

    <!-- 头像 + 基本信息 -->
    <el-row :gutter="24">
      <!-- 头像卡片 -->
      <el-col :span="6">
        <el-card shadow="never" class="avatar-card">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
              <span v-else class="avatar-letter">{{ displayName.charAt(0) }}</span>
              <div class="avatar-overlay" @click="triggerUpload">
                <el-icon :size="22"><CameraFilled /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <p class="avatar-hint">支持 JPG / PNG，建议 200x200</p>
          </div>
          <div class="user-meta">
            <div class="meta-name">{{ displayName }}</div>
            <div class="meta-uid">UID: {{ userId }}</div>
          </div>
          <!-- 隐藏的上传组件 -->
          <input ref="fileInput" type="file" accept="image/png,image/jpeg,image/jpg" style="display: none" @change="handleFileChange" />
        </el-card>
      </el-col>

      <!-- 基本信息 -->
      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">基本资料</span>
          </template>
          <el-form :model="form" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input :model-value="username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="输入昵称" maxlength="50" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的徽章 -->
    <el-card shadow="never" class="badges-card">
      <template #header>
        <div class="badges-header">
          <span class="section-title">我的徽章</span>
          <el-tag type="info" size="small">{{ unlockedCount }} / {{ badges.length }} 已解锁</el-tag>
        </div>
      </template>
      <div v-if="loadingBadges" class="badges-loading">
        <el-skeleton :rows="2" animated />
      </div>
      <div v-else-if="badges.length === 0" class="badges-empty">
        暂无成就徽章
      </div>
      <div v-else class="badges-grid">
        <div
          v-for="badge in badges"
          :key="badge.id"
          class="badge-item"
          :class="{ unlocked: badge.unlocked, equipped: badge.unlocked && equippedBadgeId == badge.id }"
        >
          <div class="badge-icon-wrapper">
            <span class="badge-icon">{{ badge.unlocked ? (badge.icon || '🏅') : '🔒' }}</span>
            <div v-if="!badge.unlocked" class="badge-lock-overlay" />
            <div v-if="badge.unlocked && equippedBadgeId == badge.id" class="badge-equipped-badge">已佩戴</div>
          </div>
          <div class="badge-name">{{ badge.name }}</div>
          <div class="badge-desc">{{ badge.conditionDesc }}</div>
          <button
            v-if="badge.unlocked"
            class="badge-equip-btn"
            :class="{ equipped: equippedBadgeId == badge.id }"
            @click="toggleEquip(badge)"
          >
            {{ equippedBadgeId == badge.id ? '卸下' : '佩戴' }}
          </button>
        </div>
      </div>
      <div v-if="badges.length > 0" class="badges-actions">
        <el-button type="primary" @click="handleBadgeSave">保存修改</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CameraFilled } from '@element-plus/icons-vue'
import { getCurrentUser, updateProfile, uploadAvatar, getMyAchievements, equipAchievement, unequipAchievement } from '../auth/api'

const fileInput = ref(null)
const saving = ref(false)
const loadingBadges = ref(true)

const userId = ref('')
const username = ref('')
const displayName = ref('')
const avatarUrl = ref('')

const form = reactive({
  nickname: '',
  phone: ''
})

const badges = ref([])

// 佩戴徽章
const equippedBadgeId = ref(localStorage.getItem('equippedBadgeId') || '')

function toggleEquip(badge) {
  if (equippedBadgeId.value == badge.id) {
    unequipAchievement().then(() => {
      equippedBadgeId.value = ''
      localStorage.removeItem('equippedBadgeId')
    }).catch(e => ElMessage.error(e.message || '操作失败'))
  } else {
    equipAchievement(badge.id).then(() => {
      equippedBadgeId.value = String(badge.id)
      localStorage.setItem('equippedBadgeId', String(badge.id))
    }).catch(e => ElMessage.error(e.message || '操作失败'))
  }
}

const unlockedCount = computed(() => badges.value.filter(b => b.unlocked).length)

onMounted(async () => {
  try {
    const user = await getCurrentUser()
    userId.value = user.id
    username.value = user.username
    displayName.value = user.nickname || user.username
    avatarUrl.value = user.avatar || ''
    form.nickname = user.nickname || ''
    form.phone = user.phone || ''

    // 同步更新 localStorage（其他页面使用）
    localStorage.setItem('username', user.username)
    if (user.avatar) localStorage.setItem('avatar', user.avatar)
    if (user.nickname) localStorage.setItem('nickname', user.nickname)
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  }

  // 加载徽章
  try {
    const result = await getMyAchievements()
    badges.value = result || []
  } catch (e) {
    console.error('加载徽章失败', e)
  } finally {
    loadingBadges.value = false
  }
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // 验证文件类型和大小
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 5MB')
    return
  }

  try {
    const url = await uploadAvatar(file)
    avatarUrl.value = url
    // 保存到 localStorage 供其他页面使用
    localStorage.setItem('avatar', url)
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }

  // 重置 input 以允许重复上传同一文件
  if (fileInput.value) fileInput.value.value = ''
}

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({
      nickname: form.nickname || null,
      phone: form.phone || null
    })
    displayName.value = form.nickname || username.value
    if (form.nickname) localStorage.setItem('nickname', form.nickname)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleBadgeSave() {
  ElMessage.success('徽章已更新')
}
</script>

<style scoped>
.profile-page {
  padding: 0;
}

/* ============ Avatar Card ============ */
.avatar-card {
  text-align: center;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}
.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid rgba(46, 125, 246, 0.2);
  box-shadow: 0 0 20px rgba(46, 125, 246, 0.1);
  transition: all 0.3s ease;
}
.avatar-wrapper:hover {
  border-color: var(--accent-blue);
  box-shadow: 0 0 30px rgba(46, 125, 246, 0.2);
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-letter {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-cyan));
  font-family: var(--font-display);
  font-size: 44px;
  font-weight: 700;
  color: #fff;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.25s ease;
  backdrop-filter: blur(2px);
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.avatar-hint {
  font-size: 12px;
  color: var(--text-dim);
  margin: 0;
}

.user-meta {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(46, 125, 246, 0.08);
}
.meta-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  font-family: var(--font-display);
  letter-spacing: 0.5px;
}
.meta-uid {
  font-size: 12px;
  color: var(--text-dim);
  margin-top: 4px;
}

/* ============ Form ============ */
.profile-form {
  max-width: 480px;
}
.profile-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(46, 125, 246, 0.12);
  box-shadow: none !important;
  border-radius: 8px;
  padding: 4px 16px;
  transition: all 0.2s ease;
}
.profile-form :deep(.el-input__wrapper:hover) {
  border-color: var(--accent-blue);
  box-shadow: 0 0 0 1px rgba(46, 125, 246, 0.08) !important;
}
.profile-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent-blue);
  box-shadow: 0 0 0 2px rgba(46, 125, 246, 0.12) !important;
  background: rgba(255, 255, 255, 0.06);
}
.profile-form :deep(.el-input__inner) {
  color: var(--text-primary);
  font-size: 15px;
}
.profile-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}
.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(46, 125, 246, 0.06);
}
.profile-form :deep(.el-input.is-disabled .el-input__inner) {
  color: var(--text-muted);
  -webkit-text-fill-color: var(--text-muted);
}

/* ============ Section Title ============ */
.section-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

/* ============ Badges ============ */
.badges-card {
  margin-top: 24px;
}
.badges-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.badges-loading,
.badges-empty {
  padding: 40px 0;
  text-align: center;
  color: var(--text-dim);
  font-size: 14px;
}
.badges-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}
.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px 16px;
  border-radius: 12px;
  border: 1px solid rgba(46, 125, 246, 0.08);
  background: rgba(255, 255, 255, 0.02);
  transition: all 0.3s ease;
  cursor: default;
  position: relative;
}
.badge-item.unlocked {
  border-color: rgba(46, 125, 246, 0.2);
  background: rgba(46, 125, 246, 0.04);
  box-shadow: 0 0 16px rgba(46, 125, 246, 0.08);
}
.badge-item.unlocked:hover {
  border-color: rgba(46, 125, 246, 0.35);
  box-shadow: 0 0 24px rgba(46, 125, 246, 0.15);
  transform: translateY(-2px);
}
.badge-item:not(.unlocked) {
  opacity: 0.55;
  filter: grayscale(0.6);
}
.badge-item:not(.unlocked):hover {
  opacity: 0.7;
}
.badge-icon-wrapper {
  position: relative;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.badge-icon {
  font-size: 32px;
  line-height: 1;
}
.badge-lock-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 50%;
}
.badge-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
  line-height: 1.3;
}
.badge-desc {
  font-size: 11px;
  color: var(--text-dim);
  text-align: center;
  line-height: 1.4;
}
.badge-equipped-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: linear-gradient(135deg, #d4a017, #f5d742);
  color: #1a1a2e;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 8px;
  letter-spacing: 0.5px;
  box-shadow: 0 0 10px rgba(212, 160, 23, 0.5);
  line-height: 1.5;
}
.badge-equip-btn {
  margin-top: 4px;
  padding: 4px 14px;
  border-radius: 6px;
  border: 1px solid rgba(255, 215, 0, 0.2);
  background: rgba(255, 215, 0, 0.06);
  color: #d4a017;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 0.5px;
  font-family: inherit;
}
.badge-equip-btn:hover {
  background: rgba(255, 215, 0, 0.15);
  border-color: rgba(255, 215, 0, 0.35);
  box-shadow: 0 0 12px rgba(255, 215, 0, 0.1);
}
.badge-equip-btn.equipped {
  background: rgba(255, 215, 0, 0.12);
  border-color: rgba(255, 215, 0, 0.3);
  color: #b8860b;
}
.badge-item.equipped {
  border-color: rgba(255, 215, 0, 0.35) !important;
  background: rgba(255, 215, 0, 0.05) !important;
  box-shadow: 0 0 24px rgba(255, 215, 0, 0.12) !important;
}
.badges-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid rgba(46, 125, 246, 0.08);
  margin-top: 24px;
}
.badges-actions .el-button {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-cyan));
  border: none;
  border-radius: 8px;
  padding: 8px 28px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 0 16px rgba(46, 125, 246, 0.2);
}
</style>
