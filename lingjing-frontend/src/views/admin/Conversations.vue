<template>
  <div class="conversations-page">
    <h2 class="page-title">对话日志</h2>

    <el-card shadow="hover">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索对话标题" clearable style="width: 300px;" @clear="search" @keyup.enter="search" />
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <el-table :data="conversations" stripe style="width: 100%; margin-top: 16px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="对话标题" min-width="200" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="updatedAt" label="最后活跃" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewMessages(row.id)">查看消息</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadConversations"
        />
      </div>
    </el-card>

    <!-- 消息查看对话框 -->
    <el-dialog v-model="dialogVisible" title="对话消息" width="700px" top="5vh">
      <div class="messages-container">
        <div v-for="msg in messages" :key="msg.id" :class="['msg-item', msg.role]">
          <div class="msg-role">{{ msg.role === 'user' ? '用户' : 'AI导游' }}</div>
          <div class="msg-content">{{ stripMarkdown(msg.content) }}</div>
          <div class="msg-time">{{ msg.createdAt }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getConversations, getConversationMessages } from './api'

const keyword = ref('')
const conversations = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const messages = ref([])

async function loadConversations() {
  try {
    const res = await getConversations({ page: page.value, pageSize: pageSize.value, keyword: keyword.value })
    conversations.value = res.records
    total.value = res.total
  } catch (e) {
    console.error('加载对话列表失败', e)
  }
}

function search() {
  page.value = 1
  loadConversations()
}

async function viewMessages(id) {
  try {
    const res = await getConversationMessages(id)
    messages.value = res.messages
    dialogVisible.value = true
  } catch (e) {
    console.error('加载消息失败', e)
  }
}

onMounted(loadConversations)

function stripMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/#{1,6}\s+/g, '')
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`(.+?)`/g, '$1')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/>\s+/g, '')
    .replace(/^[-*+]\s+/gm, '')
    .replace(/^\d+\.\s+/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}
</script>

<style scoped>
.conversations-page { padding: 0; }

.messages-container { max-height: 500px; overflow-y: auto; }
.msg-item { padding: 14px 16px; margin-bottom: 10px; border-radius: 8px; border: 1px solid transparent; }
.msg-item.user { background: rgba(46,125,246,0.04); border-color: rgba(46,125,246,0.08); }
.msg-item.assistant { background: rgba(6,182,212,0.04); border-color: rgba(6,182,212,0.08); }
.msg-role { font-weight: 600; font-size: 13px; color: var(--text-muted); margin-bottom: 6px; }
.msg-content { font-size: 14px; line-height: 1.7; white-space: pre-wrap; color: var(--text-ink); }
.msg-time { font-size: 12px; color: var(--text-light); margin-top: 6px; }
</style>
