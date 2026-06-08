<template>
  <div class="chat-page">
    <!-- 左栏：历史对话（不贴边） -->
    <div class="sidebar-wrapper">
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>历史对话</h3>
          <el-button type="primary" size="small" @click="newConversation" circle>
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>
        <div class="conversation-list">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conv-item"
            :class="{ active: conv.id === currentId }"
          >
            <div class="conv-content" @click="switchConversation(conv.id)">
              <el-icon><ChatDotRound /></el-icon>
              <span class="conv-title">{{ conv.title }}</span>
            </div>
            <el-button
              class="delete-btn"
              text
              size="small"
              type="danger"
              @click.stop="handleDelete(conv.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-empty v-if="conversations.length === 0" description="暂无对话" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 主区域：聊天窗口 -->
    <div class="chat-main">
      <div v-if="!chatting && !currentId && !loadingMessages" class="welcome-screen">
        <div class="welcome-content">
          <h2>{{ digitalHumanConfig?.name || '灵宝' }}</h2>
          <p>{{ digitalHumanConfig?.greeting || '你的智能导游，关于灵山胜境和拈花湾的任何问题都可以问我！' }}</p>
          <el-button type="primary" size="large" @click="startChat">开始对话</el-button>
        </div>

        <!-- 热搜榜 -->
        <div v-if="hotSearches.length > 0" class="welcome-hot">
          <div class="hot-header">
            <span class="hot-header-icon">🔥</span>
            <span class="hot-header-title">热搜榜</span>
            <span class="hot-header-sub">游客都在问</span>
          </div>
          <div class="hot-grid">
            <div
              v-for="(item, i) in hotSearches"
              :key="item.id"
              class="hot-chip"
              :class="'heat-' + item.heatLabel"
              @click="sendHotQuestion(item.question)"
            >
              <span class="chip-rank" :class="'r' + Math.min(i + 1, 3)">{{ rankBadge(i) }}</span>
              <span class="chip-text">{{ item.question }}</span>
              <span class="chip-heat" :class="'heat-' + item.heatLabel">{{ heatLabel(item.heatLabel) }}</span>
            </div>
          </div>
        </div>
      </div>

      <template v-else>
        <!-- 对话标题栏 -->
        <div class="chat-header">
          <span class="chat-header-name">{{ digitalHumanConfig?.name || '灵宝' }}</span>
          <el-tooltip
            :content="ttsEnabled ? (muted ? '已静音，点击取消' : '点击静音') : '管理员已关闭语音播报'"
            placement="bottom"
          >
            <el-button
              class="header-mute-btn"
              :icon="(!ttsEnabled || muted) ? MuteNotification : Headset"
              :type="(!ttsEnabled || muted) ? 'info' : 'default'"
              size="large"
              circle
              :disabled="!ttsEnabled"
              @click="toggleMute"
            />
          </el-tooltip>
        </div>
        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div v-if="loadingMessages" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message"
            :class="msg.role"
          >
            <div class="avatar" :class="msg.role">
              {{ msg.role === 'assistant' ? '宝' : '我' }}
            </div>
            <div class="bubble">
              <img
                v-if="msg.imageUrl"
                :src="msg.imageUrl"
                class="msg-image"
                @click="previewImage(msg.imageUrl)"
              />
              <div v-if="msg.content" class="msg-text">{{ stripMarkdown(msg.content) }}</div>
              <el-button
                v-if="msg.role === 'assistant' && ttsEnabled"
                class="play-btn"
                :icon="speakingMsgId === msg.id ? VideoPause : VideoPlay"
                :type="speakingMsgId === msg.id ? 'warning' : 'primary'"
                size="small"
                circle
                @click="speakMessage(msg)"
              />
            </div>
          </div>
          <div v-if="sending" class="message assistant">
            <div class="avatar assistant">宝</div>
            <div class="bubble thinking">
              <span class="dot">.</span><span class="dot">.</span><span class="dot">.</span>
            </div>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="input-area">
          <div class="input-row">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              placeholder="输入你想了解的内容..."
              @keydown.enter.exact.prevent="handleSend"
              :disabled="sending"
            />
            <div class="voice-btn-group">
              <el-button
                class="img-btn"
                :icon="PictureFilled"
                size="large"
                circle
                @click="triggerImagePick"
                :disabled="sending"
              />
              <el-button
                v-if="asrEnabled && !recording"
                class="mic-btn"
                :icon="Microphone"
                size="large"
                circle
                @click="startRecording"
                :disabled="sending"
              />
              <el-button
                v-else-if="asrEnabled && recording"
                class="mic-btn recording"
                :icon="Microphone"
                size="large"
                circle
                type="danger"
                @click="stopRecording"
              />
            </div>
          </div>
          <div v-if="recording" class="recording-hint">录音中… 点击红色按钮停止</div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleImageSelected"
          />
          <el-button type="primary" @click="handleSend" :loading="sending" style="margin-top: 8px; align-self: flex-end;">
            发送
          </el-button>
        </div>
      </template>
      <!-- 图片预览 -->
      <el-dialog v-model="imagePreviewVisible" width="auto" top="5vh" :show-close="true" destroy-on-close>
        <div class="preview-wrapper">
          <img :src="imagePreviewUrl" class="preview-img" />
        </div>
      </el-dialog>
    </div>

    <!-- 右栏：数字人面板 — 立轴装裱 -->
    <div class="dh-panel">
      <div class="dh-frame">
        <div class="dh-frame-top"></div>
        <div class="dh-frame-body">
          <Live2DHuman
            ref="live2dRef"
            :size="520"
            :model-scale="1.2"
            :talking="sending || audioPending || audioPlaying || speakingMsgId !== null"
            :expression="chatting || currentId ? currentExpression : 'happy'"
            @error="onLive2DError"
          />
        </div>
        <div class="dh-frame-bottom">
          <span class="dh-name">{{ digitalHumanConfig?.name || '灵宝' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { sendMessageStream, sendPhotoStream, getConversations, getMessages, deleteConversation, sendVoice, synthesizeText, getHotSearches } from './api'
import { getDigitalHumanConfig } from '../../api/digitalHuman'
import Live2DHuman from '../../components/Live2DHuman.vue'
import { ElMessage } from 'element-plus'
import { Microphone, VideoPlay, VideoPause, MuteNotification, Headset, PictureFilled } from '@element-plus/icons-vue'

const conversations = ref([])
const messages = ref([])
const currentId = ref(null)
const inputText = ref('')
const sending = ref(false)
const loadingMessages = ref(false)
const chatting = ref(false)
const messageListRef = ref(null)
const fileInputRef = ref(null)
const digitalHumanConfig = ref(null)
const streamAbort = ref(null)  // 流式取消控制器
const live2dRef = ref(null)
const streamingContent = ref('')     // 当前流式回复文本（用于表情检测）
const overrideExpression = ref(null)  // 临时表情覆盖（单条消息播放时）
const audioPending = ref(false)       // TTS 已触发但音频尚未开始播放
const hotSearches = ref([])

// 流式 TTS：逐句合成播放
const synthQueue = ref([])
let synthPlaying = false
let synthTokenBuf = ''
let synthSentLen = 0
let synthOrdered = []   // 有序占位：按句子顺序存放 URL 或 null
let synthPlayIdx = 0    // 下一个可播的索引

function playNextInQueue() {
  if (synthPlaying || synthQueue.value.length === 0) return
  synthPlaying = true
  const url = synthQueue.value.shift()
  const audio = new Audio(url)
  audio.onplay = () => {
    audioPlaying.value = true
    audioPending.value = false
    live2dRef.value?.startTalking()
    // 根据当前已累积的回复内容设置表情（比单条消息的全文本更实时）
    if (streamingContent.value) {
      overrideExpression.value = detectEmotion(streamingContent.value)
    } else {
      const lastMsg = messages.value.filter(m => m.role === 'assistant').pop()
      if (lastMsg) overrideExpression.value = detectEmotion(lastMsg.content || '')
    }
  }
  audio.onended = () => {
    synthPlaying = false
    if (synthQueue.value.length === 0) {
      audioPlaying.value = false
      live2dRef.value?.stopTalking()
      overrideExpression.value = null
    }
    playNextInQueue()
  }
  audio.onerror = () => {
    synthPlaying = false
    audioPending.value = false
    if (synthQueue.value.length === 0) {
      audioPlaying.value = false
      live2dRef.value?.stopTalking()
      overrideExpression.value = null
    }
    playNextInQueue()
  }
  audio.play().catch(() => {
    synthPlaying = false
    audioPending.value = false
    if (synthQueue.value.length === 0) {
      audioPlaying.value = false
      live2dRef.value?.stopTalking()
    }
    playNextInQueue()
  })
  window.__streamAudio = audio
}

function flushSynthSentences(newText) {
  synthTokenBuf = newText
  while (synthSentLen < synthTokenBuf.length) {
    const m = synthTokenBuf.slice(synthSentLen).match(/^([^。！？；\n]{1,}[。！？；\n])/)
    if (!m) break
    const sentence = m[1]
    synthSentLen += sentence.length
    if (ttsEnabled.value && !muted.value) {
      audioPending.value = true
      const idx = synthOrdered.length
      synthOrdered.push(null)  // 占位
      synthesizeText(stripMarkdown(sentence), ttsSpeed.value).then(url => {
        if (!url) { audioPending.value = false; return }
        synthOrdered[idx] = url
        flushSynthPlayable()
      }).catch(() => { audioPending.value = false })
    }
  }
}

/** 将有序数组中可播的 URL 推入队列 */
function flushSynthPlayable() {
  while (synthPlayIdx < synthOrdered.length && synthOrdered[synthPlayIdx]) {
    synthQueue.value.push(synthOrdered[synthPlayIdx])
    synthPlayIdx++
    playNextInQueue()
  }
}

function stopSynthQueue() {
  audioPending.value = false
  synthQueue.value = []
  synthPlaying = false
  synthTokenBuf = ''
  synthSentLen = 0
  synthOrdered = []
  synthPlayIdx = 0
  if (window.__streamAudio) {
    window.__streamAudio.pause()
    window.__streamAudio = null
  }
}

// 功能开关状态
const asrEnabled = ref(true)
const ttsEnabled = ref(true)

// 语音相关状态
const recording = ref(false)
const audioPlaying = ref(false)
const currentAudio = ref('')
const muted = ref(false)
const speakingMsgId = ref(null)

// 从配置中读取的 TTS 语速
const ttsSpeed = computed(() => digitalHumanConfig.value?.ttsSpeed || 1.0)

// Live2D 表情控制：根据对话状态切换表情
function detectEmotion(text) {
  if (!text) return 'idle'
  // 优先级：sad > surprised > happy > shy
  if (/抱歉|对不起|还不|无法|暂时|没有查到|暂无/.test(text)) return 'sad'
  if (/高达|长达|千年|始建于|叹为观止|难以置信|没想到|竟然|惊呆了|太厉害/.test(text)) return 'surprised'
  if (/欢迎|你好|推荐|太棒|真美|漂亮|壮观|震撼|值得|好玩|有特色|美不胜收|不错|好|喜欢|开心|高兴|厉害|赞|棒|优秀|完美|满意|美好/.test(text)) return 'happy'
  if (/谢谢|感谢|不客气|能帮到你|夸奖|夸我/.test(text)) return 'shy'
  return 'idle'
}

const currentExpression = computed(() => {
  // 单条消息播放时的临时表情覆盖
  if (overrideExpression.value) return overrideExpression.value

  if (sending.value) {
    // 流式回复中根据已收到的内容动态切换表情
    const c = streamingContent.value || ''
    if (!c) return 'thinking'
    const emotion = detectEmotion(c)
    return emotion !== 'idle' ? emotion : 'thinking'
  }

  const msgs = messages.value
  for (let i = msgs.length - 1; i >= 0; i--) {
    if (msgs[i].role === 'assistant') {
      return detectEmotion(msgs[i].content || '')
    }
  }
  return 'idle'
})

async function fetchConversations() {
  try {
    conversations.value = await getConversations()
  } catch (e) {
    // not logged in
  }
}

async function fetchConfig() {
  try {
    const data = await getDigitalHumanConfig()
    digitalHumanConfig.value = data
    if (data) {
      asrEnabled.value = data.asrEnabled !== false
      ttsEnabled.value = data.ttsEnabled !== false
      // 如果管理员关闭了语音播报，自动设为静音
      if (!data.ttsEnabled) {
        muted.value = true
      }
    }
  } catch (e) {
    // offline
  }
}

function startChat() {
  stopPlayback()
  chatting.value = true
  currentId.value = null
  messages.value = []
  inputText.value = ''
}

async function switchConversation(id) {
  stopPlayback()
  chatting.value = true
  currentId.value = id
  loadingMessages.value = true
  try {
    messages.value = await getMessages(id)
    await nextTick()
    scrollToBottom()
  } catch (e) {
    // error
  } finally {
    loadingMessages.value = false
  }
}

async function handleDelete(id) {
  try {
    if (currentId.value === id) {
      stopPlayback()
    }
    await deleteConversation(id)
    if (currentId.value === id) {
      currentId.value = null
      messages.value = []
    }
    await fetchConversations()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

async function newConversation() {
  stopPlayback()
  chatting.value = true
  currentId.value = null
  messages.value = []
  inputText.value = ''
  await nextTick()
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  sending.value = true
  streamingContent.value = ''
  inputText.value = ''
  stopSynthQueue()

  // 显示用户消息
  const userMsgId = Date.now()
  const assistantMsgId = userMsgId + 1
  messages.value.push({ id: userMsgId, role: 'user', content: text })
  // 占位 AI 消息，逐 token 填充
  messages.value.push({ id: assistantMsgId, role: 'assistant', content: '' })

  await nextTick()
  scrollToBottom()

  let fullText = ''

  streamAbort.value = sendMessageStream(
    { conversationId: currentId.value, content: text },
    // onToken: 逐字追加 + 逐句 TTS
    (token) => {
      const msg = messages.value.find(m => m.id === assistantMsgId)
      if (msg) {
        msg.content += token
        fullText += token
        streamingContent.value += token
        flushSynthSentences(fullText)
        nextTick(() => scrollToBottom())
      }
    },
    // onDone
    (info) => {
      currentId.value = info.conversationId
      sending.value = false
      streamAbort.value = null
      fetchConversations()
      streamingContent.value = ''

      // 剩余未合成文本
      const remaining = fullText.slice(synthSentLen)
      if (remaining && ttsEnabled.value && !muted.value) {
        audioPending.value = true
        const idx = synthOrdered.length
        synthOrdered.push(null)
        synthesizeText(stripMarkdown(remaining), ttsSpeed.value).then(url => {
          if (!url) { audioPending.value = false; return }
          synthOrdered[idx] = url
          flushSynthPlayable()
        }).catch(() => { audioPending.value = false })
      }
    },
    // onError
    (err) => {
      ElMessage.error('发送失败: ' + err)
      const msg = messages.value.find(m => m.id === assistantMsgId)
      if (msg && !msg.content) msg.content = '(回复失败，请重试)'
      streamingContent.value = ''
      sending.value = false
      streamAbort.value = null
      stopSynthQueue()
      overrideExpression.value = null
    }
  )
}

function scrollToBottom() {
  const el = messageListRef.value
  if (el) el.scrollTop = el.scrollHeight
}

// === 静音切换 ===
function toggleMute() {
  muted.value = !muted.value
  // 如果正在播放语音，立即打断
  if (audioPlaying.value) {
    stopPlayback()
  }
}

// === 语音输入（Web Audio API 录制 WAV → 后端 DashScope ASR 识别） ===
let mediaStream = null
let audioContext = null
let audioProcessor = null
let audioSource = null
let wavChunks = []

function startRecording() {
  navigator.mediaDevices.getUserMedia({ audio: true })
    .then((stream) => {
      mediaStream = stream
      audioContext = new AudioContext({ sampleRate: 16000 })
      audioSource = audioContext.createMediaStreamSource(stream)
      wavChunks = []

      audioProcessor = audioContext.createScriptProcessor(4096, 1, 1)
      audioProcessor.onaudioprocess = (e) => {
        const inputData = e.inputBuffer.getChannelData(0)
        // Float32 → Int16 PCM
        const pcmData = new Int16Array(inputData.length)
        for (let i = 0; i < inputData.length; i++) {
          const s = Math.max(-1, Math.min(1, inputData[i]))
          pcmData[i] = s < 0 ? s * 0x8000 : s * 0x7FFF
        }
        wavChunks.push(pcmData)
      }

      audioSource.connect(audioProcessor)
      audioProcessor.connect(audioContext.destination)
      recording.value = true
    })
    .catch((err) => {
      if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
        ElMessage.error('请允许麦克风权限')
      } else {
        ElMessage.error('无法访问麦克风')
      }
    })
}

function stopRecording() {
  recording.value = false

  // 关闭前保存采样率
  const sampleRate = audioContext ? audioContext.sampleRate : 16000

  if (audioProcessor) {
    audioProcessor.disconnect()
    audioProcessor = null
  }
  if (audioSource) {
    audioSource.disconnect()
    audioSource = null
  }
  if (audioContext) {
    audioContext.close().catch(() => {})
    audioContext = null
  }
  if (mediaStream) {
    mediaStream.getTracks().forEach((t) => t.stop())
    mediaStream = null
  }

  if (wavChunks.length === 0) return

  // 组装 WAV 文件
  const totalSamples = wavChunks.reduce((acc, val) => acc + val.length, 0)
  const buffer = new ArrayBuffer(44 + totalSamples * 2)
  const view = new DataView(buffer)

  writeWavHeader(view, sampleRate, totalSamples)

  let offset = 44
  for (const chunk of wavChunks) {
    for (let i = 0; i < chunk.length; i++) {
      view.setInt16(offset, chunk[i], true)
      offset += 2
    }
  }
  wavChunks = []

  const blob = new Blob([buffer], { type: 'audio/wav' })
  sendVoiceMessage(blob)
}

function writeWavHeader(view, sampleRate, numSamples) {
  const byteRate = sampleRate * 2
  const dataSize = numSamples * 2
  writeStr(view, 0, 'RIFF')
  view.setUint32(4, 36 + dataSize, true)
  writeStr(view, 8, 'WAVE')
  writeStr(view, 12, 'fmt ')
  view.setUint32(16, 16, true)
  view.setUint16(20, 1, true)       // PCM
  view.setUint16(22, 1, true)       // mono
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, byteRate, true)
  view.setUint16(32, 2, true)       // block align
  view.setUint16(34, 16, true)      // bits per sample
  writeStr(view, 36, 'data')
  view.setUint32(40, dataSize, true)
}

function writeStr(view, offset, str) {
  for (let i = 0; i < str.length; i++) {
    view.setUint8(offset + i, str.charCodeAt(i))
  }
}

async function sendVoiceMessage(blob) {
  sending.value = true

  const formData = new FormData()
  formData.append('audio', blob, 'recording.wav')
  if (currentId.value) {
    formData.append('conversationId', currentId.value)
  }
  formData.append('muted', muted.value ? 'true' : 'false')
  formData.append('ttsSpeed', ttsSpeed.value)

  const tempId = Date.now()
  messages.value.push({ id: tempId, role: 'user', content: '语音识别中…' })
  await nextTick()
  scrollToBottom()

  try {
    const result = await sendVoice(formData)

    currentId.value = result.conversationId

    // 替换临时消息
    messages.value = messages.value.filter((m) => m.id !== tempId)
    const userMsgId = Date.now() + 1
    const assistantMsgId = Date.now() + 2
    messages.value.push({ id: userMsgId, role: 'user', content: result.userText })
    messages.value.push({ id: assistantMsgId, role: 'assistant', content: result.assistantText })

    await nextTick()
    scrollToBottom()
    await fetchConversations()

    // TTS：后端合成，失败则降级浏览器朗读
    if (ttsEnabled.value && !muted.value && result.assistantText) {
      if (result.audioUrl) {
        // 在 playAudio 的异步 onplay 前同步设置表情，避免 sending=false 后表情闪烁
        overrideExpression.value = detectEmotion(result.assistantText || '')
        playAudio(result.audioUrl, assistantMsgId)
      } else {
        speakText(result.assistantText, assistantMsgId)
      }
    }
  } catch (e) {
    ElMessage.error('语音发送失败，请重试')
    messages.value = messages.value.filter((m) => m.id !== tempId)
  } finally {
    sending.value = false
  }
}

// === 图片识别 ===
function triggerImagePick() {
  fileInputRef.value?.click()
}

function handleImageSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploadPhoto(file)
  // 重置 input，允许重复选择同一文件
  e.target.value = ''
}

async function uploadPhoto(file) {
  const localUrl = URL.createObjectURL(file)
  const userMsgId = Date.now()
  messages.value.push({ id: userMsgId, role: 'user', content: '识别中…', imageUrl: localUrl })

  const formData = new FormData()
  formData.append('image', file)
  if (currentId.value) {
    formData.append('conversationId', currentId.value)
  }
  formData.append('ttsSpeed', ttsSpeed.value)

  sending.value = true
  streamingContent.value = ''
  stopSynthQueue()
  await nextTick()
  scrollToBottom()

  const assistantMsgId = userMsgId + 1

  streamAbort.value = sendPhotoStream(
    formData,
    // onScenic: 识别结果回来，更新用户消息
    (info) => {
      const msg = messages.value.find(m => m.id === userMsgId)
      if (msg) {
        msg.content = info.userContent
        msg.imageUrl = info.imageUrl || localUrl
      }
      URL.revokeObjectURL(localUrl)
      // 创建空的 AI 占位消息
      messages.value.push({ id: assistantMsgId, role: 'assistant', content: '' })
      scrollToBottom()
    },
    // onToken: 逐字追加
    (token) => {
      const msg = messages.value.find(m => m.id === assistantMsgId)
      if (msg) {
        msg.content += token
        streamingContent.value += token
        nextTick(() => scrollToBottom())
      }
      const allText = messages.value.find(m => m.id === assistantMsgId)?.content || ''
      flushSynthSentences(allText)
    },
    // onDone
    (info) => {
      currentId.value = info.conversationId
      sending.value = false
      streamAbort.value = null
      fetchConversations()
      streamingContent.value = ''

      const msg = messages.value.find(m => m.id === assistantMsgId)
      if (msg?.content) {
        const remaining = msg.content.slice(synthSentLen)
        if (remaining.trim() && ttsEnabled.value && !muted.value) {
          audioPending.value = true
          const idx = synthOrdered.length
          synthOrdered.push(null)
          synthesizeText(stripMarkdown(remaining), ttsSpeed.value).then(url => {
            if (!url) { audioPending.value = false; return }
            synthOrdered[idx] = url
            flushSynthPlayable()
          }).catch(() => { audioPending.value = false })
        }
      }
    },
    // onError
    (err) => {
      ElMessage.error('识别失败: ' + err)
      const msg = messages.value.find(m => m.id === assistantMsgId)
      if (msg && !msg.content) msg.content = '(识别失败，请重试)'
      streamingContent.value = ''
      sending.value = false
      streamAbort.value = null
      stopSynthQueue()
      overrideExpression.value = null
    }
  )
}

// === 图片预览 ===
const imagePreviewVisible = ref(false)
const imagePreviewUrl = ref('')

function previewImage(url) {
  imagePreviewUrl.value = url
  imagePreviewVisible.value = true
}

// === 语音播放 ===
function playAudio(url, msgId) {
  // 如果正在播放同一个，则停止
  if (audioPlaying.value && currentAudio.value === url) {
    stopPlayback()
    return
  }

  const audio = new Audio(url)
  audio.onplay = () => {
    live2dRef.value?.startTalking()
    // 根据消息内容设置表情
    if (msgId) {
      const msg = messages.value.find(m => m.id === msgId)
      if (msg) {
        overrideExpression.value = detectEmotion(msg.content || '')
      }
    }
  }
  audio.onended = () => {
    overrideExpression.value = null
    live2dRef.value?.stopTalking()
    audioPlaying.value = false
    currentAudio.value = ''
    speakingMsgId.value = null
  }
  audio.onerror = () => {
    overrideExpression.value = null
    live2dRef.value?.stopTalking()
    ElMessage.error('语音播放失败')
    audioPlaying.value = false
    currentAudio.value = ''
    speakingMsgId.value = null
  }

  // 停止之前的播放
  stopPlayback()

  currentAudio.value = url
  audioPlaying.value = true
  speakingMsgId.value = msgId || null
  audio.play().catch(() => {
    overrideExpression.value = null
    ElMessage.warning('无法自动播放语音，请点击播放按钮')
    audioPlaying.value = false
    currentAudio.value = ''
    speakingMsgId.value = null
  })

  // 保存 audio 对象以备停止
  window.__currentAudio = audio
}

function stopPlayback() {
  overrideExpression.value = null
  live2dRef.value?.stopTalking()
  if (window.__currentAudio) {
    window.__currentAudio.pause()
    window.__currentAudio = null
  }
  window.speechSynthesis.cancel()
  stopSynthQueue()
  audioPlaying.value = false
  currentAudio.value = ''
  speakingMsgId.value = null
}

function onLive2DError() {
  console.warn('Live2D 模型加载失败，数字人面板将使用静态占位')
}

/** 朗读消息（点击按钮调用）- 拆句并行合成，逐句播放 */
async function speakMessage(msg) {
  if (!ttsEnabled.value) return
  // 点击同一条正在播放的消息 → 停止并返回（toggle）
  if (speakingMsgId.value === msg.id) {
    stopPlayback()
    return
  }
  // 其他消息正在播放 → 先停止，继续播新的
  if (audioPlaying.value) {
    stopPlayback()
  }

  stopSynthQueue()
  // 标记加载状态，让用户看到反馈（实际播放由 playAudio/playNextInQueue 设置 audioPlaying）
  speakingMsgId.value = msg.id

  const text = msg.content
  const sentences = splitSentences(text)

  // 只有一句或无法拆句 — 单次合成
  if (sentences.length <= 1) {
    try {
      const url = await synthesizeText(stripMarkdown(text), ttsSpeed.value)
      if (speakingMsgId.value === msg.id && url) {
        playAudio(url, msg.id)
      } else if (speakingMsgId.value === msg.id) {
        speakText(text, msg.id)
      }
    } catch {
      if (speakingMsgId.value === msg.id) speakText(text, msg.id)
    }
    return
  }

  // 多句并行合成，保持顺序播放
  try {
    const results = await Promise.allSettled(
      sentences.map(s => synthesizeText(s, ttsSpeed.value))
    )
    // 如果用户在此期间点了停止，丢弃结果
    if (speakingMsgId.value !== msg.id) return

    let hasValid = false
    for (const r of results) {
      if (r.status === 'fulfilled' && r.value) {
        synthQueue.value.push(r.value)
        hasValid = true
      }
    }
    if (hasValid) {
      playNextInQueue()
    } else {
      // 全部失败，降级浏览器朗读
      speakText(text, msg.id)
    }
  } catch {
    if (speakingMsgId.value === msg.id) speakText(text, msg.id)
  }
}

/** 拆句：按。！？；\n 分割，最少 1 个字 */
function splitSentences(text) {
  const clean = stripMarkdown(text)
  if (!clean) return []
  const out = []
  let remaining = clean
  while (remaining.length) {
    const m = remaining.match(/^([^。！？；\n]{1,}[。！？；\n])/)
    if (m) {
      out.push(m[1])
      remaining = remaining.slice(m[1].length)
    } else {
      const t = remaining.trim()
      if (t) out.push(t)
      break
    }
  }
  return out
}

/** 浏览器原生语音朗读 */
function speakText(text, msgId) {
  if (!ttsEnabled.value || muted.value) return
  // 停止正在播放的语音
  stopPlayback()

  const cleanText = stripMarkdown(text)
  if (!cleanText) return

  const utterance = new SpeechSynthesisUtterance(cleanText)
  utterance.lang = 'zh-CN'
  utterance.rate = digitalHumanConfig.value?.ttsSpeed || 1.0
  utterance.pitch = 1.0

  utterance.onstart = () => {
    speakingMsgId.value = msgId || null
    audioPlaying.value = true
    live2dRef.value?.startTalking()
    if (msgId) {
      const msg = messages.value.find(m => m.id === msgId)
      if (msg) overrideExpression.value = detectEmotion(msg.content || '')
    }
  }
  utterance.onend = () => {
    overrideExpression.value = null
    speakingMsgId.value = null
    audioPlaying.value = false
    live2dRef.value?.stopTalking()
  }
  utterance.onerror = () => {
    overrideExpression.value = null
    speakingMsgId.value = null
    audioPlaying.value = false
    live2dRef.value?.stopTalking()
  }

  window.speechSynthesis.speak(utterance)
}

onMounted(() => {
  fetchConversations()
  fetchConfig()
  fetchHotSearches()
})

onBeforeUnmount(() => {
  stopPlayback()
  if (streamAbort.value) {
    streamAbort.value.abort()
    streamAbort.value = null
  }
})

// 去除 Markdown 格式，转为纯文本显示
function stripMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/[*_]{1,3}([^*_]+)[*_]{1,3}/g, '$1')
    .replace(/^###?\s+/gm, '')
    .replace(/^>\s+/gm, '')
    .replace(/^[-*+]\s+/gm, '')
    .replace(/^\d+\.\s+/gm, '')
    .replace(/^#{1,6}\s+/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

async function fetchHotSearches() {
  try {
    hotSearches.value = await getHotSearches()
  } catch (e) {
    // 静默失败，热搜非关键功能
  }
}

function rankBadge(i) {
  if (i === 0) return 'TOP 1'
  if (i === 1) return 'TOP 2'
  if (i === 2) return 'TOP 3'
  return ''
}

const HEAT_LABEL_MAP = { bao: '爆', re: '热', huo: '火', wen: '温' }
function heatLabel(key) {
  return HEAT_LABEL_MAP[key] || '温'
}

async function sendHotQuestion(q) {
  startChat()
  await nextTick()
  inputText.value = q
  await handleSend()
}
</script>

<style scoped>
/* ===== 书卷雅集 · 布局 ===== */
.chat-page {
  display: flex;
  height: calc(100vh - 60px);
  max-width: 1400px;
  margin: 0 auto;
  background: var(--paper-warm);
  gap: 0;
}

/* 左栏：历史对话 — 题跋/引首 */
.sidebar-wrapper {
  padding: 16px 0 16px 16px;
  flex-shrink: 0;
}

.sidebar {
  width: 240px;
  height: 100%;
  background: var(--paper-dark);
  border: 1px solid var(--ink-faint);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: var(--shadow-warm);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--ink-faint);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 14px;
  color: var(--ink-black);
  font-family: var(--font-display);
  letter-spacing: 3px;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--ink-light);
  font-size: 14px;
  margin-bottom: 2px;
  transition: all 0.2s;
}

