<!--
  通用简历封面缩略图
  - 不依赖任何具体人名/图片：所有简历共用同一套版式，按 id 自动轮换配色
  - 首字取简历姓名第一个字，其余用「纸面线条」表现简历内容，任意简历都不会空图
-->
<template>
  <view class="resume-cover" :class="'tone-' + toneIndex">
    <view class="rc-head">
      <view class="rc-glow" />
      <view class="rc-avatar">
        <text class="rc-initial">{{ initial }}</text>
      </view>
      <view class="rc-head-lines">
        <view class="rc-hline rc-hline-1" />
        <view class="rc-hline rc-hline-2" />
      </view>
    </view>

    <view class="rc-body">
      <view class="rc-sec">
        <view class="rc-sec-bar" />
        <view class="rc-line rc-w-90" />
        <view class="rc-line rc-w-72" />
      </view>
      <view class="rc-chips">
        <view class="rc-chip rc-chip-1" />
        <view class="rc-chip rc-chip-2" />
        <view class="rc-chip rc-chip-3" />
      </view>
      <view class="rc-line rc-w-84" />
      <view class="rc-line rc-w-66" />
      <view class="rc-line rc-w-76" />
    </view>

    <view class="rc-badge">
      <text class="rc-badge-text">{{ badgeText }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: '' },
  fileType: { type: String, default: '' },
  seed: { type: [String, Number], default: '' }
})

// 六套配色，按简历 id（或姓名）轮换，多份简历不会撞色
const toneIndex = computed(() => {
  const key = String(props.seed || props.name || 'resume')
  let h = 0
  for (let i = 0; i < key.length; i++) h = (h * 31 + key.charCodeAt(i)) % 9973
  return h % 6
})

// 姓名首字：中文取第一个字，英文转大写
const initial = computed(() => {
  const n = String(props.name || '').trim()
  if (!n) return '简'
  const ch = n[0]
  return /[a-z]/.test(ch) ? ch.toUpperCase() : ch
})

const badgeText = computed(() => {
  const t = String(props.fileType || '').replace(/^\./, '').toUpperCase()
  return t ? t.slice(0, 4) : 'PDF'
})
</script>

<style scoped>
.resume-cover {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  overflow: hidden;
}

/* ---------- 顶部色块 ---------- */
.rc-head {
  position: relative;
  height: 42%;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  padding: 8px 7px;
  overflow: hidden;
}
.rc-glow {
  position: absolute;
  right: -12px;
  top: -14px;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}
.rc-avatar {
  position: relative;
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}
.rc-initial {
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  color: #ffffff;
}
.rc-head-lines {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.rc-hline {
  height: 3px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.88);
}
.rc-hline-1 { width: 78%; }
.rc-hline-2 { width: 52%; height: 2px; background: rgba(255, 255, 255, 0.5); }

/* ---------- 纸面内容 ---------- */
.rc-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 7px 7px 13px;
  overflow: hidden;
}
.rc-sec { display: flex; flex-direction: column; gap: 3px; }
.rc-sec-bar {
  width: 13px;
  height: 2.5px;
  border-radius: 2px;
  background: currentColor;
  opacity: 0.8;
}
.rc-line { height: 3px; border-radius: 2px; background: #eceef4; }
.rc-w-90 { width: 90%; }
.rc-w-84 { width: 84%; }
.rc-w-76 { width: 76%; }
.rc-w-72 { width: 72%; }
.rc-w-66 { width: 66%; }

.rc-chips { display: flex; flex-direction: row; gap: 3px; }
.rc-chip {
  height: 5px;
  border-radius: 3px;
  background: currentColor;
  opacity: 0.18;
}
.rc-chip-1 { width: 16px; }
.rc-chip-2 { width: 22px; }
.rc-chip-3 { width: 12px; }

.rc-badge {
  position: absolute;
  left: 5px;
  bottom: 5px;
  padding: 1px 4px;
  border-radius: 3px;
  background: rgba(30, 27, 75, 0.72);
}
.rc-badge-text {
  font-size: 7px;
  font-weight: 700;
  letter-spacing: 0.4px;
  color: #ffffff;
}

/* ---------- 配色（按简历轮换） ---------- */
.tone-0 { color: #7c5cfc; }
.tone-0 .rc-head { background: linear-gradient(135deg, #312e81 0%, #6d4de0 60%, #7c5cfc 100%); }

.tone-1 { color: #2f80ed; }
.tone-1 .rc-head { background: linear-gradient(135deg, #0c4a6e 0%, #0369a1 45%, #38bdf8 100%); }

.tone-2 { color: #ec4899; }
.tone-2 .rc-head { background: linear-gradient(135deg, #831843 0%, #be185d 55%, #f472b6 100%); }

.tone-3 { color: #10b981; }
.tone-3 .rc-head { background: linear-gradient(135deg, #064e3b 0%, #047857 50%, #34d399 100%); }

.tone-4 { color: #f59e0b; }
.tone-4 .rc-head { background: linear-gradient(135deg, #78350f 0%, #b45309 50%, #fbbf24 100%); }

.tone-5 { color: #64748b; }
.tone-5 .rc-head { background: linear-gradient(135deg, #1e293b 0%, #334155 55%, #64748b 100%); }
</style>
