package client;

import com.interview.DTO.SessionResultDTO;
import com.interview.DTO.SessionStatsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 面试会话服务 Feign 客户端（供 ai-evaluation-service 回写评估结果、user-service 查询统计）。
 * 注意：MQ 消费线程无 UserContext，故 userId 由消息体显式传递。
 */
@FeignClient(value = "interview-session-service")
public interface SessionClient {

    /** 回写面试总评摘要 + 会话状态置为 EVALUATED */
    @PutMapping("/interviews/sessions/{sessionId}/result")
    void updateResult(@PathVariable("sessionId") Long sessionId,
                      @RequestParam("userId") Long userId,
                      @RequestBody SessionResultDTO result);

    /** 当前用户面试统计（次数 + 平均分） */
    @GetMapping("/interviews/stats")
    SessionStatsDTO getStats();
}
