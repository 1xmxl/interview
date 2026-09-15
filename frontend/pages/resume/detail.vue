<!-- 简历详情 - 移动全宽 + 桌面双栏 -->
<template>
  <PageLayout currentPage="resume" title="简历详情">
    <view class="content">
      <view v-if="loading" class="loading-wrap"><SkeletonCard :lines="4" /><SkeletonCard :lines="3" /></view>

      <view v-else class="detail-layout">
        <view class="detail-main">
          <view class="main-header glass-strong">
            <view class="mh-icon">📄</view>
            <view>
              <text class="mh-title">{{ resume.coverName || resume.originalFilename || '未命名简历' }}</text>
              <view class="mh-meta">
                <text class="mh-badge" :style="statusStyle(resume.status)">{{ statusText(resume.status) }}</text>
                <text class="mh-date">{{ formatTime(resume.createdAt) }}</text>
              </view>
            </view>
          </view>

          <view class="block glass-card">
            <text class="block-title">🎯 技能</text>
            <view v-if="resume.skills && resume.skills.length" class="skill-cloud">
              <SkillBadge v-for="(s, i) in resume.skills" :key="'sk-'+i" :name="s.skillName" :level="s.proficiency" size="md" />
            </view>
            <text v-else class="empty-text">暂无技能信息</text>
          </view>

          <view class="block glass-card">
            <text class="block-title">💼 工作经历</text>
            <view v-if="resume.experiences && resume.experiences.length">
              <view v-for="(exp, i) in resume.experiences" :key="'exp-'+i" class="exp-item">
                <view class="exp-dot" />
                <view class="exp-content glass-light">
                  <view class="exp-row">
                    <text class="exp-pos">{{ exp.position || '工作经历' }}</text>
                    <text v-if="exp.company" class="exp-company">@ {{ exp.company }}</text>
                  </view>
                  <text v-if="exp.startDate || exp.endDate" class="exp-date">{{ exp.startDate || '?' }} ~ {{ exp.endDate || '至今' }}</text>
                  <text v-if="exp.description" class="exp-desc">{{ exp.description }}</text>
                </view>
              </view>
            </view>
            <text v-else class="empty-text">暂无工作经历</text>
          </view>

          <view class="block glass-card">
            <text class="block-title">🚀 项目经历</text>
            <view v-if="resume.projects && resume.projects.length" class="resp-grid col-2">
              <view v-for="(p, i) in resume.projects" :key="'pr-'+i" class="proj-card glass-light">
                <text class="proj-name">{{ p.projectName }}</text>
                <text v-if="p.role" class="proj-role">{{ p.role }}</text>
                <text v-if="p.description" class="proj-desc">{{ p.description }}</text>
                <view v-if="p.technologies && p.technologies.length" class="tech-row">
                  <text v-for="t in p.technologies" :key="t" class="tech-tag">{{ t }}</text>
                </view>
              </view>
            </view>
            <text v-else class="empty-text">暂无项目经历</text>
          </view>

          <view class="block glass-card">
            <text class="block-title">🎓 教育背景</text>
            <view v-if="resume.educations && resume.educations.length" class="resp-grid col-2">
              <view v-for="(edu, i) in resume.educations" :key="'ed-'+i" class="edu-item glass-light">
                <text class="edu-degree">{{ edu.degree }} · {{ edu.fieldOfStudy }}</text>
                <text class="edu-school">{{ edu.institution }}</text>
              </view>
            </view>
            <text v-else class="empty-text">暂无教育信息</text>
          </view>
        </view>

        <view class="detail-sidebar hide-mobile">
          <view class="sidebar-card glass-strong">
            <text class="sb-title">操作</text>
            <view class="sb-btn primary" @tap="reparse">🔄 重新解析</view>
            <view class="sb-btn danger" @tap="deleteResume">🗑️ 删除简历</view>
          </view>
          <view class="sidebar-card glass-card">
            <text class="sb-title">文件信息</text>
            <view class="sb-info-row"><text class="sb-label">文件名</text><text class="sb-value">{{ resume.originalFilename || '--' }}</text></view>
            <view class="sb-info-row"><text class="sb-label">上传时间</text><text class="sb-value">{{ formatTime(resume.createdAt) }}</text></view>
            <view class="sb-info-row"><text class="sb-label">状态</text><text class="sb-value" :style="{color:statusColor(resume.status)}">{{ statusText(resume.status) }}</text></view>
          </view>
        </view>

        <view class="mobile-actions hide-desktop">
          <view class="btn-primary" @tap="reparse">🔄 重新解析</view>
          <view class="btn-outline" @tap="deleteResume">🗑️ 删除简历</view>
        </view>
      </view>
    </view>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PageLayout from '@/components/PageLayout.vue'
