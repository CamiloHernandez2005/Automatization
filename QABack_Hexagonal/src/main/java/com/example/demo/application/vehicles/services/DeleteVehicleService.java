package com.example.demo.application.vehicles.services;

import com.example.demo.application.vehicles.port.in.DeleteVehicleUseCase;
import com.example.demo.application.vehicles.port.out.VehicleRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class DeleteVehicleService implements DeleteVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    public DeleteVehicleService(VehicleRepositoryPort vehicleRepositoryPort) {
        this.vehicleRepositoryPort = vehicleRepositoryPort;
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepositoryPort.deleteById(id);
    }
}
