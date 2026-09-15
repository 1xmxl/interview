<!-- 薄弱点 - 响应式 -->
<template>
  <PageLayout currentPage="skills" title="薄弱点提升">
    <view class="content">
      <text class="hero-text">🎯 针对性提升，补齐短板</text>
      <view v-if="weaknesses.length > 0" class="resp-grid col-2 stagger-children">
        <view v-for="(w, idx) in weaknesses" :key="idx" class="weak-card glass-card">
          <view class="weak-header">
            <text class="weak-icon">{{ ['🔴','🟡','🟢'][idx] || '📌' }}</text>
            <text class="weak-skill gradient-text">{{ w.skill || w.name }}</text>
            <view class="weak-severity" :class="'sev-' + (w.severity || 'medium')"><text>{{ sevText(w.severity) }}</text></view>
          </view>
          <text class="weak-suggestion">{{ w.suggestion || w.description || '该技能需要针对性提升' }}</text>
        </view>
      </view>
      <EmptyState v-else icon="💪" title="暂无薄弱点" description="继续参加面试获取精准分析" />

      <view class="action-card glass-card">
        <text class="action-title">💡 通用提升建议</text>
        <view v-for="(tip, i) in tips" :key="i" class="action-item">
          <text class="action-num">{{ String(i+1).padStart(2,'0') }}</text>
          <text class="action-text">{{ tip }}</text>
        </view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import growthApi from '@/api/growth.js'
const weaknesses = ref([])
const tips = ['每天至少练习一道面试题，培养表达习惯','录制回答并回听，关注语速和填充词','针对薄弱技能制定学习计划并持续跟踪']
const sevText = (s) => ({ high:'重点关注', medium:'需要提升', low:'持续优化' }[s] || '需要提升')
onMounted(async () => { try { weaknesses.value = await growthApi.getWeaknesses() || [] } catch (e) { weaknesses.value = [] } })
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
.hero-text { font-size: 30rpx; font-weight: 700; color: #1e1b4b; margin-bottom: 22rpx; text-align: center; display: block; }
.weak-card { padding: 24rpx; }
.weak-header { display: flex; align-items: center; gap: 10rpx; margin-bottom: 16rpx; }
.weak-icon { font-size: 26rpx; }
.weak-skill { font-size: 28rpx; font-weight: 700; flex: 1; }
.weak-severity { font-size: 20rpx; padding: 2rpx 12rpx; border-radius: 8rpx; font-weight: 600; }
.sev-high { color: #f87171; background: rgba(248,113,113,0.1); }
.sev-medium { color: #fbbf24; background: rgba(251,191,36,0.1); }
.sev-low { color: #34d399; background: rgba(52,211,153,0.1); }
.weak-suggestion { font-size: 26rpx; color: #6b7280; line-height: 1.6; }
.action-card { padding: 24rpx; margin-top: 24rpx; }
.action-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 18rpx; }
.action-item { display: flex; gap: 14rpx; margin-bottom: 14rpx; align-items: flex-start; }
.action-num { font-size: 22rpx; font-weight: 800; color: #7c5cfc; flex-shrink: 0; margin-top: 2rpx; }
.action-text { font-size: 26rpx; color: #6b7280; line-height: 1.5; }
</style>
