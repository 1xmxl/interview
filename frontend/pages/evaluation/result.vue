<!-- 评估结果 - 移动全宽 + 桌面双栏报告式布局 -->
<template>
  <PageLayout currentPage="evaluation" title="评估结果">
    <view class="content">
      <view v-if="loading"><SkeletonCard :lines="4" /></view>
      <view v-else-if="evaluation" class="eval-layout">
        <!-- 左侧：报告主体 -->
        <view class="eval-main">
          <view class="overview-card glass-strong">
            <view class="ov-left">
              <view class="ov-ring">
                <text class="ov-score gradient-text">{{ overallScore }}</text>
                <text class="ov-unit">/100</text>
              </view>
            </view>
            <view class="ov-right">
              <text class="ov-grade">{{ gradeText }}</text>
              <text class="ov-summary">{{ evaluation.summary || '面试评估已完成' }}</text>
            </view>
          </view>

          <text class="sec-title">📊 各维度评分</text>
          <view class="resp-grid col-2 dims-grid">
            <view v-for="dim in dimensions" :key="dim.key" class="dim-card glass-card">
              <view class="dim-head">
                <text class="dim-icon">{{ dim.icon }}</text>
                <text class="dim-name">{{ dim.label }}</text>
                <text class="dim-score">{{ dim.score }}</text>
              </view>
              <view class="dim-bar"><view class="dim-fill" :class="dim.colorClass" :style="{width:dim.score+'%'}" /></view>
            </view>
          </view>

          <view v-if="evaluation.strengths" class="text-card glass-card">
            <text class="sec-title">💪 优势</text>
            <text class="text-body">{{ evaluation.strengths }}</text>
          </view>
          <view v-if="evaluation.weaknesses" class="text-card glass-card">
            <text class="sec-title">🎯 待提升</text>
            <text class="text-body">{{ evaluation.weaknesses }}</text>
          </view>

          <view v-if="evaluation.answers && evaluation.answers.length" class="text-card glass-card">
            <text class="sec-title">📝 答题详情</text>
            <view v-for="(a, i) in evaluation.answers" :key="a.answerId" class="answer-row" @tap="goAnswerDetail(a.answerId)">
              <text class="answer-num">{{ i + 1 }}</text>
              <text class="answer-q">{{ a.question }}</text>
              <text class="answer-go">查看 →</text>
            </view>
          </view>
        </view>

        <!-- 右侧：操作面板（桌面端） -->
        <view class="eval-sidebar hide-mobile">
          <view class="sb-card glass-strong">
            <text class="sb-title">操作</text>
            <view class="sb-btn primary" @tap="regenerate">🔄 重新生成报告</view>
            <view class="sb-btn secondary" @tap="goHistory">📋 查看答题记录</view>
          </view>
          <view class="sb-card glass-card">
            <text class="sb-title">面试信息</text>
            <view class="sb-row"><text class="sb-l">总分</text><text class="sb-v gradient-text">{{ overallScore }}</text></view>
            <view class="sb-row"><text class="sb-l">维度</text><text class="sb-v">{{ dimensions.length }} 项</text></view>
            <view class="sb-row"><text class="sb-l">等级</text><text class="sb-v">{{ gradeText }}</text></view>
          </view>
        </view>

        <!-- 移动端按钮 -->
        <view class="mobile-actions hide-desktop">
          <view class="btn-primary" @tap="regenerate">🔄 重新生成报告</view>
          <view class="btn-outline" @tap="goHistory">📋 查看答题记录</view>
        </view>
      </view>
      <EmptyState v-else icon="📊" title="暂无评估数据" description="完成面试后查看评估结果" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import evaluationApi from '@/api/evaluation.js'
let sessionId = ''
const loading = ref(true), evaluation = ref(null)
const overallScore = computed(() => { if (!evaluation.value) return '--'; const s = evaluation.value.scores || evaluation.value; const vals = Object.values(s).filter(v => typeof v === 'number'); if (!vals.length) return '--'; return Math.round(vals.reduce((a,b)=>a+b,0)/vals.length) })
const gradeText = computed(() => { const s = parseInt(overallScore.value); if (isNaN(s)) return ''; if (s>=90) return '🏆 优秀'; if (s>=75) return '👍 良好'; if (s>=60) return '📚 一般'; return '💪 需提升' })
const dimensions = computed(() => { if (!evaluation.value) return []; const scores = evaluation.value.scores || evaluation.value.metrics || {}; const defs = { technical:{label:'技术能力',icon:'⚡',colorClass:'bar-purple'}, communication:{label:'沟通表达',icon:'💬',colorClass:'bar-blue'}, clarity:{label:'逻辑清晰',icon:'🧩',colorClass:'bar-green'}, depth:{label:'思考深度',icon:'🔍',colorClass:'bar-pink'}, problem_solving:{label:'问题解决',icon:'🧠',colorClass:'bar-orange'} }; return Object.entries(scores).map(([k,v])=>({key:k, score:typeof v==='number'?v:parseInt(v)||0, label:defs[k]?.label||k, icon:defs[k]?.icon||'📌', colorClass:defs[k]?.colorClass||'bar-purple'})) })
onLoad((opt) => { sessionId = opt.sessionId || ''; loadData() })
const loadData = async () => {
  loading.value = true
  try {
    // 演示模式：无 sessionId 时展示最近一次评估（数据来自接口层 mock，非页面伪造）
    // 后端接入后：无 sessionId 应显示空态（EmptyState）
    evaluation.value = sessionId ? await evaluationApi.getEval(sessionId) : await evaluationApi.getEval(0)
  } catch (e) { evaluation.value = null } finally { loading.value = false }
}
const goAnswerDetail = (answerId) => {
  const sid = sessionId || 0
  uni.navigateTo({ url: '/pages/evaluation/answer-detail?sessionId=' + sid + '&answerId=' + answerId })
}
const regenerate = async () => { if (!sessionId) return; try { await evaluationApi.regenerateReport(sessionId); uni.showToast({ title: '已重新生成' }); loading.value = true; await loadData() } catch (e) {} }
const goHistory = () => { if (sessionId) uni.navigateTo({ url: '/pages/interview/session-history?sessionId=' + sessionId }) }
</script>

