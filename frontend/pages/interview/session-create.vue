<!-- 创建面试 - 响应式 -->
<template>
  <PageLayout currentPage="create" title="创建面试">
    <view class="content">
      <view class="hero-icon"><text class="big-icon">🎤</text></view>
      <text class="hero-text">准备开始一场AI模拟面试</text>
      <view class="form-card glass-strong">
        <view class="select-item" @tap="showConfig = true"><text class="select-label">⚙️ 面试配置</text><view class="select-value"><text :class="selectedConfig ? '' : 'placeholder'">{{ selectedConfig?.name || '请选择' }}</text><text class="select-arrow">▼</text></view></view>
        <view class="select-item" @tap="showResume = true"><text class="select-label">📄 关联简历</text><view class="select-value"><text :class="selectedResume ? '' : 'placeholder'">{{ selectedResume?.originalFilename || '可选' }}</text><text class="select-arrow">▼</text></view></view>
        <view class="btn-primary" @tap="create"><text>🚀 开始面试</text></view>
      </view>
      <picker v-if="showConfig" :range="configs" range-key="name" @change="onConfigPick" @cancel="showConfig=false" />
      <picker v-if="showResume" :range="resumes" range-key="originalFilename" @change="onResumePick" @cancel="showResume=false" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import interviewApi from '@/api/interview.js'
import resumeApi from '@/api/resume.js'
const configs = ref([]), resumes = ref([]), selectedConfig = ref(null), selectedResume = ref(null), showConfig = ref(false), showResume = ref(false)
onMounted(async () => { try { configs.value = await interviewApi.listConfigs() || [] } catch (e) {}; try { resumes.value = await resumeApi.list() || [] } catch (e) {} })
const onConfigPick = (e) => { selectedConfig.value = configs.value[e.detail.value]; showConfig.value = false }
const onResumePick = (e) => { selectedResume.value = resumes.value[e.detail.value]; showResume.value = false }
const create = async () => { if (!selectedConfig.value) { uni.showToast({ title: '请选择配置', icon: 'none' }); return }; try { uni.showLoading({ title: '创建中...' }); const session = await interviewApi.createSession({ configId: selectedConfig.value.id, resumeId: selectedResume.value?.id || null }); uni.hideLoading(); uni.navigateTo({ url: '/pages/interview/session-room?sessionId=' + session.id }) } catch (e) { uni.hideLoading() } }
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
.btn-primary { margin-top: 28rpx; height: 92rpx; background: linear-gradient(135deg, #f472b6 0%, #f97316 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 30rpx; font-weight: 700; box-shadow: 0 10rpx 28rpx rgba(244,114,182,0.3); transition: all 0.3s; }
.btn-primary:active { transform: scale(0.96); }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
</style>
