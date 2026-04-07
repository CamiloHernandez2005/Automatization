package com.example.demo.application.vehicles.port.in;

import com.example.demo.domain.Vehicle;

public interface CreateVehicleUseCase {
    Vehicle createVehicle(Vehicle vehicle);
}
