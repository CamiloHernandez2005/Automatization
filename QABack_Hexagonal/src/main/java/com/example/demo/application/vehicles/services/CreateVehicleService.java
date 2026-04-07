package com.example.demo.application.vehicles.services;


import com.example.demo.application.vehicles.port.in.CreateVehicleUseCase;
import com.example.demo.application.vehicles.port.out.VehicleRepositoryPort;
import com.example.demo.domain.Vehicle;
import org.springframework.stereotype.Service;

@Service
public class CreateVehicleService implements CreateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    public CreateVehicleService(VehicleRepositoryPort vehicleRepositoryPort) {
        this.vehicleRepositoryPort = vehicleRepositoryPort;
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepositoryPort.save(vehicle);
    }
}
