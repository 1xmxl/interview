<!-- 登录页 - 响应式液态玻璃 -->
<template>
  <view class="login-page">
    <AnimatedBg />
    <view class="container login-wrap">
      <view class="hero-section">
        <view class="logo-ring"><text class="logo-icon">🎯</text></view>
        <text class="hero-title gradient-text">AI 面试助手</text>
        <text class="hero-subtitle">让每一次面试都胸有成竹</text>
      </view>
      <view class="login-card glass-strong">
        <view class="input-wrap"><text class="input-icon">👤</text><input v-model="username" class="glass-input" placeholder="请输入用户名" placeholder-style="color:#b0b7c3" /></view>
        <view class="input-wrap"><text class="input-icon">🔒</text><input v-model="password" class="glass-input" type="password" placeholder="请输入密码" placeholder-style="color:#b0b7c3" /></view>
        <view class="login-btn" @tap="handleLogin"><text class="btn-text">登 录</text></view>
        <text class="link" @tap="goRegister">没有账号？立即注册</text>
      </view>
      <text class="footer-tip">安全加密传输 · 数据仅用于面试评估</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import userApi from '@/api/user.js'
const username = ref('')
const password = ref('')
const handleLogin = async () => {
  if (!username.value || !password.value) { uni.showToast({ title: '请填写完整信息', icon: 'none' }); return }
  try {
    uni.showLoading({ title: '登录中...', mask: true })
    const res = await userApi.login({ username: username.value, password: password.value })
    uni.setStorageSync('token', res.accessToken || res.token)
    uni.setStorageSync('refreshToken', res.refreshToken)
    uni.setStorageSync('username', res.username || username.value)
    uni.hideLoading()
    uni.showToast({ title: '登录成功 🎉', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 600)
  } catch (e) { uni.hideLoading() }
}
const goRegister = () => uni.navigateTo({ url: '/pages/register/register' })
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; }
.login-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 80rpx; padding-bottom: 60rpx; z-index: 1; }
@media (min-width: 768px) { .login-wrap { max-width: 480px; } }

.hero-section { display: flex; flex-direction: column; align-items: center; margin-bottom: 40rpx; animation: fadeInUp 0.8s cubic-bezier(0.16,1,0.3,1) both; }
.logo-ring { width: 120rpx; height: 120rpx; border-radius: 50%; background: rgba(255,255,255,0.45); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 2px solid rgba(255,255,255,0.6); display: flex; align-items: center; justify-content: center; margin-bottom: 24rpx; box-shadow: 0 12px 40px rgba(124,92,252,0.15); animation: float 4s ease-in-out infinite; }
.logo-icon { font-size: 56rpx; }
.hero-title { font-size: 44rpx; font-weight: 800; letter-spacing: 2rpx; margin-bottom: 10rpx; }
.hero-subtitle { font-size: 26rpx; color: #9ca3af; }

.login-card { width: 100%; padding: 40rpx 36rpx; display: flex; flex-direction: column; }
@media (min-width: 768px) { .login-card { padding: 48px 40px; } }
.input-wrap { display: flex; align-items: center; background: rgba(255,255,255,0.35); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.4); border-radius: 18rpx; padding: 0 20rpx; margin-bottom: 20rpx; transition: all 0.3s; }
.input-icon { font-size: 34rpx; margin-right: 14rpx; flex-shrink: 0; }
.glass-input { flex: 1; height: 90rpx; font-size: 28rpx; color: #1e1b4b; background: transparent; }

.login-btn { margin-top: 16rpx; height: 96rpx; background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; box-shadow: 0 12rpx 32rpx rgba(124,92,252,0.3); transition: all 0.3s; }
.login-btn:active { transform: scale(0.96); }
.btn-text { color: #fff; font-size: 32rpx; font-weight: 700; letter-spacing: 6rpx; }
.link { margin-top: 28rpx; text-align: center; font-size: 26rpx; color: #7c5cfc; font-weight: 500; }
.footer-tip { margin-top: 48rpx; font-size: 22rpx; color: #9ca3af; }

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-16rpx); } }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(40rpx); } to { opacity: 1; transform: translateY(0); } }
</style>
