<template>
  <div class="home-root">
    <div class="zen-home">
      <!-- ===== 远山入画 - Hero ===== -->
      <section class="hero-section">
      <!-- 背景轮播 -->
      <div v-if="banners.length > 0" class="hero-carousel-wrapper">
        <el-carousel
          height="100%"
          :interval="5000"
          :autoplay="true"
          indicator-position="bottom"
          arrow="never"
          class="hero-carousel"
        >
          <el-carousel-item v-for="b in banners" :key="b.id">
            <div class="hero-slide" :style="{ backgroundImage: `url(${b.imageUrl})` }" />
          </el-carousel-item>
        </el-carousel>
        <div class="hero-overlay" />
      </div>
      <!-- 无轮播图时的默认背景 -->
      <div v-else class="hero-bg">
        <div class="hero-mist hero-mist-1" />
        <div class="hero-mist hero-mist-2" />
      </div>
      <div class="hero-content" :class="{ 'hero-content--overlay': banners.length > 0 }">
        <p class="hero-surtitle">无锡 · 灵山胜境</p>
        <h1 class="hero-title">
          <span class="hero-title-line">灵境智游</span>
        </h1>
        <p class="hero-subtitle">一花一世界 · 一叶一菩提</p>
        <div class="hero-actions">
          <router-link to="/attractions" class="hero-btn-primary">
            <el-icon><MapLocation /></el-icon>
            探索景点
          </router-link>
          <router-link to="/chat" class="hero-btn-secondary">
            <el-icon><ChatDotRound /></el-icon>
            遇见灵宝
          </router-link>
        </div>
      </div>
      <div class="scroll-indicator">
        <span class="scroll-dot" />
      </div>
    </section>

    <!-- ===== 入景 - Quick Entries ===== -->
    <section class="entries-section">
      <div class="section-label">
        <span class="label-line" />
        <span class="label-text">入景</span>
        <span class="label-line" />
      </div>
      <div class="entries-grid">
        <div class="zen-entry" @click="router.push('/attractions')">
          <div class="entry-icon-box" style="--accent: #5b8c5a">
            <el-icon :size="26"><MapLocation /></el-icon>
          </div>
          <span class="entry-label">探索景点</span>
          <span class="entry-desc">22 处景点全景导览</span>
        </div>
        <div class="zen-entry" @click="router.push('/chat')">
          <div class="entry-icon-box" style="--accent: #b8860b">
            <el-icon :size="26"><ChatDotRound /></el-icon>
          </div>
          <span class="entry-label">智能导游</span>
          <span class="entry-desc">数字人灵宝为您讲解</span>
        </div>
        <div class="zen-entry" @click="router.push('/routes')">
          <div class="entry-icon-box" style="--accent: #c23a2b">
            <el-icon :size="26"><TrendCharts /></el-icon>
          </div>
          <span class="entry-label">推荐路线</span>
          <span class="entry-desc">精选游览行程规划</span>
        </div>
        <div v-if="loggedIn" class="zen-entry" @click="router.push('/checkin')">
          <div class="entry-icon-box" style="--accent: #3d3730">
            <el-icon :size="26"><MapLocation /></el-icon>
          </div>
          <span class="entry-label">景点打卡</span>
          <span class="entry-desc">收集足迹点亮徽章</span>
        </div>
      </div>
    </section>

    <!-- ===== 山水 - Intro ===== -->
    <section class="intro-section">
      <div class="section-label">
        <span class="label-line" />
        <span class="label-text">山水</span>
        <span class="label-line" />
      </div>
      <div class="intro-card-zen">
        <div class="intro-card-deco" />
        <h2 class="intro-heading">灵山胜境</h2>
        <p class="intro-subheading">太湖之畔的佛国胜境</p>
        <div class="intro-body">
          <p>灵山胜境坐落于无锡太湖国家旅游度假区，是一处集佛教文化、艺术博览、自然风光于一体的国家 5A 级旅游景区。景区由灵山胜境和拈花湾禅意小镇两大区域组成，拥有灵山大佛、灵山梵宫、九龙灌浴等 22 处标志性景点。</p>
          <p>在这里，您可以感受佛教文化的庄严与厚重，体验禅意生活的宁静与美好。灵境智游，带您开启一段心灵之旅。</p>
        </div>
      </div>
    </section>

    <!-- ===== 云游 - Stats ===== -->
    <section class="stats-section">
      <div class="section-label">
        <span class="label-line" />
        <span class="label-text">云游</span>
        <span class="label-line" />
      </div>
      <div class="stats-grid">
        <div class="zen-stat">
          <span class="stat-num">22</span>
          <span class="stat-unit">处</span>
          <span class="stat-label">精品景点</span>
        </div>
        <div class="zen-stat">
          <span class="stat-num">3</span>
          <span class="stat-unit">条</span>
          <span class="stat-label">推荐路线</span>
        </div>
        <div class="zen-stat">
          <span class="stat-num">24</span>
          <span class="stat-unit">h</span>
          <span class="stat-label">智能导游</span>
        </div>
        <div class="zen-stat">
          <span class="stat-num">9</span>
          <span class="stat-unit">枚</span>
          <span class="stat-label">成就徽章</span>
        </div>
      </div>
    </section>

    <!-- 底部留白 -->
    <div class="bottom-space" />
  </div>

  <!-- 未登录提示弹窗 -->
  <el-dialog
    v-model="showLoginPrompt"
    width="420px"
    :close-on-click-modal="false"
    :show-close="false"
    class="zen-dialog"
  >
    <div class="prompt-content">
      <div class="prompt-seal">灵</div>
      <h3 class="prompt-title">欢迎来到灵境智游</h3>
      <p class="prompt-desc">登录后可获得完整游览体验：智能导游 · 路线规划 · 景点打卡 · 成就徽章</p>
      <div class="prompt-actions">
        <router-link to="/login" class="prompt-btn-primary">
          <el-icon><User /></el-icon>
          去登录
        </router-link>
        <button class="prompt-btn-text" @click="dismissPrompt">
          稍后再说
        </button>
      </div>
    </div>
  </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getBanners } from '../attraction/api'

