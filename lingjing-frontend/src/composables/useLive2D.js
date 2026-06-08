/**
 * Live2D 模型控制 composable
 */
export function useLive2D() {
  let app = null
  let model = null
  let animationId = null
  let mouthLoopRunning = false
  let mouthPhase = 0
  let isTalking = false
  let modelScale = 1.0
  let disposed = false

  async function init(container, modelPath, extraScale = 1.0) {
    if (!container) throw new Error('Container element is required')
    disposed = false
    if (typeof window === 'undefined' || typeof Live2DCubismCore === 'undefined') {
      throw new Error('Live2D Cubism Core not loaded. Add the CDN script to index.html')
    }

    modelScale = extraScale

    const PIXI = await import('pixi.js')
    if (disposed) return
    window.PIXI = PIXI

    const { Live2DModel } = await import('pixi-live2d-display/cubism4')
    if (disposed) return

    const cw = container.clientWidth || 200
    const ch = container.clientHeight || 200

    app = new PIXI.Application({
      width: cw,
      height: ch,
      backgroundAlpha: 0,
      antialias: true,
      resolution: Math.min(window.devicePixelRatio || 1, 2),
      autoDensity: true,
    })

    container.appendChild(app.view)

    model = await Live2DModel.from(modelPath)
    if (disposed) {
      model = null
      app.destroy(true, { children: true, texture: true })
      app = null
      return
    }

    const scale = Math.min(cw / model.width, ch / model.height) * 0.85 * modelScale
    model.scale.set(scale)
    model.anchor.set(0.5, 0.5)
    model.position.set(cw / 2, ch / 2 + ch * 0.05)

    app.stage.addChild(model)

    // 播放 idle 动画
    try {
      model.motion('Idle', 0)
    } catch (e) {
      // 忽略
    }

    app.start()

    return { model, app }
  }

  /** 调整 PIXI 画布和模型大小 */
  function resize(width, height, extraScale) {
    if (!app || !model) return
    if (extraScale !== undefined) modelScale = extraScale
    app.renderer.resize(width, height)
    const scale = Math.min(width / model.width, height / model.height) * 0.85 * modelScale
    model.scale.set(scale)
    model.anchor.set(0.5, 0.5)
    model.position.set(width / 2, height / 2 + height * 0.05)
  }

  function setMouthOpen(value) {
    if (!model?.internalModel?.coreModel) return
    try {
      model.internalModel.coreModel.setParameterValueById(
        'ParamMouthOpenY',
        Math.max(0, Math.min(1, value))
      )
    } catch (e) {
      // ignore
    }
  }

  function startTalking() {
    if (isTalking) return
    isTalking = true
    mouthPhase = 0
    ensureMouthLoop()
  }

  function stopTalking() {
    isTalking = false
    setMouthOpen(0)
  }

  // 持久 rAF 循环，不随 talking 状态启停，避免断档
  function ensureMouthLoop() {
    if (mouthLoopRunning) return
    mouthLoopRunning = true
    const loop = () => {
      if (isTalking && model?.internalModel?.coreModel) {
        mouthPhase += 0.05
        const t = mouthPhase
        const value = 0.45 + 0.45 * Math.sin(t * 1.5) + 0.1 * Math.sin(t * 3.0)
        try {
          model.internalModel.coreModel.setParameterValueById('ParamMouthOpenY', Math.max(0.05, Math.min(1, value)))
        } catch (e) { /* ignore */ }
      }
      animationId = requestAnimationFrame(loop)
    }
    animationId = requestAnimationFrame(loop)
  }

  // 情绪 → Haru 内置表达式映射
  // f01=talking, f02=worried, f03=angry, f04=gentle, f05=smile, f06=surprised, f07=shy, f08=relaxed
  const EXPRESSION_MAP = {
    idle: null,
    happy: 'f05',    // smile
    sad: 'f02',      // worried
    surprised: 'f06', // surprised
    thinking: 'f04',  // gentle
    shy: 'f07',       // shy
  }

  function setExpression(name) {
    if (!model) return
    const expId = EXPRESSION_MAP[name]
    if (expId !== undefined) {
      try {
        model.expression(expId || undefined)
      } catch (e) { /* ignore */ }
    }
  }

  function destroy() {
    disposed = true
    stopTalking()
    if (animationId) {
      cancelAnimationFrame(animationId)
      animationId = null
      mouthLoopRunning = false
    }
    if (app) {
      app.stop()
      app.destroy(true, { children: true, texture: true })
      app = null
    }
    model = null
  }

  return {
    init,
    resize,
    setMouthOpen,
    setExpression,
    startTalking,
    stopTalking,
    destroy,
  }
}
