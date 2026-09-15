<!-- 答题详情 - 响应式 -->
<template>
  <PageLayout currentPage="evaluation" title="答题详情">
    <view class="content">
      <view v-if="loading"><SkeletonCard :lines="5" /></view>
      <view v-else class="detail-sections">
        <view class="section-card glass-card"><text class="sec-label">💬 问题</text><text class="sec-content-lg">{{ detail.question || '--' }}</text></view>
        <view class="section-card glass-card"><text class="sec-label">🙋 你的回答</text><text class="sec-content">{{ detail.answer || '--' }}</text></view>
        <view class="scores-row">
          <view class="mini-score glass-light"><text class="ms-val gradient-text">{{ detail.technicalScore || detail.score || '--' }}</text><text class="ms-label">技术评分</text></view>
          <view class="mini-score glass-light"><text class="ms-val gradient-text-cool">{{ detail.clarityScore || '--' }}</text><text class="ms-label">清晰度</text></view>
          <view class="mini-score glass-light"><text class="ms-val gradient-text-warm">{{ detail.depthScore || '--' }}</text><text class="ms-label">深度</text></view>
        </view>
        <view v-if="detail.feedbackText" class="section-card glass-card"><text class="sec-label">📝 AI 反馈</text><text class="sec-content">{{ detail.feedbackText }}</text></view>
        <view v-if="detail.improvementSuggestions" class="section-card glass-card"><text class="sec-label">💡 提升建议</text><text class="sec-content">{{ detail.improvementSuggestions }}</text></view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import evaluationApi from '@/api/evaluation.js'
let sessionId = '', answerId = ''
const loading = ref(true), detail = ref({})
onLoad((opt) => { sessionId = opt.sessionId || ''; answerId = opt.answerId || ''; loadData() })
const loadData = async () => { if (!sessionId || !answerId) { loading.value = false; return }; try { detail.value = await evaluationApi.getAnswerDetail(sessionId, answerId) || {} } catch (e) { detail.value = {} } finally { loading.value = false } }
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
@media (min-width: 768px) { .content { max-width: 700px; margin: 0 auto; } }
.section-card { padding: 22rpx; margin-bottom: 16rpx; }
.sec-label { font-size: 26rpx; font-weight: 700; color: #7c5cfc; display: block; margin-bottom: 10rpx; }
.sec-content { font-size: 28rpx; color: #6b7280; line-height: 1.7; display: block; }
.sec-content-lg { font-size: 30rpx; color: #1e1b4b; line-height: 1.7; font-weight: 600; display: block; }
.scores-row { display: flex; gap: 14rpx; margin-bottom: 16rpx; }
.mini-score { flex: 1; padding: 22rpx 14rpx; border-radius: 16rpx; display: flex; flex-direction: column; align-items: center; }
.ms-val { font-size: 36rpx; font-weight: 800; }
.ms-label { font-size: 22rpx; color: #9ca3af; margin-top: 4rpx; }
</style>
