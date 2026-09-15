<!--
  首页仪表盘
  智能引导：Banner 按钮根据状态动态变化
  流程卡片：显示当前简历/配置详情
  空数据：虚线示意 + 引导文案
-->
<template>
  <PageLayout currentPage="index" title="工作台" :showBack="false">
    <view class="dashboard">

      <!-- ===== 桌面端：欢迎行（侧边栏已有用户信息，不重复显示） ===== -->
      <view class="welcome-row hide-mobile">
        <text class="welcome-greeting">{{ greetingText }}，今天想练习什么？</text>
      </view>

      <!-- ===== 移动端：智能 Banner ===== -->
      <view class="mobile-hero hide-desktop">
        <view class="container">
          <text class="mh-greet">{{ greetingText }}</text>
          <text class="mh-name">{{ username || '面试者' }}</text>
          <view class="cta-button" @tap="smartAction">
            <text class="cta-icon">{{ smartIcon }}</text>
            <text class="cta-title">{{ smartLabel }}</text>
            <text class="cta-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- ===== 面试流程：显示当前状态详情 ===== -->
      <view class="section container">
        <text class="section-title">📋 三步开始</text>
        <view class="flow-row">
          <!-- 步骤1 -->
          <view class="flow-card glass-card" :class="{ done: hasResume }" @tap="goResume">
            <text class="flow-num">01</text>
            <text class="flow-icon">📄</text>
            <text class="flow-label">上传简历</text>
            <text class="flow-detail" v-if="hasResume && resumeName">{{ resumeName }}</text>
            <text class="flow-detail muted" v-else>PDF / Word 格式</text>
            <view class="flow-actions" v-if="hasResume">
              <text class="flow-action" @tap.stop="goResume">更换</text>
            </view>
            <text class="flow-badge" :class="hasResume ? 'done' : 'current'">
              {{ hasResume ? '✓ 已完成' : '去上传' }}
            </text>
          </view>
          <view class="flow-arrow">→</view>
          <!-- 步骤2 -->
          <view class="flow-card glass-card" :class="{ done: hasConfig, next: hasResume && !hasConfig }" @tap="goConfig">
            <text class="flow-num">02</text>
            <text class="flow-icon">⚙️</text>
            <text class="flow-label">面试配置</text>
            <text class="flow-detail" v-if="hasConfig && configName">{{ configName }}</text>
            <text class="flow-detail muted" v-else>题型、难度、风格</text>
            <view class="flow-actions" v-if="hasConfig">
              <text class="flow-action" @tap.stop="goConfig">修改</text>
            </view>
            <text class="flow-badge" :class="step2BadgeClass">{{ step2Badge }}</text>
          </view>
          <view class="flow-arrow">→</view>
          <!-- 步骤3 -->
          <view class="flow-card glass-card" :class="{ ready: hasResume && hasConfig }" @tap="startInterview">
            <text class="flow-num">03</text>
            <text class="flow-icon">🎤</text>
            <text class="flow-label">开始面试</text>
            <text class="flow-detail muted">AI 实时提问评估</text>
            <text class="flow-badge" :class="step3BadgeClass">{{ step3Badge }}</text>
          </view>
        </view>
      </view>

      <!-- ===== 数据概览 ===== -->
      <view class="section container">
        <text class="section-title">📊 数据概览</text>
        <view class="stats-grid">
          <view v-for="d in dataCards" :key="d.label" class="stat-card glass-card" @tap="d.action">
            <view class="stat-icon-wrap" :style="{ background: d.bg }">
              <text class="stat-icon">{{ d.icon }}</text>
            </view>
            <text class="stat-value" :class="{ empty: d.isEmpty }">{{ d.isEmpty ? '--' : d.value }}</text>
            <text class="stat-label">{{ d.label }}</text>
            <!-- 空状态：虚线示意 -->
            <view v-if="d.isEmpty" class="stat-placeholder">
              <view class="stat-dashes">
                <view class="stat-dash" v-for="i in d.bars || 4" :key="i" :style="{ height: (8 + (i * 5) % 17) + 'px' }" />
              </view>
              <text class="stat-guide">{{ d.guide }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ===== 桌面端：热门面试题 ===== -->
      <view class="section container hide-mobile">
        <view class="recommend glass-card">
          <text class="rec-title">💡 热门面试题预览</text>
          <text class="rec-sub">点击任意题目可直接创建新会话开始练习</text>
          <view class="rec-grid">
            <view v-for="(q, i) in recommendQuestions" :key="i" class="rec-item" @tap="startInterview">
              <text class="rec-num">{{ i + 1 }}</text>
              <text class="rec-q">{{ q }}</text>
              <text class="rec-go">开始练习 →</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ===== 移动端：AI教练提示 + 快捷入口 ===== -->
      <view class="section container hide-desktop">
        <view class="ai-tip-card glass-card" @tap="smartAction">
          <text class="ai-avatar">🤖</text>
          <view class="ai-tip-body">
            <text class="ai-tip-title">AI 教练</text>
            <text class="ai-tip-text">{{ aiTip }}</text>
          </view>
          <text class="ai-tip-arrow">→</text>
        </view>
        <view style="margin-top: 20rpx;">
          <text class="section-title">🔧 快捷操作</text>
          <view class="quick-grid">
            <view v-for="m in mobileQuickMenus" :key="m.label" class="quick-card glass-card" @tap="m.action">
              <text class="quick-icon">{{ m.icon }}</text>
              <text class="quick-label">{{ m.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="safe-bottom" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import userApi from '@/api/user.js'
import resumeApi from '@/api/resume.js'
import interviewApi from '@/api/interview.js'
import dashboardApi from '@/api/dashboard.js'

const username = ref('')
const hasResume = ref(false)
const hasConfig = ref(false)
const resumeName = ref('')
const configName = ref('')

const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了 🌙'
  if (h < 9) return '早上好 ☀️'
  if (h < 12) return '上午好 🌤️'
  if (h < 14) return '中午好 ☀️'
  if (h < 18) return '下午好 🌈'
  if (h < 22) return '晚上好 🌆'
  return '夜深了 🌙'
})

const aiTip = ref('上传简历后，AI 教练将分析你的技术栈，为你推荐针对性面试题')

// 智能 Banner：根据状态动态变化
const smartIcon = computed(() => {
  if (!hasResume.value) return '📄'
  if (!hasConfig.value) return '⚙️'
  return '🎤'
})
const smartLabel = computed(() => {
  if (!hasResume.value) return '上传简历'
  if (!hasConfig.value) return '配置面试'
  return '开始 AI 面试'
})
function smartAction() {
  if (!hasResume.value) return goResume()
  if (!hasConfig.value) return goConfig()
  return startInterview()
}

// 流程步骤状态
const step2Badge = computed(() => {
  if (hasConfig.value) return '✓ 已完成'
  if (hasResume.value) return '去设置'
  return '待完成'
})
const step2BadgeClass = computed(() => {
  if (hasConfig.value) return 'done'
  if (hasResume.value) return 'next'
  return 'todo'
})
const step3Badge = computed(() => {
  if (hasResume.value && hasConfig.value) return '开始'
  return '待准备'
})
const step3BadgeClass = computed(() => {
  if (hasResume.value && hasConfig.value) return 'ready'
  return 'todo'
})

const recommendQuestions = ref([
  '请介绍一下你最有挑战性的技术项目',
  '如何设计一个高并发的秒杀系统？',
  '描述一次你解决过的线上故障经历',
  '微服务架构相比单体有哪些实际优势？',
])

const mobileQuickMenus = ref([
  { icon:'📄', label:'我的简历', action:()=>uni.navigateTo({url:'/pages/resume/list'}) },
  { icon:'⚙️', label:'面试配置', action:()=>uni.navigateTo({url:'/pages/interview/config-list'}) },
  { icon:'📊', label:'评估结果', action:()=>uni.navigateTo({url:'/pages/evaluation/result'}) },
  { icon:'💬', label:'面试记录', action:()=>uni.navigateTo({url:'/pages/interview/session-history'}) },
  { icon:'🎙️', label:'语音分析', action:()=>uni.navigateTo({url:'/pages/feedback/analytics'}) },
  { icon:'🌱', label:'技能成长', action:()=>uni.navigateTo({url:'/pages/growth/skills'}) },
  { icon:'📋', label:'成长报告', action:()=>uni.navigateTo({url:'/pages/growth/report-list'}) },
  { icon:'👤', label:'个人资料', action:()=>uni.navigateTo({url:'/pages/user/profile'}) },
])

const dataCards = ref([
  { icon:'🎯', value:'--', label:'面试次数', isEmpty:true, bars: 3,
    guide:'完成首次面试后显示', bg:'rgba(124,92,252,0.08)',
    action:()=>smartAction() },
  { icon:'⭐', value:'--', label:'平均得分', isEmpty:true, bars: 5,
    guide:'AI 评估后自动生成', bg:'rgba(244,114,182,0.08)',
    action:()=>uni.navigateTo({url:'/pages/evaluation/result'}) },
  { icon:'🧠', value:'--', label:'技能维度', isEmpty:true, bars: 6,
    guide:'解析简历后展示', bg:'rgba(79,172,254,0.08)',
    action:()=>uni.navigateTo({url:'/pages/growth/skills'}) },
  { icon:'📈', value:'--', label:'成长趋势', isEmpty:true, bars: 5,
    guide:'积累面试数据后展示', bg:'rgba(52,211,153,0.08)',
    action:()=>uni.navigateTo({url:'/pages/growth/report-list'}) },
])

const goResume = () => uni.navigateTo({ url: '/pages/resume/list' })
const goConfig = () => uni.navigateTo({ url: '/pages/interview/config-list' })
const startInterview = () => uni.navigateTo({ url: '/pages/interview/session-create' })

onMounted(async () => {
  const stored = uni.getStorageSync('username')
  if (stored) username.value = stored
  try {
    const p = await userApi.getProfile()
    if (p) username.value = p.username || p.name || stored
  } catch (e) {}

  try {
    const resumes = await resumeApi.list()
    if (resumes && resumes.length > 0) {
      hasResume.value = true
      // 取最近一份简历的名字
      const r = resumes[0]
      resumeName.value = r.coverName || r.originalFilename || ''
      aiTip.value = '简历已就绪。下一步创建面试配置，选择题型和难度。'
    }
  } catch (e) {}

  try {
    const configs = await interviewApi.listConfigs()
    if (configs && configs.length > 0) {
      hasConfig.value = true
      const c = configs[0]
      configName.value = c.name || ''
      if (hasResume.value) {
        aiTip.value = '一切就绪！点击下方按钮立刻开始模拟面试。'
      }
    }
  } catch (e) {}

  // 数据概览：演示模式返回聚合统计；真实后端接入 dashboard 接口
  try {
    const d = await dashboardApi.getStats()
    if (d) {
      const cards = dataCards.value
      cards[0].value = d.interviewCount; cards[0].isEmpty = false
      cards[1].value = d.avgScore; cards[1].isEmpty = false
      cards[2].value = d.skillCount; cards[2].isEmpty = false
      cards[3].value = d.reportCount; cards[3].isEmpty = false
    }
  } catch (e) {}
})
</script>

<style scoped>
.dashboard { position: relative; }

.section { position: relative; z-index: 1; margin-top: 24px; }
@media (min-width: 768px) { .section { margin-top: 32px; } }

.section-title {
  font-size: 16px; font-weight: 700; color: #1e1b4b;
  margin-bottom: 14px; display: block;
}
@media (max-width: 767px) {
  .section-title { font-size: 30rpx; margin-bottom: 18rpx; }
}

/* ===== 桌面欢迎行 ===== */
.welcome-row { margin-bottom: 8px; }
.welcome-greeting { font-size: 20px; font-weight: 700; color: #1e1b4b; display: block; }

/* ===== 移动端 Hero ===== */
.mobile-hero { padding: 48rpx 0 4rpx; position: relative; z-index: 1; }
.mh-greet { font-size: 26rpx; color: #6b7280; display: block; }
.mh-name { font-size: 42rpx; font-weight: 800; color: #1e1b4b; display: block; margin: 6rpx 0 24rpx; }
.cta-button {
  display: flex; align-items: center; gap: 16rpx;
  padding: 26rpx 28rpx; border-radius: 20rpx;
  background: #7c5cfc;
  box-shadow: 0 12rpx 36rpx rgba(124,92,252,0.25);
}
.cta-button:active { transform: scale(0.97); }
.cta-icon { font-size: 44rpx; }
.cta-title { font-size: 32rpx; font-weight: 700; color: #fff; flex: 1; }
.cta-arrow { font-size: 36rpx; color: rgba(255,255,255,0.7); font-weight: 600; }

/* ===== 流程卡片 ===== */
.flow-row {
  display: flex; flex-direction: row; flex-wrap: nowrap;
  align-items: stretch; gap: 0;
}
.flow-card {
  flex: 1 1 0; min-width: 0;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 18px 12px; text-align: center; position: relative; cursor: pointer;
  transition: all 0.2s; border: 2px solid transparent;
}
.flow-card:hover { transform: translateY(-2px); }
.flow-card:active { transform: scale(0.97); }
.flow-card.done { border-color: rgba(52,211,153,0.25); }
.flow-card.next { border-color: rgba(79,172,254,0.3); }
.flow-card.ready { border-color: rgba(124,92,252,0.25); }

.flow-num {
  position: absolute; top: 8px; left: 12px;
  font-size: 11px; font-weight: 800; color: #c0c0c8;
}
.flow-icon { font-size: 28px; margin-top: 2px; }
.flow-label { font-size: 14px; font-weight: 700; color: #1e1b4b; }
.flow-detail {
  font-size: 11px; color: #6b7280; text-align: center;
  line-height: 1.3; padding: 0 4px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%;
}
.flow-detail.muted { color: #b0b0b8; }
.flow-actions { margin-top: 2px; }
.flow-action {
  font-size: 11px; color: #7c5cfc; font-weight: 600; cursor: pointer;
  padding: 2px 8px; border-radius: 4px; background: rgba(124,92,252,0.06);
  transition: background 0.15s;
}
.flow-action:hover { background: rgba(124,92,252,0.12); }
.flow-badge {
  font-size: 11px; padding: 2px 10px; border-radius: 6px; font-weight: 600;
}
.flow-badge.done { color: #34d399; background: rgba(52,211,153,0.1); }
.flow-badge.current { color: #7c5cfc; background: rgba(124,92,252,0.08); }
.flow-badge.next { color: #4facfe; background: rgba(79,172,254,0.1); }
.flow-badge.ready { color: #7c5cfc; background: rgba(124,92,252,0.1); font-weight: 700; }
.flow-badge.todo { color: #9ca3af; background: rgba(0,0,0,0.04); }

.flow-arrow {
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; width: 32px;
  font-size: 16px; color: #d0d0d8;
}
@media (max-width: 767px) {
  .flow-card { padding: 16rpx 6rpx; }
  .flow-num { font-size: 18rpx; top: 6rpx; left: 8rpx; }
  .flow-icon { font-size: 32rpx; }
  .flow-label { font-size: 22rpx; }
  .flow-detail { font-size: 18rpx; }
  .flow-badge { font-size: 18rpx; }
  .flow-arrow { width: 18rpx; font-size: 14rpx; }
}

/* ===== 数据概览 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14rpx;
}
@media (min-width: 768px) {
  .stats-grid { grid-template-columns: repeat(4, 1fr); gap: 18px; }
}
.stat-card {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 20rpx 10rpx; cursor: pointer; transition: all 0.2s;
}
.stat-card:hover { transform: translateY(-1px); }
.stat-icon-wrap {
  width: 60rpx; height: 60rpx; border-radius: 14rpx;
  display: flex; align-items: center; justify-content: center;
}
.stat-icon { font-size: 26rpx; }
.stat-value { font-size: 28rpx; font-weight: 800; color: #1e1b4b; }
.stat-value.empty { color: #d0d0d8; }
.stat-label { font-size: 22rpx; color: #6b7280; font-weight: 600; }
/* 空状态虚线图 */
.stat-placeholder { display: flex; flex-direction: column; align-items: center; gap: 6rpx; width: 100%; }
.stat-dashes {
  display: flex; align-items: flex-end; gap: 3rpx;
  height: 36rpx; opacity: 0.25;
}
.stat-dash {
  width: 6rpx; border-radius: 3rpx; background: #9ca3af;
  min-height: 4rpx;
}
.stat-guide { font-size: 18rpx; color: #9ca3af; text-align: center; line-height: 1.3; }
@media (min-width: 768px) {
  .stat-card { padding: 20px 12px; gap: 6px; }
  .stat-icon-wrap { width: 38px; height: 38px; border-radius: 10px; }
  .stat-icon { font-size: 16px; }
  .stat-value { font-size: 24px; }
  .stat-label { font-size: 12px; }
  .stat-dashes { height: 28px; gap: 2px; }
  .stat-dash { width: 5px; }
  .stat-guide { font-size: 10px; }
}

/* ===== 桌面端推荐题 ===== */
.recommend { padding: 22px; }
.rec-title { font-size: 15px; font-weight: 700; color: #1e1b4b; display: block; }
.rec-sub { font-size: 12px; color: #9ca3af; display: block; margin-top: 4px; margin-bottom: 14px; }
.rec-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 24px;
}
.rec-item {
  display: flex; gap: 10px; padding: 10px 0;
  border-bottom: 1px solid rgba(0,0,0,0.04); align-items: flex-start;
  cursor: pointer; transition: all 0.15s;
}
.rec-item:hover { background: rgba(124,92,252,0.02); margin: 0 -8px; padding-left: 8px; padding-right: 8px; border-radius: 6px; }
.rec-item:nth-last-child(-n+2) { border-bottom: none; }
.rec-num { font-size: 12px; color: #7c5cfc; font-weight: 700; background: rgba(124,92,252,0.08); width: 20px; height: 20px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 1px; }
.rec-q { font-size: 13px; color: #6b7280; line-height: 1.5; flex: 1; }
.rec-go { font-size: 12px; color: #7c5cfc; font-weight: 600; flex-shrink: 0; opacity: 0; transition: opacity 0.15s; }
.rec-item:hover .rec-go { opacity: 1; }

/* ===== 移动端 AI 提示 ===== */
.ai-tip-card {
  display: flex; align-items: center; gap: 16rpx;
  padding: 22rpx; border-radius: 18rpx;
  /* 背景由 glass-card 统一提供，不额外设色 */
}
.ai-avatar { font-size: 44rpx; flex-shrink: 0; }
.ai-tip-body { flex: 1; }
.ai-tip-title { font-size: 24rpx; font-weight: 700; color: #7c5cfc; display: block; }
.ai-tip-text { font-size: 24rpx; color: #6b7280; display: block; margin-top: 4rpx; line-height: 1.5; }
.ai-tip-arrow { font-size: 32rpx; color: #c0c0c8; flex-shrink: 0; }

/* 移动端快捷入口 */
.quick-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 10rpx;
}
.quick-card {
  display: flex; flex-direction: column; align-items: center; gap: 4rpx;
  padding: 16rpx 4rpx;
}
.quick-icon { font-size: 34rpx; }
.quick-label { font-size: 20rpx; font-weight: 600; color: #1e1b4b; }

.safe-bottom { height: 80rpx; }
</style>
