package com.example.demo.adapter.region.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record RegionRequest(
        @NotBlank(message = "name is required")
        String name
) {
}
