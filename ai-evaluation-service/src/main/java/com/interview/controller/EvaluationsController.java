package com.interview.controller;

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
    public EvaluationResultVO getEvaluation(@PathVariable Long sessionId) {
        log.info("获取面试完整评估结果, sessionId={}", sessionId);
        return evaluationService.getEvaluation(sessionId);
    }

    /** 单题评估详情 */
    @GetMapping("/{sessionId}/answers/{answerId}")
    public AnswerEvaluations getAnswerDetail(@PathVariable Long sessionId, @PathVariable Long answerId) {
        log.info("获取单题评估详情, sessionId={}, answerId={}", sessionId, answerId);
        return evaluationService.getAnswerDetail(sessionId, answerId);
    }
}
