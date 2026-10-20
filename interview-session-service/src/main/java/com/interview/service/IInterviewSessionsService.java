package com.interview.service;

import com.interview.DTO.SessionResultDTO;
import com.interview.DTO.SessionStatsDTO;
import com.interview.domain.dto.AnswerSubmit;
import com.interview.domain.dto.SessionSubmit;
import com.interview.domain.po.InterviewSessions;
import com.interview.domain.vo.NextQuestionVO;
import com.interview.domain.vo.SessionHistoryVO;
import com.interview.domain.vo.StartQuestionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
public interface IInterviewSessionsService extends IService<InterviewSessions> {

    /** 创建会话，返回新会话 ID（前端需要跳转） */
    Long createSession(SessionSubmit submit);

    InterviewSessions getSession(Long id);

    List<SessionHistoryVO> getHistory(Long id);

    /** 开始面试：AI 出第一题 */
    StartQuestionVO start(Long sessionId);

    /** 提交答案：存答案 + 发评估消息 + AI 出下一题 */
    NextQuestionVO answer(Long sessionId, AnswerSubmit submit);

    /** 结束面试：触发报告生成 */
    void end(Long sessionId);

    /** 回写评估结果（Feign 供 ai-evaluation-service 调用） */
    void updateResult(Long sessionId, Long userId, SessionResultDTO result);

    /** 当前用户面试统计（次数 + 平均分），供首页数据概览 */
    SessionStatsDTO getStats();
}
