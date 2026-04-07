package com.example.demo.adapter.component.inbound.controllers;

import com.example.demo.adapter.component.inbound.dto.ComponentRequest;
import com.example.demo.adapter.component.inbound.dto.ComponentResponse;
import com.example.demo.adapter.component.inbound.mapper.ComponentMapper;
import com.example.demo.adapter.vehicle.inbound.mapper.VehicleMapper;
import com.example.demo.application.components.ports.in.*;
import com.example.demo.domain.Component;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/components")
public class ComponentController {

    private final CreateComponentUseCase createComponentUseCase;
    private final GetAllComponentUseCase getAllComponentUseCase;
    private final GetComponentUseCase getComponentUseCase;
    private final UpdateComponentUseCase updateComponentUseCase;
    private final DeleteComponentUseCase deleteComponentUseCase;
    private final ComponentMapper mapper;


    public ComponentController(CreateComponentUseCase createComponentUseCase, GetAllComponentUseCase getAllComponentUseCase, GetComponentUseCase getComponentUseCase, UpdateComponentUseCase updateComponentUseCase, DeleteComponentUseCase deleteComponentUseCase, VehicleMapper vehicleMapper, ComponentMapper componentMapper) {
        this.createComponentUseCase = createComponentUseCase;
        this.getAllComponentUseCase = getAllComponentUseCase;
        this.getComponentUseCase = getComponentUseCase;
        this.updateComponentUseCase = updateComponentUseCase;
        this.deleteComponentUseCase = deleteComponentUseCase;
        this.mapper = componentMapper;
    }

    @PostMapping
    public ResponseEntity<ComponentResponse> create(@RequestBody ComponentRequest request) {
        Component component = mapper.toDomain(request);
        Component created = createComponentUseCase.createComponent(component);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<ComponentResponse>> getAll() {
        List<ComponentResponse> component = getAllComponentUseCase.getAllComponents()
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(component);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComponentResponse> getById(@PathVariable Long id) {
        return getComponentUseCase.getComponent(id)
                .map(vehicle -> ResponseEntity.ok(mapper.toResponse(vehicle)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComponentResponse> update(
            @PathVariable Long id,
            @RequestBody ComponentRequest request) {
        Component updated = updateComponentUseCase.updateComponent(
                id,
                request.name(),
                request.description()
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteComponentUseCase.deleteComponent(id);
        return ResponseEntity.noContent().build();
    }


}
