<template>
  <div class="dh-wrapper" :style="{ width: size + 'px', height: size + 'px' }">
    <div class="dh-body">
      <!-- 身体/衣服 -->
      <div class="dh-collar">
        <div class="dh-collar-left"></div>
        <div class="dh-collar-right"></div>
      </div>

      <!-- 头 -->
      <div class="dh-head" :class="{ talking: talking }">
        <!-- 头发 - 刘海 -->
        <div class="dh-bangs">
          <div class="dh-bang dh-bang-1"></div>
          <div class="dh-bang dh-bang-2"></div>
          <div class="dh-bang dh-bang-3"></div>
          <div class="dh-bang dh-bang-4"></div>
          <div class="dh-bang dh-bang-5"></div>
        </div>

        <!-- 侧发 -->
        <div class="dh-side-hair left"></div>
        <div class="dh-side-hair right"></div>

        <!-- 小花发饰 -->
        <div class="dh-flower">
          <div class="dh-petal" v-for="i in 5" :key="i" :style="{ transform: 'rotate(' + (i * 72) + 'deg)' }"></div>
          <div class="dh-flower-center"></div>
        </div>

        <!-- 眉毛 -->
        <div class="dh-eyebrow left"></div>
        <div class="dh-eyebrow right"></div>

        <!-- 眼睛 -->
        <div class="dh-eyes">
          <div class="dh-eye left">
            <div class="dh-eye-white">
              <div class="dh-iris">
                <div class="dh-pupil"></div>
                <div class="dh-highlight h1"></div>
                <div class="dh-highlight h2"></div>
              </div>
            </div>
          </div>
          <div class="dh-eye right">
            <div class="dh-eye-white">
              <div class="dh-iris">
                <div class="dh-pupil"></div>
                <div class="dh-highlight h1"></div>
                <div class="dh-highlight h2"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 腮红 -->
        <div class="dh-cheek left"></div>
        <div class="dh-cheek right"></div>

        <!-- 鼻子 -->
        <div class="dh-nose"></div>

        <!-- 嘴巴 -->
        <div class="dh-mouth" :class="{ open: talking }"></div>
      </div>
    </div>

    <!-- 影子 -->
    <div class="dh-shadow"></div>
  </div>
</template>

<script setup>
defineProps({
  talking: { type: Boolean, default: false },
  size: { type: Number, default: 200 }
})
</script>

<style scoped>
.dh-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  position: relative;
}

/* ---- 影子 ---- */
.dh-shadow {
  width: 40%;
  height: 6%;
  background: rgba(0,0,0,0.08);
  border-radius: 50%;
  margin-top: 2%;
  animation: shadow-pulse 3s ease-in-out infinite;
}

@keyframes shadow-pulse {
  0%, 100% { transform: scaleX(1); opacity: 0.6; }
  50%      { transform: scaleX(0.85); opacity: 0.35; }
}

/* ---- 身体 ---- */
.dh-body {
  width: 78%;
  height: 82%;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: bob 3.2s ease-in-out infinite;
}

.talking ~ .dh-body,
.dh-body:has(~ .dh-shadow) { /* talking 类在 head 上，此处用 sibling 逻辑表达不太方便 */ }

.dh-body { animation: bob 3.2s ease-in-out infinite; }
.dh-body:has(+ .dh-shadow) { animation: bob 3.2s ease-in-out infinite; }

@keyframes bob {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-6px); }
}

