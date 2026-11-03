# 前端接口契约（与后端实际返回对齐）

> 本文件是前端开发的「数据契约」：改页面前先看这里，不要凭页面现有变量猜后端字段。
> 后端：Spring Cloud 微服务，统一走网关 `http://<SERVER_IP>:10010`。
> 除 `/as/**` 外所有请求需要 header：`Authorization: Bearer <accessToken>`。

## 0. 通用约定

| 项目 | 说明 |
|------|------|
| 网关前缀 | `/as` auth、`/us` user、`/res` resume、`/is` session、`/ae` evaluation、`/fa` feedback、`/ug` growth |
| 返回格式 | **没有统一 `{code,msg,data}` 包装**，成功直接返回对象/数组；失败返回 Spring 错误 JSON `{timestamp,status,error,path}` |
| 空值 | 后端字段可能为 `null`，页面要判空（`?.` / `||`） |
| 日期时间 | `LocalDateTime` 序列化为 ISO 字符串，如 `2026-11-03T14:17:41` |
| ID | 除配置外都是自增数字；配置 ID 已是自增（不再是雪花 ID） |
| 枚举 | 大小写固定：`FRIENDLY/STRICT/PRESSURE`、`EASY/MEDIUM/HARD`、`ZH/EN`、`PENDING/PARSING/PARSED/FAILED`、`CREATED/IN_PROGRESS/COMPLETED/EVALUATED` |
| 列表接口 | 部分接口没有分页，直接返回数组 |

## 1. auth（`/as`）

| 方法 | 路径 | 请求 | 返回 |
|------|------|------|------|
| POST | `/as/auth/register` | `{username, password, email, realName?, roleCode?, deptId?}` | 空 |
| POST | `/as/auth/login` | `{username, password}` | `LoginResponse` |
| POST | `/as/auth/refresh` | Header: `Authorization: Bearer <refreshToken>` | `{access_token, refresh_token}` |
| POST | `/as/auth/logout` | Header: `Authorization: Bearer <accessToken>`、`Refresh-Token: Bearer <refreshToken>` | 空 |

`LoginResponse`：

```
accessToken   string   访问令牌（30 分钟）
refreshToken  string   刷新令牌（7 天）
username      string
role          string   STUDENT / COUNSELOR / HR / COLLEGE_ADMIN
roleName      string   学生 / 辅导员 / 企业HR / 院系管理员
dataScope     string   SELF / DEPT / COLLEGE / ALL
deptId        number
permissions   string[] 权限码（menu: / btn: / api:）
menus         string[] 菜单权限码（侧边栏按此过滤）
```

> 登录后必须把 `role / roleName / menus / permissions / dataScope / deptId` 存本地，侧边栏和页面按 `menus` 过滤。

## 2. user（`/us`）

| 方法 | 路径 | 返回 |
|------|------|------|
| GET | `/us/users/users/me/profile` | `{username, email, roleCode, roleName, deptId, deptName, createdAt, updatedAt}` |
| PUT | `/us/users/me` | 空（`{username, email, password?}`） |
| GET | `/us/users/me/stats` | `{interviewCount, avgScore, skillCount, reportCount}` |
| GET | `/us/user-profiles/me` | `{userId, fullName, avatarUrl, phone}` |
| GET | `/us/users/admin/list` | `[{id, username, realName, email, roleCode, roleName, deptId, deptName, createdAt}]`（仅 COLLEGE_ADMIN） |
| PUT | `/us/users/admin/{userId}/role?roleCode=` | 空（仅 COLLEGE_ADMIN） |
| GET | `/us/users/admin/roles` | `[{code, name, dataScope}]` |
| GET | `/us/users/admin/permissions?roleCode=` | `string[]` |

## 3. resume（`/res`）

| 方法 | 路径 | 返回 |
|------|------|------|
| POST | `/res/resumes/upload` | 空（form-data: `file`） |
| GET | `/res/resumes` | `[{id, originalFilename, status, createdAt, updatedAt, coverName, fileType}]` |
| GET | `/res/resumes/{resumeId}` | `{educations, experiences, skills, projects}` |
| DELETE | `/res/resumes/{resumeId}` | 空 |
| GET | `/res/resumes/{resumeId}/skills` | `[{id, resumeId, skillName, proficiency, category}]` |
| GET | `/res/resumes/{resumeId}/experiences` | `[{id, resumeId, company, position, startDate, endDate, description, orderIndex}]` |
| POST | `/res/resumes/{resumeId}/reparse` | 空 |

> 简历状态：`PENDING → PARSING → PARSED / FAILED`，轮询或刷新列表看状态。
> 详情里的 `educations/experiences/skills/projects` 是结构化数组，字段分别为：
> education `{institution, degree, fieldOfStudy, startDate, endDate}`
> experience `{company, position, startDate, endDate, description}`
> skill `{skillName, proficiency, category}`
> project `{projectName, description, technologies[], role, startDate, endDate}`

## 4. session（`/is`）

### 4.1 配置

| 方法 | 路径 | 请求 | 返回 |
|------|------|------|------|
| POST | `/is/interviews/configs` | `ConfigSubbmit` | 空 |
| GET | `/is/interviews/configs` | - | `[InterviewConfigs]` |
| GET | `/is/interviews/configs/{id}` | - | `InterviewConfigs` |
| PUT | `/is/interviews/configs/{id}` | `ConfigSubbmit` | 空 |
| DELETE | `/is/interviews/configs/{id}` | - | 空 |

`ConfigSubbmit` / `InterviewConfigs`：

```
id                   number
userId               number
name                 string
description          string
interviewerStyle     string   FRIENDLY / STRICT / PRESSURE
difficulty           string   EASY(3题) / MEDIUM(5题) / HARD(8题)
targetSkills         string[]
questionCategories   string[]
language             string   ZH / EN
createdAt            string
```

