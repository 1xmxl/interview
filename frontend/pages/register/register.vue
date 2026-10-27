<template>
  <view class="reg-page">
    <!-- 背景柔光遮罩 -->
    <view class="bg-overlay"></view>
    <view class="reg-container">
      <!-- 右侧注册卡片 -->
      <view class="reg-area">
        <view class="reg-card glass-card">
          <!-- 卡片头部 -->
          <view class="card-header">
            <image class="card-logo" src="/static/log-m.png" mode="aspectFit"></image>
            <view class="header-info">
              <text class="card-title" style="text-align:center">注册账号</text>
              <text class="card-subtitle">开启你的专属面试之旅</text>
            </view>
          </view>
          <!-- 注册表单 -->
          <view class="form-wrap">
            <view class="input-group">
              <input
                v-model="username"
                class="glass-input"
                placeholder="用户名"
                placeholder-style="color:#a0aec0"
              />
            </view>
            <view class="input-group">
              <input
                v-model="email"
                class="glass-input"
                placeholder="邮箱地址"
                placeholder-style="color:#a0aec0"
              />
            </view>
            <view class="input-group">
              <input
                v-model="password"
                class="glass-input"
                :type="showPwd ? 'text' : 'password'"
                placeholder="密码"
                placeholder-style="color:#a0aec0"
              />
            </view>
            <view class="input-group">
              <input
                v-model="confirmPwd"
                class="glass-input"
                :type="showConfirm ? 'text' : 'password'"
                placeholder="再次输入密码"
                placeholder-style="color:#a0aec0"
              />
            </view>

            <view v-if="password" class="strength-bar">
              <view class="strength-fill" :class="strengthClass" :style="{ width: strengthPercent + '%' }" />
            </view>

            <!-- 注册按钮 -->
            <view class="submit-btn" @tap="handleRegister">
              <text class="btn-text">注册</text>
            </view>
            <!-- 登录入口 -->
            <text class="register-bar">
              已有账号？
              <text class="register-link" @tap="goLogin"> 立即登录 </text>
            </text>
            <!-- 协议 -->
            <view class="agree" @tap="agreed = !agreed">
              <view class="checkbox" :class="{ checked: agreed }">{{ agreed ? '✓' : '' }}</view>
              <text class="agree-text">我已阅读并同意《用户协议》和《隐私政策》</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import userApi from '@/api/user.js'
const username = ref(''), email = ref(''), password = ref(''), confirmPwd = ref('')
const showPwd = ref(false), showConfirm = ref(false), agreed = ref(false)

const strengthPercent = computed(() => { const p = password.value; if (!p) return 0; let s = 0; if (p.length >= 6) s += 30; if (p.length >= 10) s += 20; if (/[A-Z]/.test(p)) s += 15; if (/[0-9]/.test(p)) s += 15; if (/[^A-Za-z0-9]/.test(p)) s += 20; return Math.min(s, 100) })
const strengthClass = computed(() => { const s = strengthPercent.value; if (s >= 80) return 'strong'; if (s >= 50) return 'medium'; return 'weak' })

const handleRegister = async () => {
  if (!agreed.value) { uni.showToast({ title: '请先阅读并同意协议', icon: 'none' }); return }
  if (!username.value || !email.value || !password.value) { uni.showToast({ title: '请填写完整信息', icon: 'none' }); return }
  if (password.value !== confirmPwd.value) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return }
  if (password.value.length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return }
  try { uni.showLoading({ title: '注册中...', mask: true }); await userApi.register({ username: username.value, password: password.value, email: email.value }); uni.hideLoading(); uni.showToast({ title: '注册成功 🎉', icon: 'none' }); setTimeout(() => uni.navigateBack(), 800) } catch (e) { uni.hideLoading() }
}
const goLogin = () => uni.navigateBack()
</script>

<style scoped>
.reg-page {
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

.reg-container {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
}

/* ========== 右侧注册卡区域 ========== */
.reg-area {
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
  -webkit-backdrop-filter: blur(12px);
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
}

/* 密码强度条 */
.strength-bar {
  height: 6px;
  background: rgba(43, 124, 255, 0.1);
  border-radius: 3px;
  margin-bottom: 18px;
  overflow: hidden;
}
.strength-fill { height: 100%; border-radius: 3px; transition: all 0.4s; }
.strength-fill.weak { background: linear-gradient(90deg, #f87171, #fbbf24); }
.strength-fill.medium { background: linear-gradient(90deg, #5ba3ff, #2b7cff); }
.strength-fill.strong { background: linear-gradient(90deg, #2b7cff, #34d399); }

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

.btn-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
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

/* 协议勾选 */
.agree {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 18px;
}
.checkbox {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid #b9c9ee;
  background: rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  margin-right: 8px;
  flex-shrink: 0;
  transition: all 0.2s;
}
.checkbox.checked { background: #2b7cff; border-color: #2b7cff; }
.agree-text { font-size: 12px; color: #64748b; }

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
  .reg-area {
    right: auto;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    max-width: 400px;
    padding: 0 24px;
    box-sizing: border-box;
  }
}
</style>
