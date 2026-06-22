package com.interview.controller;


import com.interview.DTO.LoginRequest;
import com.interview.DTO.LoginResponse;
import client.UserClient;

import com.interview.DTO.UserDTO;
import com.interview.DTO.UsersDTO;
import com.interview.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@EnableFeignClients(basePackages = "client")
public class AuthController
{
    private final AuthService authService;
    private final UserClient userClient;
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest)
    {
        String password = loginRequest.getPassword();
        String username = loginRequest.getUsername();
        log.info("start login,{},{}",username,password);
        LoginResponse login = authService.login(username,password);
        return login;
    }
    @PostMapping("/register")
    public void register(@RequestBody UserDTO usersDTO)
    {
        log.info("start to rergister",usersDTO);
        authService.register(usersDTO);
    }
    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestHeader("Authorization") String authHeader)
    {
        log.info("start to refresh",authHeader);
        return authService.refresh(authHeader);
    }
    @PostMapping("/logout")
    public void logout ( @RequestHeader(value = "Authorization", required = false) String authHeader,
                         @RequestHeader(value = "Refresh-Token", required = false) String refreshTokenHeader)
    {
        authService.logout(authHeader,refreshTokenHeader);
    }
}
