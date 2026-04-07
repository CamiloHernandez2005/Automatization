package com.example.demo.application.vehicles.services;


import com.example.demo.application.vehicles.port.in.GetVehicleUseCase;
import com.example.demo.application.vehicles.port.out.VehicleRepositoryPort;
import com.example.demo.domain.Vehicle;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetVehicleService implements GetVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    public GetVehicleService(VehicleRepositoryPort vehicleRepositoryPort) {
        this.vehicleRepositoryPort = vehicleRepositoryPort;
    }

    @Override
    public Optional<Vehicle> getVehicle(Long id) {
        return vehicleRepositoryPort.findById(id);
    }
}
