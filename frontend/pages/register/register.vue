<!-- 注册页 - 响应式 -->
<template>
  <view class="reg-page">
    <AnimatedBg />
    <view class="container reg-wrap">
      <view class="hero-section">
        <view class="logo-ring"><text class="logo-icon">🚀</text></view>
        <text class="hero-title gradient-text">创建账号</text>
        <text class="hero-subtitle">开启你的 AI 面试之旅</text>
      </view>
      <view class="reg-card glass-strong">
        <view class="input-wrap"><text class="input-icon">👤</text><input v-model="username" class="glass-input" placeholder="用户名" placeholder-style="color:#b0b7c3" /></view>
        <view class="input-wrap"><text class="input-icon">📧</text><input v-model="email" class="glass-input" placeholder="邮箱" placeholder-style="color:#b0b7c3" /></view>
        <view class="input-wrap"><text class="input-icon">🔒</text><input v-model="password" class="glass-input" type="password" placeholder="密码 (6位以上)" placeholder-style="color:#b0b7c3" /></view>
        <view class="input-wrap"><text class="input-icon">✓</text><input v-model="confirmPwd" class="glass-input" type="password" placeholder="确认密码" placeholder-style="color:#b0b7c3" /></view>
        <view v-if="password" class="strength-bar"><view class="strength-fill" :class="strengthClass" :style="{width:strengthPercent+'%'}" /></view>
        <view class="reg-btn" @tap="handleRegister"><text class="btn-text">注 册</text></view>
        <text class="link" @tap="goLogin">已有账号？立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import userApi from '@/api/user.js'
const username = ref(''), email = ref(''), password = ref(''), confirmPwd = ref('')
const strengthPercent = computed(() => { const p = password.value; if (!p) return 0; let s = 0; if (p.length >= 6) s += 30; if (p.length >= 10) s += 20; if (/[A-Z]/.test(p)) s += 15; if (/[0-9]/.test(p)) s += 15; if (/[^A-Za-z0-9]/.test(p)) s += 20; return Math.min(s, 100) })
const strengthClass = computed(() => { const s = strengthPercent.value; if (s >= 80) return 'strong'; if (s >= 50) return 'medium'; return 'weak' })
const handleRegister = async () => {
  if (!username.value || !email.value || !password.value) { uni.showToast({ title: '请填写完整信息', icon: 'none' }); return }
  if (password.value !== confirmPwd.value) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return }
  if (password.value.length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return }
  try { uni.showLoading({ title: '注册中...' }); await userApi.register({ username: username.value, password: password.value, email: email.value }); uni.hideLoading(); uni.showToast({ title: '注册成功 🎉' }); setTimeout(() => uni.navigateBack(), 800) } catch (e) { uni.hideLoading() }
}
const goLogin = () => uni.navigateBack()
</script>

<style scoped>
.reg-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; }
.reg-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 60rpx; padding-bottom: 60rpx; z-index: 1; }
@media (min-width: 768px) { .reg-wrap { max-width: 480px; } }
.hero-section { display: flex; flex-direction: column; align-items: center; margin-bottom: 32rpx; animation: fadeInUp 0.8s cubic-bezier(0.16,1,0.3,1) both; }
.logo-ring { width: 110rpx; height: 110rpx; border-radius: 50%; background: rgba(255,255,255,0.45); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 2px solid rgba(255,255,255,0.6); display: flex; align-items: center; justify-content: center; margin-bottom: 20rpx; box-shadow: 0 12px 40px rgba(244,114,182,0.15); animation: float 4s ease-in-out infinite; }
.logo-icon { font-size: 52rpx; }
.hero-title { font-size: 40rpx; font-weight: 800; margin-bottom: 8rpx; }
.hero-subtitle { font-size: 26rpx; color: #9ca3af; }
.reg-card { width: 100%; padding: 36rpx; }
@media (min-width: 768px) { .reg-card { padding: 44px 40px; } }
.input-wrap { display: flex; align-items: center; background: rgba(255,255,255,0.35); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.4); border-radius: 18rpx; padding: 0 20rpx; margin-bottom: 18rpx; }
.input-icon { font-size: 34rpx; margin-right: 14rpx; flex-shrink: 0; }
.glass-input { flex: 1; height: 86rpx; font-size: 28rpx; color: #1e1b4b; background: transparent; }
.strength-bar { height: 8rpx; background: rgba(0,0,0,0.06); border-radius: 4rpx; margin-bottom: 20rpx; overflow: hidden; }
.strength-fill { height: 100%; border-radius: 4rpx; transition: all 0.4s; }
.strength-fill.weak { background: linear-gradient(90deg, #f87171, #fbbf24); }
.strength-fill.medium { background: linear-gradient(90deg, #fbbf24, #4facfe); }
.strength-fill.strong { background: linear-gradient(90deg, #34d399, #4facfe); }
.reg-btn { margin-top: 12rpx; height: 96rpx; background: linear-gradient(135deg, #f472b6 0%, #7c5cfc 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; box-shadow: 0 12rpx 32rpx rgba(244,114,182,0.3); transition: all 0.3s; }
.reg-btn:active { transform: scale(0.96); }
.btn-text { color: #fff; font-size: 32rpx; font-weight: 700; letter-spacing: 6rpx; }
.link { margin-top: 28rpx; text-align: center; font-size: 26rpx; color: #f472b6; font-weight: 500; }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-14rpx); } }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(40rpx); } to { opacity: 1; transform: translateY(0); } }
</style>
