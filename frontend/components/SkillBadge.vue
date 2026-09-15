<!-- 技能标签组件 -->
<template>
  <view class="skill-badge" :class="[levelClass, sizeClass]" :style="badgeStyle">
    <text class="skill-dot" v-if="showDot" />
    <text class="skill-name">{{ name }}</text>
    <text v-if="level" class="skill-level">{{ levelText }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  level: { type: String, default: '' },
  showDot: { type: Boolean, default: true },
  size: { type: String, default: 'md' },
  color: { type: String, default: '' }
})

const levelMap = {
  EXPERT: '专家',
  ADVANCED: '高级',
  INTERMEDIATE: '中级',
  BEGINNER: '初级'
}

const levelText = computed(() => levelMap[props.level] || props.level)

const levelClass = computed(() => {
  if (!props.level) return ''
  const l = props.level.toLowerCase()
  if (l === 'expert') return 'level-expert'
  if (l === 'advanced') return 'level-advanced'
  if (l === 'intermediate') return 'level-intermediate'
  return 'level-beginner'
})

const sizeClass = computed(() => `size-${props.size}`)

const badgeStyle = computed(() => {
  if (props.color) {
    return { borderColor: props.color, color: props.color }
  }
  return {}
})
</script>

<style scoped>
.skill-badge {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 20rpx;
  border-radius: 9999rpx;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(124, 92, 252, 0.15);
  transition: all 0.2s;
}

.skill-badge:active {
  transform: scale(0.95);
}

.size-sm { padding: 4rpx 14rpx; }
.size-md { padding: 8rpx 20rpx; }
.size-lg { padding: 12rpx 28rpx; }

.skill-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #7c5cfc;
  flex-shrink: 0;
}

.skill-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #1e1b4b;
}

.size-sm .skill-name { font-size: 22rpx; }
.size-lg .skill-name { font-size: 30rpx; }

.skill-level {
  font-size: 20rpx;
  color: #9ca3af;
  margin-left: 4rpx;
}

/* Level colors */
.level-expert { border-color: rgba(124, 92, 252, 0.3); }
.level-expert .skill-dot { background: linear-gradient(135deg, #7c5cfc, #4facfe); }

.level-advanced { border-color: rgba(79, 172, 254, 0.3); }
.level-advanced .skill-dot { background: #4facfe; }

.level-intermediate { border-color: rgba(251, 191, 36, 0.3); }
.level-intermediate .skill-dot { background: #fbbf24; }

.level-beginner { border-color: rgba(156, 163, 175, 0.3); }
.level-beginner .skill-dot { background: #9ca3af; }
</style>
