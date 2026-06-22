package com.interview.service;

import com.interview.DTO.LoginResponse;
import com.interview.DTO.UserDTO;

import java.util.Map;

public interface AuthService {
    public LoginResponse login(String username, String password) ;

    void register(UserDTO usersDTO);

    Map<String, String> refresh(String authHeader);

    Map<String, String> logout(String authHeader, String refreshTokenHeader);
}
