<!-- 简历列表 - 移动优先 + 桌面增强 -->
<template>
  <PageLayout currentPage="resume" title="我的简历">
    <view class="content">
      <!-- 顶部横幅 -->
      <view class="hero">
        <view class="hero-orb hero-orb-1" />
        <view class="hero-orb hero-orb-2" />
        <view class="hero-main">
          <text class="hero-eyebrow">RESUME LIBRARY</text>
          <text class="hero-title">让简历先替你说话</text>
          <text class="hero-sub">上传后 AI 自动解析技能与经历，并据此为你定制面试题</text>
          <view class="hero-stats">
            <view class="stat">
              <text class="stat-num">{{ list.length }}</text>
              <text class="stat-label">简历总数</text>
            </view>
            <view class="stat-sep" />
            <view class="stat">
              <text class="stat-num">{{ parsedCount }}</text>
              <text class="stat-label">已解析</text>
            </view>
            <view class="stat-sep" />
            <view class="stat">
              <text class="stat-num">{{ pendingCount }}</text>
              <text class="stat-label">待处理</text>
            </view>
          </view>
        </view>
        <view class="hero-upload" @tap="uploadResume">
          <view class="upload-plus">
            <text class="upload-plus-icon">+</text>
          </view>
          <view class="upload-texts">
            <text class="upload-title">上传简历</text>
            <text class="upload-sub">PDF / DOCX / TXT</text>
          </view>
        </view>
      </view>

      <!-- 筛选栏 -->
      <view class="toolbar">
        <view class="filter-list">
          <view
            v-for="f in filters"
            :key="f.key"
            class="filter-chip"
            :class="{ active: activeFilter === f.key }"
            @tap="activeFilter = f.key"
          >
            <text class="filter-text">{{ f.label }}</text>
            <text v-if="f.key === 'ALL'" class="filter-count">{{ list.length }}</text>
          </view>
        </view>
        <text class="toolbar-count only-desktop">共 {{ visibleList.length }} 份</text>
      </view>

      <!-- 简历网格：手机 1 列，桌面 2~3 列 -->
      <view v-if="loading" class="resume-grid">
        <view v-for="i in 6" :key="i" class="resume-cell">
          <SkeletonCard :lines="3" />
        </view>
      </view>

      <view v-else-if="visibleList.length > 0" class="resume-grid">
        <view
          v-for="(item, idx) in visibleList"
          :key="item.id"
          class="resume-cell"
          :style="{ animationDelay: (idx * 0.06) + 's' }"
        >
          <view class="resume-card" @tap="goDetail(item.id)">
            <view class="rc-cover">
              <ResumeCover
                :name="item.coverName || item.originalFilename"
                :file-type="formatType(item.fileType)"
                :seed="item.id"
              />
            </view>

            <view class="rc-info">
              <text class="rc-name">{{ item.coverName || item.originalFilename }}</text>
              <text class="rc-file">{{ item.originalFilename }}</text>
              <view class="rc-meta">
                <view class="rc-status" :style="statusStyle(item.status)">
                  <view
                    class="rc-dot"
                    :class="{ breathing: item.status === 'PARSING' }"
                    :style="{ background: statusColor(item.status) }"
                  />
                  <text class="rc-status-text">{{ statusText(item.status) }}</text>
                </view>
                <text class="rc-meta-item">{{ formatType(item.fileType) }} 文档</text>
                <text class="rc-meta-dot">·</text>
                <text class="rc-meta-item">{{ formatTime(item.createdAt) }}</text>
              </view>
              <view class="rc-foot">
                <text class="rc-open">查看解析详情</text>
                <text class="rc-arrow">→</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <EmptyState
        v-else-if="list.length > 0"
        icon="🔍"
        title="该状态下暂无简历"
        description="切换筛选条件，或上传一份新的简历"
        action-text="查看全部"
        @action="activeFilter = 'ALL'"
      />
      <EmptyState
        v-else
        icon="📭"
        title="暂无简历"
        description="上传你的第一份简历，AI 将自动解析技能与经历"
        action-text="上传简历"
        @action="uploadResume"
      />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import ResumeCover from '@/components/ResumeCover.vue'
import resumeApi from '@/api/resume.js'

const list = ref([])
const loading = ref(true)
const activeFilter = ref('ALL')

