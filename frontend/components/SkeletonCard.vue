<!-- 骨架屏加载组件 -->
<template>
  <view class="skeleton-card glass-card">
    <view class="sk-header">
      <view class="sk-avatar skeleton" />
      <view class="sk-header-lines">
        <view class="sk-line sk-line-short skeleton" />
        <view class="sk-line sk-line-long skeleton" />
      </view>
    </view>
    <view class="sk-body">
      <view v-for="i in lines" :key="i" class="sk-line skeleton" :class="getLineClass(i)" />
    </view>
  </view>
</template>

<script setup>
defineProps({
  lines: { type: Number, default: 3 },
  showAvatar: { type: Boolean, default: true }
})

function getLineClass(index) {
  const classes = ['sk-line-full', 'sk-line-full', 'sk-line-half']
  return classes[index % 3]
}
</script>

<style scoped>
.skeleton-card {
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.sk-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 28rpx;
}

.sk-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.sk-header-lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.sk-line {
  height: 16rpx;
  border-radius: 8rpx;
}

.sk-line-short { width: 40%; }
.sk-line-long { width: 70%; }
.sk-line-full { width: 100%; }
.sk-line-half { width: 55%; }

.sk-body {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.skeleton {
  background: linear-gradient(
    90deg,
    rgba(200, 200, 210, 0.3) 25%,
    rgba(220, 220, 230, 0.5) 50%,
    rgba(200, 200, 210, 0.3) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
