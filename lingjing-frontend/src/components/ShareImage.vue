<template>
  <el-dialog v-model="visible" title="打卡分享图" width="460px" top="3vh" destroy-on-close>
    <div class="share-wrap">
      <canvas ref="canvasRef" :width="CANVAS_W" :height="CANVAS_H" class="share-canvas" />
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="saveImage">保存图片</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const CANVAS_W = 800
const CANVAS_H = 1100

const props = defineProps({
  modelValue: Boolean,
  checkInCount: Number,
  totalCount: Number,
  lingshanCount: Number,
  lingshanTotal: Number,
  nianhuaCount: Number,
  nianhuaTotal: Number,
  checkedAttractions: { type: Array, default: () => [] } // [{ name, area }]
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)
const canvasRef = ref(null)

watch(() => props.modelValue, (v) => {
  visible.value = v
  if (v) nextTick(render)
})

watch(visible, (v) => {
  emit('update:modelValue', v)
  if (v) nextTick(render)
})

// ==================== 手绘风格绘制工具 ====================

function rand(min, max) { return Math.random() * (max - min) + min }

function shakyTo(ctx, x1, y1, x2, y2, amp = 1.2) {
  const dist = Math.hypot(x2 - x1, y2 - y1)
  const steps = Math.max(4, Math.round(dist / 3))
  ctx.beginPath()
  ctx.moveTo(x1, y1)
  for (let i = 1; i <= steps; i++) {
    const t = i / steps
    ctx.lineTo(
      x1 + (x2 - x1) * t + rand(-amp, amp),
      y1 + (y2 - y1) * t + rand(-amp, amp)
    )
  }
  ctx.stroke()
}

function shakyArc(ctx, cx, cy, r, start, end, amp = 1) {
  const pts = 24
  ctx.beginPath()
  for (let i = 0; i <= pts; i++) {
    const t = i / pts
    const angle = start + (end - start) * t
    const rr = r + rand(-amp, amp)
    const x = cx + Math.cos(angle) * rr
    const y = cy + Math.sin(angle) * rr
    i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y)
  }
  ctx.stroke()
}

function fillShakyRect(ctx, x, y, w, h, amp = 1) {
  ctx.beginPath()
  ctx.moveTo(x + rand(-amp, amp), y + rand(-amp, amp))
  ctx.lineTo(x + w + rand(-amp, amp), y + rand(-amp, amp))
  ctx.lineTo(x + w + rand(-amp, amp), y + h + rand(-amp, amp))
  ctx.lineTo(x + rand(-amp, amp), y + h + rand(-amp, amp))
  ctx.closePath()
  ctx.fill()
  ctx.stroke()
}

// ==================== 主要绘制函数 ====================

function render() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const W = CANVAS_W
  const H = CANVAS_H
  const data = props

  ctx.clearRect(0, 0, W, H)

  // --- 1. 背景：仿古纸 ---
  bg(ctx, W, H)

  // --- 2. 外边框（手绘感）---
  ctx.strokeStyle = '#8b7a5a'
  ctx.lineWidth = 3
  shakyTo(ctx, 30, 30, W - 30, 30, 1.5)
  shakyTo(ctx, W - 30, 30, W - 30, H - 30, 1.5)
  shakyTo(ctx, W - 30, H - 30, 30, H - 30, 1.5)
  shakyTo(ctx, 30, H - 30, 30, 30, 1.5)
  ctx.lineWidth = 1

  // --- 3. 远山（水墨风格）---
  mountains(ctx, W)

  // --- 4. 云朵 ---
  clouds(ctx, W)

  // --- 5. 标题 ---
  title(ctx, W)

  // --- 6. 进度圆环 ---
  progressGauge(ctx, W, data.checkInCount, data.totalCount)

  // --- 7. 区域进度条 ---
  areaBars(ctx, W, data)

  // --- 8. 已打卡景点列表 ---
  checkedList(ctx, W, data.checkedAttractions)

  // --- 9. 底部装饰 ---
  footer(ctx, W)
}

// ========== 各绘制模块 ==========

function bg(ctx, W, H) {
  // 底色
  const grad = ctx.createLinearGradient(0, 0, W, H)
  grad.addColorStop(0, '#f7f1e3')
  grad.addColorStop(0.5, '#f5edd6')
  grad.addColorStop(1, '#efe6cc')
  ctx.fillStyle = grad
  ctx.fillRect(0, 0, W, H)

  // 纹理噪点（模拟宣纸）
  ctx.save()
  for (let i = 0; i < 3000; i++) {
    const x = rand(0, W)
    const y = rand(0, H)
    const a = rand(0.02, 0.08)
    const s = rand(0.5, 2)
    ctx.fillStyle = `rgba(139, 122, 90, ${a})`
    ctx.fillRect(x, y, s, s)
  }
  ctx.restore()
}

