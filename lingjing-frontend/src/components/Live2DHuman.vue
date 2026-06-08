<template>
  <div
    class="l2d-wrapper"
    :class="{
      floating: floating,
      dragging: dragging,
      loaded: loaded,
      interactive: interactive,
      'scale-up': modelScale > 1,
    }"
    :style="wrapperStyle"
    @pointerdown="onPointerDown"
  >
    <!-- Live2D 画布 -->
    <div
      ref="containerRef"
      class="l2d-canvas"
      :class="{ loaded: loaded }"
    />
    <!-- 交互气泡（全局浮动模式下 hover 显示"问问灵宝"） -->
    <div
      v-if="interactive"
      class="l2d-bubble"
      @click.stop="onBubbleClick"
    >
      问问灵宝
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick, reactive } from 'vue'
import { useLive2D } from '../composables/useLive2D'

const props = defineProps({
  talking: { type: Boolean, default: false },
  size: { type: Number, default: 200 },
  expression: { type: String, default: 'idle' },
  modelPath: { type: String, default: '/models/live2d/haru/haru_greeter_t03.model3.json' },
  floating: { type: Boolean, default: false },
  draggable: { type: Boolean, default: false },
  interactive: { type: Boolean, default: false },
  modelScale: { type: Number, default: 1.0 },
  initX: { type: Number, default: undefined },
  initY: { type: Number, default: undefined },
})

const emit = defineEmits(['ready', 'error', 'drag-start', 'drag-end', 'click-bubble'])

const STORAGE_KEY = 'l2d_position'

const containerRef = ref(null)
const loaded = ref(false)
const dragging = ref(false)

const pos = reactive({ x: props.initX, y: props.initY })

const { init, resize, setExpression, startTalking, stopTalking, destroy } = useLive2D()

const wrapperStyle = computed(() => {
  const style = { width: props.size + 'px', height: props.size + 'px' }
  if (props.floating) {
    if (pos.x !== undefined) {
      style.left = pos.x + 'px'
    } else {
      style.right = '24px'
    }
    if (pos.y !== undefined) {
      style.top = pos.y + 'px'
    } else {
      style.top = '50%'
      style.transform = 'translateY(-50%)'
    }
  }
  return style
})

// ---- 鼠标 / 触摸拖拽（仅拖拽手柄触发） ----
let dragStartX = 0
let dragStartY = 0
let startPosX = 0
let startPosY = 0
let hasMoved = false

function onPointerDown(e) {
  if (!props.draggable || !props.floating) return
  // 点击气泡不触发拖拽
  if (e.target.closest('.l2d-bubble')) return
  e.preventDefault()
  hasMoved = false
  dragging.value = true
  dragStartX = e.clientX
  dragStartY = e.clientY

  if (pos.x == null) {
    const el = containerRef.value?.parentElement
    if (el) {
      const rect = el.getBoundingClientRect()
      pos.x = rect.left
      pos.y = rect.top
    }
  }
  startPosX = pos.x
  startPosY = pos.y

  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', onPointerUp)
  emit('drag-start')
}

function onPointerMove(e) {
  if (!dragging.value) return
  const dx = e.clientX - dragStartX
  const dy = e.clientY - dragStartY
  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) hasMoved = true

  pos.x = Math.max(0, startPosX + dx)
  pos.y = Math.max(0, startPosY + dy)
}

function onPointerUp() {
  window.removeEventListener('pointermove', onPointerMove)
  window.removeEventListener('pointerup', onPointerUp)
  dragging.value = false

  if (hasMoved) {
    const w = props.size
    const h = props.size
    if (pos.x + w > window.innerWidth) pos.x = window.innerWidth - w
    if (pos.y + h > window.innerHeight) pos.y = window.innerHeight - h
    if (pos.x < 0) pos.x = 0
    if (pos.y < 0) pos.y = 0

    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ x: pos.x, y: pos.y }))
    } catch (_) {}

    emit('drag-end', { x: pos.x, y: pos.y })
  }
}

function loadPosition() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const p = JSON.parse(saved)
      pos.x = p.x
      pos.y = p.y
      return true
    }
  } catch (_) {}
  return false
}

function onBubbleClick() {
  emit('click-bubble')
}

onMounted(async () => {
  await nextTick()
  if (!containerRef.value) return

  if (props.floating && props.draggable) {
    loadPosition()
  }

  try {
    await init(containerRef.value, props.modelPath, props.modelScale)
    loaded.value = true
    setExpression(props.expression)
    if (props.talking) startTalking()
    emit('ready')
  } catch (e) {
    console.warn('Live2D 模型加载失败，使用 CSS 降级:', e.message)
    emit('error', e)
  }
})

watch(() => props.talking, (val) => {
  if (!loaded.value) return
  if (val) startTalking()
  else stopTalking()
})

watch(() => props.expression, (val) => {
  if (!loaded.value) return
  setExpression(val)
})

watch(() => props.size, (val) => {
  if (!loaded.value || !containerRef.value) return
  resize(val, val, props.modelScale)
})

onBeforeUnmount(() => {
  destroy()
})

defineExpose({ startTalking, stopTalking })
</script>

<style scoped>
.l2d-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.l2d-wrapper.floating {
  position: fixed;
  z-index: 999;
  user-select: none;
  cursor: grab;
  touch-action: none;
}

.l2d-wrapper.floating.dragging {
  cursor: grabbing;
}

/* 交互模式：气泡可溢出容器 */
.l2d-wrapper.interactive {
  overflow: visible;
}

/* 透明桥接：防止鼠标移到气泡途中 hover 断开 */
.l2d-wrapper.interactive::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 36px;
}

/* 放大角色时允许内容溢出 */
.l2d-wrapper.scale-up {
  overflow: visible;
}

/* "问问灵宝" 气泡 */
.l2d-bubble {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(4px);
  margin-top: 6px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  padding: 5px 14px;
  font-size: 12px;
  color: #409eff;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  pointer-events: none;
  z-index: 20;
  opacity: 0;
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.l2d-wrapper:hover .l2d-bubble {
  opacity: 1;
  pointer-events: auto;
  transform: translateX(-50%) translateY(0);
}

/* 气泡箭头 */
.l2d-bubble::after {
  content: '';
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 6px solid white;
}

.l2d-canvas {
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.l2d-canvas.loaded {
  opacity: 1;
}
</style>
