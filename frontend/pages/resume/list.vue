<!-- 简历列表 - 移动优先 + 桌面增强 -->
<template>
  <PageLayout currentPage="resume" title="我的简历">
    <view class="content">
      <!-- 头部操作栏 -->
      <view class="toolbar">
        <view class="toolbar-info">
          <text class="toolbar-count">共 {{ list.length }} 份简历</text>
        </view>
        <view class="toolbar-upload glass-light" @tap="uploadResume">
          <text class="tu-icon">+</text>
          <text class="tu-text">上传简历</text>
        </view>
      </view>

      <!-- 简历网格：手机 1 列，桌面 3 列 -->
      <view v-if="loading" class="resp-grid col-3 resume-grid">
        <SkeletonCard v-for="i in 6" :key="i" :lines="3" />
      </view>
      <view v-else-if="list.length > 0" class="resp-grid col-3 resume-grid">
        <view v-for="(item, idx) in list" :key="item.id" class="resume-card glass-card" @tap="goDetail(item.id)">
          <view class="rc-preview">
            <text class="rc-icon">📄</text>
            <view class="rc-badge">{{ formatType(item.fileType) }}</view>
          </view>
          <view class="rc-body">
            <text class="rc-name">{{ item.coverName || item.originalFilename }}</text>
            <text class="rc-time">{{ formatTime(item.createdAt) }}</text>
            <view class="rc-status-wrap hide-mobile">
              <view class="rc-status-dot" :style="{background: statusColor(item.status)}" />
              <text class="rc-status-text" :style="{color: statusColor(item.status)}">{{ statusText(item.status) }}</text>
            </view>
          </view>
          <view class="rc-actions hide-mobile">
            <text class="rc-action" @tap.stop="goDetail(item.id)">查看详情 →</text>
          </view>
        </view>
      </view>

      <EmptyState v-else icon="📭" title="暂无简历" description="上传你的第一份简历，AI 将自动解析技能与经历" action-text="上传简历" @action="uploadResume" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import resumeApi from '@/api/resume.js'
const list = ref([])
const loading = ref(true)
onMounted(async () => { try { list.value = await resumeApi.list() || [] } catch (e) {} finally { loading.value = false } })
const goDetail = (id) => uni.navigateTo({ url: '/pages/resume/detail?resumeId=' + id })
const uploadResume = () => {
  uni.chooseFile({ count: 1, success: async (res) => {
    try { uni.showLoading({ title: '上传中...' }); await resumeApi.upload(res.tempFiles[0].path, {}); uni.hideLoading(); uni.showToast({ title: '上传成功', icon: 'none' }); list.value = await resumeApi.list() || [] } catch (e) { uni.hideLoading() }
  }})
}
const formatType = (t) => { if (!t) return 'FILE'; return t.replace(/^\./, '').toUpperCase().slice(0, 4) }
const formatTime = (d) => { if (!d) return ''; const dt = new Date(d); return dt.getFullYear()+'-'+String(dt.getMonth()+1).padStart(2,'0')+'-'+String(dt.getDate()).padStart(2,'0') }
const statusText = (s) => ({ PENDING:'等待解析', PARSING:'解析中', PARSED:'已解析', FAILED:'解析失败' }[s] || s)
const statusColor = (s) => ({ PENDING:'#fbbf24', PARSING:'#4facfe', PARSED:'#34d399', FAILED:'#f87171' }[s] || '#999')
</script>

<style scoped>
.content { padding-bottom: 40rpx; }

/* 工具栏 */
.toolbar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24rpx;
}
@media (min-width: 768px) { .toolbar { margin-bottom: 20px; } }
.toolbar-count { font-size: 26rpx; color: #6b7280; font-weight: 600; }
@media (min-width: 768px) { .toolbar-count { font-size: 14px; } }
.toolbar-upload {
  display: flex; align-items: center; gap: 8rpx;
  padding: 14rpx 24rpx; border-radius: 16rpx;
  transition: all 0.3s;
}
@media (min-width: 768px) { .toolbar-upload { padding: 8px 18px; border-radius: 10px; } }
.toolbar-upload:active { transform: scale(0.95); }
.tu-icon { font-size: 28rpx; color: #7c5cfc; font-weight: 700; }
.tu-text { font-size: 26rpx; color: #7c5cfc; font-weight: 600; }

/* 简历卡片 */
.resume-card {
  padding: 24rpx; border-radius: 20rpx;
  display: flex; flex-direction: column; gap: 16rpx;
  transition: all 0.3s;
}
.resume-card:active { transform: scale(0.97); }
.rc-preview {
  position: relative;
  height: 140rpx;
  background: linear-gradient(135deg, rgba(124,92,252,0.06), rgba(79,172,254,0.06));
  border-radius: 14rpx;
  display: flex; align-items: center; justify-content: center;
}
@media (min-width: 768px) { .rc-preview { height: 120px; } }
.rc-icon { font-size: 56rpx; }
.rc-badge {
  position: absolute; right: 12rpx; bottom: 12rpx;
  background: rgba(124,92,252,0.85); color: #fff;
  font-size: 18rpx; font-weight: 700;
  padding: 4rpx 12rpx; border-radius: 10rpx;
}
.rc-body { display: flex; flex-direction: column; gap: 6rpx; }
.rc-name { font-size: 28rpx; font-weight: 700; color: #1e1b4b; line-height: 1.3; }
.rc-time { font-size: 24rpx; color: #9ca3af; }
.rc-status-wrap { display: flex; align-items: center; gap: 8rpx; margin-top: 4rpx; }
.rc-status-dot { width: 10rpx; height: 10rpx; border-radius: 50%; }
.rc-status-text { font-size: 22rpx; font-weight: 600; }
.rc-actions { padding-top: 8rpx; border-top: 1rpx solid rgba(0,0,0,0.05); }
.rc-action { font-size: 24rpx; color: #7c5cfc; font-weight: 600; }
</style>