import resumeApi from '@/api/resume.js'
const resume = ref({}), loading = ref(true)
let resumeId = ''
onLoad((opt) => { resumeId = opt.resumeId; loadDetail() })
const loadDetail = async () => {
  try {
    loading.value = true
    const [detail, list] = await Promise.all([
      resumeApi.detail(resumeId),
      resumeApi.list().catch(() => [])
    ])
    const meta = (list || []).find(r => String(r.id) === String(resumeId)) || {}
    resume.value = {
      ...(detail || {}),
      // 详情接口(ParsedResumeDataVO)已带 originalFilename/status/createdAt；列表兜底仅作兼容
      coverName: detail.coverName || meta.coverName || '',
      originalFilename: detail.originalFilename || meta.originalFilename || '',
      status: detail.status || meta.status || '',
      createdAt: detail.createdAt || meta.createdAt || '',
      // 空数据容错 + 去重（后端解析/入库可能产生空壳或重复条目）
      skills: dedupe(detail.skills || [], s => s.skillName || ''),
      experiences: dedupe(detail.experiences || [], e => [e.company, e.position, e.startDate].join('|')),
      projects: detail.projects || [],
      educations: dedupe(detail.educations || [], e => [e.institution, e.degree, e.fieldOfStudy].join('|')),
    }
  } catch (e) { uni.showToast({ title: '加载失败', icon: 'none' }) } finally { loading.value = false }
}
const dedupe = (arr, keyFn) => {
  const seen = new Set()
  return (arr || []).filter(x => {
    const k = keyFn(x)
    if (!k || seen.has(k)) return false
    seen.add(k)
    return true
  })
}
const reparse = async () => { try { await resumeApi.reparse(resumeId); uni.showToast({ title: '已提交', icon: 'none' }) } catch (e) {} }
const deleteResume = async () => { uni.showModal({ title: '确认删除', content: '删除后不可恢复', success: async (r) => { if (r.confirm) { try { await resumeApi.delete(resumeId); uni.showToast({ title: '已删除' }); uni.navigateBack() } catch (e) {} } } }) }
const formatTime = (d) => { if (!d) return ''; const dt = new Date(String(d).replace(' ', 'T')); if (isNaN(dt.getTime())) return ''; return dt.getFullYear()+'-'+String(dt.getMonth()+1).padStart(2,'0')+'-'+String(dt.getDate()).padStart(2,'0') }
const statusText = (s) => ({ PENDING:'等待解析', PARSING:'解析中', PARSED:'已解析', FAILED:'解析失败' }[s] || s)
const statusColor = (s) => ({ PENDING:'#fbbf24', PARSING:'#4facfe', PARSED:'#34d399', FAILED:'#f87171' }[s] || '#999')
const statusStyle = (s) => { const m = { PENDING:'color:#f59e0b;background:rgba(251,191,36,0.1)', PARSING:'color:#4facfe;background:rgba(79,172,254,0.1)', PARSED:'color:#34d399;background:rgba(52,211,153,0.1)', FAILED:'color:#f87171;background:rgba(248,113,113,0.1)' }; return m[s] || '' }
</script>

<style scoped>
.content { padding-bottom: 80rpx; }

.detail-layout { display: flex; flex-direction: column; gap: 20rpx; }
@media (min-width: 768px) {
  .detail-layout { flex-direction: row; gap: 32px; align-items: flex-start; }
}
.detail-main { flex: 1; min-width: 0; }
@media (min-width: 768px) {
  .detail-sidebar { width: 280px; flex-shrink: 0; position: sticky; top: 80px; }
}

