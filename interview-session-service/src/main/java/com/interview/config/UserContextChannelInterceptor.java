package com.interview.config;

import com.interview.context.UserContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * STOMP 入站通道拦截器：
 * CONNECT 时从原生请求头读取网关透传的 X-User-Id / X-Username 并存入会话属性；
 * 后续每条 MESSAGE 在进入 @MessageMapping 前设置 UserContext，处理完成后清理。
 */
public class UserContextChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand()) && accessor.getSessionAttributes() != null) {
            String userId = accessor.getFirstNativeHeader("X-User-Id");
            String username = accessor.getFirstNativeHeader("X-Username");
            if (userId != null) {
                accessor.getSessionAttributes().put("userId", userId);
            }
            if (username != null) {
                accessor.getSessionAttributes().put("username", username);
            }
        }
        if (accessor.getSessionAttributes() != null) {
            String userId = (String) accessor.getSessionAttributes().get("userId");
            String username = (String) accessor.getSessionAttributes().get("username");
            if (userId != null) {
                UserContext.setUserId(userId);
            }
            if (username != null) {
                UserContext.setUsername(username);
            }
        }
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        UserContext.clear();
    }
}
