package com.example.demo.adapter.vehicle.inbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VehicleRequest(
        @NotBlank(message = "Brand is required")
        String brand,

        @NotBlank(message = "Model is required")
        String model,

        @Min(value = 1886, message = "Year must be greater than 1885")
        int year,

        @NotBlank(message = "License plate is required")
        String licensePlate
) {}
