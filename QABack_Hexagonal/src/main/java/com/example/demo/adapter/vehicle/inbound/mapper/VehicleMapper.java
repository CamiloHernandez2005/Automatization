package com.example.demo.adapter.vehicle.inbound.mapper;


import com.example.demo.adapter.vehicle.inbound.dto.VehicleRequest;
import com.example.demo.adapter.vehicle.inbound.dto.VehicleResponse;
import com.example.demo.adapter.vehicle.outbound.entities.VehicleEntity;
import com.example.demo.domain.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    // DTO Request -> Dominio
    public Vehicle toDomain(VehicleRequest request) {
        return new Vehicle(
                request.brand(),
                request.model(),
                request.year(),
                request.licensePlate()
        );
    }

    // Entidad -> Dominio
    public Vehicle toDomain(VehicleEntity entity) {
        return new Vehicle(
                entity.getId(),
                entity.getBrand(),
                entity.getModel(),
                entity.getYear(),
                entity.getLicensePlate()
        );
    }

    // Dominio -> Entidad
    public VehicleEntity toEntity(Vehicle vehicle) {
        return new VehicleEntity(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getLicensePlate()
        );
    }

    // Dominio -> DTO Response
    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getLicensePlate()
        );
    }
}