function mountains(ctx, W) {
  const layers = [
    { yBase: 130, height: 80, color: '#c8d4b8', alpha: 0.5 },
    { yBase: 160, height: 60, color: '#8fa87a', alpha: 0.6 },
    { yBase: 190, height: 40, color: '#6b8c5e', alpha: 0.7 }
  ]

  for (const layer of layers) {
    ctx.save()
    ctx.globalAlpha = layer.alpha
    ctx.fillStyle = layer.color
    ctx.strokeStyle = layer.color
    ctx.lineWidth = 1.5
    ctx.beginPath()
    ctx.moveTo(40, layer.yBase + layer.height)

    const peaks = [
      { x: 80, h: -layer.height * 0.6 },
      { x: 140, h: -layer.height * 0.9 },
      { x: 200, h: -layer.height * 0.4 },
      { x: 270, h: -layer.height * 0.85 },
      { x: 340, h: -layer.height * 0.5 },
      { x: 400, h: -layer.height * 0.7 },
      { x: 460, h: -layer.height * 0.3 },
      { x: 520, h: -layer.height * 0.8 },
      { x: 580, h: -layer.height * 0.45 },
      { x: 640, h: -layer.height * 0.75 },
      { x: 700, h: -layer.height * 0.35 },
      { x: 760, h: -layer.height * 0.5 }
    ]

    for (const p of peaks) {
      ctx.lineTo(p.x + rand(-3, 3), layer.yBase + p.h + rand(-2, 2))
    }
    ctx.lineTo(W - 40, layer.yBase + layer.height)
    ctx.closePath()
    ctx.fill()
    ctx.stroke()
    ctx.restore()
  }
}

function clouds(ctx, W) {
  ctx.save()
  ctx.fillStyle = 'rgba(255, 255, 255, 0.5)'
  ctx.strokeStyle = 'rgba(200, 200, 200, 0.3)'
  ctx.lineWidth = 1

  const groups = [
    { cx: 120, cy: 70, r: 30 },
    { cx: 160, cy: 60, r: 35 },
    { cx: 200, cy: 70, r: 25 },
    { cx: 580, cy: 55, r: 28 },
    { cx: 620, cy: 45, r: 32 },
    { cx: 660, cy: 55, r: 22 }
  ]

  for (const g of groups) {
    ctx.beginPath()
    ctx.arc(g.cx, g.cy, g.r, 0, Math.PI * 2)
    ctx.fill()
    ctx.stroke()
  }
  ctx.restore()
}

function title(ctx, W) {
  // 主标题
  ctx.save()
  ctx.fillStyle = '#3a3226'
  ctx.font = 'bold 42px "STKaiti", "KaiTi", "楷体", "Noto Serif SC", serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('灵山胜境 · 打卡足迹', W / 2, 280)
  ctx.restore()

  // 红印装饰（手绘印章风格）
  ctx.save()
  const sx = W / 2 + 220, sy = 260
  ctx.strokeStyle = '#c0392b'
  ctx.lineWidth = 2.5
  ctx.fillStyle = 'rgba(192, 57, 43, 0.15)'
  // 不规则矩形印章
  const sw = 56, sh = 38
  ctx.beginPath()
  ctx.moveTo(sx + rand(-2, 2), sy + rand(-2, 2))
  ctx.lineTo(sx + sw + rand(-2, 2), sy + rand(-2, 2))
  ctx.lineTo(sx + sw + rand(-2, 2), sy + sh + rand(-2, 2))
  ctx.lineTo(sx + rand(-2, 2), sy + sh + rand(-2, 2))
  ctx.closePath()
  ctx.fill()
  ctx.stroke()

  ctx.fillStyle = '#c0392b'
  ctx.font = 'bold 16px "STKaiti", "KaiTi", serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('灵境智游', sx + sw / 2, sy + sh / 2)
  ctx.restore()

  // 分隔线
  ctx.save()
  ctx.strokeStyle = '#b8a88a'
  ctx.lineWidth = 1.5
  const lineY = 315
  const lineStart = 120, lineEnd = W - 120
  shakyTo(ctx, lineStart, lineY, lineEnd, lineY, 1)
  // 分隔线两端小圆点
  ctx.fillStyle = '#b8a88a'
  for (const lx of [lineStart, lineEnd]) {
    ctx.beginPath()
    ctx.arc(lx, lineY, 4, 0, Math.PI * 2)
    ctx.fill()
  }
  ctx.restore()
}

