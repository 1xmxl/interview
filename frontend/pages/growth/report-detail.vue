<!-- 报告详情 - 响应式 -->
<template>
  <PageLayout currentPage="reports" title="报告详情">
    <view class="content">
      <view v-if="loading"><SkeletonCard :lines="6" /></view>
      <view v-else-if="reportHtml" class="report-body glass-strong">
        <rich-text :nodes="reportHtml" />
      </view>
      <EmptyState v-else icon="📋" title="报告加载失败" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import growthApi from '@/api/growth.js'
let reportId = ''
const loading = ref(true), report = ref({})
onLoad((opt) => { reportId = opt.id || ''; loadData() })
const loadData = async () => { try { report.value = await growthApi.reportDetail(reportId) || {} } catch (e) { report.value = {} } finally { loading.value = false } }

// 兼容两种数据：mock 的 content（HTML 字符串）/ 后端的 insights（JSON 对象）
const reportHtml = computed(() => {
  const r = report.value || {}
  if (r.content) return r.content
  const ins = r.insights
  if (!ins) return ''
  if (typeof ins === 'string') return ins
  const esc = (s) => String(s == null ? '' : s).replace(/[&<>]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' }[c]))
  let html = ''
  if (ins.title) html += '<h2>' + esc(ins.title) + '</h2>'
  if (ins.summary) html += '<p>' + esc(ins.summary) + '</p>'
  for (const [k, v] of Object.entries(ins)) {
    if (k === 'title' || k === 'summary' || k === 'content') continue
    if (v == null) continue
    const text = Array.isArray(v) ? v.join('；') : (typeof v === 'object' ? JSON.stringify(v) : v)
    html += '<h3>' + esc(k) + '</h3><p>' + esc(text) + '</p>'
  }
  return html
})
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
@media (min-width: 768px) { .content { max-width: 800px; margin: 0 auto; } }
.report-body { padding: 32rpx; line-height: 1.8; }
</style>
