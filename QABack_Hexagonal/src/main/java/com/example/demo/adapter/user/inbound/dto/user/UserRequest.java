package com.example.demo.adapter.user.inbound.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotBlank(message = "full name is required")
        String fullName,

        @NotBlank(message = "password is required")
        String password,

        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "role is required")
        String role,

        @NotNull(message = "status is required")
        Boolean status
) {}