.main-header {
  display: flex; align-items: center; gap: 20rpx;
  padding: 28rpx; margin-bottom: 18rpx; border-radius: 22rpx;
}
.mh-icon { font-size: 48rpx; }
.mh-title { font-size: 30rpx; font-weight: 700; color: #1e1b4b; display: block; }
.mh-meta { display: flex; gap: 14rpx; align-items: center; margin-top: 8rpx; }
.mh-badge { font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 10rpx; font-weight: 600; }
.mh-date { font-size: 24rpx; color: #9ca3af; }

.block { padding: 24rpx; margin-bottom: 16rpx; border-radius: 18rpx; }
.block-title { font-size: 26rpx; font-weight: 700; color: #1e1b4b; display: block; margin-bottom: 16rpx; }
.skill-cloud { display: flex; flex-wrap: wrap; gap: 10rpx; }

.exp-item { display: flex; gap: 16rpx; margin-bottom: 14rpx; }
.exp-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #7c5cfc; margin-top: 10rpx; flex-shrink: 0; }
.exp-content { flex: 1; padding: 16rpx; border-radius: 14rpx; }
.exp-row { display: flex; align-items: baseline; gap: 8rpx; }
.exp-pos { font-size: 26rpx; font-weight: 700; color: #1e1b4b; }
.exp-company { font-size: 24rpx; color: #7c5cfc; }
.exp-date { font-size: 22rpx; color: #9ca3af; display: block; margin-top: 4rpx; }
.exp-desc { font-size: 24rpx; color: #6b7280; display: block; margin-top: 8rpx; line-height: 1.5; }

.proj-card { padding: 16rpx; border-radius: 14rpx; }
.proj-name { font-size: 26rpx; font-weight: 700; color: #1e1b4b; display: block; }
.proj-role { font-size: 22rpx; color: #f472b6; display: block; }
.proj-desc { font-size: 24rpx; color: #6b7280; display: block; margin-top: 6rpx; line-height: 1.5; }
.tech-row { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 10rpx; }
.tech-tag { font-size: 20rpx; padding: 2rpx 10rpx; background: rgba(124,92,252,0.08); color: #7c5cfc; border-radius: 8rpx; }

.edu-item { padding: 16rpx; border-radius: 14rpx; }
.edu-degree { font-size: 26rpx; font-weight: 700; color: #1e1b4b; display: block; }
.edu-school { font-size: 24rpx; color: #6b7280; display: block; margin-top: 4rpx; }

.empty-text { font-size: 24rpx; color: #9ca3af; text-align: center; padding: 28rpx 0; display: block; }

.sidebar-card { padding: 24rpx; margin-bottom: 18rpx; border-radius: 18rpx; }
.sb-title { font-size: 24rpx; font-weight: 700; color: #9ca3af; margin-bottom: 16rpx; display: block; text-transform: uppercase; letter-spacing: 1px; }
.sb-btn { height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 15px; font-weight: 600; margin-bottom: 10px; transition: all 0.2s; cursor: pointer; }
.sb-btn:active { transform: scale(0.96); }
.sb-btn.primary { background: linear-gradient(135deg, #7c5cfc, #4facfe); color: #fff; }
.sb-btn.danger { border: 1px solid rgba(248,113,113,0.3); color: #f87171; }
.sb-info-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid rgba(0,0,0,0.04); }
.sb-label { font-size: 14px; color: #9ca3af; }
.sb-value { font-size: 14px; color: #1e1b4b; font-weight: 500; max-width: 60%; text-align: right; word-break: break-all; }

.mobile-actions { display: flex; gap: 14rpx; margin-top: 20rpx; }
.mobile-actions .btn-primary { flex: 1; height: 88rpx; background: linear-gradient(135deg,#7c5cfc,#4facfe); border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28rpx; font-weight: 700; }
.mobile-actions .btn-outline { flex: 1; height: 88rpx; border: 2rpx solid rgba(248,113,113,0.3); border-radius: 22rpx; display: flex; align-items: center; justify-content: center; color: #f87171; font-size: 28rpx; font-weight: 600; }
</style>
