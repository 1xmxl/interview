<!--
  桌面端侧边导航栏
  固定左侧240px，全屏高度，玻璃质感
-->
<template>
  <view v-if="isDesktop" class="desktop-sidebar">
    <!-- Logo 区域 -->
    <view class="sidebar-brand">
      <view class="brand-logo">
        <text class="brand-emoji">🤖</text>
      </view>
      <text class="brand-name">AI 面试助手</text>
    </view>

    <!-- 导航菜单 -->
    <view class="sidebar-nav">
      <view
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: currentPage === item.key }"
        @tap="navigateTo(item)"
      >
        <text class="nav-icon">{{ item.icon }}</text>
        <text class="nav-label">{{ item.label }}</text>
        <view v-if="currentPage === item.key" class="nav-indicator" />
      </view>
    </view>

    <!-- 底部分隔 + 用户区 -->
    <view class="sidebar-footer">
      <view class="sidebar-divider" />
      <view class="footer-user" @tap="goProfile">
        <text class="user-avatar">🙋</text>
        <view class="user-info">
          <text class="user-name">{{ username || '用户' }}</text>
          <text class="user-sub">查看资料</text>
        </view>
      </view>
      <view class="footer-logout" @tap="logout">
        <text class="logout-icon">🚪</text>
        <text class="logout-text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { isDesktop } from '@/common/js/device.js'

const props = defineProps({
  currentPage: { type: String, default: 'index' }
})

const username = ref('')

const navItems = [
  { key: 'index',       icon: '🏠', label: '首页',       path: '/pages/index/index' },
  { key: 'resume',      icon: '📄', label: '我的简历',    path: '/pages/resume/list' },
  { key: 'config',      icon: '⚙️', label: '面试配置',    path: '/pages/interview/config-list' },
  { key: 'history',     icon: '💬', label: '面试记录',    path: '/pages/interview/session-history' },
  { key: 'evaluation',  icon: '📊', label: '评估结果',    path: '/pages/evaluation/result' },
  { key: 'analytics',   icon: '🎙️', label: '语音分析',    path: '/pages/feedback/analytics' },
  { key: 'skills',      icon: '🌱', label: '技能成长',    path: '/pages/growth/skills' },
  { key: 'reports',     icon: '📋', label: '成长报告',    path: '/pages/growth/report-list' },
  { key: 'profile',     icon: '👤', label: '个人资料',    path: '/pages/user/profile' },
]

function navigateTo(item) {
  if (item.key === props.currentPage) return
  uni.navigateTo({ url: item.path })
}

function goProfile() {
  uni.navigateTo({ url: '/pages/user/profile' })
}

function logout() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('username')
  uni.reLaunch({ url: '/pages/login/login' })
}

onMounted(() => {
  const stored = uni.getStorageSync('username')
  if (stored) username.value = stored
})
</script>

<style scoped>
.desktop-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 240px;
  height: 100vh;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  z-index: 200;
  overflow-y: auto;
}

/* ---- Brand ---- */
.sidebar-brand {
  padding: 28px 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-emoji { font-size: 20px; }
.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #1e1b4b;
  letter-spacing: 0.5px;
}

/* ---- Nav ---- */
.sidebar-nav {
  flex: 1;
  padding: 4px 12px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  border-radius: 12px;
  margin-bottom: 4px;
  position: relative;
  transition: all 0.2s ease;
  cursor: pointer;
}
.nav-item:hover {
  background: rgba(0, 0, 0, 0.04);
}
.nav-item.active {
  background: rgba(124, 92, 252, 0.06);
}
.nav-icon { font-size: 18px; flex-shrink: 0; }
.nav-label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
}
.nav-item.active .nav-label {
  color: #5b3cc4;
  font-weight: 600;
}
.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #7c5cfc;
  border-radius: 0 3px 3px 0;
}

/* ---- Footer ---- */
.sidebar-footer {
  padding: 12px 16px 20px;
}
.sidebar-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
}
.footer-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.footer-user:hover { background: rgba(124, 92, 252, 0.06); }
.user-avatar { font-size: 22px; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: 13px; font-weight: 600; color: #1e1b4b; }
.user-sub { font-size: 11px; color: #9ca3af; margin-top: 1px; }
.footer-logout {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  margin-top: 6px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.footer-logout:hover { background: rgba(248, 113, 113, 0.06); }
.logout-icon { font-size: 14px; }
.logout-text { font-size: 12px; color: #9ca3af; }
</style>
