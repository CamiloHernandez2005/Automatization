package com.example.demo.application.vehicles.port.in;

import com.example.demo.domain.Vehicle;

import java.util.List;

public interface GetAllVehiclesUseCase {
    List <Vehicle> getAllVehicles();
}
