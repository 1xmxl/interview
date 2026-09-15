<!-- 环形评分组件 - 带动画效果的分数环 -->
<template>
  <view class="score-ring" :style="{ width: size + 'rpx', height: size + 'rpx' }">
    <!-- 背景环 -->
    <view
      class="ring-bg"
      :style="{
        width: size + 'rpx',
        height: size + 'rpx',
        borderWidth: strokeWidth + 'rpx'
      }"
    />
    <!-- 进度环 (SVG circle for web, simulated for mini-program) -->
    <view class="ring-content">
      <view class="score-value" :class="'score-' + level" :style="{ fontSize: valueFontSize + 'rpx' }">
        <text v-if="!animating">{{ displayScore }}</text>
        <text v-else class="counting">{{ animScore }}</text>
        <text v-if="showTotal && !animating" class="score-total">/{{ max }}</text>
      </view>
      <text v-if="label" class="score-label">{{ label }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  score: { type: Number, default: 0 },
  max: { type: Number, default: 100 },
  size: { type: Number, default: 200 },
  strokeWidth: { type: Number, default: 8 },
  label: { type: String, default: '' },
  showTotal: { type: Boolean, default: false },
  animate: { type: Boolean, default: true },
  duration: { type: Number, default: 1500 }
})

const animScore = ref(0)
const animating = ref(false)

const displayScore = computed(() => {
  if (props.max === 10) return props.score.toFixed(1)
  return Math.round(props.score)
})

const valueFontSize = computed(() => {
  if (props.size < 150) return 32
  if (props.size < 200) return 44
  return 56
})

const level = computed(() => {
  const pct = props.score / props.max
  if (pct >= 0.85) return 'excellent'
  if (pct >= 0.7) return 'good'
  if (pct >= 0.5) return 'average'
  return 'needs-work'
})

onMounted(() => {
  if (props.animate) {
    animateCount()
  }
})

watch(() => props.score, () => {
  if (props.animate) {
    animateCount()
  }
})

function animateCount() {
  animating.value = true
  const target = props.score
  const start = 0
  const startTime = Date.now()

  function tick() {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / props.duration, 1)
    // ease out cubic
    const eased = 1 - Math.pow(1 - progress, 3)
    animScore.value = Math.round(start + (target - start) * eased)

    if (progress < 1) {
      requestAnimationFrame(tick)
    } else {
      animScore.value = Math.round(target)
      animating.value = false
    }
  }
  requestAnimationFrame(tick)
}
</script>

<style scoped>
.score-ring {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-bg {
  position: absolute;
  border-radius: 50%;
  border: solid rgba(124, 92, 252, 0.1);
}

/* 进度环用 conic-gradient 模拟 */
.ring-progress {
  position: absolute;
  border-radius: 50%;
}

.ring-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.score-value {
  font-weight: 800;
  letter-spacing: -1rpx;
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.score-excellent {
  background: linear-gradient(135deg, #34d399, #4facfe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.score-good {
  background: linear-gradient(135deg, #4facfe, #7c5cfc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.score-average {
  background: linear-gradient(135deg, #fbbf24, #f97316);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.score-needs-work {
  background: linear-gradient(135deg, #f87171, #f472b6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.score-total {
  font-size: 0.5em;
  font-weight: 400;
  color: #9ca3af;
}

.score-label {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 8rpx;
  font-weight: 500;
}

.counting {
  font-variant-numeric: tabular-nums;
}
</style>
