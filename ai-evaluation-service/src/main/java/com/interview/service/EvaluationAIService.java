package com.interview.service;

import com.interview.domain.po.AnswerEvaluations;
import com.interview.domain.vo.AnswerEvaluationResult;
import com.interview.domain.vo.ReportResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评估 / 报告生成 AI 服务。复用 ai-evaluation-service 的 noMemoryChatClient。
 * system prompt 硬编码默认值。
 */
@Service
@Slf4j
public class EvaluationAIService {

    @Autowired
    @Qualifier("noMemoryChatClient")
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String EVALUATE_SYSTEM =
            "你是一名资深技术面试官，负责评估候选人的回答。请从「技术准确性、表达清晰度、回答深度」三个维度评分（0-100 分），并给出具体、有建设性的反馈。";

    private static final String REPORT_SYSTEM =
            "你是一名资深技术面试官，负责为候选人生成面试复盘报告。请基于各题评估结果，给出客观、可执行的总结。";

    /** 评估单题 */
    public AnswerEvaluationResult evaluateAnswer(String question, String answerText) {
        String user = "面试题：" + question + "\n候选人回答：" + answerText +
                "\n\n请以 JSON 格式返回评估结果，字段为：technicalScore(数字)、clarityScore(数字)、depthScore(数字)、" +
                "feedbackText(针对该回答的具体反馈文字)、improvementSuggestions(改进建议文字)。只输出 JSON。";
        return chatClient.prompt()
                .system(EVALUATE_SYSTEM)
                .user(user)
                .call()
                .entity(AnswerEvaluationResult.class);
    }

    /** 汇总生成综合报告 */
    public ReportResult generateReport(List<AnswerEvaluations> evaluations) {
        String evalsJson;
        try {
            evalsJson = objectMapper.writeValueAsString(evaluations);
        } catch (Exception e) {
            throw new RuntimeException("序列化评估结果失败", e);
        }
        String user = "以下是候选人各题评估结果（JSON 数组）：\n" + evalsJson +
                "\n\n请以 JSON 格式返回综合报告，字段为：overallScore(总分 0-100，取各题平均并综合)、" +
                "summary(整体总结文字)、strengths(优点，分条列出)、weaknesses(缺点与待提升点，分条列出)、" +
                "metrics(各维度平均分，如 {\"technical\":85,\"clarity\":80,\"depth\":75})。只输出 JSON。";
        return chatClient.prompt()
                .system(REPORT_SYSTEM)
                .user(user)
                .call()
                .entity(ReportResult.class);
    }
}
