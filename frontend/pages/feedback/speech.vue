<!-- 语音分析详情 - 响应式 -->
<template>
  <PageLayout currentPage="analytics" title="语音分析">
    <view class="content">
      <view v-if="data.length > 0" class="resp-grid col-2">
        <view v-for="item in data" :key="item.id || item.feature" class="data-card glass-card">
          <text class="data-feature">{{ item.feature || item.name }}</text>
          <text class="data-value" :class="(item.value > 70) ? 'val-good' : 'val-warn'">{{ item.value }}{{ item.unit || '' }}</text>
        </view>
      </view>
      <EmptyState v-else icon="🎙️" title="暂无语音数据" description="完成带语音的面试后可查看分析" />
      <view class="wave-card glass-card">
        <text class="wave-title">🎵 语音波形</text>
        <view class="wave-bars"><view v-for="(h, i) in waveHeights" :key="i" class="wave-bar" :style="{animationDelay:(i*0.05)+'s', height:h+'rpx'}" /></view>
        <text class="wave-note">🎙️ 语音波形（演示数据）</text>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import feedbackApi from '@/api/feedback.js'
let sessionId = ''
const data = ref([])
// 固定波形高度（模拟真实录音波形，避免每次渲染随机抖动）
const waveHeights = [12,20,34,28,16,40,52,38,22,10,18,30,44,36,24,14,32,48,40,26,16,28,42,50,34,20,12,24,38,46,30,18,10,22,36,44,28,14,26,40]
onLoad((opt) => { sessionId = opt.sessionId || ''; loadData() })
const loadData = async () => { try { data.value = await feedbackApi.getSpeechAnalysis(sessionId) || [] } catch (e) { data.value = [] } }
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
.data-card { padding: 22rpx; display: flex; justify-content: space-between; align-items: center; }
.data-feature { font-size: 28rpx; color: #6b7280; }
.data-value { font-size: 30rpx; font-weight: 800; }
.val-good { color: #34d399; }
.val-warn { color: #fbbf24; }
.wave-card { padding: 22rpx; margin-top: 20rpx; }
.wave-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 18rpx; }
.wave-bars { display: flex; align-items: center; justify-content: center; gap: 4rpx; height: 90rpx; }
.wave-bar { width: 8rpx; border-radius: 4rpx; background: linear-gradient(180deg, #7c5cfc, #4facfe60); animation: wave 1.2s ease-in-out infinite alternate; }
.wave-note { font-size: 22rpx; color: #9ca3af; text-align: center; display: block; margin-top: 14rpx; }
@keyframes wave { 0% { transform: scaleY(0.4); opacity: 0.5; } 100% { transform: scaleY(1); opacity: 1; } }
</style>
