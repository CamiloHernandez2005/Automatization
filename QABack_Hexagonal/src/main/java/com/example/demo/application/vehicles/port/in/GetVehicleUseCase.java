package com.example.demo.application.vehicles.port.in;

import com.example.demo.domain.Vehicle;

import java.util.Optional;

public interface GetVehicleUseCase {
    Optional<Vehicle> getVehicle(Long id);
}