function progressGauge(ctx, W, count, total) {
  const cx = W / 2
  const cy = 440
  const r = 80
  const pct = total > 0 ? count / total : 0

  ctx.save()

  // 外圈装饰（手绘圆）
  ctx.strokeStyle = '#d4c5a9'
  ctx.lineWidth = 2
  shakyArc(ctx, cx, cy, r + 18, 0, Math.PI * 2, 1.5)

  // 底色弧
  ctx.strokeStyle = '#e8e0d0'
  ctx.lineWidth = 14
  ctx.beginPath()
  ctx.arc(cx, cy, r, -Math.PI * 0.75, Math.PI * 0.75)
  ctx.stroke()

  // 进度弧（渐变）
  const grad = ctx.createLinearGradient(cx - r, cy, cx + r, cy)
  grad.addColorStop(0, '#409eff')
  grad.addColorStop(0.5, '#67c23a')
  grad.addColorStop(1, '#e6a23c')
  ctx.strokeStyle = grad
  ctx.lineWidth = 14
  ctx.lineCap = 'round'
  ctx.beginPath()
  ctx.arc(cx, cy, r, -Math.PI * 0.75, -Math.PI * 0.75 + Math.PI * 1.5 * pct)
  ctx.stroke()

  // 百分比文字
  ctx.fillStyle = '#3a3226'
  ctx.font = 'bold 52px "Arial", sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(`${Math.round(pct * 100)}%`, cx, cy - 8)

  ctx.fillStyle = '#8b7a5a'
  ctx.font = '18px "STKaiti", "KaiTi", serif'
  ctx.fillText(`已完成 ${count}/${total}`, cx, cy + 38)

  ctx.restore()
}

function areaBars(ctx, W, data) {
  ctx.save()
  const leftX = 120
  const barW = 420
  const startY = 540
  const rowH = 70

  const areas = [
    { label: '灵山胜境', count: data.lingshanCount, total: data.lingshanTotal, color: '#409eff' },
    { label: '拈花湾', count: data.nianhuaCount, total: data.nianhuaTotal, color: '#67c23a' }
  ]

  areas.forEach((area, idx) => {
    const y = startY + idx * rowH
    const pct = area.total > 0 ? area.count / area.total : 0

    // 标签
    ctx.fillStyle = '#3a3226'
    ctx.font = '18px "STKaiti", "KaiTi", serif'
    ctx.textAlign = 'left'
    ctx.textBaseline = 'middle'
    ctx.fillText(area.label, leftX, y + 8)

    // 手绘外框
    const bx = leftX, by = y + 20, bw = barW, bh = 18
    ctx.strokeStyle = '#d4c5a9'
    ctx.lineWidth = 1.5
    fillShakyRect(ctx, bx, by, bw, bh, 0.5)

    // 填充条
    if (pct > 0) {
      ctx.save()
      ctx.beginPath()
      ctx.rect(bx + 2, by + 2, Math.max(4, (bw - 4) * pct), bh - 4)
      ctx.clip()
      const g = ctx.createLinearGradient(bx, by, bx + bw, by)
      g.addColorStop(0, area.color)
      g.addColorStop(1, area.color + '99')
      ctx.fillStyle = g
      // 手绘填充
      for (let i = 0; i < 20; i++) {
        const fx = bx + rand(0, Math.max(4, (bw - 4) * pct))
        const fy = by + 2 + rand(0, bh - 4)
        ctx.fillRect(fx, fy, rand(3, 8), rand(3, 6))
      }
      ctx.restore()
    }

    // 计数
    ctx.fillStyle = '#6b5d4a'
    ctx.font = '15px "Arial", sans-serif'
    ctx.textAlign = 'right'
    ctx.textBaseline = 'middle'
    ctx.fillText(`${area.count}/${area.total}`, leftX + barW, y + 8)

    // 区域小圆点色标
    ctx.fillStyle = area.color
    ctx.beginPath()
    ctx.arc(leftX - 16, y + 4, 5, 0, Math.PI * 2)
    ctx.fill()
  })

  ctx.restore()
}

