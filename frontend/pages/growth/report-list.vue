<!-- 成长报告列表 - 响应式 -->
<template>
  <PageLayout currentPage="reports" title="成长报告">
    <view class="content">
      <view v-if="reports.length > 0" class="resp-grid col-2 stagger-children">
        <view v-for="r in reports" :key="r.id" class="report-card glass-card" @tap="goDetail(r.id)">
          <view class="report-icon-wrap"><text class="report-icon">📋</text></view>
          <view class="report-info"><text class="report-title">{{ r.title || '成长报告' }}</text><text class="report-date">{{ r.date || r.generatedAt || '' }}</text></view>
          <text class="report-arrow">›</text>
        </view>
      </view>
      <EmptyState v-else icon="📋" title="暂无成长报告" description="积累面试数据后自动生成" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import growthApi from '@/api/growth.js'
const reports = ref([])
onMounted(async () => { try { reports.value = await growthApi.listReports() || [] } catch (e) { reports.value = [] } })
const goDetail = (id) => uni.navigateTo({ url: '/pages/growth/report-detail?id=' + id })
</script>

<style scoped>
.content { padding-bottom: 60rpx; }
.report-card { display: flex; align-items: center; padding: 24rpx; gap: 18rpx; }
.report-icon-wrap { width: 76rpx; height: 76rpx; border-radius: 18rpx; background: linear-gradient(135deg, rgba(124,92,252,0.08), rgba(79,172,254,0.08)); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.report-icon { font-size: 36rpx; }
.report-info { flex: 1; }
.report-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; }
.report-date { font-size: 24rpx; color: #9ca3af; display: block; margin-top: 4rpx; }
.report-arrow { font-size: 34rpx; color: #c0c0c8; }
</style>
