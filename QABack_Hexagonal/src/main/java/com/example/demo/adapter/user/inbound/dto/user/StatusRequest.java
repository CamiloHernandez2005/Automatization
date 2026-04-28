package com.example.demo.adapter.user.inbound.dto.user;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull(message = "status is required")
        Boolean status
) {}
