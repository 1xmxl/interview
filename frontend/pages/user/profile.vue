<!-- 个人资料 - 响应式 -->
<template>
  <PageLayout currentPage="profile" title="个人资料">
    <view class="content">
      <view class="avatar-section">
        <view class="big-avatar-ring"><text class="big-avatar-emoji">🙋</text></view>
        <text class="user-name gradient-text">{{ form.username || '用户' }}</text>
      </view>
      <view class="form-card glass-strong">
        <view class="form-item"><text class="form-label">👤 用户名</text><input v-model="form.username" class="glass-input" placeholder="请输入用户名" placeholder-style="color:#b0b7c3" /></view>
        <view class="form-item"><text class="form-label">📧 邮箱</text><input v-model="form.email" class="glass-input" placeholder="请输入邮箱" placeholder-style="color:#b0b7c3" /></view>
        <view class="btn-primary" @tap="update">保存修改</view>
        <view class="btn-outline" @tap="logout">退出登录</view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import userApi from '@/api/user.js'
const form = ref({ username: '', email: '' })
onMounted(async () => { try { const data = await userApi.getProfile(); form.value = { username: data.username || '', email: data.email || '' } } catch (e) {} })
const update = async () => { try { await userApi.updateProfile(form.value); uni.showToast({ title: '已更新 ✅', icon: 'none' }) } catch (e) {} }
const logout = async () => { try { await userApi.logout() } catch (e) {}; uni.removeStorageSync('token'); uni.removeStorageSync('refreshToken'); uni.reLaunch({ url: '/pages/login/login' }) }
</script>

<style scoped>
.content { padding-bottom: 60rpx; }
@media (min-width: 768px) { .content { max-width: 560px; margin: 0 auto; } }
.avatar-section { display: flex; flex-direction: column; align-items: center; padding: 40rpx 0 32rpx; }
.big-avatar-ring { width: 140rpx; height: 140rpx; border-radius: 50%; background: rgba(255,255,255,0.45); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 1px solid rgba(0,0,0,0.06); display: flex; align-items: center; justify-content: center; box-shadow: 0 12px 40px rgba(124,92,252,0.12); margin-bottom: 20rpx; animation: float 4s ease-in-out infinite; }
.big-avatar-emoji { font-size: 64rpx; }
.user-name { font-size: 36rpx; font-weight: 800; }
.form-card { padding: 36rpx; }
@media (min-width: 768px) { .form-card { padding: 40px; } }
.form-item { margin-bottom: 24rpx; }
.form-label { font-size: 26rpx; color: #6b7280; font-weight: 600; display: block; margin-bottom: 10rpx; }
.glass-input { width: 100%; height: 84rpx; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border: 1px solid rgba(0,0,0,0.06); border-radius: 16rpx; padding: 0 20rpx; font-size: 28rpx; color: #1e1b4b; box-sizing: border-box; }
.btn-primary { margin-top: 32rpx; height: 92rpx; background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 30rpx; font-weight: 700; box-shadow: 0 10rpx 28rpx rgba(124,92,252,0.3); transition: all 0.3s; }
.btn-primary:active { transform: scale(0.96); }
.btn-outline { margin-top: 18rpx; height: 84rpx; border: 2rpx solid rgba(124,92,252,0.2); border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #6b7280; font-size: 28rpx; font-weight: 600; background: transparent; transition: all 0.3s; }
.btn-outline:active { background: rgba(124,92,252,0.05); }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-14rpx); } }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
</style>
