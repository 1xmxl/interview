<!-- 面试房间 - 响应式（AI 逐题提问 + 即时点评） -->
<template>
  <PageLayout currentPage="room" title="模拟面试">
    <view class="content">
      <!-- 等待开始 -->
      <view v-if="!started" class="wait-section animate-fadeInUp">
        <view class="wait-icon"><text class="wait-emoji">🤖</text></view>
        <text class="wait-title">AI 面试官已就绪</text>
        <text class="wait-desc">共 {{ totalQuestions }} 题 · 请认真作答，AI 将逐题点评</text>
        <view class="start-btn" @tap="startInterview"><text class="start-text">▶ 开始面试</text></view>
      </view>

      <!-- 面试中 -->
      <view v-else class="interview-section">
        <view class="progress-bar"><view class="progress-fill" :style="{width: progressPercent + '%'}" /></view>
        <text class="progress-text">第 {{ currentIndex + 1 }} / {{ totalQuestions }} 题</text>

        <view class="question-card glass-strong animate-fadeInUp" :key="currentIndex">
          <view class="question-badge">💬 当前问题</view>
          <text class="question-text">{{ currentQuestion }}</text>
        </view>

        <!-- AI 即时点评（上题反馈） -->
        <view v-if="aiComment" class="comment-card glass-card">
          <text class="comment-label">🤖 AI 点评</text>
          <text class="comment-text">{{ aiComment }}</text>
        </view>

        <view class="answer-card glass-card">
          <textarea v-model="answerText" class="answer-input" placeholder="在这里输入你的回答..." placeholder-style="color:#b0b7c3" :maxlength="5000" />
          <view class="answer-count">{{ answerText.length }}/5000</view>
        </view>

        <view class="action-row">
          <view class="btn-primary" :class="{ submitting }" @tap="submitAnswer">
            <text>{{ submitting ? '⏳ AI 思考中...' : (finished ? '📊 提交并查看结果' : '📤 提交答案') }}</text>
          </view>
          <view class="btn-ghost" @tap="goSpeech"><text>🎙️ 语音练习</text></view>
        </view>
        <view class="end-btn" @tap="endInterview"><text>⏹ 结束面试</text></view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import interviewApi from '@/api/interview.js'

let sessionId = ''
const started = ref(false)
const currentQuestion = ref('')
const currentIndex = ref(0)
const totalQuestions = ref(5)
const answerText = ref('')
const aiComment = ref('')
const finished = ref(false)
const submitting = ref(false)

const progressPercent = computed(() =>
  totalQuestions.value === 0 ? 0 : Math.round((currentIndex.value / totalQuestions.value) * 100))

onLoad((opt) => { sessionId = opt.sessionId || '' })

const startInterview = async () => {
  try {
    uni.showLoading({ title: 'AI 出题中...' })
    const res = await interviewApi.startSession(sessionId)
    currentQuestion.value = res.question || res.firstQuestion || '请简单介绍一下你自己'
    if (res.totalQuestions) totalQuestions.value = res.totalQuestions
    uni.hideLoading()
    started.value = true
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '加载失败，请重试', icon: 'none' })
  }
}

