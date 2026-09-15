/**
 * ============================================================
 *  演示 Mock 数据层
 *  ------------------------------------------------------------
 *  USE_MOCK = true  → 所有接口返回本地假数据（带模拟延迟），
 *                     用于前后端分离演示，无需启动后端。
 *  USE_MOCK = false → 走真实后端（网关 http://localhost:10010）。
 *
 *  后端每完成一个接口，把对应 api/ 模块里的 USE_MOCK 关掉即可，
 *  页面无需任何改动（数据结构按后端接口文档对齐）。
 * ============================================================
 */

/**
 * 接口粒度开关：
 *   false = 后端已实现 → 走真实后端（http://localhost:10010）
 *   true  = 后端未实现 → 走本地 mock 数据
 *
 * 后端每完成一个接口，把对应项置 false 即可切换，页面无需改动。
 */
export const USE_MOCK = {
  /* ===== 后端已实现 → 真实 ===== */
  auth: false,          // auth-service：login/register/refresh/logout 已实现
  resumeList: false,    // resume-service：list/detail/delete/skills/experiences 已实现
  resumeUpload: false,  // resume-service：upload 已实现（依赖 OSS + RabbitMQ 解析闭环）
  configs: false,       // interview-session：配置 CRUD 已实现（⚠️ 服务当前编译不过，见 README）
  createSession: false, // interview-session：创建会话已实现（⚠️ 同上）

  /* ===== 后端未实现 → mock ===== */
  getProfile: false,     // user-service 已实现资料接口 GET /users/users/me/profile
  updateProfile: false,  // user-service 已注册 UserInfoInterceptor，PUT /users/me 可用
  reparse: true,        // 后端无 reparse 接口
  sessionFlow: true,    // start/answer/end 未实现
  history: true,        // getHistory 空壳（编译不过）
  evaluation: true,     // ai-evaluation-service 无 controller
  feedback: true,       // feedback-analysis-service 空壳
  growth: true,         // user-growth-service 空壳
  dashboard: true,      // 首页聚合为演示专用接口
};

// 模拟网络延迟（毫秒），让 loading 状态可见、更接近真实体验
const delay = (ms = 350) => new Promise((resolve) => setTimeout(resolve, ms));

/* ===================== 基础工具 ===================== */

let idSeq = 100;
const nextId = () => ++idSeq;

// 生成演示用 token（纯展示，后端接入后由真实 JWT 替代）
const fakeToken = () =>
  'demo.' + Math.random().toString(36).slice(2) + '.' + Date.now().toString(36);

/* ===================== 用户 ===================== */

const mockUser = {
  id: 1,
  username: '张伟',
  email: 'zhangwei@example.com',
  fullName: '张伟',
  avatarUrl: '',
  phone: '138****8888',
};

/* ===================== 简历 ===================== */

