package com.example.demo.adapter.componentRegion.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record ComponentRegionRequest(
        @NotBlank(message = "Port is required")
        String port,

        @NotBlank(message = "Link is required")
        String link,

        String dns
) {}