<style scoped>
.content { padding-bottom: 80rpx; }

.eval-layout { display: flex; flex-direction: column; gap: 20rpx; }
@media (min-width: 768px) {
  .eval-layout { flex-direction: row; gap: 32px; align-items: flex-start; }
}
.eval-main { flex: 1; min-width: 0; }
@media (min-width: 768px) {
  .eval-sidebar { width: 260px; flex-shrink: 0; position: sticky; top: 80px; }
}

.overview-card { display: flex; gap: 28rpx; padding: 28rpx; border-radius: 22rpx; margin-bottom: 24rpx; align-items: center; }
.ov-ring { width: 120rpx; height: 120rpx; border-radius: 50%; background: rgba(255,255,255,0.5); border: 3rpx solid rgba(124,92,252,0.12); display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; }
.ov-score { font-size: 40rpx; font-weight: 800; }
.ov-unit { font-size: 22rpx; color: #9ca3af; }
.ov-right { flex: 1; }
.ov-grade { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 6rpx; }
.ov-summary { font-size: 24rpx; color: #6b7280; line-height: 1.5; }

.sec-title { font-size: 26rpx; font-weight: 700; color: #1e1b4b; margin-bottom: 14rpx; display: block; }

.dim-card { padding: 18rpx 20rpx; border-radius: 16rpx; }
.dim-head { display: flex; align-items: center; gap: 10rpx; margin-bottom: 10rpx; }
.dim-icon { font-size: 24rpx; }
.dim-name { font-size: 24rpx; color: #6b7280; flex: 1; }
.dim-score { font-size: 24rpx; font-weight: 700; color: #1e1b4b; }
.dim-bar { height: 6rpx; background: rgba(0,0,0,0.06); border-radius: 3rpx; overflow: hidden; }
.dim-fill { height: 100%; border-radius: 3rpx; transition: width 1s; }
.bar-purple { background: linear-gradient(90deg, #7c5cfc, #a78bfa); }
.bar-blue { background: linear-gradient(90deg, #4facfe, #7ec8fe); }
.bar-green { background: linear-gradient(90deg, #34d399, #6ee7b7); }
.bar-pink { background: linear-gradient(90deg, #f472b6, #f9a8d4); }
.bar-orange { background: linear-gradient(90deg, #f97316, #fbbf24); }

.text-card { padding: 22rpx; border-radius: 16rpx; margin-bottom: 14rpx; }
.text-body { font-size: 26rpx; color: #6b7280; line-height: 1.7; display: block; margin-top: 8rpx; }

.sb-card { padding: 22rpx; margin-bottom: 16rpx; border-radius: 16rpx; }
.sb-title { font-size: 13px; font-weight: 700; color: #9ca3af; margin-bottom: 14px; display: block; text-transform: uppercase; letter-spacing: 1px; }
.sb-btn { height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; margin-bottom: 8px; transition: all 0.2s; cursor: pointer; }
.sb-btn.primary { background: linear-gradient(135deg, #7c5cfc, #4facfe); color: #fff; }
.sb-btn.secondary { border: 1px solid rgba(124,92,252,0.2); color: #7c5cfc; }
.sb-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid rgba(0,0,0,0.04); }
.answer-row { display: flex; align-items: center; gap: 12rpx; padding: 16rpx 0; border-bottom: 1rpx solid rgba(0,0,0,0.04); }
.answer-row:last-child { border-bottom: none; }
.answer-num { width: 36rpx; height: 36rpx; border-radius: 50%; background: rgba(124,92,252,0.1); color: #7c5cfc; font-size: 22rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.answer-q { flex: 1; font-size: 26rpx; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.answer-go { font-size: 24rpx; color: #7c5cfc; font-weight: 600; flex-shrink: 0; }
.sb-l { font-size: 14px; color: #9ca3af; }
.sb-v { font-size: 14px; font-weight: 600; color: #1e1b4b; }

.mobile-actions { display: flex; gap: 14rpx; margin-top: 20rpx; }
.mobile-actions .btn-primary { flex: 1; height: 88rpx; background: linear-gradient(135deg,#7c5cfc,#4facfe); border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28rpx; font-weight: 700; }
.mobile-actions .btn-outline { flex: 1; height: 88rpx; border: 2rpx solid rgba(124,92,252,0.2); border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #7c5cfc; font-size: 28rpx; font-weight: 600; }
</style>
