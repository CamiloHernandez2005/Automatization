package com.example.demo.adapter.vehicle.inbound.controllers;
import com.example.demo.adapter.vehicle.inbound.dto.VehicleRequest;
import com.example.demo.adapter.vehicle.inbound.dto.VehicleResponse;
import com.example.demo.adapter.vehicle.inbound.mapper.VehicleMapper;
import com.example.demo.application.vehicles.port.in.*;
import com.example.demo.domain.Vehicle;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final GetAllVehiclesUseCase getAllVehiclesUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final VehicleMapper mapper;

    public VehicleController(CreateVehicleUseCase createVehicleUseCase, GetAllVehiclesUseCase getAllVehiclesUseCase, GetVehicleUseCase getVehicleUseCase, UpdateVehicleUseCase updateVehicleUseCase, DeleteVehicleUseCase deleteVehicleUseCase, VehicleMapper mapper) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.getAllVehiclesUseCase = getAllVehiclesUseCase;
        this.getVehicleUseCase = getVehicleUseCase;
        this.updateVehicleUseCase = updateVehicleUseCase;
        this.deleteVehicleUseCase = deleteVehicleUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@RequestBody VehicleRequest request) {
        Vehicle vehicle = mapper.toDomain(request);
        Vehicle created = createVehicleUseCase.createVehicle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll() {
        List<VehicleResponse> vehicles = getAllVehiclesUseCase.getAllVehicles()
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        return getVehicleUseCase.getVehicle(id)
                .map(vehicle -> ResponseEntity.ok(mapper.toResponse(vehicle)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable Long id,
            @RequestBody VehicleRequest request) {
        Vehicle updated = updateVehicleUseCase.updateVehicle(
                id,
                request.brand(),
                request.model(),
                request.year(),
                request.licensePlate()
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteVehicleUseCase.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

}