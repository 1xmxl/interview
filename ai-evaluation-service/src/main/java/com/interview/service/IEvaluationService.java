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

    /** 生成 / 重新生成综合报告（入库 + Feign 回写） */
    void generateReport(Long sessionId, Long userId);
}
