<!-- 语音统计 - 响应式 -->
<template>
  <PageLayout currentPage="analytics" title="语音统计">
    <view class="content">
      <view class="overview-card glass-strong">
        <text class="overview-title">📈 30天语音能力总览</text>
        <view class="overview-stats">
          <view class="ov-stat"><text class="ov-value gradient-text">{{ stats.fluency || '--' }}</text><text class="ov-label">平均流利度</text></view>
          <view class="ov-divider" />
          <view class="ov-stat"><text class="ov-value gradient-text-cool">{{ stats.pronunciation || '--' }}</text><text class="ov-label">发音准确度</text></view>
          <view class="ov-divider" />
          <view class="ov-stat"><text class="ov-value gradient-text-warm">{{ stats.wpm || '--' }}</text><text class="ov-label">语速 (词/分)</text></view>
        </view>
      </view>

      <view class="resp-grid col-2">
        <view class="chart-card glass-card">
          <text class="chart-title">📊 流利度趋势</text>
          <view class="mock-chart">
            <view v-for="(v, i) in trend" :key="i" class="mock-bar" :style="{height: v + '%'}" />
          </view>
          <view class="mock-labels"><text v-for="d in ['一','二','三','四','五','六','日']" :key="d" class="mock-label">{{ d }}</text></view>
        </view>
        <view class="section-card glass-card">
          <text class="sec-title">🗣️ 填充词使用</text>
          <view class="filler-list">
            <view v-for="fw in fillerWords" :key="fw.word" class="filler-item">
              <text class="fw-word">{{ fw.word }}</text>
              <view class="fw-bar-wrap"><view class="fw-bar" :style="{width: fw.count * 10 + '%'}" /></view>
              <text class="fw-count">{{ fw.count }}次</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import feedbackApi from '@/api/feedback.js'
const stats = ref({ fluency: 0, pronunciation: 0, wpm: 0 })
const trend = ref([62, 68, 65, 72, 75, 74, 78])
const fillerWords = ref([{ word: '嗯...', count: 12 },{ word: '那个...', count: 8 },{ word: '然后...', count: 6 },{ word: '就是...', count: 4 }])
onMounted(async () => { try { const data = await feedbackApi.getAnalytics({ period: '30d' }); if (data) { stats.value = { ...stats.value, ...data }; if (data.trend) trend.value = data.trend; if (data.fillerWords) fillerWords.value = data.fillerWords } } catch (e) {} })
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
.overview-card { padding: 28rpx; margin-bottom: 20rpx; }
.overview-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 22rpx; }
.overview-stats { display: flex; align-items: center; }
.ov-stat { flex: 1; display: flex; flex-direction: column; align-items: center; }
.ov-value { font-size: 40rpx; font-weight: 800; }
.ov-label { font-size: 22rpx; color: #9ca3af; margin-top: 4rpx; }
.ov-divider { width: 2rpx; height: 56rpx; background: rgba(0,0,0,0.06); }
.chart-card { padding: 22rpx; }
.chart-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 18rpx; }
.mock-chart { display: flex; align-items: flex-end; justify-content: space-around; height: 180rpx; }
.mock-bar { width: 40rpx; border-radius: 8rpx 8rpx 0 0; background: linear-gradient(180deg, #7c5cfc, #a78bfa50); min-height: 16rpx; }
.mock-labels { display: flex; justify-content: space-around; padding: 10rpx 0 0; }
.mock-label { font-size: 22rpx; color: #9ca3af; }
.sec-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 18rpx; }
.section-card { padding: 22rpx; }
.filler-list { display: flex; flex-direction: column; gap: 12rpx; }
.filler-item { display: flex; align-items: center; gap: 12rpx; }
.fw-word { font-size: 26rpx; color: #6b7280; width: 90rpx; text-align: center; }
.fw-bar-wrap { flex: 1; height: 8rpx; background: rgba(0,0,0,0.06); border-radius: 4rpx; overflow: hidden; }
.fw-bar { height: 100%; border-radius: 4rpx; background: linear-gradient(90deg, #f87171, #fbbf24); }
.fw-count { font-size: 24rpx; color: #f87171; width: 50rpx; text-align: right; }
</style>
