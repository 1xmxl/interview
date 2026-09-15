<!-- 技能评分 - 响应式 -->
<template>
  <PageLayout currentPage="skills" title="技能评分">
    <view class="content">
      <view class="resp-grid col-2">
        <view class="radar-card glass-strong">
          <text class="radar-title">🎯 技能雷达</text>
          <view class="radar-visual">
            <view class="radar-bg"><view class="radar-ring r1" /><view class="radar-ring r2" /><view class="radar-ring r3" /></view>
            <view class="radar-center"><text class="radar-avg gradient-text">{{ avgScore }}</text><text class="radar-avg-label">综合</text></view>
          </view>
        </view>
        <view class="skill-list">
          <text class="section-title">📋 技能详情</text>
          <view v-for="skill in skills" :key="skill.name" class="skill-card glass-card">
            <view class="skill-header">
              <SkillBadge :name="skill.name" :level="skill.level" size="md" />
              <text class="skill-score gradient-text">{{ skill.score }}</text>
            </view>
            <view class="skill-bar-wrap"><view class="skill-bar" :style="{width:(skill.score||0)+'%'}" :class="getBarClass(skill.score||0)" /></view>
          </view>
        </view>
      </view>
      <view class="link-cards">
        <view class="link-card glass-card" @tap="$goHistory(firstSkill)"><text class="link-icon">📈</text><text class="link-text">查看技能历史趋势</text><text class="link-arrow">→</text></view>
        <view class="link-card glass-card" @tap="$goWeaknesses"><text class="link-icon">💪</text><text class="link-text">薄弱点提升建议</text><text class="link-arrow">→</text></view>
        <view class="link-card glass-card" @tap="$goReports"><text class="link-icon">📋</text><text class="link-text">成长报告</text><text class="link-arrow">→</text></view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import growthApi from '@/api/growth.js'
const skills = ref([])
const avgScore = computed(() => { const vals = skills.value.map(s => s.score || 0).filter(Boolean); if (!vals.length) return '--'; return Math.round(vals.reduce((a, b) => a + b, 0) / vals.length) })
const firstSkill = computed(() => (skills.value[0] && skills.value[0].name) || 'Java')
onMounted(async () => {
  try {
    const data = await growthApi.getSkills()
    if (data && typeof data === 'object') {
      // 兼容数组结构（推荐）与对象结构两种返回
      skills.value = Array.isArray(data)
        ? data.map(s => ({ name: s.name, score: typeof s.score === 'number' ? s.score : parseInt(s.score) || 0, level: s.level || getLevel(s.score) }))
        : Object.entries(data).map(([k, v]) => ({ name: k, score: typeof v === 'number' ? v : parseInt(v) || 0, level: getLevel(v) }))
    }
  } catch (e) {}
})
const getLevel = (score) => { const s = typeof score === 'number' ? score : parseInt(score) || 0; if (s >= 85) return 'EXPERT'; if (s >= 70) return 'ADVANCED'; if (s >= 50) return 'INTERMEDIATE'; return 'BEGINNER' }
const getBarClass = (score) => { if (score >= 85) return 'bar-expert'; if (score >= 70) return 'bar-advanced'; if (score >= 50) return 'bar-intermediate'; return 'bar-beginner' }
const $goHistory = (skill) => uni.navigateTo({ url: '/pages/growth/skill-history?skill=' + encodeURIComponent(skill || 'Java') })
const $goWeaknesses = () => uni.navigateTo({ url: '/pages/growth/weaknesses' })
const $goReports = () => uni.navigateTo({ url: '/pages/growth/report-list' })
</script>

<style scoped>
.content { padding-bottom: 80rpx; }
.radar-card { padding: 24rpx; }
.radar-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 20rpx; }
.radar-visual { position: relative; height: 260rpx; display: flex; align-items: center; justify-content: center; }
.radar-bg { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
.radar-ring { position: absolute; border-radius: 50%; border: 1rpx solid rgba(124,92,252,0.1); }
.r1 { width: 240rpx; height: 240rpx; }
.r2 { width: 160rpx; height: 160rpx; }
.r3 { width: 80rpx; height: 80rpx; }
.radar-center { display: flex; flex-direction: column; align-items: center; z-index: 1; }
.radar-avg { font-size: 48rpx; font-weight: 800; }
.radar-avg-label { font-size: 22rpx; color: #9ca3af; }
.section-title { font-size: 28rpx; font-weight: 700; color: #1e1b4b; margin-bottom: 14rpx; display: block; }
.skill-card { padding: 18rpx 20rpx; margin-bottom: 10rpx; }
.skill-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.skill-score { font-size: 28rpx; font-weight: 800; }
.skill-bar-wrap { height: 8rpx; background: rgba(0,0,0,0.06); border-radius: 4rpx; overflow: hidden; }
.skill-bar { height: 100%; border-radius: 4rpx; transition: width 1s cubic-bezier(0.16,1,0.3,1); }
.bar-expert { background: linear-gradient(90deg, #7c5cfc, #a78bfa); }
.bar-advanced { background: linear-gradient(90deg, #4facfe, #7ec8fe); }
.bar-intermediate { background: linear-gradient(90deg, #fbbf24, #f97316); }
.bar-beginner { background: linear-gradient(90deg, #9ca3af, #d1d5db); }
.link-cards { display: flex; flex-direction: column; gap: 14rpx; margin-top: 20rpx; }
.link-card { display: flex; align-items: center; padding: 22rpx; gap: 14rpx; }
.link-icon { font-size: 34rpx; }
.link-text { flex: 1; font-size: 28rpx; font-weight: 600; color: #1e1b4b; }
.link-arrow { font-size: 30rpx; color: #7c5cfc; font-weight: 700; }
</style>
