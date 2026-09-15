<!-- 技能趋势 - 纯 CSS 图表（跨端兼容） -->
<template>
  <PageLayout currentPage="skills" :title="skill + ' 历史趋势'">
    <view class="content">
      <view class="trend-card glass-strong">
        <view class="trend-header">
          <text class="trend-skill">{{ skill }}</text>
          <view class="trend-change" :class="isUp ? 'up' : 'down'"><text>{{ isUp ? '📈' : '📉' }} {{ changeText }}</text></view>
        </view>
        <view class="chart-wrap">
          <view class="chart-bars">
            <view v-for="(p, i) in chartData" :key="i" class="chart-col">
              <view class="bar-fill" :style="{ height: (p.score / 100 * 100) + '%' }" />
              <view class="bar-dot" :style="{ bottom: (p.score / 100 * 100) + '%' }" />
            </view>
          </view>
          <view class="chart-labels">
            <text v-for="(p, i) in chartData" :key="i" class="clabel">{{ p.date }}</text>
          </view>
        </view>
      </view>

      <text class="section-title">📊 历史记录</text>
      <view class="resp-grid col-2">
        <view v-for="(item, idx) in chartData" :key="idx" class="data-card glass-card">
          <text class="data-date">{{ item.date }}</text>
          <text class="data-score gradient-text">{{ item.score }}</text>
        </view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import growthApi from '@/api/growth.js'
let skill = ''
const chartData = ref([])
const isUp = computed(() => { if (chartData.value.length < 2) return true; return chartData.value[chartData.value.length-1].score >= chartData.value[0].score })
const changeText = computed(() => { if (chartData.value.length < 2) return '--'; const diff = chartData.value[chartData.value.length-1].score - chartData.value[0].score; return (diff >= 0 ? '+' : '') + diff })
onLoad((opt) => { skill = opt.skill || 'Java'; loadData() })
const loadData = async () => {
  try {
    const data = await growthApi.getSkillHistory(skill, '6m') || []
    if (data.length) chartData.value = data.map(p => ({ date: p.date, score: p.score || p.value || 0 }))
  } catch (e) { chartData.value = [] }
}
</script>

<style scoped>
.content { padding-bottom: 80rpx; }

.trend-card { padding: 28rpx; margin-bottom: 24rpx; }
.trend-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.trend-skill { font-size: 32rpx; font-weight: 800; color: #1e1b4b; }
.trend-change { font-size: 24rpx; font-weight: 700; padding: 4rpx 14rpx; border-radius: 10rpx; }
.trend-change.up { color: #34d399; background: rgba(52,211,153,0.1); }
.trend-change.down { color: #f87171; background: rgba(248,113,113,0.1); }

.chart-wrap { margin: 8rpx 0; }
.chart-bars {
  display: flex; align-items: flex-end; justify-content: space-around;
  height: 220rpx; padding: 0 10rpx;
}
.chart-col {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  height: 100%; justify-content: flex-end; position: relative; margin: 0 6rpx;
}
.bar-fill {
  width: 100%; max-width: 52rpx;
  background: linear-gradient(180deg, #7c5cfc, rgba(79,172,254,0.3));
  border-radius: 10rpx 10rpx 0 0; min-height: 8rpx;
  transition: height 0.8s cubic-bezier(0.16,1,0.3,1);
}
.bar-dot {
  position: absolute; width: 14rpx; height: 14rpx; border-radius: 50%;
  background: #7c5cfc; border: 3rpx solid #fff;
  box-shadow: 0 2rpx 8rpx rgba(124,92,252,0.3); transform: translateY(7rpx);
}
.chart-labels { display: flex; justify-content: space-around; padding-top: 16rpx; }
.clabel { font-size: 20rpx; color: #9ca3af; }

.section-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; margin-bottom: 14rpx; display: block; }
.data-card { padding: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.data-date { font-size: 26rpx; color: #6b7280; }
.data-score { font-size: 30rpx; font-weight: 800; }
</style>
