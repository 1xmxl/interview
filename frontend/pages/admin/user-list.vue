<!-- 系统管理：用户列表 + 角色授权（苍穹权限模型，院系管理员） -->
<template>
  <PageLayout currentPage="admin" title="系统管理">
    <view class="content">
      <!-- 角色筛选 -->
      <view class="filter-row">
        <view
          v-for="f in filters"
          :key="f.code"
          class="filter-chip"
          :class="{ active: activeFilter === f.code }"
          @tap="activeFilter = f.code; load()"
        >
          <text class="filter-text">{{ f.label }}</text>
        </view>
      </view>

      <!-- 搜索 -->
      <view class="search-row">
        <input v-model="keyword" class="search-input" placeholder="搜索用户名 / 姓名" placeholder-style="color:#b0b7c3" @confirm="load" />
        <view class="search-btn" @tap="load"><text class="search-btn-text">搜索</text></view>
      </view>

      <!-- 用户列表 -->
      <view v-for="u in users" :key="u.id" class="user-card glass-card">
        <view class="user-main">
          <text class="user-name">{{ u.realName || u.username }}</text>
          <text class="user-meta">@{{ u.username }} · {{ u.deptName || '未分配院系' }}</text>
        </view>
        <view class="role-tag" @tap="changeRole(u)">
          <text class="role-tag-text">{{ u.roleName || u.roleCode }}</text>
        </view>
      </view>

      <EmptyState v-if="!loading && !users.length" icon="👥" title="暂无用户" description="换个筛选条件试试" />
    </view>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageLayout from '@/components/PageLayout.vue'
import adminApi from '@/api/admin.js'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const activeFilter = ref('')

const filters = [
  { code: '', label: '全部' },
  { code: 'STUDENT', label: '学生' },
  { code: 'COUNSELOR', label: '辅导员' },
  { code: 'HR', label: '企业HR' },
  { code: 'COLLEGE_ADMIN', label: '院系管理员' },
]

const load = async () => {
  loading.value = true
  try {
    const all = await adminApi.listUsers() || []
    // mock 与真实后端都支持前端过滤，保证演示效果一致
    const kw = keyword.value.trim()
    users.value = all.filter((u) => {
      const roleOk = !activeFilter.value || u.roleCode === activeFilter.value
      const kwOk = !kw || (u.username || '').includes(kw) || (u.realName || '').includes(kw)
      return roleOk && kwOk
    })
  } catch (e) {
    users.value = []
  } finally {
    loading.value = false
  }
}

const changeRole = (u) => {
  const roles = filters.slice(1)
  uni.showActionSheet({
    itemList: roles.map((r) => r.label),
    success: async (res) => {
      const role = roles[res.tapIndex]
      try {
        await adminApi.updateRole(u.id, role.code)
        uni.showToast({ title: '角色已更新', icon: 'none' })
        load()
      } catch (e) {}
    },
  })
}

onMounted(load)
</script>

<style scoped>
.content { padding: 24rpx 24rpx 60rpx; }
@media (min-width: 768px) { .content { max-width: 860px; margin: 0 auto; } }

.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 20rpx; }
.filter-chip { padding: 10rpx 24rpx; border-radius: 999rpx; background: rgba(255,255,255,0.5); border: 1px solid rgba(255,255,255,0.7); }
.filter-chip.active { background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); border-color: transparent; }
.filter-text { font-size: 26rpx; color: #6b7280; font-weight: 600; }
.filter-chip.active .filter-text { color: #fff; }

.search-row { display: flex; gap: 12rpx; margin-bottom: 24rpx; }
.search-input { flex: 1; height: 76rpx; padding: 0 22rpx; border-radius: 16rpx; background: rgba(255,255,255,0.55); border: 1px solid rgba(255,255,255,0.7); font-size: 27rpx; color: #1e1b4b; }
.search-btn { padding: 0 28rpx; height: 76rpx; border-radius: 16rpx; background: linear-gradient(135deg, #7c5cfc 0%, #4facfe 100%); display: flex; align-items: center; }
.search-btn-text { color: #fff; font-size: 26rpx; font-weight: 700; }

.user-card { display: flex; align-items: center; justify-content: space-between; padding: 24rpx; margin-bottom: 16rpx; }
.user-main { display: flex; flex-direction: column; }
.user-name { font-size: 30rpx; font-weight: 700; color: #1e1b4b; }
.user-meta { font-size: 24rpx; color: #9ca3af; margin-top: 6rpx; }
.role-tag { padding: 10rpx 20rpx; border-radius: 999rpx; background: rgba(124,92,252,0.1); }
.role-tag-text { font-size: 24rpx; color: #5b3cc4; font-weight: 600; }
</style>
