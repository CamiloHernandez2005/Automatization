package com.example.demo.adapter.vehicle.outbound;

import com.example.demo.adapter.vehicle.inbound.mapper.VehicleMapper;
import com.example.demo.adapter.vehicle.outbound.entities.VehicleEntity;
import com.example.demo.adapter.vehicle.outbound.repository.VehicleRepository;
import com.example.demo.application.vehicles.port.out.VehicleRepositoryPort;
import com.example.demo.domain.Vehicle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaVehicleRepositoryAdapter implements VehicleRepositoryPort {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    public JpaVehicleRepositoryAdapter(VehicleRepository vehicleRepository, VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
    }


    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = vehicleMapper.toEntity(vehicle);
        VehicleEntity saved = vehicleRepository.save(entity);
        return vehicleMapper.toDomain(saved);
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toDomain);
    }

    @Override
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        vehicleRepository.deleteById(id);
    }
}
