package com.interview.service.impl;

import client.ResumeClient;
import client.UserClient;
import client.support.DataScopeSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.DTO.AnswerEvaluateMessage;
import com.interview.DTO.ParsedResumeData;
import com.interview.DTO.ReportGenerateMessage;
import com.interview.DTO.SessionResultDTO;
import com.interview.DTO.SessionStatsDTO;
import com.interview.DTO.SpeechAnalysisMessage;
import com.interview.constant.RabbitMQConstant;
import com.interview.context.UserContext;
import com.interview.domain.dto.AnswerSubmit;
import com.interview.domain.dto.SessionSubmit;
import com.interview.domain.po.InterviewConfigs;
import com.interview.domain.po.InterviewResults;
import com.interview.domain.po.InterviewSessions;
import com.interview.domain.po.SessionAnswers;
import com.interview.domain.po.SessionQuestions;
import com.interview.domain.vo.NextQuestionVO;
import com.interview.domain.vo.SessionHistoryVO;
import com.interview.domain.vo.StartQuestionVO;
import com.interview.mapper.InterviewSessionsMapper;
import com.interview.service.IInterviewConfigsService;
import com.interview.service.IInterviewResultsService;
import com.interview.service.IInterviewSessionsService;
import com.interview.service.ISessionAnswersService;
import com.interview.service.ISessionQuestionsService;
import com.interview.service.InterviewQuestionService;
import com.interview.service.SessionContextService;
import com.interview.service.SessionContextService.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  面试会话服务实现类
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionsServiceImpl extends ServiceImpl<InterviewSessionsMapper, InterviewSessions> implements IInterviewSessionsService {

    private final ISessionQuestionsService sessionQuestionsService;
    private final ISessionAnswersService sessionAnswersService;
    private final IInterviewConfigsService interviewConfigsService;
    private final IInterviewResultsService interviewResultsService;
    private final InterviewQuestionService interviewQuestionService;
    private final SessionContextService sessionContextService;
    private final RabbitTemplate rabbitTemplate;
    private final ResumeClient resumeClient;
    private final UserClient userClient;

    @Override
    public Long createSession(SessionSubmit submit) {
        Long userId = Long.parseLong(UserContext.getUserId());
        InterviewSessions session = new InterviewSessions();
        session.setUserId(userId);
        session.setConfigId(submit.getConfigId());
        session.setResumeId(submit.getResumeId());
        session.setStatus("CREATED");
        session.setCurrentQuestionIndex(0);
        session.setTotalQuestions(0);
        this.save(session);
        return session.getId();
    }

    @Override
    public InterviewSessions getSession(Long id) {
        return getOwnSession(id);
    }

    @Override
    public List<SessionHistoryVO> getHistory(Long id) {
        InterviewSessions session = getOwnSession(id);
        if (session == null) {
            throw new RuntimeException("会话不存在或无权限操作");
        }
        LambdaQueryWrapper<SessionQuestions> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(SessionQuestions::getSessionId, id)
                .orderByAsc(SessionQuestions::getOrderIndex);
        List<SessionQuestions> questions = sessionQuestionsService.list(questionWrapper);
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> questionIds = questions.stream().map(SessionQuestions::getId).collect(Collectors.toList());
        LambdaQueryWrapper<SessionAnswers> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.in(SessionAnswers::getQuestionId, questionIds);
        List<SessionAnswers> answers = sessionAnswersService.list(answerWrapper);
        Map<Long, SessionAnswers> answerMap = answers.stream()
                .collect(Collectors.toMap(SessionAnswers::getQuestionId, a -> a, (a1, a2) -> a1));
        List<SessionHistoryVO> history = new ArrayList<>();
        for (SessionQuestions question : questions) {
            SessionAnswers answer = answerMap.get(question.getId());
            history.add(SessionHistoryVO.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .expectedSkills(question.getExpectedSkills())
                    .orderIndex(question.getOrderIndex())
                    .askedAt(question.getAskedAt())
                    .answerId(answer != null ? answer.getId() : null)
                    .answerText(answer != null ? answer.getAnswerText() : null)
                    .audioFileUrl(answer != null ? answer.getAudioFileUrl() : null)
                    .submittedAt(answer != null ? answer.getSubmittedAt() : null)
                    .build());
        }
        return history;
    }

    @Override
    public StartQuestionVO start(Long sessionId) {
        InterviewSessions session = getOwnSession(sessionId);
        if (!"CREATED".equals(session.getStatus())) {
            throw new RuntimeException("会话状态不允许开始面试，当前状态: " + session.getStatus());
        }
        InterviewConfigs config = getConfig(session.getConfigId());
        ParsedResumeData resume = loadResume(session.getResumeId());

        // AI 面试官主动开场：打招呼 + 提到看过简历 + 第一个问题
        String opening = interviewQuestionService.generateOpening(config, resume, currentUserName());

        SessionQuestions sq = new SessionQuestions();
        sq.setSessionId(sessionId);
        sq.setQuestionText(opening);
        sq.setExpectedSkills(joinSkills(config.getTargetSkills()));
        sq.setOrderIndex(0);
        sq.setAskedAt(LocalDateTime.now());
        sessionQuestionsService.save(sq);

        session.setStatus("IN_PROGRESS");
        session.setStartTime(LocalDateTime.now());
        // 不再按难度预设固定题数，totalQuestions 恒为 0（表示不限制）
        session.setTotalQuestions(0);
        session.setCurrentQuestionIndex(0);
        this.updateById(session);

        List<ChatMessage> context = new ArrayList<>();
        context.add(ChatMessage.builder().role("assistant").content(opening).build());
        sessionContextService.saveContext(sessionId, context);

        log.info("面试已开始（自然对话模式），sessionId={}, 开场题目id={}", sessionId, sq.getId());
        return StartQuestionVO.builder()
                .questionId(sq.getId())
                .question(opening)
                .totalQuestions(0)
                .build();
    }

    @Override
    public NextQuestionVO answer(Long sessionId, AnswerSubmit submit) {
        InterviewSessions session = getOwnSession(sessionId);
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new RuntimeException("会话状态不允许提交答案，当前状态: " + session.getStatus());
        }
        Long userId = Long.parseLong(UserContext.getUserId());
        int currentIndex = session.getCurrentQuestionIndex() == null ? 0 : session.getCurrentQuestionIndex();

        SessionQuestions currentQuestion = getQuestionByIndex(sessionId, currentIndex);
        if (currentQuestion == null) {
            throw new RuntimeException("当前题目不存在，请先开始面试");
        }
        String answerText = submit.getText() == null ? "" : submit.getText();

        SessionAnswers answer = new SessionAnswers();
        answer.setSessionId(sessionId);
        answer.setQuestionId(currentQuestion.getId());
        answer.setAnswerText(answerText);
        answer.setAudioFileUrl(submit.getAudioUrl());
        answer.setSubmittedAt(LocalDateTime.now());
        sessionAnswersService.save(answer);

        sessionContextService.append(sessionId, ChatMessage.builder().role("user").content(answerText).build());

        AnswerEvaluateMessage msg = AnswerEvaluateMessage.builder()
                .sessionId(sessionId)
                .questionId(currentQuestion.getId())
                .answerId(answer.getId())
                .userId(userId)
                .questionText(currentQuestion.getQuestionText())
                .answerText(answerText)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstant.Exchange.INTERVIEW_EXCHANGE,
                RabbitMQConstant.Key.ANSWER_EVALUATE_KEY, msg);

        SpeechAnalysisMessage speechMsg = SpeechAnalysisMessage.builder()
                .sessionId(sessionId)
                .questionId(currentQuestion.getId())
                .answerId(answer.getId())
                .userId(userId)
                .audioUrl(submit.getAudioUrl())
                .answerText(answerText)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstant.Exchange.INTERVIEW_EXCHANGE,
                RabbitMQConstant.Key.SPEECH_ANALYSIS_KEY, speechMsg);

        // 自然对话：把完整历史交给 AI，由它决定继续追问还是收尾
        InterviewConfigs config = getConfig(session.getConfigId());
        ParsedResumeData resume = loadResume(session.getResumeId());
        List<ChatMessage> context = sessionContextService.getContext(sessionId);
        int askedCount = currentIndex + 1;
        String reply = interviewQuestionService.generateReply(config, resume, currentUserName(), context, askedCount);

        boolean ending = interviewQuestionService.isEnding(reply);
        String visible = interviewQuestionService.stripEndMarker(reply);

        // 硬性兜底：未到最少轮数忽略结束标记；达到最多轮数强制结束
        if (ending && askedCount < InterviewQuestionService.MIN_ROUNDS) {
            log.info("AI 想在第 {} 轮结束，但未达到最少 {} 轮，继续提问（sessionId={}）",
                    askedCount, InterviewQuestionService.MIN_ROUNDS, sessionId);
            ending = false;
            // AI 已写了收尾语但被拦下，需要重新追一轮问题，避免出现"无问题"的空回复
            if (!visible.endsWith("?") && !visible.endsWith("？")) {
                visible = visible + "\n那我想再了解一下：你在项目中遇到最棘手的技术问题是什么，当时是怎么定位和解决的？";
            }
        }
        if (askedCount >= InterviewQuestionService.MAX_ROUNDS) {
            log.info("已达到最多 {} 轮，强制结束（sessionId={}）",
                    InterviewQuestionService.MAX_ROUNDS, sessionId);
            if (!ending) {
                // AI 还在提问但已到上限：丢弃问题，只保留收尾语
                ending = true;
                visible = "今天我们就聊到这里，感谢你的参与。稍后会有一份完整的评估报告给你，可以针对性地复盘。";
            } else {
                visible = visible + "\n今天我们就聊到这里，感谢你的参与，稍后会有一份评估报告给你。";
            }
        }

        if (ending) {
            // AI 判断面试结束：记录结束语，自动收尾并触发报告生成
            sessionContextService.append(sessionId, ChatMessage.builder().role("assistant").content(visible).build());
            this.finishSession(sessionId, session);
            log.info("AI 判断面试结束，sessionId={}, 共 {} 轮问答", sessionId, askedCount);
            return NextQuestionVO.builder()
                    .hasNext(false)
                    .questionId(null)
                    .aiMessage(visible)
                    .nextQuestion(visible)
                    .askedCount(askedCount)
                    .build();
        }

        SessionQuestions next = new SessionQuestions();
        next.setSessionId(sessionId);
        next.setQuestionText(visible);
        next.setExpectedSkills(joinSkills(config.getTargetSkills()));
        next.setOrderIndex(currentIndex + 1);
        next.setAskedAt(LocalDateTime.now());
        sessionQuestionsService.save(next);

        sessionContextService.append(sessionId, ChatMessage.builder().role("assistant").content(visible).build());

        session.setCurrentQuestionIndex(currentIndex + 1);
        this.updateById(session);

        return NextQuestionVO.builder()
                .hasNext(true)
                .questionId(next.getId())
                .aiMessage(visible)
                .nextQuestion(visible)
                .askedCount(askedCount)
                .build();
    }

    /** 收尾：置 COMPLETED 并发消息生成报告（AI 自主结束与用户手动结束共用） */
    private void finishSession(Long sessionId, InterviewSessions session) {
        if ("COMPLETED".equals(session.getStatus()) || "EVALUATED".equals(session.getStatus())) {
            return;
        }
        Long userId = Long.parseLong(UserContext.getUserId());
        session.setStatus("COMPLETED");
        session.setEndTime(LocalDateTime.now());
        this.updateById(session);

        ReportGenerateMessage msg = ReportGenerateMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstant.Exchange.INTERVIEW_EXCHANGE,
                RabbitMQConstant.Key.REPORT_GENERATE_KEY, msg);
        log.info("已发送报告生成消息，sessionId={}", sessionId);
    }

    /** 当前用户称呼（优先真实姓名，退回用户名） */
    private String currentUserName() {
        String name = UserContext.getUsername();
        return (name == null || name.isBlank()) ? "候选人" : name;
    }

    @Override
    public void end(Long sessionId) {
        InterviewSessions session = getOwnSession(sessionId);
        if ("COMPLETED".equals(session.getStatus()) || "EVALUATED".equals(session.getStatus())) {
            return;
        }
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new RuntimeException("会话状态不允许结束，当前状态: " + session.getStatus());
        }
        finishSession(sessionId, session);
    }

    @Override
    public void updateResult(Long sessionId, Long userId, SessionResultDTO result) {
        InterviewSessions session = this.lambdaQuery()
                .eq(InterviewSessions::getId, sessionId)
                .eq(InterviewSessions::getUserId, userId)
                .one();
        if (session == null) {
            throw new RuntimeException("会话不存在或用户不匹配，sessionId=" + sessionId);
        }
        InterviewResults existing = interviewResultsService.lambdaQuery()
                .eq(InterviewResults::getSessionId, sessionId).one();
        if (existing == null) {
            existing = new InterviewResults();
            existing.setSessionId(sessionId);
        }
        existing.setOverallScore(result.getOverallScore());
        existing.setSummary(result.getSummary());
        existing.setStrengths(result.getStrengths());
        existing.setWeaknesses(result.getWeaknesses());
        existing.setGeneratedAt(LocalDateTime.now());
        if (existing.getId() == null) {
            interviewResultsService.save(existing);
        } else {
            interviewResultsService.updateById(existing);
        }
        this.lambdaUpdate()
                .eq(InterviewSessions::getId, sessionId)
                .set(InterviewSessions::getStatus, "EVALUATED")
                .update();
        log.info("评估结果已回写，sessionId={}, status=EVALUATED", sessionId);
    }

    @Override
    public SessionStatsDTO getStats() {
        // 数据权限：学生=自己；辅导员/管理员=本院；HR=全部
        List<Long> userIds = DataScopeSupport.visibleUserIds(userClient);
        Long interviewCount = this.lambdaQuery()
                .in(InterviewSessions::getUserId, userIds)
                .count();
        BigDecimal avgScore = null;
        List<InterviewSessions> sessions = this.lambdaQuery()
                .in(InterviewSessions::getUserId, userIds)
                .list();
        if (sessions != null && !sessions.isEmpty()) {
            List<Long> sessionIds = sessions.stream()
                    .map(InterviewSessions::getId)
                    .collect(Collectors.toList());
            List<InterviewResults> results = interviewResultsService.lambdaQuery()
                    .in(InterviewResults::getSessionId, sessionIds)
                    .isNotNull(InterviewResults::getOverallScore)
                    .list();
            if (!results.isEmpty()) {
                BigDecimal total = results.stream()
                        .map(InterviewResults::getOverallScore)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                avgScore = total.divide(BigDecimal.valueOf(results.size()), 1, java.math.RoundingMode.HALF_UP);
            }
        }
        return SessionStatsDTO.builder()
                .interviewCount(interviewCount)
                .avgScore(avgScore)
                .build();
    }

    private InterviewSessions getOwnSession(Long sessionId) {
        Long userId = Long.parseLong(UserContext.getUserId());
        InterviewSessions session = this.lambdaQuery()
                .eq(InterviewSessions::getId, sessionId)
                .eq(InterviewSessions::getUserId, userId)
                .one();
        if (session == null) {
            throw new RuntimeException("会话不存在或无权限操作");
        }
        return session;
    }

    private InterviewConfigs getConfig(Long configId) {
        if (configId != null) {
            InterviewConfigs config = interviewConfigsService.getById(configId);
            if (config != null) {
                return config;
            }
        }
        InterviewConfigs def = new InterviewConfigs();
        def.setInterviewerStyle("FRIENDLY");
        def.setDifficulty("MEDIUM");
        def.setLanguage("ZH");
        return def;
    }

    private ParsedResumeData loadResume(Long resumeId) {
        if (resumeId == null) {
            return null;
        }
        try {
            return resumeClient.getResume(resumeId);
        } catch (Exception e) {
            log.warn("拉取简历失败，resumeId={}，将不带简历背景出题", resumeId, e);
            return null;
        }
    }

    private SessionQuestions getQuestionByIndex(Long sessionId, int orderIndex) {
        return sessionQuestionsService.lambdaQuery()
                .eq(SessionQuestions::getSessionId, sessionId)
                .eq(SessionQuestions::getOrderIndex, orderIndex)
                .one();
    }

    private String joinSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return String.join(",", skills);
    }
}
