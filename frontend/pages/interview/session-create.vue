<!-- 创建面试 - 响应式 -->
<template>
  <PageLayout currentPage="create" title="创建面试">
    <view class="content">
      <view class="hero-icon"><text class="big-icon">🎤</text></view>
      <text class="hero-text">准备开始一场AI模拟面试</text>
      <view class="form-card glass-strong">
        <!-- 面试配置：picker 包住选择框，点击即可弹出 -->
        <picker :range="configs" range-key="name" :disabled="!configs.length" @change="onConfigPick">
          <view class="select-item">
            <text class="select-label">⚙️ 面试配置</text>
            <view class="select-value">
              <text :class="selectedConfig ? '' : 'placeholder'">{{ selectedConfig?.name || '请选择' }}</text>
              <text class="select-arrow">▼</text>
            </view>
          </view>
        </picker>
        <view v-if="!configs.length" class="empty-tip" @tap="goConfigForm">还没有面试配置，点击去创建 →</view>

        <!-- 关联简历：可选，第一项为「不关联」 -->
        <picker :range="resumeOptions" range-key="label" :disabled="!resumes.length" @change="onResumePick">
          <view class="select-item">
            <text class="select-label">📄 关联简历</text>
            <view class="select-value">
              <text :class="selectedResume ? '' : 'placeholder'">{{ selectedResume?.originalFilename || '可选' }}</text>
              <text class="select-arrow">▼</text>
            </view>
          </view>
        </picker>
        <view v-if="!resumes.length" class="empty-tip" @tap="goResumeList">还没有简历，点击去上传 →</view>

        <view class="btn-primary" :class="{ 'btn-disabled': !selectedConfig }" @tap="create"><text>🚀 开始面试</text></view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import interviewApi from '@/api/interview.js'
import resumeApi from '@/api/resume.js'

const configs = ref([])
const resumes = ref([])
const selectedConfig = ref(null)
const selectedResume = ref(null)

// 简历选择：第一项固定为「不关联简历」
const resumeOptions = computed(() => [
  { id: null, label: '（不关联简历）' },
  ...resumes.value.map((r) => ({ id: r.id, label: r.originalFilename || ('简历 #' + r.id) })),
])

onMounted(async () => {
  try {
    configs.value = (await interviewApi.listConfigs()) || []
  } catch (e) {
    configs.value = []
  }
  try {
    resumes.value = (await resumeApi.list()) || []
  } catch (e) {
    resumes.value = []
  }
  // 只有一条配置时自动选中，减少操作
  if (configs.value.length === 1) {
    selectedConfig.value = configs.value[0]
  }
})

const onConfigPick = (e) => {
  selectedConfig.value = configs.value[Number(e.detail.value)] || null
}

const onResumePick = (e) => {
  const idx = Number(e.detail.value)
  selectedResume.value = idx === 0 ? null : resumes.value[idx - 1] || null
}

const goConfigForm = () => uni.navigateTo({ url: '/pages/interview/config-form' })
const goResumeList = () => uni.navigateTo({ url: '/pages/resume/list' })

const create = async () => {
  if (!selectedConfig.value) {
    uni.showToast({ title: '请选择配置', icon: 'none' })
    return
  }
  try {
    uni.showLoading({ title: '创建中...' })
    const session = await interviewApi.createSession({
      configId: selectedConfig.value.id,
      resumeId: selectedResume.value?.id || null,
    })
    uni.hideLoading()
    // 后端返回会话 ID（Long），兼容对象形式
    const sid = session && typeof session === 'object' ? (session.id ?? session.sessionId) : session
    uni.navigateTo({ url: '/pages/interview/session-room?sessionId=' + sid })
  } catch (e) {
    uni.hideLoading()
  }
}
</script>

<style scoped>
.content { padding-bottom: 60rpx; display: flex; flex-direction: column; align-items: center; }
@media (min-width: 768px) { .content { max-width: 600px; margin: 0 auto; } }
.hero-icon { margin: 40rpx 0 16rpx; }
.big-icon { font-size: 90rpx; display: block; }
.hero-text { font-size: 28rpx; color: #6b7280; margin-bottom: 36rpx; }
.form-card { width: 100%; padding: 32rpx; }
.select-item { margin-bottom: 24rpx; }
.select-label { font-size: 26rpx; color: #6b7280; font-weight: 600; display: block; margin-bottom: 10rpx; }
.select-value { display: flex; justify-content: space-between; align-items: center; height: 84rpx; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.4); border-radius: 16rpx; padding: 0 20rpx; font-size: 28rpx; color: #1e1b4b; }
.placeholder { color: #b0b7c3; }
.select-arrow { font-size: 20rpx; color: #9ca3af; }
.empty-tip { font-size: 24rpx; color: #f472b6; font-weight: 600; margin: -10rpx 0 22rpx; }
.btn-primary { margin-top: 28rpx; height: 92rpx; background: linear-gradient(135deg, #f472b6 0%, #f97316 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 30rpx; font-weight: 700; box-shadow: 0 10rpx 28rpx rgba(244,114,182,0.3); transition: all 0.3s; }
.btn-primary:active { transform: scale(0.96); }
.btn-primary.btn-disabled { opacity: 0.5; }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
</style>
