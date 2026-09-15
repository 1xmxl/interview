<!-- 面试配置列表 - 移动 + 桌面增强 -->
<template>
  <PageLayout currentPage="config" title="面试配置">
    <view class="content">
      <view class="toolbar">
        <text class="toolbar-title">我的面试配置（{{ list.length }}）</text>
        <view class="toolbar-add glass-light" @tap="add">
          <text class="ta-icon">+</text>
          <text class="ta-text">新建</text>
        </view>
      </view>

      <view v-if="loading" class="resp-grid col-3 config-grid">
        <SkeletonCard v-for="i in 6" :key="i" :lines="3" />
      </view>
      <view v-else-if="list.length > 0" class="resp-grid col-3 config-grid">
        <view v-for="cfg in list" :key="cfg.id" class="cfg-card glass-card" @tap="edit(cfg.id)">
          <view class="cfg-top">
            <view class="cfg-icon-wrap"><text class="cfg-icon">⚙️</text></view>
            <text class="cfg-name">{{ cfg.name }}</text>
          </view>
          <text v-if="cfg.description" class="cfg-desc">{{ cfg.description }}</text>
          <text v-else class="cfg-desc empty">暂无描述</text>
          <view class="cfg-footer">
            <text class="cfg-action" @tap.stop="edit(cfg.id)">✏️ 编辑</text>
            <text class="cfg-action danger" @tap.stop="remove(cfg.id)">🗑️ 删除</text>
          </view>
        </view>
      </view>

      <EmptyState v-else icon="⚙️" title="暂无面试配置" description="创建面试配置，自定义题型、难度和面试风格" action-text="新建配置" @action="add" />

      <view class="add-card glass-card hide-desktop" @tap="add">
        <view class="add-ring"><text class="add-plus">+</text></view>
        <text class="add-text">新建面试配置</text>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import interviewApi from '@/api/interview.js'
const list = ref([])
const loading = ref(true)
onMounted(async () => { try { list.value = await interviewApi.listConfigs() || [] } catch (e) {} finally { loading.value = false } })
const edit = (id) => uni.navigateTo({ url: '/pages/interview/config-form?id=' + id })
const add = () => uni.navigateTo({ url: '/pages/interview/config-form' })
const remove = (id) => {
  uni.showModal({
    title: '确认删除',
    content: '删除后不可恢复，确定删除该配置吗？',
    success: async (r) => {
      if (r.confirm) {
        try {
          await interviewApi.deleteConfig(id)
          list.value = list.value.filter(c => c.id !== id)
          uni.showToast({ title: '已删除', icon: 'none' })
        } catch (e) {}
      }
    }
  })
}
</script>

<style scoped>
.content { padding-bottom: 60rpx; }

.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
@media (min-width: 768px) { .toolbar { margin-bottom: 20px; } }
.toolbar-title { font-size: 26rpx; color: #6b7280; font-weight: 600; }
@media (min-width: 768px) { .toolbar-title { font-size: 14px; } }
.toolbar-add { display: flex; align-items: center; gap: 6rpx; padding: 12rpx 20rpx; border-radius: 14rpx; transition: all 0.3s; }
@media (min-width: 768px) { .toolbar-add { padding: 8px 16px; border-radius: 10px; } }
.toolbar-add:active { transform: scale(0.95); }
.ta-icon { font-size: 26rpx; color: #7c5cfc; font-weight: 700; }
.ta-text { font-size: 24rpx; color: #7c5cfc; font-weight: 600; }

.cfg-card { padding: 24rpx; border-radius: 20rpx; display: flex; flex-direction: column; gap: 12rpx; transition: all 0.3s; }
.cfg-card:active { transform: scale(0.97); }
.cfg-top { display: flex; align-items: center; gap: 14rpx; }
.cfg-icon-wrap { width: 56rpx; height: 56rpx; border-radius: 16rpx; background: linear-gradient(135deg, rgba(124,92,252,0.08), rgba(79,172,254,0.08)); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.cfg-icon { font-size: 28rpx; }
.cfg-name { font-size: 28rpx; font-weight: 700; color: #1e1b4b; }
.cfg-desc { font-size: 24rpx; color: #6b7280; line-height: 1.5; }
.cfg-desc.empty { color: #c0c0c8; font-style: italic; }
.cfg-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 14rpx; border-top: 1rpx solid rgba(0,0,0,0.04); }
.cfg-action { font-size: 24rpx; color: #7c5cfc; font-weight: 600; padding: 6rpx 0; }
.cfg-action.danger { color: #f87171; }

.add-card { display: flex; flex-direction: column; align-items: center; padding: 36rpx; margin-top: 16rpx; }
.add-ring { width: 88rpx; height: 88rpx; border-radius: 50%; background: rgba(124,92,252,0.06); border: 3rpx dashed rgba(124,92,252,0.25); display: flex; align-items: center; justify-content: center; margin-bottom: 14rpx; }
.add-plus { font-size: 44rpx; color: #7c5cfc; font-weight: 300; }
.add-text { font-size: 28rpx; font-weight: 700; color: #7c5cfc; }
</style>
