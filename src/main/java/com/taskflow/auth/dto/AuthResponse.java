package com.taskflow.auth.dto;

public record AuthResponse(
    String token,
    UserDto user
) {
    public record UserDto(Long id, String email, String displayName) {}
}