### 4.2 会话

| 方法 | 路径 | 请求 | 返回 |
|------|------|------|------|
| POST | `/is/interviews/sessions` | `{configId, resumeId}` | **数字**（会话 ID，如 `8`），不是对象 |
| GET | `/is/interviews/sessions/{id}` | - | `{id, userId, configId, resumeId, status, startTime, endTime, currentQuestionIndex, totalQuestions}` |
| GET | `/is/interviews/sessions/{id}/history` | - | `[SessionHistoryVO]` |
| POST | `/is/interviews/sessions/{id}/start` | - | `{questionId, question, totalQuestions}` |
| POST | `/is/interviews/sessions/{id}/answer` | `{text, audioUrl}` | `{hasNext, questionId, nextQuestion}` |
| POST | `/is/interviews/sessions/{id}/end` | - | 空（触发异步生成报告） |
| GET | `/is/interviews/stats` | - | `{interviewCount, avgScore}` |

`SessionHistoryVO`：

```
questionId      number
questionText    string
expectedSkills  string
orderIndex      number
askedAt         string
answerId        number
answerText      string
audioFileUrl    string
submittedAt     string
```

> ⚠️ `createSession` 返回**数字**，不要写 `session.id`；用 `const id = typeof res === 'object' ? res.id : res`。
> `answer` 返回 `hasNext=false` 表示题目答完，应调用 `end`。

## 5. evaluation（`/ae`）

| 方法 | 路径 | 返回 |
|------|------|------|
| GET | `/ae/evaluations/{sessionId}` | `EvaluationResultVO` |
| GET | `/ae/evaluations/{sessionId}/answers/{answerId}` | `AnswerEvaluations` |
| POST | `/ae/evaluations/{sessionId}/report/regenerate` | 空 |

`EvaluationResultVO`：

```
sessionId       number
reportContent   string  报告正文（HTML 字符串）
metrics         string  各维度分数（JSON 字符串，需要 JSON.parse）
generatedAt     string
answers         array   逐题评估（AnswerEvaluations）
```

`AnswerEvaluations`：

```
id                      number
sessionId               number
questionId              number
answerId                number
technicalScore          number
clarityScore            number
depthScore              number
feedbackText            string
improvementSuggestions  string
evaluatedAt             string
```

> ⚠️ 后端**没有** `overallScore / scores / summary / strengths / weaknesses / question / answer` 这些字段。
> 页面要展示总分时，从 `metrics`（`JSON.parse` 后）计算平均分；展示题目/答案时用 `/is/interviews/sessions/{id}/history` 的数据。
> 未生成报告时返回 `{sessionId, answers: []}`（`reportContent` 为 null），页面要显示空态。

## 6. feedback（`/fa`）

| 方法 | 路径 | 返回 |
|------|------|------|
| GET | `/fa/feedback/{sessionId}/speech` | `[{feature, value, unit}]` |
| GET | `/fa/feedback/analytics?userId=&period=30d` | `{fluency, pronunciation, wpm, fillerWords: [{word, count}], trend: number[]}` |

## 7. growth（`/ug`）

| 方法 | 路径 | 返回 |
|------|------|------|
| GET | `/ug/growth/skills` | `[{name, skillName, score, level, dimension, recordedAt}]` |
| GET | `/ug/growth/skills/history?skill=Java&period=6m` | `[{date, score}]` |
| GET | `/ug/growth/reports` | `[{id, userId, periodStart, periodEnd, insights, generatedAt}]` |
| GET | `/ug/growth/reports/{id}` | 同上 |
| GET | `/ug/growth/weaknesses` | `[{skill, skillName, score, severity, dimension, suggestion}]` |
| GET | `/ug/growth/stats` | `{skillCount, reportCount}` |

> ⚠️ 成长报告**没有 `title / date / content` 字段**；页面标题用 `periodStart ~ periodEnd` 或 `insights.title`，正文用 `insights`（对象，`JSON.stringify` 后给 `rich-text`）。

## 8. mock 切换

`mock/index.js` 里 `USE_MOCK` 为接口粒度开关：

- `false`：走真实后端（当前默认）
- `true`：走本地演示数据

> mock 数据里有一些**后端不存在的字段**（如 `scores`、`comment`、`title`、`date`、`question`/`answer`），只用于演示。改页面时以本契约文档为准，不要以 mock 字段为准。

## 9. 页面字段对照（改这些页面前必看）

| 页面 | 当前使用的字段 | 后端实际字段 | 处理方式 |
|------|---------------|-------------|---------|
| `pages/evaluation/result.vue` | `scores / summary / strengths / weaknesses / overallScore` | `metrics / reportContent / answers` | 解析 `metrics` 计算平均分；`reportContent` 用 `rich-text` 渲染 |
| `pages/evaluation/answer-detail.vue` | `question / answer` | `questionId / answerId / technicalScore...`（无题目/答案文本） | 题目/答案从 `history` 接口按 `answerId` 匹配 |
| `pages/growth/report-list.vue` | `title / date` | `periodStart / periodEnd / insights` | 显示周期或 `insights.title` |
| `pages/growth/report-detail.vue` | `content` | `insights` | `JSON.stringify(insights)` |
| `pages/interview/session-room.vue` | `comment / currentQuestionIndex` | 无（`answer` 只返回 `hasNext/questionId/nextQuestion`） | 不要依赖 AI 点评；进度用本地计数 |
| `pages/interview/session-create.vue` | `session.id` | `createSession` 返回数字 | 已做兼容：`typeof res === 'object' ? res.id : res` |

## 10. 后端未实现/预留的接口

- 暂无（feedback 的语音转写依赖外部 ASR，未接入时返回空数组）
