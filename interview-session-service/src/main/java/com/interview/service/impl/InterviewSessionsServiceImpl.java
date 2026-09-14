package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.context.UserContext;
import com.interview.domain.dto.SessionSubmit;
import com.interview.domain.po.InterviewSessions;
import com.interview.domain.po.SessionAnswers;
import com.interview.domain.po.SessionQuestions;
import com.interview.domain.vo.SessionHistoryVO;
import com.interview.mapper.InterviewSessionsMapper;
import com.interview.service.IInterviewSessionsService;
import com.interview.service.ISessionAnswersService;
import com.interview.service.ISessionQuestionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@Service
@RequiredArgsConstructor
public class InterviewSessionsServiceImpl extends ServiceImpl<InterviewSessionsMapper, InterviewSessions> implements IInterviewSessionsService {

    private final ISessionQuestionsService sessionQuestionsService;
    private final ISessionAnswersService sessionAnswersService;

    @Override
    public void createSession(SessionSubmit submit) {
        Long userId = Long.parseLong(UserContext.getUserId());
        InterviewSessions session = new InterviewSessions();
        session.setUserId(userId);
        session.setConfigId(submit.getConfigId());
        session.setResumeId(submit.getResumeId());
        session.setStatus("CREATED");
        session.setCurrentQuestionIndex(0);
        session.setTotalQuestions(0);
        this.save(session);
    }

    @Override
    public InterviewSessions getSession(Long id) {
        Long userId = Long.parseLong(UserContext.getUserId());
        LambdaQueryWrapper<InterviewSessions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewSessions::getId, id)
                .eq(InterviewSessions::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    public List<SessionHistoryVO> getHistory(Long id) {
        // 校验归属权
        InterviewSessions session = getSession(id);
        if (session == null) {
            throw new RuntimeException("会话不存在或无权限操作");
        }
        // 查该会话所有题目，按顺序
        LambdaQueryWrapper<SessionQuestions> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(SessionQuestions::getSessionId, id)
                .orderByAsc(SessionQuestions::getOrderIndex);
        List<SessionQuestions> questions = sessionQuestionsService.list(questionWrapper);
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量查答案
        List<Long> questionIds = questions.stream().map(SessionQuestions::getId).collect(Collectors.toList());
        LambdaQueryWrapper<SessionAnswers> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.in(SessionAnswers::getQuestionId, questionIds);
        List<SessionAnswers> answers = sessionAnswersService.list(answerWrapper);
        // 按 questionId 分组
        Map<Long, SessionAnswers> answerMap = answers.stream()
                .collect(Collectors.toMap(SessionAnswers::getQuestionId, a -> a, (a1, a2) -> a1));
        // 组装 VO
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
}