const mockResumes = [
  {
    id: 1,
    resumeId: 1,
    coverName: '张伟-Java后端开发工程师.pdf',
    originalFilename: '张伟-简历-2026.pdf',
    fileType: '.pdf',
    status: 'PARSED',
    createdAt: '2026-07-20 14:32:00',
    skills: [
      { skillName: 'Java', proficiency: 'EXPERT' },
      { skillName: 'Spring Boot', proficiency: 'ADVANCED' },
      { skillName: 'Spring Cloud', proficiency: 'ADVANCED' },
      { skillName: 'MySQL', proficiency: 'ADVANCED' },
      { skillName: 'Redis', proficiency: 'INTERMEDIATE' },
      { skillName: 'RabbitMQ', proficiency: 'INTERMEDIATE' },
      { skillName: 'Docker', proficiency: 'INTERMEDIATE' },
      { skillName: 'Kubernetes', proficiency: 'BEGINNER' },
    ],
    experiences: [
      {
        company: '杭州云帆科技有限公司',
        position: 'Java 后端开发工程师',
        startDate: '2023-03',
        endDate: '至今',
        description:
          '负责电商中台订单与库存模块的设计与开发，主导订单状态机重构，支撑日均 200 万订单量；搭建基于 RabbitMQ 的异步削峰链路，峰值下单成功率提升至 99.95%。',
      },
      {
        company: '上海明略信息技术有限公司',
        position: 'Java 开发工程师',
        startDate: '2021-07',
        endDate: '2023-02',
        description:
          '参与企业级 SaaS 系统的报表模块开发，使用 Spring Boot + MyBatis-Plus 完成复杂报表查询优化，接口平均耗时从 2.3s 优化至 380ms。',
      },
    ],
    projects: [
      {
        projectName: '高并发秒杀系统',
        role: '核心开发',
        description:
          '基于 Redis 预扣库存 + RabbitMQ 异步下单 + 本地标记，将热点商品秒杀 QPS 提升至 1.2 万；通过 Sentinel 限流与降级保证系统稳定性。',
        technologies: ['Spring Boot', 'Redis', 'RabbitMQ', 'Sentinel'],
      },
      {
        projectName: '微服务治理平台',
        role: '模块负责人',
        description:
          '基于 Spring Cloud Alibaba 搭建微服务基础框架，接入 Nacos 注册配置中心、OpenFeign 服务调用、Seata 分布式事务，统一网关鉴权与日志链路。',
        technologies: ['Spring Cloud', 'Nacos', 'OpenFeign', 'Seata'],
      },
    ],
    educations: [
      {
        institution: '武汉理工大学',
        degree: '本科',
        fieldOfStudy: '计算机科学与技术',
        startDate: '2017-09',
        endDate: '2021-06',
      },
    ],
  },
];

/* ===================== 面试配置 ===================== */

const mockConfigs = [
  {
    id: 1,
    name: 'Java 后端面试',
    description: 'Java 基础 + Spring 全家桶 + 微服务，综合考察',
    interviewerStyle: 'FRIENDLY',
    difficulty: 'MEDIUM',
    targetSkills: ['Java', 'Spring Boot', '微服务'],
    questionCategories: ['基础知识', '框架原理', '系统设计'],
    language: 'ZH',
    createdAt: '2026-07-21 10:00:00',
  },
  {
    id: 2,
    name: '高难度系统设计',
    description: '大厂风格，重点考察架构与高并发设计能力',
    interviewerStyle: 'STRICT',
    difficulty: 'HARD',
    targetSkills: ['系统设计', '高并发', '分布式'],
    questionCategories: ['系统设计', '架构演进'],
    language: 'ZH',
    createdAt: '2026-07-24 15:30:00',
  },
  {
    id: 3,
    name: '英文技术面试',
    description: '全英文提问，模拟外企面试场景',
    interviewerStyle: 'PRESSURE',
    difficulty: 'MEDIUM',
    targetSkills: ['Java', 'Communication'],
    questionCategories: ['Basics', 'Behavioral'],
    language: 'EN',
    createdAt: '2026-07-25 09:12:00',
  },
];

/* ===================== 面试会话与题目 ===================== */

// 面试官题库（演示用通用题库）
const QUESTION_POOL = [
  '请先做一个简短的自我介绍，重点介绍你的技术栈和最有代表性的项目。',
  '谈谈你对 JVM 内存模型的理解，以及线上 OOM 你会如何排查和定位？',
  'Spring Boot 的自动装配原理是什么？如果让你自定义一个 Starter，你会怎么做？',
  '你负责的系统是如何做 MySQL 分库分表的？深分页查询有哪些优化手段？',
  'Redis 缓存穿透、击穿、雪崩分别是什么？你的项目中是如何应对的？',
  '请描述一次你解决过的线上故障，当时的排查思路和最终方案是什么？',
  '你们微服务之间的分布式事务是怎么解决的？为什么选择这种方案？',
  '如果让你设计一个短链接系统，你会如何设计？请说明关键取舍。',
  '谈谈你对消息队列的理解，如何保证消息不丢失、不重复消费？',
  '你对未来三年的职业规划是怎样的？为什么想加入我们团队？',
];