const router = useRouter()
const loggedIn = computed(() => !!localStorage.getItem('token'))

const showLoginPrompt = ref(false)
const banners = ref([])

function dismissPrompt() {
  showLoginPrompt.value = false
  sessionStorage.setItem('loginPromptDismissed', '1')
}

onMounted(async () => {
  // 加载轮播图
  try {
    banners.value = await getBanners()
  } catch (e) {
    // 静默失败，没有轮播图也不影响页面
  }

  await nextTick()
  if (!loggedIn.value && !sessionStorage.getItem('loginPromptDismissed')) {
    setTimeout(() => { showLoginPrompt.value = true }, 600)
  }
})
</script>

<style scoped>
/* ===== 布局 ===== */
.zen-home {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 28px;
}

.label-line {
  display: block;
  width: 32px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(168, 160, 149, 0.4), transparent);
}

.label-text {
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--ink-mist, #a8a095);
  letter-spacing: 6px;
}

/* ===== Hero Section ===== */
.hero-section {
  position: relative;
  min-height: 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 -24px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(44, 44, 44, 0.04) 0%,
    rgba(245, 240, 232, 1) 70%
  );
}

/* 背景轮播 */
.hero-carousel-wrapper {
  position: absolute;
  inset: 0;
}

.hero-carousel,
.hero-carousel :deep(.el-carousel__container),
.hero-carousel :deep(.el-carousel__item) {
  height: 100% !important;
}

.hero-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(44, 44, 44, 0.6) 0%,
    rgba(44, 44, 44, 0.3) 50%,
    rgba(44, 44, 44, 0.05) 100%
  );
  pointer-events: none;
}

/* 轮播指示器 — 半透明白色小圆点 */
.hero-carousel-wrapper :deep(.el-carousel__indicator) {
  padding: 6px 4px;
}