/* ---- 头 ---- */
.dh-head {
  width: 100%;
  aspect-ratio: 1 / 1.05;
  border-radius: 50% 50% 48% 48%;
  background: linear-gradient(170deg, #ffe4c9 0%, #fdceb0 40%, #f5b896 100%);
  position: relative;
  overflow: hidden;
  box-shadow:
    0 6px 24px rgba(210,150,110,0.35),
    inset 0 -6px 12px rgba(200,120,80,0.12),
    inset 0 2px 6px rgba(255,255,255,0.25);
}

/* ---- 刘海 ---- */
.dh-bangs {
  position: absolute;
  top: -2%;
  left: 0;
  width: 100%;
  height: 38%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  z-index: 3;
}
.dh-bang {
  background: linear-gradient(180deg, #3d2517 0%, #5a3828 60%, #6b4330 100%);
  border-radius: 0 0 50% 50%;
  position: relative;
}
.dh-bang-1 { width: 16%; height: 70%; margin-top: 0; border-radius: 0 0 50% 40%; }
.dh-bang-2 { width: 20%; height: 85%; margin-top: 0; margin-left: -3%; border-radius: 0 0 48% 46%; }
.dh-bang-3 { width: 22%; height: 90%; margin-top: 0; margin-left: -2%; border-radius: 0 0 45% 48%; z-index: 4; }
.dh-bang-4 { width: 18%; height: 78%; margin-top: 0; margin-left: -3%; border-radius: 0 0 48% 40%; }
.dh-bang-5 { width: 14%; height: 60%; margin-top: 0; margin-left: -4%; border-radius: 0 0 50% 36%; }

/* ---- 侧发 ---- */
.dh-side-hair {
  position: absolute;
  top: 0;
  width: 10%;
  height: 60%;
  background: linear-gradient(180deg, #3d2517 0%, #5a3828 50%, #724a35 100%);
  z-index: 2;
  border-radius: 0 0 30% 30%;
}
.dh-side-hair.left  { left: -2%; border-radius: 0 0 20% 40%; }
.dh-side-hair.right { right: -2%; border-radius: 0 0 40% 20%; }

/* ---- 小花发饰 ---- */
.dh-flower {
  position: absolute;
  top: 22%;
  right: 12%;
  width: 18%;
  aspect-ratio: 1;
  z-index: 5;
  animation: flower-sway 2.5s ease-in-out infinite;
}
@keyframes flower-sway {
  0%, 100% { transform: rotate(-3deg); }
  50%      { transform: rotate(3deg); }
}
.dh-petal {
  position: absolute;
  top: 0; left: 50%;
  width: 40%; height: 50%;
  background: #ffb3c6;
  border-radius: 50%;
  transform-origin: 50% 100%;
  opacity: 0.85;
}
.dh-petal:nth-child(1) { transform: rotate(0deg); }
.dh-petal:nth-child(2) { transform: rotate(72deg); }
.dh-petal:nth-child(3) { transform: rotate(144deg); }
.dh-petal:nth-child(4) { transform: rotate(216deg); }
.dh-petal:nth-child(5) { transform: rotate(288deg); }
.dh-flower-center {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%,-50%);
  width: 35%; aspect-ratio: 1;
  background: #ffe066;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

/* ---- 眉毛 ---- */
.dh-eyebrow {
  position: absolute;
  top: 34%;
  width: 16%;
  height: 2.5%;
  background: #5a3828;
  border-radius: 40%;
  z-index: 2;
}
.dh-eyebrow.left  { left: 22%; }
.dh-eyebrow.right { right: 22%; }

/* ---- 眼睛 ---- */
.dh-eyes {
  position: absolute;
  top: 36%;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 23%;
  z-index: 2;
}
.dh-eye {
  width: 22%;
  aspect-ratio: 1.05;
  animation: dh-blink 4.5s ease-in-out infinite;
}
.dh-eye-white {
  width: 100%; height: 100%;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 2px 3px rgba(0,0,0,0.06);
  overflow: hidden;
}
.dh-iris {
  width: 78%; aspect-ratio: 1;
  background: radial-gradient(circle at 45% 40%, #5a8a5c, #2d5a30);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.dh-pupil {
  width: 48%; aspect-ratio: 1;
  background: #0f0f0f;
  border-radius: 50%;
}
.dh-highlight {
  position: absolute;
  background: white;
  border-radius: 50%;
}
.dh-highlight.h1 {
  top: 16%; right: 22%;
  width: 28%; aspect-ratio: 1;
}
.dh-highlight.h2 {
  top: 35%; right: 10%;
  width: 14%; aspect-ratio: 1;
  opacity: 0.7;
}

@keyframes dh-blink {
  0%, 94%, 100% { transform: scaleY(1); }
  96% { transform: scaleY(0.06); }
}

/* ---- 腮红 ---- */
.dh-cheek {
  position: absolute;
  top: 47%;
  width: 18%;
  aspect-ratio: 1;
  background: radial-gradient(circle, rgba(255,160,160,0.32) 0%, rgba(255,180,180,0.08) 70%, transparent 100%);
  border-radius: 50%;
  z-index: 1;
}
.dh-cheek.left  { left: 12%; }
.dh-cheek.right { right: 12%; }

/* ---- 鼻子 ---- */
.dh-nose {
  position: absolute;
  top: 46%;
  left: 50%;
  transform: translateX(-50%);
  width: 4%; aspect-ratio: 1.3;
  background: #e0a080;
  border-radius: 50%;
  z-index: 2;
  opacity: 0.5;
}

/* ---- 嘴巴 ---- */
.dh-mouth {
  position: absolute;
  top: 52%;
  left: 50%;
  transform: translateX(-50%);
  width: 14%;
  height: 5%;
  border-bottom: 2.5px solid #d48585;
  border-radius: 0 0 55% 55%;
  z-index: 2;
  transition: all 0.25s ease;
}
.dh-mouth.open {
  width: 18%;
  height: 10%;
  background: #e89292;
  border-bottom: none;
  border-radius: 50%;
  box-shadow: inset 0 2px 3px rgba(0,0,0,0.15);
}

/* ---- 衣领 ---- */
.dh-collar {
  width: 60%;
  height: 12%;
  margin-top: -2%;
  display: flex;
  justify-content: center;
  gap: 8%;
  z-index: 1;
  position: relative;
}
.dh-collar-left, .dh-collar-right {
  width: 35%;
  height: 100%;
  background: linear-gradient(160deg, #e8f0fe 0%, #d0dff5 100%);
  border-radius: 0 0 40% 40%;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.dh-collar-left  { border-radius: 0 0 30% 50%; }
.dh-collar-right { border-radius: 0 0 50% 30%; }
</style>