function checkedList(ctx, W, attractions) {
  if (!attractions || attractions.length === 0) return

  ctx.save()
  const leftX = 120
  const startY = 700
  const colW = 280

  // 小标题
  ctx.fillStyle = '#8b7a5a'
  ctx.font = '16px "STKaiti", "KaiTi", serif'
  ctx.textAlign = 'left'
  ctx.textBaseline = 'middle'
  ctx.fillText('—— 已打卡景点 ——', W / 2, startY - 5)
  ctx.textAlign = 'center'
  ctx.fillText('—— 已打卡景点 ——', W / 2, startY - 5)
  ctx.textAlign = 'left'

  // 两列布局
  const col1 = attractions.filter((_, i) => i % 2 === 0).slice(0, 10)
  const col2 = attractions.filter((_, i) => i % 2 === 1).slice(0, 10)
  const maxRows = Math.max(col1.length, col2.length)

  for (let i = 0; i < maxRows; i++) {
    const y = startY + 10 + i * 28
    // 列1
    if (i < col1.length) {
      const a = col1[i]
      ctx.fillStyle = '#409eff'
      ctx.font = '14px sans-serif'
      ctx.fillText('✓', leftX, y)
      ctx.fillStyle = '#3a3226'
      ctx.font = '15px "STKaiti", "KaiTi", serif'
      ctx.fillText(a.name, leftX + 22, y)
    }
    // 列2
    if (i < col2.length) {
      const a = col2[i]
      ctx.fillStyle = '#409eff'
      ctx.font = '14px sans-serif'
      ctx.fillText('✓', leftX + colW, y)
      ctx.fillStyle = '#3a3226'
      ctx.font = '15px "STKaiti", "KaiTi", serif'
      ctx.fillText(a.name, leftX + colW + 22, y)
    }
  }

  ctx.restore()
}

function footer(ctx, W) {
  ctx.save()

  // 底部波浪装饰
  ctx.strokeStyle = '#c8d4b8'
  ctx.lineWidth = 2
  ctx.globalAlpha = 0.5
  const waveY = 960
  ctx.beginPath()
  ctx.moveTo(40, waveY)
  for (let x = 40; x <= W - 40; x += 4) {
    const wave = Math.sin((x - 40) * 0.03) * 8 + Math.sin((x - 40) * 0.07) * 3
    ctx.lineTo(x + rand(-0.5, 0.5), waveY + wave + rand(-0.5, 0.5))
  }
  ctx.stroke()

  // 第二层波浪
  ctx.strokeStyle = '#b8a88a'
  ctx.globalAlpha = 0.3
  const waveY2 = 980
  ctx.beginPath()
  ctx.moveTo(40, waveY2)
  for (let x = 40; x <= W - 40; x += 4) {
    const wave = Math.sin((x - 40) * 0.04 + 1) * 5
    ctx.lineTo(x + rand(-0.5, 0.5), waveY2 + wave + rand(-0.5, 0.5))
  }
  ctx.stroke()

  // 品牌文字
  ctx.globalAlpha = 1
  ctx.fillStyle = '#b8a88a'
  ctx.font = '14px "STKaiti", "KaiTi", serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('灵境智游 · 智慧导览', W / 2, 1030)

  // 日期
  const today = new Date()
  const dateStr = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`
  ctx.fillStyle = '#c8b89a'
  ctx.font = '12px sans-serif'
  ctx.fillText(dateStr, W / 2, 1055)

  ctx.restore()
}

// ==================== 保存图片 ====================

async function saveImage() {
  const canvas = canvasRef.value
  if (!canvas) return

  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png'))
  if (!blob) {
    ElMessage.error('图片生成失败')
    return
  }

  // 使用 File System Access API，浏览器会弹出原生保存对话框，保存后再提示
  if ('showSaveFilePicker' in window) {
    try {
      const handle = await window.showSaveFilePicker({
        suggestedName: `打卡足迹_${new Date().toISOString().slice(0, 10)}.png`,
        types: [{ description: 'PNG 图片', accept: { 'image/png': ['.png'] } }]
      })
      const writable = await handle.createWritable()
      await writable.write(blob)
      await writable.close()
      ElMessage.success('图片已保存')
    } catch (e) {
      if (e.name !== 'AbortError') {
        // AbortError 是用户主动取消，不弹错误
        ElMessage.error('保存失败')
      }
    }
  } else {
    // 降级：不支持 File System API 的浏览器用传统方式
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `打卡足迹_${new Date().toISOString().slice(0, 10)}.png`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }
}
</script>

<style scoped>
.share-wrap {
  display: flex;
  justify-content: center;
  background: #f5f0e8;
  border-radius: 8px;
  padding: 12px;
}

.share-canvas {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>
