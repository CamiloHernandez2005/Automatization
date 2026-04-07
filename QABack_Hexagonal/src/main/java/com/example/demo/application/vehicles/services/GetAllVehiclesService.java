package com.example.demo.application.vehicles.services;


import com.example.demo.application.vehicles.port.in.GetAllVehiclesUseCase;
import com.example.demo.application.vehicles.port.out.VehicleRepositoryPort;
import com.example.demo.domain.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllVehiclesService implements GetAllVehiclesUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    public GetAllVehiclesService(VehicleRepositoryPort vehicleRepositoryPort) {
        this.vehicleRepositoryPort = vehicleRepositoryPort;
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepositoryPort.findAll();
    }
}
