<!--
  动态渐变光斑背景
  桌面端：极淡光斑，低视觉干扰
  移动端：保持原有丰富效果
-->
<template>
  <view class="animated-bg" :class="{ subdued: isDesktop }">
    <!-- 桌面端：只渲染2个极淡光斑 -->
    <template v-if="isDesktop">
      <view class="blob-desktop blob-d-1" />
      <view class="blob-desktop blob-d-2" />
    </template>
    <!-- 移动端：4个光斑 + 粒子 -->
    <template v-else>
      <view class="blob blob-1" />
      <view class="blob blob-2" />
      <view class="blob blob-3" />
      <view class="blob blob-4" />
      <view class="particles">
        <view v-for="i in 6" :key="i" class="particle" :style="getParticleStyle(i)" />
      </view>
    </template>
  </view>
</template>

<script setup>
import { isDesktop } from '@/common/js/device.js'

function getParticleStyle(index) {
  const positions = [
    { top: '10%', left: '15%', size: '6rpx', delay: '0s', duration: '4s' },
    { top: '20%', left: '80%', size: '8rpx', delay: '1s', duration: '5s' },
    { top: '60%', left: '10%', size: '4rpx', delay: '0.5s', duration: '3.5s' },
    { top: '75%', left: '70%', size: '10rpx', delay: '2s', duration: '6s' },
    { top: '40%', left: '50%', size: '5rpx', delay: '1.5s', duration: '4.5s' },
    { top: '85%', left: '35%', size: '7rpx', delay: '0.8s', duration: '5.5s' }
  ]
  const p = positions[index - 1] || positions[0]
  return {
    top: p.top, left: p.left,
    width: p.size, height: p.size,
    animationDelay: p.delay, animationDuration: p.duration
  }
}
</script>

<style scoped>
.animated-bg {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  pointer-events: none; z-index: 0; overflow: hidden;
}

/* ---- 桌面端：极淡光斑 ---- */
.blob-desktop {
  position: absolute; border-radius: 50%; filter: blur(120px); opacity: 0.15;
}
.blob-d-1 {
  top: -10%; right: -8%; width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(124,92,252,0.15), transparent 70%);
}
.blob-d-2 {
  bottom: -10%; left: -8%; width: 350px; height: 350px;
  background: radial-gradient(circle, rgba(79,172,254,0.1), transparent 70%);
}

/* ---- 移动端：丰富光斑 + 动画 ---- */
.blob {
  position: absolute; border-radius: 50%; filter: blur(80rpx); opacity: 0.5;
  animation: blob-morph 12s ease-in-out infinite, blob-move 16s ease-in-out infinite;
}
.blob-1 {
  top: -15%; right: -10%; width: 500rpx; height: 500rpx;
  background: radial-gradient(circle, rgba(124,92,252,0.2), transparent 70%);
}
.blob-2 {
  bottom: -15%; left: -10%; width: 450rpx; height: 450rpx;
  background: radial-gradient(circle, rgba(244,114,182,0.18), transparent 70%);
  animation-delay: -4s; animation-direction: reverse;
}
.blob-3 {
  top: 40%; left: -5%; width: 350rpx; height: 350rpx;
  background: radial-gradient(circle, rgba(79,172,254,0.15), transparent 70%);
  animation-delay: -8s;
}
.blob-4 {
  top: 25%; right: -8%; width: 380rpx; height: 380rpx;
  background: radial-gradient(circle, rgba(52,211,153,0.12), transparent 70%);
  animation-delay: -12s; animation-direction: reverse;
}

.particle {
  position: absolute; background: rgba(124,92,252,0.4); border-radius: 50%;
  animation: float calc(var(--duration) * 1s) ease-in-out infinite;
  animation-delay: var(--delay);
}

@keyframes blob-morph {
  0%, 100% { border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%; }
  25% { border-radius: 30% 60% 70% 40% / 50% 60% 30% 60%; }
  50% { border-radius: 50% 50% 40% 60% / 40% 40% 60% 60%; }
  75% { border-radius: 40% 60% 30% 50% / 60% 30% 70% 40%; }
}
@keyframes blob-move {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(40rpx, -30rpx) scale(1.05); }
  50% { transform: translate(-20rpx, -50rpx) scale(0.95); }
  75% { transform: translate(-40rpx, -10rpx) scale(1.02); }
}
@keyframes float {
  0%, 100% { transform: translateY(0); opacity: 0.4; }
  50% { transform: translateY(-30rpx); opacity: 1; }
}
</style>
