<!-- 配置表单 - 响应式 -->
<template>
  <PageLayout currentPage="config" :title="isEdit?'编辑配置':'新建配置'">
    <view class="content">
      <view class="form-card glass-strong">
        <view class="form-item"><text class="form-label">⚙️ 配置名称</text><input v-model="form.name" class="glass-input" placeholder="例如：Java后端面试" placeholder-style="color:#b0b7c3" /></view>
        <view class="form-item"><text class="form-label">📝 描述</text><input v-model="form.description" class="glass-input" placeholder="题型、难度等说明" placeholder-style="color:#b0b7c3" /></view>
        <view class="btn-primary" @tap="save"><text>{{ isEdit ? '💾 保存修改' : '✨ 创建配置' }}</text></view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import interviewApi from '@/api/interview.js'
let configId = ''
const isEdit = ref(false), form = ref({ name: '', description: '' })
onLoad((opt) => { if (opt.id) { configId = opt.id; isEdit.value = true; loadData() } })
const loadData = async () => { try { const data = await interviewApi.getConfig(configId); form.value = { name: data.name || '', description: data.description || '' } } catch (e) {} }
const save = async () => { if (!form.value.name) { uni.showToast({ title: '请填写配置名称', icon: 'none' }); return }; try { if (isEdit.value) { await interviewApi.updateConfig(configId, form.value) } else { await interviewApi.createConfig(form.value) }; uni.showToast({ title: '已保存 ✅', icon: 'none' }); setTimeout(() => uni.navigateBack(), 500) } catch (e) {} }
</script>

<style scoped>
.content { padding-bottom: 60rpx; }
@media (min-width: 768px) { .content { max-width: 600px; margin: 0 auto; } }
.form-card { padding: 36rpx; }
.form-item { margin-bottom: 24rpx; }
.form-label { font-size: 26rpx; color: #6b7280; font-weight: 600; display: block; margin-bottom: 10rpx; }
.glass-input { width: 100%; height: 84rpx; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border: 1px solid rgba(0,0,0,0.06); border-radius: 16rpx; padding: 0 20rpx; font-size: 28rpx; color: #1e1b4b; box-sizing: border-box; }
.btn-primary { margin-top: 32rpx; height: 92rpx; background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); background-size: 200% 200%; animation: gradient-shift 4s ease infinite; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 30rpx; font-weight: 700; box-shadow: 0 10rpx 28rpx rgba(124,92,252,0.3); transition: all 0.3s; }
.btn-primary:active { transform: scale(0.96); }
@keyframes gradient-shift { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
</style>
