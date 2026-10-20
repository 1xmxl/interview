package com.interview.constant;

/**
 * 面试会话相关 Redis key 与过期约定。
 */
public interface SessionRedisConstant {

    /** 会话对话上下文 key 模板：session:{sessionId}:context，存 AI 与用户的完整问答历史 */
    String SESSION_CONTEXT_KEY = "session:%d:context";

    /** 对话上下文过期时间（小时），面试结束后保留 1 小时供复盘查询 */
    long SESSION_CONTEXT_TTL_HOURS = 1;

    /** 拼接会话上下文 key */
    static String contextKey(Long sessionId) {
        return String.format(SESSION_CONTEXT_KEY, sessionId);
    }
}