const mockSessions = {};

// 某次已完成会话的问答记录（历史页 / 答题详情共用）
const mockHistoryQA = [
  {
    questionId: 1,
    questionText: QUESTION_POOL[0],
    expectedSkills: ['沟通表达'],
    orderIndex: 0,
    askedAt: '2026-07-26 10:02:11',
    answerId: 101,
    answerText:
      '您好，我叫张伟，有 5 年 Java 后端开发经验。先后任职于明略信息与云帆科技，主要负责电商中台订单、库存模块以及微服务基础框架的搭建。最近的项目是主导高并发秒杀系统，把热点商品 QPS 提升到了 1.2 万。我比较擅长 Spring 技术栈、高并发场景设计和性能优化。',
    audioFileUrl: '',
    submittedAt: '2026-07-26 10:03:40',
  },
  {
    questionId: 2,
    questionText: QUESTION_POOL[1],
    expectedSkills: ['JVM', '性能调优'],
    orderIndex: 1,
    askedAt: '2026-07-26 10:03:42',
    answerId: 102,
    answerText:
      'JVM 内存主要分为堆、方法区、虚拟机栈、本地方法栈和程序计数器。线上 OOM 我一般先用 jmap 导出堆快照，再用 MAT 分析大对象和引用链。之前遇到过一次堆内存溢出，最后定位到是本地缓存用了静态 Map 没有设置过期策略，改成 Caffeine 并限制容量后解决。',
    audioFileUrl: '',
    submittedAt: '2026-07-26 10:05:18',
  },
  {
    questionId: 3,
    questionText: QUESTION_POOL[2],
    expectedSkills: ['Spring Boot', '框架原理'],
    orderIndex: 2,
    askedAt: '2026-07-26 10:05:20',
    answerId: 103,
    answerText:
      '自动装配的核心是 @EnableAutoConfiguration，通过 spring.factories 或 AutoConfiguration.imports 加载所有候选配置类，再结合 @Conditional 条件注解按需生效。自定义 Starter 的话，需要定义自动配置类、编写 spring.factories 注册，并提供配置属性类，通过 @ConfigurationProperties 绑定。',
    audioFileUrl: '',
    submittedAt: '2026-07-26 10:07:05',
  },
  {
    questionId: 4,
    questionText: QUESTION_POOL[3],
    expectedSkills: ['MySQL', '分库分表'],
    orderIndex: 3,
    askedAt: '2026-07-26 10:07:07',
    answerId: 104,
    answerText:
      '我们订单表按用户 ID 做哈希分片，分了 16 个库 64 张表。深分页我们通过游标方式，记录上次查询的最后一条 ID，用 where id > lastId 代替 offset 分页。另外查询条件都强制带分片键，避免全库扫描。',
    audioFileUrl: '',
    submittedAt: '2026-07-26 10:08:45',
  },
  {
    questionId: 5,
    questionText: QUESTION_POOL[4],
    expectedSkills: ['Redis', '缓存设计'],
    orderIndex: 4,
    askedAt: '2026-07-26 10:08:47',
    answerId: 105,
    answerText:
      '穿透是指查询不存在的 key，我们用了布隆过滤器前置拦截；击穿是热点 key 过期瞬间大量请求打到数据库，我们给热点 key 加互斥锁重建缓存；雪崩是大面积 key 同时过期，通过随机过期时间 + 多级缓存来规避。',
    audioFileUrl: '',
    submittedAt: '2026-07-26 10:10:02',
  },
];

// 面试官对答案的即时点评（演示用，逐题变化）
const AI_COMMENTS = [
  '回答结构清晰，STAR 法则用得很好。如果能补充具体的量化数据会更有说服力。',
  '思路正确，排查链路完整。建议把"先看监控 → 再看日志 → 最后堆转储"的优先级讲得更明确。',
  '原理掌握扎实。可以再补充一下条件注解的生效顺序，以及如何通过 IDE 查看自动配置报告。',
  '方案成熟，考虑到了强制分片键的细节。如果提到数据迁移的双写方案会更完整。',
  '三个概念区分清楚，应对策略也到位。可以再提一下缓存与数据库的一致性保证方式。',
];

