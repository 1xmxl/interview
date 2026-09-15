<!--
  页面布局容器
  - 桌面端：左侧侧边栏 + 右侧内容区
  - 移动端：顶部导航栏 + 内容（保持现有行为）

  用法：
  <PageLayout currentPage="index" title="首页">
    <view>页面内容</view>
    <template #header-right>按钮</template>
  </PageLayout>
-->
<template>
  <view class="page-layout" :class="{ 'has-sidebar': isDesktop }">
    <!-- 桌面端侧边栏 -->
    <DesktopSidebar :currentPage="currentPage" />

    <!-- 主内容区 -->
    <view class="page-main" :class="{ 'with-sidebar': isDesktop }">
      <!-- 移动端：顶部导航栏 -->
      <PageHeader v-if="!isDesktop" :title="title" :showBack="showBack">
        <template v-if="$slots['header-right']" #right>
          <slot name="header-right" />
        </template>
      </PageHeader>

      <!-- 桌面端：简化面包屑头部 -->
      <view v-if="isDesktop" class="desktop-topbar">
        <text class="topbar-title">{{ title }}</text>
        <view class="topbar-actions">
          <slot name="header-right" />
        </view>
      </view>

      <!-- 页面内容 -->
      <view class="page-body">
        <AnimatedBg v-if="!isDesktop" />
        <slot />
      </view>
    </view>
  </view>
</template>

<script setup>
import { isDesktop } from '@/common/js/device.js'
import DesktopSidebar from './DesktopSidebar.vue'
import PageHeader from './PageHeader.vue'
import AnimatedBg from './AnimatedBg.vue'

defineProps({
  currentPage: { type: String, default: 'index' },
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: true }
})
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  display: flex;
}

/* ---- 主内容区 ---- */
.page-main {
  flex: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-main.with-sidebar {
  margin-left: 240px;
}

/* ---- 桌面端顶部栏 ---- */
.desktop-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px 16px;
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(245, 246, 250, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}
.topbar-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e1b4b;
}
.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ---- 页面内容 ---- */
.page-body {
  flex: 1;
  padding: 0;
  position: relative;
  animation: pageFadeInUp 0.45s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.page-main.with-sidebar .page-body {
  padding: 20px 32px 40px;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

@keyframes pageFadeInUp {
  from { opacity: 0; transform: translateY(16rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