const submitAnswer = async () => {
  if (submitting.value) return
  if (!answerText.value.trim()) { uni.showToast({ title: '请输入回答', icon: 'none' }); return }
  submitting.value = true
  try {
    const res = await interviewApi.submitAnswer(sessionId, { text: answerText.value })
    answerText.value = ''
    aiComment.value = res.comment || ''
    if (res.hasNext && res.nextQuestion) {
      currentIndex.value = res.currentQuestionIndex != null ? res.currentQuestionIndex : currentIndex.value + 1
      currentQuestion.value = res.nextQuestion
    } else {
      finished.value = true
      uni.showModal({
        title: '面试完成 🎉',
        content: '所有问题已作答完毕，是否立即查看评估报告？',
        confirmText: '查看报告',
        cancelText: '稍后再说',
        success: (r) => { if (r.confirm) goResult() }
      })
    }
  } catch (e) {
    uni.showToast({ title: '提交失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const endInterview = () => {
  uni.showModal({
    title: '确认结束',
    content: '结束面试后将生成评估报告',
    success: async (r) => {
      if (r.confirm) {
        try { await interviewApi.endSession(sessionId) } catch (e) {}
        uni.showToast({ title: '面试已结束', icon: 'none' })
        setTimeout(() => goResult(), 600)
      }
    }
  })
}

const goResult = () => uni.navigateTo({ url: '/pages/evaluation/result?sessionId=' + sessionId })
const goSpeech = () => uni.navigateTo({ url: '/pages/feedback/speech?sessionId=' + sessionId })
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
@media (min-width: 768px) { .content { max-width: 720px; margin: 0 auto; } }

.wait-section { display: flex; flex-direction: column; align-items: center; padding-top: 100rpx; }
.wait-icon { margin-bottom: 28rpx; animation: float 4s ease-in-out infinite; }
.wait-emoji { font-size: 120rpx; display: block; }
.wait-title { font-size: 36rpx; font-weight: 800; color: #1e1b4b; margin-bottom: 10rpx; }
.wait-desc { font-size: 26rpx; color: #9ca3af; margin-bottom: 48rpx; }
.start-btn { width: 100%; max-width: 400rpx; height: 96rpx; background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; box-shadow: 0 12rpx 36rpx rgba(124,92,252,0.35); transition: all 0.3s; }
.start-btn:active { transform: scale(0.95); }
.start-text { color: #fff; font-size: 34rpx; font-weight: 700; }

.progress-bar { height: 8rpx; background: rgba(0,0,0,0.06); border-radius: 4rpx; overflow: hidden; margin-bottom: 6rpx; }
.progress-fill { height: 100%; border-radius: 4rpx; background: linear-gradient(90deg, #7c5cfc, #4facfe); transition: width 0.5s; }
.progress-text { font-size: 24rpx; color: #6b7280; text-align: center; display: block; margin-bottom: 22rpx; }

.question-card { padding: 28rpx; margin-bottom: 18rpx; }
.question-badge { font-size: 24rpx; color: #7c5cfc; font-weight: 600; display: block; margin-bottom: 14rpx; }
.question-text { font-size: 30rpx; font-weight: 600; color: #1e1b4b; line-height: 1.6; }

.comment-card { padding: 22rpx; margin-bottom: 18rpx; border-left: 6rpx solid #34d399; }
.comment-label { font-size: 24rpx; color: #34d399; font-weight: 700; display: block; margin-bottom: 8rpx; }
.comment-text { font-size: 26rpx; color: #6b7280; line-height: 1.6; }

.answer-card { padding: 22rpx; margin-bottom: 18rpx; }
.answer-input { width: 100%; min-height: 220rpx; font-size: 28rpx; color: #1e1b4b; line-height: 1.6; background: transparent; }
.answer-count { text-align: right; font-size: 22rpx; color: #9ca3af; margin-top: 6rpx; }

.action-row { display: flex; gap: 14rpx; margin-bottom: 18rpx; }
.action-row .btn-primary { flex: 1.5; height: 88rpx; background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); border-radius: 24rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28rpx; font-weight: 700; transition: all 0.3s; }
.action-row .btn-primary:active { transform: scale(0.96); }
.action-row .btn-primary.submitting { opacity: 0.7; }
.btn-ghost { flex: 1; height: 88rpx; border: 2rpx solid rgba(124,92,252,0.2); border-radius: 24rpx; display: flex; align-items: center; justify-content: center; color: #7c5cfc; font-size: 26rpx; font-weight: 600; }
.end-btn { height: 76rpx; border: 2rpx solid rgba(248,113,113,0.25); border-radius: 20rpx; display: flex; align-items: center; justify-content: center; color: #f87171; font-size: 28rpx; font-weight: 600; }

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-16rpx); } }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
</style>