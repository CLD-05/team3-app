package com.foldy.domain.user.dto;
 
public record LoginResponse(
        String token,
        String nickname,
        String role
) {}