package com.example.demo.adapter.region.inbound.controllers;

import com.example.demo.adapter.region.inbound.dto.RegionRequest;
import com.example.demo.adapter.region.inbound.dto.RegionResponse;
import com.example.demo.adapter.region.inbound.mapper.RegionMapper;
import com.example.demo.application.regions.port.in.*;
import com.example.demo.domain.Region;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regions")
public class RegionController {

    private final CreateRegionUseCase createRegionUseCase;
    private final GetAllRegionsUseCase getAllRegionsUseCase;
    private final UpdateRegionUseCase updateRegionUseCase;
    private final DeleteRegionUseCase deleteRegionUseCase;
    private final GetRegionUseCase getRegionUseCase;
    private final RegionMapper mapper;

    public RegionController(CreateRegionUseCase createRegionUseCase, GetAllRegionsUseCase getAllRegionsUseCase, UpdateRegionUseCase updateRegionUseCase, DeleteRegionUseCase deleteRegionUseCase, GetRegionUseCase getRegionUseCase, RegionMapper mapper) {
        this.createRegionUseCase = createRegionUseCase;
        this.getAllRegionsUseCase = getAllRegionsUseCase;
        this.updateRegionUseCase = updateRegionUseCase;
        this.deleteRegionUseCase = deleteRegionUseCase;
        this.getRegionUseCase = getRegionUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<RegionResponse> create(@RequestBody RegionRequest request) {
        Region region = mapper.toDomain(request);
        Region created = createRegionUseCase.createRegion(region);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));

    }

    @GetMapping
    public ResponseEntity<List<RegionResponse>> getAll() {
        List<RegionResponse> component = getAllRegionsUseCase.getAllRegions()
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(component);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionResponse> getById(@PathVariable Long id) {
        return getRegionUseCase.getRegion(id)
                .map(region -> ResponseEntity.ok(mapper.toResponse(region)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionResponse> update(@PathVariable Long id, @RequestBody RegionRequest request) {
        Region updated = updateRegionUseCase.updateRegion(
                id,
                request.name()
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRegionUseCase.deleteRegion(id);
        return ResponseEntity.noContent().build();
    }

}
