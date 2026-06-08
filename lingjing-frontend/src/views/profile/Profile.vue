<template>
  <div class="profile-page">
    <div class="page-container">
      <!-- 用户信息卡片 -->
      <el-card class="user-card">
        <div class="user-info">
          <div class="avatar-wrapper" @click="triggerUpload">
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
            <div v-else class="avatar-letter">{{ nickname.charAt(0) }}</div>
            <div class="avatar-overlay">
              <el-icon :size="18"><CameraFilled /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <div class="info">
            <h2>{{ nickname }}</h2>
            <p>UID: {{ userId }}</p>
          </div>
          <input ref="fileInput" type="file" accept="image/png,image/jpeg,image/jpg" style="display:none" @change="handleFileChange" />
        </div>
      </el-card>

      <!-- 编辑资料 -->
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">编辑资料</span>
            <el-button v-if="!showEditForm" text type="primary" size="small" @click="showEditForm = true">修改</el-button>
          </div>
        </template>
        <div v-if="showEditForm" class="edit-form">
          <el-form :model="form" label-width="70px" class="profile-form">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="输入昵称" maxlength="50" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
              <el-button @click="showEditForm = false">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-else class="edit-info">
          <div class="info-row"><span class="info-label">手机号</span><span class="info-value">{{ form.phone || '未设置' }}</span></div>
        </div>
      </el-card>

      <!-- 统计 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-value">{{ checkInCount }}</div>
            <div class="stat-label">已打卡景点</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-value">{{ unlockedCount }}</div>
            <div class="stat-label">已获得成就</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-value">{{ totalAttractions }}</div>
            <div class="stat-label">总景点数</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 打卡记录 -->
      <el-card class="section-card">
        <template #header>
          <span class="card-title">打卡记录</span>
        </template>
        <div v-if="loadingCheckIns" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        <el-empty v-else-if="checkIns.length === 0" description="还没打卡过景点" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="item in checkIns"
            :key="item.id"
            :timestamp="item.createdAt"
            placement="top"
          >
            <p>{{ getAttractionName(item.attractionId) }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 成就 -->
      <el-card class="section-card">
        <template #header>
          <span class="card-title">成就</span>
        </template>
        <div v-if="loadingAchievements" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else class="achievement-list">
          <div
            v-for="item in achievements"
            :key="item.id"
            class="achievement-item"
            :class="{ unlocked: item.unlocked, equipped: item.unlocked && equippedBadgeId == item.id }"
          >
            <div class="achievement-icon" :class="{ locked: !item.unlocked }">
              {{ item.unlocked ? (item.icon || '🏆') : '🔒' }}
            </div>
            <div class="achievement-info">
              <div class="achievement-name">{{ item.name }}</div>
              <div class="achievement-desc">{{ item.conditionDesc }}</div>
            </div>
            <el-tag v-if="item.unlocked" type="success" size="small">已达成</el-tag>
            <el-tag v-else type="info" size="small">未达成</el-tag>
            <button
              v-if="item.unlocked"
              class="equip-btn"
              :class="{ equipped: equippedBadgeId == item.id }"
              @click="toggleEquip(item)"
            >
              {{ equippedBadgeId == item.id ? '卸下' : '佩戴' }}
            </button>
          </div>
          <div v-if="achievements.length > 0" class="badges-actions">
            <el-button type="primary" size="small" @click="handleBadgeSave">保存修改</el-button>
          </div>
          <el-empty v-if="achievements.length === 0" description="暂无成就" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CameraFilled } from '@element-plus/icons-vue'
import { getCurrentUser, updateProfile, uploadAvatar } from '../auth/api'
import { getMyAchievements, equipAchievement, unequipAchievement } from './api'
import { getMyCheckIns } from '../attraction/checkin'
import { getAttractions } from '../attraction/api'

const router = useRouter()
const fileInput = ref(null)
const saving = ref(false)
const showEditForm = ref(false)

const userId = ref('')
const nickname = ref('访客')
const avatarUrl = ref('')
const checkIns = ref([])
const checkInCount = ref(0)
const achievements = ref([])
const unlockedCount = ref(0)
const totalAttractions = ref(22)
const loadingCheckIns = ref(true)
const loadingAchievements = ref(true)
const attractionMap = ref({})

const form = reactive({
  nickname: '',
  phone: ''
})

const equippedBadgeId = ref(localStorage.getItem('equippedBadgeId') || '')

