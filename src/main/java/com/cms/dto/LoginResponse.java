package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private AuthUserDto user;
    private String accessToken;
    private Instant expiresAt;
}
