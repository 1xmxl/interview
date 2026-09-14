package com.interview.service;

import com.interview.domain.po.AnswerEvaluations;
import com.interview.domain.vo.EvaluationResultVO;

/**
 * AI 评估查询服务
 */
public interface IEvaluationService {

    /** 获取面试完整评估结果（报告 + 各题明细） */
    EvaluationResultVO getEvaluation(Long sessionId);

    /** 单题评估详情 */
    AnswerEvaluations getAnswerDetail(Long sessionId, Long answerId);
}