async function toggleEquip(item) {
  try {
    if (equippedBadgeId.value == item.id) {
      await unequipAchievement()
      equippedBadgeId.value = ''
      localStorage.removeItem('equippedBadgeId')
    } else {
      await equipAchievement(item.id)
      equippedBadgeId.value = String(item.id)
      localStorage.setItem('equippedBadgeId', String(item.id))
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 获取用户信息
  try {
    const user = await getCurrentUser()
    nickname.value = user.nickname || user.username
    userId.value = user.id
    avatarUrl.value = user.avatar || ''
    form.nickname = user.nickname || ''
    form.phone = user.phone || ''

    localStorage.setItem('username', user.username)
    if (user.avatar) localStorage.setItem('avatar', user.avatar)
    if (user.nickname) localStorage.setItem('nickname', user.nickname)
  } catch (e) {
    localStorage.removeItem('token')
    router.push('/login')
    return
  }

  try {
    const attrs = await getAttractions({ page: 1, size: 100 })
    for (const a of attrs.records) {
      attractionMap.value[a.id] = a.name
    }
  } catch (e) {
    // ignore
  }

  try {
    const result = await getMyCheckIns()
    checkIns.value = result
    checkInCount.value = result.length
  } catch (e) {
    // ignore
  } finally {
    loadingCheckIns.value = false
  }

  try {
    const result = await getMyAchievements()
    achievements.value = result
    unlockedCount.value = result.filter(a => a.unlocked).length
  } catch (e) {
    // ignore
  } finally {
    loadingAchievements.value = false
  }
})

function getAttractionName(id) {
  return attractionMap.value[id] || `景点 #${id}`
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
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
    localStorage.setItem('avatar', url)
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
  if (fileInput.value) fileInput.value.value = ''
}

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({
      nickname: form.nickname || null,
      phone: form.phone || null
    })
    nickname.value = form.nickname || nickname.value
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
.page-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ============ User Card ============ */
.user-card {
  text-align: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
  justify-content: center;
  position: relative;
}
.avatar-wrapper {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--ink-faint);
  box-shadow: var(--shadow-soft);
  transition: all 0.3s ease;
  flex-shrink: 0;
}
.avatar-wrapper:hover {
  border-color: var(--ink-mist);
  box-shadow: var(--shadow-warm);
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
  background: linear-gradient(135deg, var(--ink-dark), var(--ink-light));
  color: #fff;
  font-size: 28px;
  font-weight: bold;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #fff;
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.25s ease;
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.user-info .info {
  text-align: left;
}
.user-info h2 {
  margin: 0;
  font-size: 20px;
  color: var(--ink-black);
}
.user-info p {
  margin: 4px 0 0;
  color: var(--ink-light);
  font-size: 13px;
}

/* ============ Edit Form ============ */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.edit-form {
  padding: 4px 0;
}
.edit-form .profile-form {
  max-width: 400px;
}
.edit-info {
  padding: 4px 0;
}
.info-row {
  font-size: 14px;
  color: var(--ink-light);
  line-height: 2;
}
.info-label {
  color: var(--ink-mist);
  margin-right: 12px;
}
.info-value {
  color: var(--ink-black);
}
.card-title {
  font-weight: 600;
  font-size: 16px;
}

/* ============ Stats ============ */
.stats-row {
  margin-bottom: 0 !important;
}
.stat-card {
  text-align: center;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--ink-dark);
}
.stat-label {
  font-size: 14px;
  color: var(--ink-mist);
  margin-top: 4px;
}

/* ============ Achievements ============ */
.achievement-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.achievement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--paper-dark);
  transition: all 0.2s;
  border: 1px solid transparent;
}
.achievement-item.unlocked {
  background: rgba(91, 140, 90, 0.08);
}
.achievement-item.equipped {
  background: #fef7e0 !important;
  border-color: rgba(212, 160, 23, 0.3) !important;
  box-shadow: 0 0 12px rgba(212, 160, 23, 0.08);
}
.achievement-icon {
  font-size: 28px;
  width: 44px;
  text-align: center;
}
.achievement-icon.locked {
  filter: grayscale(1);
  opacity: 0.5;
}
.achievement-info {
  flex: 1;
}
.achievement-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--ink-black);
}
.achievement-desc {
  font-size: 13px;
  color: var(--ink-light);
}
.equip-btn {
  margin-left: 4px;
  padding: 3px 12px;
  border-radius: 4px;
  border: 1px solid rgba(212, 160, 23, 0.2);
  background: rgba(212, 160, 23, 0.06);
  color: #b8860b;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  font-family: inherit;
}
.equip-btn:hover {
  background: rgba(212, 160, 23, 0.15);
  border-color: rgba(212, 160, 23, 0.35);
}
.equip-btn.equipped {
  background: rgba(212, 160, 23, 0.12);
  border-color: rgba(212, 160, 23, 0.3);
  color: #8b6914;
}
.badges-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  margin-top: 4px;
}

.loading-container {
  padding: 20px;
}
.section-card {
  margin-top: 0;
}
</style>
