package com.example.demo.adapter.vehicle.inbound.dto;

public record VehicleResponse(
        Long id,
        String brand,
        String model,
        int year,
        String licensePlate
) {
}
