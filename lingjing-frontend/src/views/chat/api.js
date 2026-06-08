import request from '../../api'

import { fetchEventSource } from '@microsoft/fetch-event-source'

const BASE = import.meta.env.VITE_API_BASE || ''

function getAuthHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function sendMessage(data) {
  return request({
    url: '/chat/send',
    method: 'post',
    data
  })
}

/**
 * 流式拍照识景：视觉识别后 LLM 讲解逐 token 推送
 * @param {FormData} formData - 包含 image, conversationId, ttsSpeed
 * @param {Function} onScenic - 识别结果回调，参数 { scenicName, imageUrl, userContent }
 * @param {Function} onToken - 每个 token 的回调
 * @param {Function} onDone - 完成回调，参数 { conversationId }
 * @param {Function} onError - 错误回调
 */
export function sendPhotoStream(formData, onScenic, onToken, onDone, onError) {
  const controller = new AbortController()
  fetchEventSource(`${BASE}/api/chat/recognize-scenic/stream`, {
    method: 'POST',
    headers: getAuthHeaders(), // Content-Type 由 fetch 自动设置 multipart
    body: formData,
    signal: controller.signal,
    openWhenHidden: true,
    onmessage(event) {
      if (event.event === 'scenic') {
        onScenic(JSON.parse(event.data))
      } else if (event.event === 'token') {
        onToken(event.data)
      } else if (event.event === 'done') {
        const info = JSON.parse(event.data)
        onDone(info)
      } else if (event.event === 'error') {
        onError(event.data)
      }
    },
    onerror(err) {
      onError(err.message || '连接中断')
      throw err
    }
  })
  return controller
}

export function sendMessageStream(data, onToken, onDone, onError) {
  const controller = new AbortController()
  fetchEventSource(`${BASE}/api/chat/send/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders()
    },
    body: JSON.stringify(data),
    signal: controller.signal,
    openWhenHidden: true,
    onmessage(event) {
      if (event.event === 'token') {
        onToken(event.data)
      } else if (event.event === 'done') {
        const info = JSON.parse(event.data)
        onDone(info)
      } else if (event.event === 'error') {
        onError(event.data)
      }
    },
    onerror(err) {
      onError(err.message || '连接中断')
      throw err // 停止重连
    },
    onclose() {
      // stream ended
    }
  })
  return controller
}

export function getConversations() {
  return request({
    url: '/chat/conversations',
    method: 'get'
  })
}

export function getMessages(conversationId) {
  return request({
    url: `/chat/conversations/${conversationId}/messages`,
    method: 'get'
  })
}

export function deleteConversation(conversationId) {
  return request({
    url: `/chat/conversations/${conversationId}`,
    method: 'delete'
  })
}

export function sendPhoto(formData) {
  return request({
    url: '/chat/recognize-scenic',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 180000
  })
}

export function sendVoice(formData) {
  return request({
    url: '/chat/send-voice',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 180000
  })
}

export function synthesizeTts(conversationId, speed) {
  return request({
    url: `/chat/tts/${conversationId}`,
    method: 'get',
    params: { speed },
    timeout: 60000
  })
}

export function synthesizeText(content, speed) {
  return request({
    url: '/chat/tts',
    method: 'post',
    data: { content, speed },
    timeout: 60000
  })
}

export function getHotSearches() {
  return request.get('/hot-searches')
}
