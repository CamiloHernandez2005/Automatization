package com.example.demo.application.vehicles.port.in;

import com.example.demo.domain.Vehicle;

public interface UpdateVehicleUseCase {
    Vehicle updateVehicle(Long id, String brand, String model, int year, String licensePlate);
}