/* ===================== 评估 ===================== */

const mockEvaluation = {
  sessionId: 1001,
  overallScore: 78,
  scores: {
    technical: 82,
    communication: 74,
    clarity: 79,
    depth: 76,
    problem_solving: 80,
  },
  summary:
    '整体表现良好，技术功底扎实，对 JVM、Spring 原理和缓存设计都有比较深入的理解。回答普遍结构清晰，能结合项目实际举例。主要扣分点在部分回答缺少量化数据支撑，以及系统设计类问题的边界考虑不够全面。',
  strengths:
    '1. JVM 与性能调优经验真实且落地，能完整描述 OOM 的排查链路\n2. Spring Boot 自动装配原理理解透彻，能讲清 Starter 的实现步骤\n3. 缓存三大问题应对方案完整，有实际项目支撑',
  weaknesses:
    '1. 系统设计类问题（如短链接、分库分表）回答偏重"做了什么"，缺少"为什么这样选"的权衡分析\n2. 部分回答量化数据不足，建议多积累关键性能指标\n3. 结尾收束略快，可补充总结句强化记忆点',
  answers: [
    {
      answerId: 101,
      questionId: 1,
      question: QUESTION_POOL[0],
      technicalScore: 75,
      clarityScore: 82,
      depthScore: 70,
      feedbackText:
        '自我介绍信息密度高，项目亮点突出。建议把"5 年经验"与"秒杀项目"用数字进一步量化，比如"支撑日均 200 万订单"。',
      improvementSuggestions:
        '按照"我是谁 → 做什么 → 最亮点的成绩 → 为什么适合这个岗位"四段式组织，控制在 90 秒内。',
    },
    {
      answerId: 102,
      questionId: 2,
      question: QUESTION_POOL[1],
      technicalScore: 88,
      clarityScore: 80,
      depthScore: 85,
      feedbackText:
        '对 JVM 内存结构描述准确，OOM 排查链路完整，且有真实案例支撑，是全场表现最好的回答之一。',
      improvementSuggestions:
        '可以补充 jmap/jstack 的具体命令和 MAT 的分析步骤，进一步体现工程能力。',
    },
    {
      answerId: 103,
      questionId: 3,
      question: QUESTION_POOL[2],
      technicalScore: 85,
      clarityScore: 78,
      depthScore: 80,
      feedbackText:
        '自动装配原理讲解清晰，Starter 实现步骤完整。若能提到 @ConditionalOnMissingBean 等条件注解的细节会更出彩。',
      improvementSuggestions:
        '准备一个"debug 模式下查看 AutoConfigurationReport"的实操描述。',
    },
    {
      answerId: 104,
      questionId: 4,
      question: QUESTION_POOL[3],
      technicalScore: 82,
      clarityScore: 72,
      depthScore: 74,
      feedbackText:
        '分库分表方案成熟，深分页优化讲到了游标方式。但未提及数据迁移、扩容等工程化问题。',
      improvementSuggestions:
        '系统设计类回答建议补充"方案对比与取舍"，说明为什么选哈希分片而不是范围分片。',
    },
    {
      answerId: 105,
      questionId: 5,
      question: QUESTION_POOL[4],
      technicalScore: 80,
      clarityScore: 74,
      depthScore: 71,
      feedbackText:
        '三个概念区分准确，应对策略完整。可以再补充缓存与数据库一致性（如延迟双删、Binlog 订阅）的说明。',
      improvementSuggestions:
        '多准备一个"缓存一致性"追问，这是高频考点。',
    },
  ],
};

/* ===================== 语音反馈 ===================== */

