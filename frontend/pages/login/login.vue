<template>
  <view class="login-page">
    <!-- 背景柔光遮罩 -->
    <view class="bg-overlay"></view>
    <view class="login-container">
      <!-- 左侧：独立立即体验按钮 -->
      <view class="left-experience">
        <view class="experience-btn" @tap="goExperience">
          <text class="exp-text">立即体验</text>
        </view>
      </view>

      <!-- 右侧登录卡片 -->
      <view class="login-area">
        <view class="login-card glass-card">
          <!-- 卡片头部 -->
          <view class="card-header">
            <image class="card-logo" src="/static/log-m.png" mode="aspectFit"></image>
            <view class="header-info">
              <text class="card-title" style="text-align:center">AI面试</text>
              <text class="card-subtitle">欢迎回来，开启你的职场新旅程</text>
            </view>
          </view>
          <!-- 登录表单 -->
          <view class="form-wrap">
            <view class="input-group">
              <text class="input-icon"></text>
              <input
                v-model="username"
                class="glass-input"
                placeholder="请输入手机号或邮箱"
                placeholder-style="color:#a0aec0"
              />
            </view>
            <view class="input-group">
              <text class="input-icon"></text>
              <input
                v-model="password"
                class="glass-input"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                placeholder-style="color:#a0aec0"
              />
            </view>
            <!-- 忘记密码 -->
            <view class="form-row">
              <text class="forget-text" @tap="goForgetPwd">忘记密码？</text>
            </view>
            <!-- 登录按钮 -->
            <view class="submit-btn" :class="{ 'btn-loading': submitting }" @tap="handleLogin">
              <text class="btn-text">登录</text>
            </view>
            <!-- 注册入口 -->
            <text class="register-bar">
              还没有账号？
              <text class="register-link" @tap="goRegister"> 立即注册 </text>
            </text>
          </view>
        </view>
      </view>
    </view>
    <!-- 底部标注 -->
    <view class="footer-tip">
      <text>智能 / 专业 / 高效 / 安全</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import userApi from '@/api/user.js'

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const submitting = ref(false)

// 登录逻辑
const handleLogin = async () => {
  const name = username.value.trim()
  if (!name || !password.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    uni.showLoading({ title: '登录中...', mask: true })
    const res = await userApi.login({
      username: name,
      password: password.value
    })
    uni.setStorageSync('token', res.accessToken || res.token)
    uni.setStorageSync('refreshToken', res.refreshToken)
    uni.setStorageSync('username', res.username || name)
    uni.hideLoading()
    uni.showToast({ title: '登录成功 🎉', icon: 'none' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 600)
  } catch (e) {
    uni.hideLoading()
  } finally {
    submitting.value = false
  }
}

const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/register' })
}

const goForgetPwd = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 立即体验按钮逻辑
const goExperience = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({
      title: '还没有登录呢！-！',
      icon: 'none',
      duration: 2000
    })
  } else {
    uni.navigateTo({ url: '/pages/experience/experience' })
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100vh;
  position: relative;
  background-image: url('/static/login-bg2.png');
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  overflow: hidden;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.2) 70%, rgba(255,255,255,0.35) 100%);
  z-index: 0;
  pointer-events: none;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
}

/* ========== 左侧独立按钮区域 ========== */
.left-experience {
  position: absolute;
  top: 75%;
  left: 8%;
  transform: translateY(-50%);
  z-index: 2;
}

.experience-btn {
  width: 320rpx;
  height: 100rpx;
  background: #528cff;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 8px 24px rgba(82, 140, 255, 0.35);
  transition: all 0.3s ease;
}

.experience-btn:active {
  transform: scale(0.96);
}

.exp-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}

.exp-arrow {
  color: #fff;
  font-size: 32rpx;
}

/* ========== 右侧登录卡区域 ========== */
.login-area {
  position: absolute;
  top: 50%;
  right: 5%;
  transform: translateY(-50%);
  width: 620rpx;
  max-width: 420px;
  flex-shrink: 0;
  animation: fadeInRight 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.glass-card {
  width: 100%;
  padding: 36px 32px;
  background: rgba(255, 255, 255, 0.50);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(43, 124, 255, 0.12);
  box-sizing: border-box;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.card-logo {
  width: 140rpx;
  height: 140rpx;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.card-subtitle {
  font-size: 14px;
  color: #64748b;
}

.form-wrap {
  display: flex;
  flex-direction: column;
}

.input-group {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 16px;
  margin-bottom: 18px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.input-group:focus-within {
  border-color: #2b7cff;
  box-shadow: 0 0 0 4px rgba(43, 124, 255, 0.1);
}

.input-icon {
  font-size: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.glass-input {
  flex: 1;
  height: 100%;
  font-size: 15px;
  color: #1e293b;
  background: transparent;
}

.eye-btn {
  font-size: 18px;
  color: #94a3b8;
  padding-left: 12px;
  cursor: pointer;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 24px;
}

.forget-text {
  font-size: 13px;
  color: #2b7cff;
  font-weight: 500;
}

.submit-btn {
  height: 48px;
  background: linear-gradient(135deg, #5ba3ff 0%, #2b7cff 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 10px 20px rgba(43, 124, 255, 0.28);
  transition: all 0.3s ease;
  margin-bottom: 24px;
}

.submit-btn:active {
  transform: scale(0.96);
}

.submit-btn.btn-loading {
  opacity: 0.7;
}

.btn-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.btn-arrow {
  color: #fff;
  font-size: 15px;
}

.register-bar {
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.register-link {
  color: #2b7cff;
  font-weight: 600;
  margin-left: 4px;
}

.footer-tip {
  position: absolute;
  bottom: 20px;
  right: 40px;
  z-index: 1;
  font-size: 12px;
  color: #64748b;
  letter-spacing: 1px;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(40px);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
  }
}

/* ========== 响应式适配 ========== */
@media (max-width: 768px) {
  .left-experience {
    /* 移动端按钮移到登录卡片上方居中 */
    top: 18%;
    left: 50%;
    transform: translateX(-50%);
  }
  
  .login-area {
    right: auto;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    max-width: 400px;
    padding: 0 24px;
    box-sizing: border-box;
  }
  
  .footer-tip {
    left: 50%;
    right: auto;
    transform: translateX(-50%);
  }
}
</style>
