package com.example.demo.adapter.user.inbound.dto.user;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        Boolean status
) {
}
