<!-- 配置表单 - 响应式（字段与后端 ConfigSubbmit 对齐） -->
<template>
  <PageLayout currentPage="config" :title="isEdit ? '编辑配置' : '新建配置'">
    <view class="content">
      <view class="form-card glass-strong">
        <view class="form-item">
          <text class="form-label">⚙️ 配置名称</text>
          <input v-model="form.name" class="glass-input" placeholder="例如：Java后端面试" placeholder-style="color:#b0b7c3" />
        </view>

        <view class="form-item">
          <text class="form-label">📝 描述</text>
          <input v-model="form.description" class="glass-input" placeholder="这套配置的用途说明（可选）" placeholder-style="color:#b0b7c3" />
        </view>

        <view class="form-item">
          <text class="form-label">🎭 面试官风格</text>
          <view class="chip-row">
            <view
              v-for="o in styleOptions"
              :key="o.value"
              class="chip"
              :class="{ active: form.interviewerStyle === o.value }"
              @tap="form.interviewerStyle = o.value"
            >
              <text class="chip-text">{{ o.label }}</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">📊 难度（决定题数：简单 3 / 中等 5 / 困难 8）</text>
          <view class="chip-row">
            <view
              v-for="o in difficultyOptions"
              :key="o.value"
              class="chip"
              :class="{ active: form.difficulty === o.value }"
              @tap="form.difficulty = o.value"
            >
              <text class="chip-text">{{ o.label }}</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">🌐 面试语言</text>
          <view class="chip-row">
            <view
              v-for="o in languageOptions"
              :key="o.value"
              class="chip"
              :class="{ active: form.language === o.value }"
              @tap="form.language = o.value"
            >
              <text class="chip-text">{{ o.label }}</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">🎯 目标技能（逗号分隔）</text>
          <input v-model="skillsText" class="glass-input" placeholder="Java, Spring Boot, Redis" placeholder-style="color:#b0b7c3" />
        </view>

        <view class="form-item">
          <text class="form-label">📚 题型类别（逗号分隔）</text>
          <input v-model="categoriesText" class="glass-input" placeholder="基础知识, 框架原理, 系统设计" placeholder-style="color:#b0b7c3" />
        </view>

        <view class="btn-primary" @tap="save"><text>{{ isEdit ? '💾 保存修改' : '✨ 创建配置' }}</text></view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import interviewApi from '@/api/interview.js'

let configId = ''
const isEdit = ref(false)
const form = ref({
  name: '',
  description: '',
  interviewerStyle: 'FRIENDLY',
  difficulty: 'MEDIUM',
  language: 'ZH',
})
const skillsText = ref('')
const categoriesText = ref('')

const styleOptions = [
  { value: 'FRIENDLY', label: '🙂 友好' },
  { value: 'STRICT', label: '😐 严格' },
  { value: 'PRESSURE', label: '🔥 压力' },
]
const difficultyOptions = [
  { value: 'EASY', label: '简单' },
  { value: 'MEDIUM', label: '中等' },
  { value: 'HARD', label: '困难' },
]
const languageOptions = [
  { value: 'ZH', label: '中文' },
  { value: 'EN', label: 'English' },
]

onLoad((opt) => {
  if (opt.id) {
    configId = opt.id
    isEdit.value = true
    loadData()
  }
})

const loadData = async () => {
  try {
    const data = await interviewApi.getConfig(configId)
    form.value = {
      name: data.name || '',
      description: data.description || '',
      interviewerStyle: data.interviewerStyle || 'FRIENDLY',
      difficulty: data.difficulty || 'MEDIUM',
      language: data.language || 'ZH',
    }
    skillsText.value = (data.targetSkills || []).join(', ')
    categoriesText.value = (data.questionCategories || []).join(', ')
  } catch (e) {}
}

// 逗号（中英文）分隔 → 数组，与后端 List<String> 对齐
const splitList = (text) =>
  (text || '')
    .split(/[,，、]/)
    .map((s) => s.trim())
    .filter(Boolean)

const save = async () => {
  if (!form.value.name) {
    uni.showToast({ title: '请填写配置名称', icon: 'none' })
    return
  }
  const payload = {
    ...form.value,
    targetSkills: splitList(skillsText.value),
    questionCategories: splitList(categoriesText.value),
  }
  try {
    if (isEdit.value) {
      await interviewApi.updateConfig(configId, payload)
    } else {
      await interviewApi.createConfig(payload)
    }
    uni.showToast({ title: '已保存 ✅', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {}
}
</script>

<style scoped>
.content { padding-bottom: 60rpx; }
@media (min-width: 768px) { .content { max-width: 600px; margin: 0 auto; } }
.form-card { padding: 36rpx; }
.form-item { margin-bottom: 24rpx; }
.form-label { font-size: 26rpx; color: #6b7280; font-weight: 600; display: block; margin-bottom: 10rpx; }
.glass-input { width: 100%; height: 84rpx; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border: 1px solid rgba(0,0,0,0.06); border-radius: 16rpx; padding: 0 20rpx; font-size: 28rpx; color: #1e1b4b; box-sizing: border-box; }
.chip-row { display: flex; flex-wrap: wrap; gap: 12rpx; }
.chip { padding: 12rpx 26rpx; border-radius: 999rpx; background: rgba(255,255,255,0.45); border: 1px solid rgba(0,0,0,0.06); }
.chip.active { background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); border-color: transparent; box-shadow: 0 6rpx 18rpx rgba(124,92,252,0.25); }
.chip-text { font-size: 26rpx; color: #6b7280; font-weight: 600; }
.chip.active .chip-text { color: #fff; }
.btn-primary { margin-top: 32rpx; height: 92rpx; background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 30rpx; font-weight: 700; box-shadow: 0 10rpx 28rpx rgba(124,92,252,0.3); transition: all 0.3s; }
.btn-primary:active { transform: scale(0.96); }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
</style>
