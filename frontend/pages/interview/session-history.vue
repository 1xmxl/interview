<!-- 面试记录 - 单会话问答历史（结构化渲染 + 评估入口） -->
<template>
  <PageLayout currentPage="history" title="面试记录">
    <view class="content">
      <!-- 评估入口 -->
      <view class="eval-entry glass-card" @tap="goResult">
        <text class="eval-entry-icon">📊</text>
        <view class="eval-entry-body">
          <text class="eval-entry-title">查看本次面试评估报告</text>
          <text class="eval-entry-desc">各维度评分、AI 点评与提升建议</text>
        </view>
        <text class="eval-entry-arrow">→</text>
      </view>

      <view v-if="loading"><SkeletonCard :lines="5" /></view>

      <view v-else-if="history.length > 0" class="chat-list">
        <view v-for="(item, idx) in history" :key="item.questionId || idx" class="chat-item" :class="{ 'stagger-children': idx < 3 }">
          <view class="chat-bubble glass-card bubble-q">
            <text class="bubble-role">🤖 面试官</text>
            <text class="bubble-text">{{ item.questionText || item.question }}</text>
          </view>
          <view class="chat-bubble glass-card bubble-a">
            <text class="bubble-role">🙋 你的回答</text>
            <text class="bubble-text">{{ item.answerText || item.answer }}</text>
          </view>
        </view>
      </view>

      <EmptyState v-else icon="💬" title="暂无记录" description="完成面试后可查看问答历史" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import interviewApi from '@/api/interview.js'
let sessionId = ''
const history = ref([])
const loading = ref(true)
onLoad((opt) => { sessionId = opt.sessionId || ''; loadData() })
const loadData = async () => {
  loading.value = true
  try { history.value = await interviewApi.getHistory(sessionId) || [] } catch (e) { history.value = [] } finally { loading.value = false }
}
const goResult = () => {
  if (sessionId) uni.navigateTo({ url: '/pages/evaluation/result?sessionId=' + sessionId })
  else uni.navigateTo({ url: '/pages/evaluation/result' })
}
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
@media (min-width: 768px) { .content { max-width: 720px; margin: 0 auto; } }

.eval-entry { display: flex; align-items: center; gap: 16rpx; padding: 22rpx; margin-bottom: 24rpx; border-left: 6rpx solid #7c5cfc; }
.eval-entry-icon { font-size: 34rpx; }
.eval-entry-body { flex: 1; }
.eval-entry-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; }
.eval-entry-desc { font-size: 22rpx; color: #9ca3af; display: block; margin-top: 4rpx; }
.eval-entry-arrow { font-size: 30rpx; color: #7c5cfc; font-weight: 700; }

.chat-list { display: flex; flex-direction: column; gap: 18rpx; }
.chat-item { display: flex; flex-direction: column; gap: 14rpx; }
.chat-bubble { padding: 22rpx; }
.bubble-q { border-left: 6rpx solid #7c5cfc; }
.bubble-a { border-left: 6rpx solid #f472b6; }
.bubble-role { font-size: 24rpx; font-weight: 600; color: #6b7280; display: block; margin-bottom: 8rpx; }
.bubble-text { font-size: 28rpx; color: #1e1b4b; line-height: 1.6; display: block; }
</style>