.hero-carousel-wrapper :deep(.el-carousel__button) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  opacity: 1;
}

.hero-carousel-wrapper :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: rgba(255, 255, 255, 0.85);
  width: 10px;
  height: 10px;
}

/* 无轮播图时的降级 mist 效果 */
.hero-mist {
  position: absolute;
  inset: 0;
  opacity: 0.5;
}

.hero-mist-1 {
  background: radial-gradient(ellipse 60% 40% at 30% 30%, rgba(168, 160, 149, 0.12) 0%, transparent 70%);
}

.hero-mist-2 {
  background: radial-gradient(ellipse 50% 30% at 70% 20%, rgba(180, 160, 140, 0.08) 0%, transparent 60%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 60px 24px 80px;
}

.hero-surtitle {
  font-family: 'Noto Serif SC', serif;
  font-size: 13px;
  color: var(--ink-mist, #a8a095);
  letter-spacing: 6px;
  margin: 0 0 20px;
}

.hero-title {
  margin: 0 0 16px;
  font-family: 'Noto Serif SC', serif;
  font-size: 56px;
  font-weight: 900;
  color: var(--ink-black, #2c2c2c);
  letter-spacing: 12px;
  line-height: 1.2;
}

.hero-title-line {
  display: block;
}

.hero-subtitle {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  color: var(--ink-mist, #a8a095);
  letter-spacing: 4px;
  margin: 0 0 36px;
}

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.hero-btn-primary,
.hero-btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.hero-btn-primary {
  background: var(--ink-dark, #3d3730);
  color: var(--paper-warm, #f5f0e8);
}

.hero-btn-primary:hover {
  background: var(--ink-black, #2c2c2c);
  box-shadow: 0 4px 20px rgba(44, 44, 44, 0.2);
  transform: translateY(-2px);
}

.hero-btn-secondary {
  background: transparent;
  color: var(--ink-dark, #3d3730);
  border: 1.5px solid rgba(168, 160, 149, 0.3);
}

.hero-btn-secondary:hover {
  border-color: var(--ink-dark, #3d3730);
  background: rgba(61, 55, 48, 0.04);
  transform: translateY(-2px);
}

/* ===== Hero Overlay (有轮播背景时) ===== */
.hero-content--overlay .hero-surtitle {
  color: rgba(255, 255, 255, 0.7);
}

.hero-content--overlay .hero-title {
  color: #fff;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
}

.hero-content--overlay .hero-subtitle {
  color: rgba(255, 255, 255, 0.65);
  text-shadow: 0 1px 10px rgba(0, 0, 0, 0.2);
}

.hero-content--overlay .hero-btn-primary {
  background: rgba(255, 255, 255, 0.95);
  color: #2c2c2c;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-content--overlay .hero-btn-primary:hover {
  background: #fff;
  box-shadow: 0 6px 28px rgba(0, 0, 0, 0.25);
  transform: translateY(-2px);
}

.hero-content--overlay .hero-btn-secondary {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(4px);
}

.hero-content--overlay .hero-btn-secondary:hover {
  border-color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.hero-content--overlay .scroll-dot {
  background: rgba(255, 255, 255, 0.6);
}

/* Scroll Indicator */
.scroll-indicator {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  opacity: 0.5;
  animation: float-down 2s ease-in-out infinite;
}

.scroll-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--ink-mist, #a8a095);
}

@keyframes float-down {
  0%, 100% { transform: translateX(-50%) translateY(0); opacity: 0.5; }
  50% { transform: translateX(-50%) translateY(6px); opacity: 0.25; }
}

/* ===== Entries ===== */
.entries-section {
  padding: 40px 0 48px;
}

.entries-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.zen-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 32px 16px 28px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(168, 160, 149, 0.12);
  cursor: pointer;
  transition: all 0.35s ease;
  text-align: center;
}

.zen-entry:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(168, 160, 149, 0.25);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.06);
  transform: translateY(-4px);
}

.entry-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--accent) 10%, transparent);
  color: var(--accent);
  transition: all 0.3s ease;
}

.zen-entry:hover .entry-icon-box {
  background: color-mix(in srgb, var(--accent) 18%, transparent);
  transform: scale(1.05);
}

.entry-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-dark, #3d3730);
  letter-spacing: 1px;
}

.entry-desc {
  font-size: 12px;
  color: var(--ink-mist, #a8a095);
  letter-spacing: 0.5px;
}

/* ===== Intro ===== */
.intro-section {
  padding: 0 0 48px;
}

.intro-card-zen {
  position: relative;
  padding: 40px 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(168, 160, 149, 0.12);
  text-align: center;
  overflow: hidden;
}

.intro-card-deco {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(168, 160, 149, 0.3), transparent);
}

.intro-heading {
  font-family: 'Noto Serif SC', serif;
  font-size: 26px;
  font-weight: 700;
  color: var(--ink-black, #2c2c2c);
  margin: 0 0 6px;
  letter-spacing: 8px;
}

.intro-subheading {
  font-size: 14px;
  color: var(--ink-mist, #a8a095);
  margin: 0 0 24px;
  letter-spacing: 2px;
}

.intro-body p {
  font-size: 14px;
  color: var(--ink-light, #6b6358);
  line-height: 1.9;
  margin: 0 0 14px;
  max-width: 640px;
  margin-left: auto;
  margin-right: auto;
}

.intro-body p:last-child {
  margin-bottom: 0;
}

/* ===== Stats ===== */
.stats-section {
  padding: 0 0 48px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.zen-stat {
  text-align: center;
  padding: 28px 16px 24px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(168, 160, 149, 0.08);
  transition: all 0.3s ease;
}

.zen-stat:hover {
  background: rgba(255, 255, 255, 0.6);
  border-color: rgba(168, 160, 149, 0.18);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.stat-num {
  display: inline;
  font-family: 'Noto Serif SC', serif;
  font-size: 36px;
  font-weight: 700;
  color: var(--ink-dark, #3d3730);
  line-height: 1;
}

.stat-unit {
  display: inline;
  font-size: 14px;
  color: var(--ink-mist, #a8a095);
  margin-left: 2px;
}

.stat-label {
  display: block;
  font-size: 13px;
  color: var(--ink-mist, #a8a095);
  margin-top: 8px;
  letter-spacing: 1px;
}

.bottom-space {
  height: 32px;
}

/* 登录提示弹窗 */
:deep(.zen-dialog) {
  background: var(--paper-warm);
  border: 1px solid var(--ink-faint);
  border-radius: 12px;
  box-shadow: var(--shadow-float);
  padding: 0;
  overflow: hidden;
}
:deep(.zen-dialog .el-dialog__body) {
  padding: 40px 36px 32px;
}

.prompt-content {
  text-align: center;
}

.prompt-seal {
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

.prompt-title {
  font-family: var(--font-display);
  font-size: 20px;
  color: var(--ink-black);
  margin: 0 0 12px;
  letter-spacing: 4px;
}

.prompt-desc {
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink-light);
  margin: 0 0 28px;
}

.prompt-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.prompt-btn-primary {
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
  text-decoration: none;
  transition: all 0.25s ease;
}

.prompt-btn-primary:hover {
  background: var(--ink-black);
  box-shadow: 0 4px 16px rgba(44, 44, 44, 0.15);
  transform: translateY(-1px);
}

.prompt-btn-text {
  background: none;
  border: none;
  font-size: 13px;
  color: var(--ink-mist);
  cursor: pointer;
  padding: 4px 12px;
  transition: color 0.2s ease;
  font-family: var(--font-body);
}

.prompt-btn-text:hover {
  color: var(--ink-light);
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
    letter-spacing: 8px;
  }

  .entries-grid,
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .intro-card-zen {
    padding: 28px 20px;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>
