<template>
  <div class="digital-human-page">
    <h2 class="page-title">数字人形象管理</h2>
    <el-card shadow="hover">
      <el-form :model="form" label-width="120px" size="large">
        <el-form-item label="数字人名称">
          <el-input v-model="form.name" placeholder="如：灵宝" maxlength="20" />
        </el-form-item>
        <el-form-item label="欢迎语">
          <el-input v-model="form.greeting" type="textarea" :rows="3" placeholder="欢迎语内容" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="性格设定">
          <el-select v-model="form.personality" placeholder="选择性格" style="width:100%">
            <el-option label="温柔知性" value="温柔知性" />
            <el-option label="幽默风趣" value="幽默风趣" />
            <el-option label="沉稳专业" value="沉稳专业" />
            <el-option label="活泼可爱" value="活泼可爱" />
          </el-select>
        </el-form-item>
        <el-form-item label="形象风格">
          <el-select v-model="form.style" placeholder="选择风格" style="width:100%">
            <el-option label="古风典雅" value="古风典雅" />
            <el-option label="现代简约" value="现代简约" />
            <el-option label="禅意素净" value="禅意素净" />
          </el-select>
        </el-form-item>
        <el-form-item label="大模型">
          <el-select v-model="form.model" placeholder="选择模型" style="width:100%">
            <el-option label="通义千问-max" value="qwen-max" />
            <el-option label="通义千问-plus" value="qwen-plus" />
            <el-option label="通义千问-turbo" value="qwen-turbo" />
          </el-select>
        </el-form-item>
        <el-divider />
        <el-form-item label="语音语速">
          <el-slider v-model="form.ttsSpeed" :min="0.5" :max="2.0" :step="0.1" show-stops :marks="{ 0.5:'慢', 1.0:'正常', 1.5:'快', 2.0:'极快' }" style="width:100%" />
        </el-form-item>
        <el-divider />
        <el-form-item label="功能开关">
          <el-switch v-model="form.ttsEnabled" active-text="语音播报（TTS）" />
        </el-form-item>
        <el-form-item>
          <el-switch v-model="form.asrEnabled" active-text="语音输入（ASR）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
          <el-button @click="fetchConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDigitalHumanConfig, updateDigitalHumanConfig } from './api'
import { ElMessage } from 'element-plus'

const saving = ref(false)

const form = reactive({
  name: '灵宝',
  greeting: '你的智能导游，关于灵山胜境和拈花湾的任何问题都可以问我！',
  personality: '温柔知性',
  style: '古风典雅',
  model: 'qwen-max',
  ttsSpeed: 1.0,
  ttsEnabled: true,
  asrEnabled: true
})

async function fetchConfig() {
  try {
    const data = await getDigitalHumanConfig()
    if (data) {
      Object.assign(form, data)
    }
  } catch (e) {
    // 使用默认值
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateDigitalHumanConfig({ ...form })
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchConfig)
</script>

<style scoped>
.digital-human-page { padding: 0; }
.el-form { max-width: 600px; }
</style>
