<!--
  空状态组件 - 引导式而非报错式
  不显示 "--" 或空数据，而是告诉用户"下一步做什么"
-->
<template>
  <view class="empty-state">
    <view class="empty-visual">
      <view class="empty-glow" />
      <text class="empty-icon">{{ icon }}</text>
    </view>
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-desc">{{ description }}</text>
    <view v-if="actionText" class="empty-action" @tap="$emit('action')">
      <text class="action-text">{{ actionText }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '📋' },
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
  text-align: center;
}

.empty-visual {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}

.empty-glow {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(124,92,252,0.08) 0%, transparent 70%);
  animation: pulse 3s ease-in-out infinite;
}

.empty-icon {
  font-size: 72rpx;
  position: relative;
  z-index: 1;
  filter: grayscale(0.3);
}

.empty-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1e1b4b;
  margin-bottom: 10rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #9ca3af;
  line-height: 1.6;
  max-width: 500rpx;
}

.empty-action {
  margin-top: 32rpx;
  padding: 20rpx 48rpx;
  background: linear-gradient(135deg, #7c5cfc, #4facfe);
  border-radius: 9999rpx;
  box-shadow: 0 8rpx 24rpx rgba(124,92,252,0.3);
  transition: all 0.2s;
}

.empty-action:active {
  transform: scale(0.95);
}

.action-text {
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.15); opacity: 1; }
}
</style>
