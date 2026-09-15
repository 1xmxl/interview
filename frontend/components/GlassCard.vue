<!-- 液态玻璃卡片组件 -->
<template>
  <view
    class="glass-card"
    :class="[hoverable ? 'hoverable' : '', className]"
    :style="cardStyle"
    @tap="$emit('tap')"
  >
    <view v-if="title || $slots.header" class="glass-card-header">
      <text v-if="title" class="glass-card-title">{{ title }}</text>
      <slot name="header" />
    </view>
    <view class="glass-card-body">
      <slot />
    </view>
    <view v-if="$slots.footer" class="glass-card-footer">
      <slot name="footer" />
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  hoverable: { type: Boolean, default: false },
  padding: { type: String, default: '32rpx' },
  margin: { type: String, default: '0' },
  className: { type: String, default: '' },
  noBorder: { type: Boolean, default: false },
  bgOpacity: { type: Number, default: 0.55 }
})

defineEmits(['tap'])

const cardStyle = computed(() => ({
  padding: props.padding,
  margin: props.margin,
  background: `rgba(255, 255, 255, ${props.bgOpacity})`,
  border: props.noBorder ? 'none' : '1px solid rgba(255,255,255,0.5)'
}))
</script>

<style scoped>
.glass-card {
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 24rpx;
  box-shadow: 0 4px 24px rgba(31, 38, 135, 0.08);
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.glass-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.6), transparent);
}

.hoverable:active {
  transform: scale(0.97);
  box-shadow: 0 2px 16px rgba(31, 38, 135, 0.14);
}

.glass-card-header {
  margin-bottom: 24rpx;
}

.glass-card-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1e1b4b;
  letter-spacing: 0.5rpx;
}

.glass-card-body {
  position: relative;
  z-index: 1;
}

.glass-card-footer {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}
</style>
