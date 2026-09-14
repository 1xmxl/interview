package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.domain.po.AnswerEvaluations;
import com.interview.domain.po.SessionReports;
import com.interview.domain.vo.EvaluationResultVO;
import com.interview.mapper.AnswerEvaluationsMapper;
import com.interview.mapper.SessionReportsMapper;
import com.interview.service.IEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 评估查询服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationServiceImpl implements IEvaluationService {

    private final SessionReportsMapper sessionReportsMapper;
    private final AnswerEvaluationsMapper answerEvaluationsMapper;

    @Override
    public EvaluationResultVO getEvaluation(Long sessionId) {
        SessionReports report = sessionReportsMapper.selectOne(
                new LambdaQueryWrapper<SessionReports>()
                        .eq(SessionReports::getSessionId, sessionId));

        List<AnswerEvaluations> answers = answerEvaluationsMapper.selectList(
                new LambdaQueryWrapper<AnswerEvaluations>()
                        .eq(AnswerEvaluations::getSessionId, sessionId)
                        .orderByAsc(AnswerEvaluations::getId));

        EvaluationResultVO vo = new EvaluationResultVO();
        vo.setSessionId(sessionId);
        if (report != null) {
            vo.setReportContent(report.getReportContent());
            vo.setMetrics(report.getMetrics());
            vo.setGeneratedAt(report.getGeneratedAt());
        }
        vo.setAnswers(answers);
        return vo;
    }

    @Override
    public AnswerEvaluations getAnswerDetail(Long sessionId, Long answerId) {
        return answerEvaluationsMapper.selectOne(
                new LambdaQueryWrapper<AnswerEvaluations>()
                        .eq(AnswerEvaluations::getSessionId, sessionId)
                        .eq(AnswerEvaluations::getAnswerId, answerId));
    }
}
