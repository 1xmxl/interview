package com.interview.DTO;

import lombok.Data;

@Data
public class LoginResponse{
private String accessToken;
private String username;
private String refreshToken;
}
