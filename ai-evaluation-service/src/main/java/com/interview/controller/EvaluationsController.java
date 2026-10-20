package com.interview.controller;

import com.interview.context.UserContext;
import com.interview.domain.po.AnswerEvaluations;
import com.interview.domain.vo.EvaluationResultVO;
import com.interview.service.IEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * AI 评估控制器
 * </p>
 */
@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
@Slf4j
public class EvaluationsController {

    private final IEvaluationService evaluationService;

    /** 获取面试完整评估结果 */
    @GetMapping("/{sessionId}")
    public EvaluationResultVO getEvaluation(@PathVariable("sessionId") Long sessionId) {
        log.info("获取面试完整评估结果, sessionId={}", sessionId);
        return evaluationService.getEvaluation(sessionId);
    }

    /** 单题评估详情 */
    @GetMapping("/{sessionId}/answers/{answerId}")
    public AnswerEvaluations getAnswerDetail(@PathVariable("sessionId") Long sessionId, @PathVariable("answerId") Long answerId) {
        log.info("获取单题评估详情, sessionId={}, answerId={}", sessionId, answerId);
        return evaluationService.getAnswerDetail(sessionId, answerId);
    }

    /** 手动触发报告重新生成 */
    @PostMapping("/{sessionId}/report/regenerate")
    public void regenerate(@PathVariable("sessionId") Long sessionId) {
        log.info("手动重新生成报告, sessionId={}", sessionId);
        Long userId = Long.parseLong(UserContext.getUserId());
        evaluationService.generateReport(sessionId, userId);
    }
}
