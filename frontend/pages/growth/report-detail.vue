<!-- 报告详情 - 响应式 -->
<template>
  <PageLayout currentPage="reports" title="报告详情">
    <view class="content">
      <view v-if="loading"><SkeletonCard :lines="6" /></view>
      <view v-else-if="report.content || report.insights" class="report-body glass-strong">
        <rich-text :nodes="report.content || JSON.stringify(report.insights)" />
      </view>
      <EmptyState v-else icon="📋" title="报告加载失败" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import growthApi from '@/api/growth.js'
let reportId = ''
const loading = ref(true), report = ref({})
onLoad((opt) => { reportId = opt.id || ''; loadData() })
const loadData = async () => { try { report.value = await growthApi.reportDetail(reportId) || {} } catch (e) { report.value = {} } finally { loading.value = false } }
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
@media (min-width: 768px) { .content { max-width: 800px; margin: 0 auto; } }
.report-body { padding: 32rpx; line-height: 1.8; }
</style>
