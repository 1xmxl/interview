package com.interview.service.impl;

import client.SessionClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.DTO.SessionResultDTO;
import com.interview.domain.po.AnswerEvaluations;
import com.interview.domain.po.SessionReports;
import com.interview.domain.vo.EvaluationResultVO;
import com.interview.domain.vo.ReportResult;
import com.interview.mapper.AnswerEvaluationsMapper;
import com.interview.mapper.SessionReportsMapper;
import com.interview.service.EvaluationAIService;
import com.interview.service.IEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 评估服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationServiceImpl implements IEvaluationService {

    private final SessionReportsMapper sessionReportsMapper;
    private final AnswerEvaluationsMapper answerEvaluationsMapper;
    private final EvaluationAIService evaluationAIService;
    private final SessionClient sessionClient;
    private final ObjectMapper objectMapper;

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

    @Override
    public void generateReport(Long sessionId, Long userId) {
        List<AnswerEvaluations> evaluations = answerEvaluationsMapper.selectList(
                new LambdaQueryWrapper<AnswerEvaluations>()
                        .eq(AnswerEvaluations::getSessionId, sessionId)
                        .orderByAsc(AnswerEvaluations::getId));
        if (evaluations.isEmpty()) {
            log.warn("无评估记录，跳过报告生成, sessionId={}", sessionId);
            return;
        }

        ReportResult result = evaluationAIService.generateReport(evaluations);

        SessionReports existing = sessionReportsMapper.selectOne(
                new LambdaQueryWrapper<SessionReports>().eq(SessionReports::getSessionId, sessionId));
        SessionReports report = existing == null ? new SessionReports() : existing;
        report.setSessionId(sessionId);
        report.setReportContent(buildReportContent(result));
        report.setMetrics(serializeMetrics(result.getMetrics()));
        report.setGeneratedAt(LocalDateTime.now());
        if (existing == null) {
            sessionReportsMapper.insert(report);
        } else {
            sessionReportsMapper.updateById(report);
        }
        log.info("综合报告已生成入库, sessionId={}", sessionId);

        SessionResultDTO dto = SessionResultDTO.builder()
                .overallScore(result.getOverallScore())
                .summary(result.getSummary())
                .strengths(result.getStrengths())
                .weaknesses(result.getWeaknesses())
                .build();
        try {
            sessionClient.updateResult(sessionId, userId, dto);
            log.info("评估结果已回写会话服务, sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("回写评估结果失败（报告已入库）, sessionId={}", sessionId, e);
        }
    }

    private String buildReportContent(ReportResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 面试复盘报告\n\n");
        sb.append("**总分：").append(result.getOverallScore()).append("**\n\n");
        sb.append("## 整体总结\n\n").append(result.getSummary()).append("\n\n");
        sb.append("## 优点\n\n").append(result.getStrengths()).append("\n\n");
        sb.append("## 待提升\n\n").append(result.getWeaknesses()).append("\n");
        return sb.toString();
    }

    private String serializeMetrics(Map<String, BigDecimal> metrics) {
        try {
            return metrics == null ? "{}" : objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            log.warn("序列化 metrics 失败", e);
            return "{}";
        }
    }
}