const filters = [
  { key: 'ALL', label: '全部' },
  { key: 'PARSED', label: '已解析' },
  { key: 'PARSING', label: '解析中' },
  { key: 'PENDING', label: '待解析' },
  { key: 'FAILED', label: '解析失败' },
]

const visibleList = computed(() =>
  activeFilter.value === 'ALL' ? list.value : list.value.filter((i) => i.status === activeFilter.value)
)
const parsedCount = computed(() => list.value.filter((i) => i.status === 'PARSED').length)
const pendingCount = computed(() => list.value.filter((i) => i.status !== 'PARSED').length)

const loadList = async () => {
  try {
    list.value = (await resumeApi.list()) || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadList)

const goDetail = (id) => {
  uni.navigateTo({ url: '/pages/resume/detail?resumeId=' + id })
}

const uploadResume = () => {
  uni.chooseFile({
    count: 1,
    success: async (res) => {
      const file = res.tempFiles && res.tempFiles[0]
      if (!file) return
      try {
        uni.showLoading({ title: '上传中...' })
        await resumeApi.upload(file.path, {})
        uni.hideLoading()
        uni.showToast({ title: '上传成功', icon: 'none' })
        loading.value = true
        await loadList()
      } catch (e) {
        uni.hideLoading()
      }
    }
  })
}

const formatType = (t) => {
  if (!t) return 'FILE'
  return t.replace(/^\./, '').toUpperCase().slice(0, 4)
}

// 简历接口暂未返回文件大小，按文件名/类型给出一个稳定的展示值
const formatTime = (d) => {
  if (!d) return ''
  const dt = new Date(String(d).replace(' ', 'T'))
  if (isNaN(dt.getTime())) return String(d).slice(0, 10)
  return dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0')
}

const statusText = (s) => ({ PENDING: '等待解析', PARSING: '解析中', PARSED: '已解析', FAILED: '解析失败' }[s] || '未知状态')
const statusColor = (s) => ({ PENDING: '#f59e0b', PARSING: '#0ea5e9', PARSED: '#10b981', FAILED: '#ef4444' }[s] || '#9ca3af')
const statusStyle = (s) => ({
  PENDING: 'color:#b45309;background:#fef3c7',
  PARSING: 'color:#0369a1;background:#e0f2fe',
  PARSED: 'color:#047857;background:#d1fae5',
  FAILED: 'color:#b91c1c;background:#fee2e2'
}[s] || 'color:#6b7280;background:#f3f4f6')
</script>

<style scoped>
.content { padding-bottom: 24px; }

/* ---------- 顶部横幅 ---------- */
.hero {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 26px;
  margin-bottom: 18px;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, #2f2668 0%, #5b3cc4 52%, #4facfe 145%);
  box-shadow: 0 12px 32px rgba(76, 59, 158, 0.22);
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.07);
}
.hero-orb-1 { width: 220px; height: 220px; top: -110px; right: -40px; }
.hero-orb-2 { width: 140px; height: 140px; bottom: -80px; right: 130px; }

