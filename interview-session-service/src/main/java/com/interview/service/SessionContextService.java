package com.interview.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.constant.SessionRedisConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会话对话上下文服务：AI 与用户的完整问答历史存 Redis。
 * key = session:{sessionId}:context，value = JSON 数组 [{role, content}, ...]，TTL 1 小时。
 * 出题时从这里读历史喂给 AI，保持多轮上下文。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionContextService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /** 读取对话上下文（不存在返回空列表） */
    public List<ChatMessage> getContext(Long sessionId) {
        String json = stringRedisTemplate.opsForValue().get(SessionRedisConstant.contextKey(sessionId));
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {
            });
        } catch (Exception e) {
            log.warn("解析会话上下文失败，sessionId={}", sessionId, e);
            return new ArrayList<>();
        }
    }

    /** 覆盖保存对话上下文（TTL 1 小时） */
    public void saveContext(Long sessionId, List<ChatMessage> messages) {
        try {
            String json = objectMapper.writeValueAsString(messages);
            stringRedisTemplate.opsForValue().set(
                    SessionRedisConstant.contextKey(sessionId), json,
                    SessionRedisConstant.SESSION_CONTEXT_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("保存会话上下文失败，sessionId={}", sessionId, e);
        }
    }

    /** 追加一条消息（读-改-写） */
    public void append(Long sessionId, ChatMessage message) {
        List<ChatMessage> context = getContext(sessionId);
        context.add(message);
        saveContext(sessionId, context);
    }

    /** 清空上下文 */
    public void clear(Long sessionId) {
        stringRedisTemplate.delete(SessionRedisConstant.contextKey(sessionId));
    }

    /** 对话消息：role = assistant（AI 出题） / user（用户回答） */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
