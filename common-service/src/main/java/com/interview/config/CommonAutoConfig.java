package com.interview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.interview.utils.JwtUtil;

@AutoConfiguration
public class CommonAutoConfig {
    @Bean
    public JwtUtil jwtUtil(@Value("${tj.secret}") String secret) {
        JwtUtil util = new JwtUtil();
        util.setJwtToken(secret);
        util.init(); // 手动调用初始化
        return util;
    }
}
