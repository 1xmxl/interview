package com.interview.service.impl;

import client.ResumeClient;
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
        int total = resolveTotalQuestions(config.getDifficulty());

        String question = interviewQuestionService.generateFirstQuestion(config, resume, total);

        SessionQuestions sq = new SessionQuestions();
        sq.setSessionId(sessionId);
        sq.setQuestionText(question);
        sq.setExpectedSkills(joinSkills(config.getTargetSkills()));
        sq.setOrderIndex(0);
        sq.setAskedAt(LocalDateTime.now());
        sessionQuestionsService.save(sq);

        session.setStatus("IN_PROGRESS");
        session.setStartTime(LocalDateTime.now());
        session.setTotalQuestions(total);
        session.setCurrentQuestionIndex(0);
        this.updateById(session);

        List<ChatMessage> context = new ArrayList<>();
        context.add(ChatMessage.builder().role("assistant").content(question).build());
        sessionContextService.saveContext(sessionId, context);

        log.info("面试已开始，sessionId={}, 首题id={}", sessionId, sq.getId());
        return StartQuestionVO.builder()
                .questionId(sq.getId())
                .question(question)
                .totalQuestions(total)
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

        int total = session.getTotalQuestions() == null ? 0 : session.getTotalQuestions();
        if (currentIndex + 1 >= total) {
            return NextQuestionVO.builder().hasNext(false).build();
        }

        InterviewConfigs config = getConfig(session.getConfigId());
        ParsedResumeData resume = loadResume(session.getResumeId());
        List<ChatMessage> context = sessionContextService.getContext(sessionId);
        String nextQuestion = interviewQuestionService.generateNextQuestion(config, resume, context, currentIndex + 2);

        SessionQuestions next = new SessionQuestions();
        next.setSessionId(sessionId);
        next.setQuestionText(nextQuestion);
        next.setExpectedSkills(joinSkills(config.getTargetSkills()));
        next.setOrderIndex(currentIndex + 1);
        next.setAskedAt(LocalDateTime.now());
        sessionQuestionsService.save(next);

        sessionContextService.append(sessionId, ChatMessage.builder().role("assistant").content(nextQuestion).build());

        session.setCurrentQuestionIndex(currentIndex + 1);
        this.updateById(session);

        return NextQuestionVO.builder()
                .hasNext(true)
                .questionId(next.getId())
                .nextQuestion(nextQuestion)
                .build();
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
        Long userId = Long.parseLong(UserContext.getUserId());
        Long interviewCount = this.lambdaQuery()
                .eq(InterviewSessions::getUserId, userId)
                .count();
        BigDecimal avgScore = null;
        List<InterviewSessions> sessions = this.lambdaQuery()
                .eq(InterviewSessions::getUserId, userId)
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

    private int resolveTotalQuestions(String difficulty) {
        if (difficulty == null) {
            return 5;
        }
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 3;
            case "HARD" -> 8;
            default -> 5;
        };
    }

    private String joinSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return String.join(",", skills);
    }
}