.conv-item:hover {
  background: var(--ink-faint);
  color: var(--ink-dark);
}

.conv-item.active {
  background: rgba(194, 58, 43, 0.06);
  color: var(--vermillion);
}

.conv-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  padding: 6px 0;
}

.delete-btn {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.conv-item:hover .delete-btn {
  opacity: 1;
}

.conv-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 中央：聊天主区（画心） ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--paper-warm);
  border: 1px solid var(--ink-faint);
  border-radius: 8px;
  margin: 16px 8px;
  overflow: hidden;
  min-width: 0;
  position: relative;
  box-shadow: var(--shadow-warm);
}

/* 卷轴杆装饰 — 上下两根深色条 */
.chat-main::before,
.chat-main::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(to bottom, var(--ink-dark), var(--ink-light));
  z-index: 2;
  pointer-events: none;
}
.chat-main::before {
  top: 0;
}
.chat-main::after {
  bottom: 0;
}

/* 欢迎屏 */
.welcome-screen {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  background: var(--paper-warm);
  padding: 40px;
}

.welcome-content {
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.welcome-content h2 {
  font-family: var(--font-display);
  font-size: 32px;
  color: var(--ink-black);
  letter-spacing: 8px;
  margin: 0;
  position: relative;
}

.welcome-content h2::after {
  content: '';
  display: block;
  width: 40px;
  height: 2px;
  background: var(--vermillion);
  margin: 16px auto 0;
  border-radius: 1px;
}

.welcome-content p {
  font-size: 15px;
  color: var(--ink-light);
  line-height: 1.8;
  margin: 0;
}

/* ===== 热搜榜（欢迎屏） ===== */
.welcome-hot {
  margin-top: 40px;
  max-width: 520px;
  width: 100%;
}
.hot-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  justify-content: center;
}
.hot-header-icon {
  font-size: 18px;
}
.hot-header-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-black);
  letter-spacing: 3px;
}
.hot-header-sub {
  font-size: 12px;
  color: var(--ink-mist);
  letter-spacing: 1px;
  margin-top: 2px;
}
.hot-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.hot-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 100px;
  background: var(--paper-dark);
  border: 1px solid var(--ink-faint);
  cursor: pointer;
  transition: all 0.25s ease;
  max-width: 100%;
}
.hot-chip:hover {
  border-color: var(--vermillion);
  background: var(--paper-warm);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(191,63,63,0.1);
}
.chip-rank {
  font-size: 10px;
  font-weight: 700;
  font-family: var(--font-display);
  letter-spacing: 0.5px;
  flex-shrink: 0;
}
.chip-rank.r1 { color: var(--vermillion); }
.chip-rank.r2 { color: var(--gold); }
.chip-rank.r3 { color: var(--jade); }
.chip-text {
  font-size: 13px;
  color: var(--ink-black);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
.chip-heat {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 3px;
  flex-shrink: 0;
}
.chip-heat.heat-bao { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.chip-heat.heat-re { background: #fff7ed; color: #ea580c; border: 1px solid #fed7aa; }
.chip-heat.heat-huo { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
.chip-heat.heat-wen { background: #f8fafc; color: #64748b; border: 1px solid #e2e8f0; }

/* 对话标题栏 */
.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--ink-faint);
  background: var(--paper-dark);
  flex-shrink: 0;
}

.chat-header-name {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-black);
  letter-spacing: 3px;
  margin-right: auto;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px 20px;
  background: var(--paper-warm);
  scroll-behavior: smooth;
}

.message-list::-webkit-scrollbar {
  width: 4px;
}
.message-list::-webkit-scrollbar-thumb {
  background: var(--ink-mist);
  border-radius: 2px;
}

.message {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
  max-width: 80%;
  animation: fadeInMsg 0.3s ease;
}

@keyframes fadeInMsg {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

/* 头像 = 印章风格 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  flex-shrink: 0;
  font-family: var(--font-display);
  letter-spacing: 1px;
  position: relative;
}

.avatar.assistant {
  background: var(--ink-dark);
  border: 1px solid var(--ink-black);
  box-shadow: 0 0 6px rgba(44, 44, 44, 0.15);
}

.avatar.user {
  background: var(--vermillion);
  border: 1px solid var(--vermillion);
  box-shadow: 0 0 6px rgba(194, 58, 43, 0.15);
}

/* 气泡 */
.bubble {
  padding: 12px 18px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.9;
  white-space: pre-wrap;
  position: relative;
}

.message.assistant .bubble {
  background: var(--paper-dark);
  color: var(--ink-black);
  border: 1px solid var(--ink-faint);
  border-left: 3px solid var(--ink-mist);
}

.message.user .bubble {
  background: rgba(194, 58, 43, 0.04);
  color: var(--ink-black);
  border: 1px solid rgba(194, 58, 43, 0.1);
  border-right: 3px solid var(--vermillion);
}

.msg-image {
  max-width: 200px;
  max-height: 180px;
  border-radius: 4px;
  cursor: pointer;
  display: block;
  margin-bottom: 6px;
  transition: transform 0.2s;
  border: 1px solid var(--ink-faint);
}

.msg-image:hover {
  transform: scale(1.02);
}

.msg-text {
  white-space: pre-wrap;
}

.preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-img {
  max-width: 80vw;
  max-height: 80vh;
  display: block;
}

.thinking {
  min-width: 40px;
  text-align: center;
}

.dot {
  animation: dot 1.4s infinite;
  font-size: 24px;
  line-height: 1;
  color: var(--ink-mist);
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dot {
  0%, 60%, 100% { opacity: 0.3; }
  30% { opacity: 1; }
}

/* 输入区 = 砚台 */
.input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--ink-faint);
  background: var(--paper-dark);
  display: flex;
  flex-direction: column;
}

.input-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.input-row .el-textarea {
  flex: 1;
}

.input-row .el-textarea :deep(textarea) {
  background: var(--paper-warm);
  border: 1px solid var(--ink-faint);
  color: var(--ink-black);
  font-family: var(--font-body);
  transition: border-color 0.2s;
}

.input-row .el-textarea :deep(textarea:focus) {
  border-color: var(--ink-mist);
}

.voice-btn-group {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-top: 2px;
}

.mic-btn {
  transition: all 0.3s;
}

.mic-btn.recording {
  animation: mic-pulse 1s ease-in-out infinite;
  box-shadow: 0 0 0 4px rgba(194, 58, 43, 0.2);
}

@keyframes mic-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.recording-hint {
  font-size: 12px;
  color: var(--vermillion);
  margin-top: 4px;
  animation: blink 1.5s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.play-btn {
  margin-left: 8px;
  flex-shrink: 0;
}

.loading-container {
  padding: 40px;
}

/* ===== 右栏：数字人立轴 ===== */
.dh-panel {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 16px 16px 16px 0;
}

.dh-frame {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: var(--shadow-warm);
  border: 1px solid var(--ink-faint);
}

/* 天杆 */
.dh-frame-top {
  height: 18px;
  background: linear-gradient(to bottom, #4a4238, var(--ink-dark));
  position: relative;
  flex-shrink: 0;
}

.dh-frame-top::before,
.dh-frame-top::after {
  content: '';
  position: absolute;
  top: 3px;
  width: 6px;
  height: 12px;
  border-radius: 3px;
  background: linear-gradient(to right, #5a5248, var(--ink-light));
}
.dh-frame-top::before { left: -3px; }
.dh-frame-top::after { right: -3px; }

/* 画心 */
.dh-frame-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--paper-warm);
  padding: 8px;
  min-height: 0;
}

/* 地杆 */
.dh-frame-bottom {
  height: 36px;
  background: linear-gradient(to top, var(--ink-black), var(--ink-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
}

/* 轴头（地杆两端突出的小圆头） */
.dh-frame-bottom::before,
.dh-frame-bottom::after {
  content: '';
  position: absolute;
  top: 4px;
  bottom: 4px;
  width: 8px;
  border-radius: 4px;
  background: linear-gradient(to right, #5a5248, var(--ink-light));
}
.dh-frame-bottom::before { left: -4px; }
.dh-frame-bottom::after { right: -4px; }

.dh-name {
  font-family: var(--font-display);
  font-size: 13px;
  color: var(--gold-light);
  letter-spacing: 6px;
  text-shadow: 0 0 8px rgba(212, 160, 23, 0.2);
  white-space: nowrap;
}
</style>
