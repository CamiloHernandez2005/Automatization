package com.example.demo.application.vehicles.port.out;

import com.example.demo.domain.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepositoryPort {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(Long id);
    List<Vehicle> findAll();
    void deleteById(Long id);
}
