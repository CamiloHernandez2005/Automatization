package com.example.demo.application.vehicles.services;


import com.example.demo.application.vehicles.port.in.UpdateVehicleUseCase;
import com.example.demo.application.vehicles.port.out.VehicleRepositoryPort;
import com.example.demo.domain.Vehicle;
import org.springframework.stereotype.Service;

@Service
public class UpdateVehicleService implements UpdateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    public UpdateVehicleService(VehicleRepositoryPort vehicleRepositoryPort) {
        this.vehicleRepositoryPort = vehicleRepositoryPort;
    }

    @Override
    public Vehicle updateVehicle(Long id, String brand, String model, int year, String licensePlate) {
        Vehicle existing = vehicleRepositoryPort.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle with id " + id + " not found"
                        )
                );

        existing.updateData(brand, model, year, licensePlate);

        return vehicleRepositoryPort.save(existing);
    }
}