const mockSpeechAnalysis = [
  { feature: '语速 (词/分钟)', value: 132, unit: '词/分' },
  { feature: '流利度', value: 78, unit: '分' },
  { feature: '清晰度', value: 84, unit: '分' },
  { feature: '平均停顿 (ms)', value: 420, unit: 'ms' },
  { feature: '总词数', value: 856, unit: '词' },
  { feature: '录音时长', value: 386, unit: '秒' },
];

const mockAnalytics = {
  fluency: 78,
  pronunciation: 84,
  wpm: 132,
  fillerWords: [
    { word: '嗯...', count: 12 },
    { word: '那个...', count: 8 },
    { word: '然后...', count: 6 },
    { word: '就是...', count: 4 },
  ],
  trend: [62, 68, 65, 72, 75, 74, 78],
};

/* ===================== 成长 ===================== */

const mockSkills = [
  { name: 'Java', score: 88, level: 'EXPERT' },
  { name: 'Spring Boot', score: 84, level: 'EXPERT' },
  { name: '微服务', score: 76, level: 'ADVANCED' },
  { name: 'MySQL', score: 80, level: 'ADVANCED' },
  { name: 'Redis', score: 72, level: 'ADVANCED' },
  { name: '系统设计', score: 62, level: 'INTERMEDIATE' },
  { name: '算法', score: 58, level: 'INTERMEDIATE' },
  { name: '沟通表达', score: 70, level: 'ADVANCED' },
];

// 不同技能的演示趋势（按技能名返回不同曲线）
const SKILL_TRENDS = {
  Java: [
    { date: '02月', score: 74 },
    { date: '03月', score: 76 },
    { date: '04月', score: 79 },
    { date: '05月', score: 82 },
    { date: '06月', score: 85 },
    { date: '07月', score: 88 },
  ],
  微服务: [
    { date: '02月', score: 58 },
    { date: '03月', score: 62 },
    { date: '04月', score: 65 },
    { date: '05月', score: 70 },
    { date: '06月', score: 73 },
    { date: '07月', score: 76 },
  ],
  系统设计: [
    { date: '02月', score: 52 },
    { date: '03月', score: 54 },
    { date: '04月', score: 57 },
    { date: '05月', score: 59 },
    { date: '06月', score: 61 },
    { date: '07月', score: 62 },
  ],
};
const defaultTrend = [
  { date: '02月', score: 60 },
  { date: '03月', score: 63 },
  { date: '04月', score: 65 },
  { date: '05月', score: 68 },
  { date: '06月', score: 70 },
  { date: '07月', score: 72 },
];

const mockReports = [
  {
    id: 1,
    title: '2026年7月成长总结',
    date: '2026-07-28',
    content:
      '<h2>📊 本月成长总结</h2><p>本月共完成 <b>3 场</b>模拟面试，平均得分 <b>76 分</b>，较上月提升 <b style="color:#34d399">+6 分</b>。</p><h3>🎯 进步明显的维度</h3><ul><li><b>JVM 与性能调优</b>：从 70 → 88 分，OOM 排查链路表达完整</li><li><b>缓存设计</b>：从 68 → 80 分，三大问题应对方案清晰</li></ul><h3>📉 需要加强的维度</h3><ul><li><b>系统设计</b>：62 分，建议补充方案权衡分析</li><li><b>算法</b>：58 分，建议每周完成 3 道 LeetCode 中高难度题</li></ul><h3>💡 下月计划</h3><p>1. 完成 5 场模拟面试，重点覆盖系统设计题型<br/>2. 每周录制 2 次回答并回听，降低填充词频率<br/>3. 整理 10 个高频系统设计题的答题框架</p>',
  },
  {
    id: 2,
    title: '2026年6月成长总结',
    date: '2026-06-30',
    content:
      '<h2>📊 6月成长总结</h2><p>本月完成 <b>2</b> 场模拟面试，平均得分 <b>70 分</b>。</p><h3>🎯 亮点</h3><ul><li>Spring 原理掌握扎实，基础题得分率高</li></ul><h3>📉 待提升</h3><ul><li>沟通表达：回答缺乏结构，建议使用 STAR 法则</li><li>系统设计：经验不足，需加强架构训练</li></ul>',
  },
  {
    id: 3,
    title: '2026年5月成长总结',
    date: '2026-05-31',
    content:
      '<h2>📊 5月成长总结</h2><p>本月完成 <b>1</b> 场模拟面试，平均得分 <b>65 分</b>，处于起步阶段。</p><h3>📉 主要问题</h3><ul><li>回答缺少量化数据，说服力不足</li><li>对微服务架构理解停留在概念层面</li></ul><h3>💡 建议</h3><p>先从简历项目出发，把每个项目的技术选型、难点、量化收益梳理成文档。</p>',
  },
];

