package com.example.demo.adapter.component.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record ComponentRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description
) {
}
