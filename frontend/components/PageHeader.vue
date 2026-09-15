<!--
  页面头部组件 - 液态玻璃导航栏
  条件编译：H5 无状态栏占位，APP-PLUS 有状态栏
-->
<template>
  <view class="page-header">
    <!-- 状态栏占位（仅 APP 端） -->
    <!-- #ifdef APP-PLUS -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
    <!-- #endif -->

    <view class="header-inner">
      <view class="nav-bar">
        <view class="nav-left" @tap="goBack">
          <view v-if="showBack" class="back-btn">
            <text class="back-icon">←</text>
          </view>
          <view v-else class="nav-placeholder" />
        </view>
        <view class="nav-center">
          <text class="nav-title">{{ title }}</text>
        </view>
        <view class="nav-right">
          <slot name="right" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: true }
})

// 系统信息（仅在 APP 端有意义）
// #ifdef APP-PLUS
const sysInfo = uni.getSystemInfoSync()
const statusBarHeight = computed(() => sysInfo.statusBarHeight || 0)
// #endif

function goBack() {
  if (props.showBack) {
    uni.navigateBack({ delta: 1 })
  }
}
</script>

<style scoped>
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(30px) saturate(180%);
  -webkit-backdrop-filter: blur(30px) saturate(180%);
  background: rgba(255, 255, 255, 0.65);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.status-bar {
  width: 100%;
}

.header-inner {
  width: 100%;
}

@media (min-width: 768px) {
  .header-inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 40px;
  }
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 24rpx;
  position: relative;
}

@media (min-width: 768px) {
  .nav-bar {
    padding: 0;
  }
}

.nav-left {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.nav-right {
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.nav-placeholder {
  width: 32rpx;
}

.nav-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #7c5cfc, #4facfe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.2s;
}

.back-btn:active {
  transform: scale(0.9);
  background: rgba(255, 255, 255, 0.8);
}

.back-icon {
  font-size: 32rpx;
  color: #7c5cfc;
  font-weight: 700;
}
</style>
