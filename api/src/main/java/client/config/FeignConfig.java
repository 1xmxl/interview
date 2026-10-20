package client.config;

import com.interview.context.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 公共配置：服务间调用透传用户身份。
 * 从当前线程的 UserContext 取出 userId/username，写入 X-User-Id / X-Username 请求头，
 * 下游服务的 UserInfoInterceptor 据此还原用户身份（归属校验一致）。
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor userContextInterceptor() {
        return template -> {
            String userId = UserContext.getUserId();
            if (userId != null && !userId.isBlank()) {
                template.header("X-User-Id", userId);
            }
            String username = UserContext.getUsername();
            if (username != null && !username.isBlank()) {
                template.header("X-Username", username);
            }
        };
    }
}