.hero-main {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.hero-eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2.4px;
  color: rgba(255, 255, 255, 0.55);
}
.hero-title {
  font-size: 23px;
  font-weight: 800;
  letter-spacing: 0.5px;
  color: #ffffff;
  line-height: 1.3;
}
.hero-sub {
  font-size: 12.5px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
  max-width: 470px;
}
.hero-stats {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 18px;
  margin-top: 10px;
}
.stat { display: flex; flex-direction: column; }
.stat-num { font-size: 19px; font-weight: 800; color: #ffffff; line-height: 1.2; }
.stat-label { font-size: 11px; color: rgba(255, 255, 255, 0.6); margin-top: 1px; }
.stat-sep { width: 1px; height: 26px; background: rgba(255, 255, 255, 0.18); }

.hero-upload {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  padding: 11px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.28);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}
.hero-upload:hover { background: rgba(255, 255, 255, 0.22); transform: translateY(-1px); }
.hero-upload:active { transform: scale(0.97); }
.upload-plus {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(30, 27, 75, 0.18);
}
.upload-plus-icon { font-size: 20px; font-weight: 700; color: #5b3cc4; line-height: 1; }
.upload-texts { display: flex; flex-direction: column; }
.upload-title { font-size: 13.5px; font-weight: 700; color: #ffffff; }
.upload-sub { font-size: 10.5px; color: rgba(255, 255, 255, 0.65); margin-top: 1px; }

/* ---------- 筛选栏 ---------- */
.toolbar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.filter-list {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8px;
}
.filter-chip {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  padding: 7px 13px;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
}
.filter-chip:active { transform: scale(0.96); }
.filter-chip.active {
  background: linear-gradient(135deg, #7c5cfc, #4facfe);
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(124, 92, 252, 0.28);
}
.filter-text { font-size: 12.5px; font-weight: 600; color: #6b7280; }
.filter-chip.active .filter-text { color: #ffffff; }
.filter-count {
  font-size: 11px;
  font-weight: 700;
  color: #9ca3af;
  background: rgba(0, 0, 0, 0.05);
  padding: 1px 6px;
  border-radius: 999px;
}
.filter-chip.active .filter-count { color: #ffffff; background: rgba(255, 255, 255, 0.24); }
.toolbar-count { font-size: 12.5px; color: #9ca3af; font-weight: 600; flex-shrink: 0; }

/* ---------- 网格 ---------- */
.resume-grid {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  margin: 0 -7px;
}
.resume-cell {
  width: 100%;
  padding: 0 7px 14px;
  box-sizing: border-box;
  animation: cardIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

/* ---------- 卡片 ---------- */
.resume-card {
  display: flex;
  flex-direction: row;
  gap: 14px;
  height: 100%;
  padding: 14px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 3px rgba(30, 27, 75, 0.05);
  transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
}
.resume-card:hover {
  transform: translateY(-3px);
  border-color: rgba(124, 92, 252, 0.28);
  box-shadow: 0 14px 30px rgba(76, 59, 158, 0.14);
}
.resume-card:active { transform: scale(0.985); }

.rc-cover {
  position: relative;
  width: 88px;
  height: 122px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(160deg, #f4f1ff, #eef6ff);
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 3px 10px rgba(30, 27, 75, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.rc-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.rc-name {
  font-size: 13.5px;
  font-weight: 700;
  color: #1e1b4b;
  line-height: 1.35;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.rc-status {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  padding: 3px 8px;
  border-radius: 999px;
}
.rc-dot { width: 5px; height: 5px; border-radius: 50%; }
.rc-dot.breathing { animation: dotBreath 1.6s ease-in-out infinite; }
.rc-status-text { font-size: 10.5px; font-weight: 700; }

.rc-file {
  font-size: 11.5px;
  color: #9ca3af;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.rc-meta { display: flex; flex-direction: row; flex-wrap: wrap; align-items: center; gap: 6px; row-gap: 4px; }
.rc-meta-item { font-size: 11px; color: #9ca3af; }
.rc-meta-dot { font-size: 11px; color: #d1d5db; }

.rc-foot {
  margin-top: auto;
  padding-top: 9px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
}
.rc-open { font-size: 11.5px; font-weight: 700; color: #7c5cfc; }
.rc-arrow { font-size: 12px; color: #7c5cfc; transition: transform 0.2s; }
.resume-card:hover .rc-arrow { transform: translateX(3px); }

/* ---------- 响应式 ---------- */
.only-desktop { display: none; }

@media (min-width: 768px) {
  .resume-grid { margin: 0 -10px; }
  .resume-cell { width: 50%; padding: 0 10px 20px; }
  .only-desktop { display: flex; }
}
@media (min-width: 1200px) {
  .resume-cell { width: 33.3333%; }
}

@media (max-width: 767px) {
  .content { padding-bottom: 20px; }
  .hero {
    flex-direction: column;
    align-items: stretch;
    padding: 20px;
    gap: 16px;
    border-radius: 18px;
  }
  .hero-title { font-size: 20px; }
  .hero-upload { justify-content: center; }
  .toolbar { align-items: flex-start; }
  .filter-list { gap: 6px; }
  .filter-chip { padding: 6px 11px; }
  .filter-text { font-size: 12px; }
  .rc-cover { width: 74px; height: 104px; }
  .rc-name { font-size: 13px; }
  .rc-file { font-size: 11px; }
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes dotBreath {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.45; transform: scale(0.75); }
}
</style>
