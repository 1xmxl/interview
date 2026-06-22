package com.interview.filter;


import com.interview.utils.RefreshTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.interview.utils.JwtUtil;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter , Ordered {
    private final JwtUtil jwtUtil;
    private final RefreshTokenHelper refreshTokenHelper;
    private static final List<String> WHITE_LIST = List.of("/as/");
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().value();
        // 1. 白名单放行
        if (WHITE_LIST.stream().anyMatch(requestPath::startsWith)) {
            return chain.filter(exchange);
        }
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if(!jwtUtil.validateToken(token)){
            return unauthorizedResponse(exchange, "Invalid token");
        }
        String jti = jwtUtil.getJwtIdFromToken(token);
        if (refreshTokenHelper.isTokenBlacklisted(jti)) {
            log.warn("Token已在黑名单中，拒绝访问: {}", jti);
            return unauthorizedResponse(exchange, "Token has been logged out");
        }
        String userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        ServerHttpRequest build = request.mutate().header("X-User-Id", userId)
                .header("X-Username", username)
                .build();
        ServerWebExchange build1 = exchange.mutate().request(build).build();
        return chain.filter(build1);
    }
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
    @Override
    public int getOrder() {
        return -1;
    }


}
