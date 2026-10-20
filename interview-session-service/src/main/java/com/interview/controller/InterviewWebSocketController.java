package com.interview.controller;

import com.interview.domain.dto.AnswerSubmit;
import com.interview.domain.vo.EndInterviewVO;
import com.interview.domain.vo.NextQuestionVO;
import com.interview.domain.vo.StartQuestionVO;
import com.interview.service.IInterviewSessionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 面试实时交互（STOMP over WebSocket）。
 * 客户端订阅 /topic/interview/{sessionId}，向 /app/interview/{sessionId}/... 发送指令。
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class InterviewWebSocketController {

    private final IInterviewSessionsService interviewSessionsService;
    private final SimpMessagingTemplate messagingTemplate;

    /** 开始面试：推送首题 */
    @MessageMapping("/interview/{sessionId}/start")
    public void start(@DestinationVariable("sessionId") Long sessionId) {
        log.info("WS 开始面试, sessionId={}", sessionId);
        StartQuestionVO vo = interviewSessionsService.start(sessionId);
        messagingTemplate.convertAndSend(topic(sessionId), vo);
    }

    /** 提交答案：推送下一题（或 hasNext=false 表示题目已答完） */
    @MessageMapping("/interview/{sessionId}/answer")
    public void answer(@DestinationVariable("sessionId") Long sessionId, AnswerSubmit submit) {
        log.info("WS 提交答案, sessionId={}", sessionId);
        NextQuestionVO vo = interviewSessionsService.answer(sessionId, submit);
        messagingTemplate.convertAndSend(topic(sessionId), vo);
    }

    /** 结束面试：推送结束事件 */
    @MessageMapping("/interview/{sessionId}/end")
    public void end(@DestinationVariable("sessionId") Long sessionId) {
        log.info("WS 结束面试, sessionId={}", sessionId);
        interviewSessionsService.end(sessionId);
        messagingTemplate.convertAndSend(topic(sessionId), EndInterviewVO.builder()
                .sessionId(sessionId)
                .status("COMPLETED")
                .message("面试已结束，评估报告生成中")
                .build());
    }

    private String topic(Long sessionId) {
        return "/topic/interview/" + sessionId;
    }
}
