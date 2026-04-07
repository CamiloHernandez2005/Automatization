package com.example.demo.adapter.user.inbound.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "password is required")
        String password
) {
}