const mockWeaknesses = [
  {
    skill: '系统设计',
    suggestion:
      '建议学习分布式系统设计模式（缓存、限流、幂等、分库分表），每周完成 1 个系统设计题的答题框架整理。',
    severity: 'high',
  },
  {
    skill: '算法',
    suggestion:
      '多刷 LeetCode 中高难度题目，重点攻克动态规划与二叉树，建议每天 1-2 题并记录思路。',
    severity: 'medium',
  },
  {
    skill: '沟通表达',
    suggestion:
      '使用 STAR 法则结构化回答，先讲背景与目标，再讲行动与结果；录制回答回听，减少填充词。',
    severity: 'low',
  },
];

/* ===================== 首页仪表盘聚合 ===================== */

const mockDashboard = {
  interviewCount: 6,
  avgScore: 78,
  skillCount: 8,
  reportCount: 3,
};

/* ===================== Mock 出口（供 api 层调用） ===================== */

export const mock = {
  /* ---- 用户 ---- */
  login: ({ username }) => delay().then(() => ({
    accessToken: fakeToken(),
    refreshToken: fakeToken(),
    username: username || mockUser.username,
  })),
  register: () => delay(500).then(() => true),
  logout: () => delay(200).then(() => true),
  getProfile: () => delay(300).then(() => ({ ...mockUser })),
  updateProfile: (data) => delay(300).then(() => {
    Object.assign(mockUser, data);
    return true;
  }),

  /* ---- 简历 ---- */
  listResumes: () => delay().then(() => mockResumes.map((r) => ({
    id: r.id, coverName: r.coverName, originalFilename: r.originalFilename,
    fileType: r.fileType, status: r.status, createdAt: r.createdAt,
  }))),
  resumeDetail: (id) => delay(400).then(() => {
    const r = mockResumes.find((x) => String(x.id) === String(id)) || mockResumes[0];
    return { ...r };
  }),
  uploadResume: () => delay(1200).then(() => ({ id: nextId() })),
  deleteResume: () => delay(300).then(() => true),
  reparseResume: () => delay(800).then(() => true),

  /* ---- 面试配置 ---- */
  listConfigs: () => delay().then(() => mockConfigs.map((c) => ({ ...c }))),
  configDetail: (id) => delay(250).then(() =>
    mockConfigs.find((c) => String(c.id) === String(id)) || mockConfigs[0]),
  saveConfig: (data, id) => delay(400).then(() => {
    if (id) {
      const idx = mockConfigs.findIndex((c) => String(c.id) === String(id));
      if (idx >= 0) mockConfigs[idx] = { ...mockConfigs[idx], ...data };
    } else {
      mockConfigs.unshift({ id: nextId(), createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '), ...data });
    }
    return true;
  }),
  deleteConfig: (id) => delay(300).then(() => {
    const idx = mockConfigs.findIndex((c) => String(c.id) === String(id));
    if (idx >= 0) mockConfigs.splice(idx, 1);
    return true;
  }),

  /* ---- 面试会话 ---- */
  createSession: (data) => delay(500).then(() => {
    const id = nextId();
    mockSessions[id] = { id, status: 'CREATED', currentQuestionIndex: 0, totalQuestions: QUESTION_POOL.length };
    return { id };
  }),
  getSession: (id) => delay(250).then(() =>
    mockSessions[id] || { id: Number(id) || 1001, status: 'CREATED', currentQuestionIndex: 0, totalQuestions: QUESTION_POOL.length }),
  startSession: (id) => delay(900).then(() => {
    mockSessions[id] = mockSessions[id] || { id: Number(id) || 1001, status: 'IN_PROGRESS', currentQuestionIndex: 0, totalQuestions: QUESTION_POOL.length };
    mockSessions[id].status = 'IN_PROGRESS';
    return { question: QUESTION_POOL[0], firstQuestion: QUESTION_POOL[0], totalQuestions: QUESTION_POOL.length };
  }),
  submitAnswer: (id, { text }) => delay(800).then(() => {
    const s = mockSessions[id] || (mockSessions[id] = { id: Number(id) || 1001, status: 'IN_PROGRESS', currentQuestionIndex: 0, totalQuestions: QUESTION_POOL.length });
    const idx = s.currentQuestionIndex;
    const hasNext = idx + 1 < QUESTION_POOL.length;
    const next = hasNext ? QUESTION_POOL[idx + 1] : '';
    const comment = AI_COMMENTS[idx % AI_COMMENTS.length];
    if (hasNext) s.currentQuestionIndex = idx + 1;
    else s.status = 'COMPLETED';
    return {
      nextQuestion: next,
      hasNext,
      comment,
      currentQuestionIndex: s.currentQuestionIndex,
    };
  }),
  endSession: (id) => delay(600).then(() => {
    const s = mockSessions[id] || (mockSessions[id] = { id: Number(id) || 1001 });
    s.status = 'COMPLETED';
    return true;
  }),
  getHistory: (id) => delay(400).then(() =>
    mockHistoryQA.map((h) => ({ ...h }))),

  /* ---- 评估 ---- */
  getEval: (sessionId) => delay(800).then(() => {
    const e = { ...mockEvaluation, sessionId: Number(sessionId) || mockEvaluation.sessionId };
    return { ...e, answers: e.answers.map((a) => ({ ...a })) };
  }),
  getAnswerDetail: (sessionId, answerId) => delay(500).then(() => {
    const a = mockEvaluation.answers.find((x) => String(x.answerId) === String(answerId)) || mockEvaluation.answers[0];
    return {
      question: a.question,
      answer: mockHistoryQA.find((h) => h.answerId === a.answerId)?.answerText || '',
      technicalScore: a.technicalScore,
      clarityScore: a.clarityScore,
      depthScore: a.depthScore,
      feedbackText: a.feedbackText,
      improvementSuggestions: a.improvementSuggestions,
    };
  }),
  regenerateReport: () => delay(1500).then(() => true),

  /* ---- 语音反馈 ---- */
  getSpeechAnalysis: () => delay(500).then(() => mockSpeechAnalysis.map((s) => ({ ...s }))),
  getAnalytics: () => delay(600).then(() => ({
    ...mockAnalytics,
    trend: [...mockAnalytics.trend],
    fillerWords: mockAnalytics.fillerWords.map((f) => ({ ...f })),
  })),

  /* ---- 成长 ---- */
  getSkills: () => delay(500).then(() => mockSkills.map((s) => ({ ...s }))),
  getSkillHistory: (skill, period) => delay(400).then(() => {
    const list = SKILL_TRENDS[skill] || defaultTrend;
    return list.map((p) => ({ ...p }));
  }),
  listReports: () => delay(400).then(() => mockReports.map((r) => ({ id: r.id, title: r.title, date: r.date }))),
  reportDetail: (id) => delay(500).then(() =>
    mockReports.find((r) => String(r.id) === String(id)) || mockReports[0]),
  getWeaknesses: () => delay(500).then(() => mockWeaknesses.map((w) => ({ ...w }))),

  /* ---- 首页聚合 ---- */
  getDashboard: () => delay(400).then(() => ({ ...mockDashboard })),
};

export default mock;