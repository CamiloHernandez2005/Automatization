package com.example.demo.adapter.vehicle.outbound.repository;

import com.example.demo.adapter.vehicle.outbound.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
}
