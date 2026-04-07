package com.example.demo.adapter.componentRegion.inbound;

import com.example.demo.adapter.componentRegion.inbound.dto.ComponentRegionRequest;
import com.example.demo.adapter.componentRegion.inbound.dto.ComponentRegionResponse;
import com.example.demo.adapter.componentRegion.inbound.mapper.ComponentRegionMapper;
import com.example.demo.application.componentsRegions.ports.in.AssignComponentToRegionUseCase;
import com.example.demo.application.componentsRegions.ports.in.RemoveComponentFromRegionUseCase;
import com.example.demo.domain.ComponentRegion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ComponentRegionController {

    private final AssignComponentToRegionUseCase assignUseCase;
    private final RemoveComponentFromRegionUseCase removeUseCase;

    private final ComponentRegionMapper mapper;

    public ComponentRegionController(
            AssignComponentToRegionUseCase assignUseCase,
            RemoveComponentFromRegionUseCase removeUseCase,
            ComponentRegionMapper mapper) {

        this.assignUseCase = assignUseCase;
        this.removeUseCase = removeUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/components/{componentId}/regions/{regionId}")
    public ResponseEntity<ComponentRegionResponse> assign(
            @PathVariable Long componentId,
            @PathVariable Long regionId,
            @RequestBody ComponentRegionRequest request) {

        assignUseCase.assign(
                regionId,
                componentId,
                request.port(),
                request.link(),
                request.dns()
        );

        ComponentRegion domain = mapper.toDomain(regionId, componentId, request);

        return ResponseEntity.ok(mapper.toResponse(domain));
    }

    @DeleteMapping("/components/{componentId}/regions/{regionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long componentId,
            @PathVariable Long regionId) {

        removeUseCase.remove(regionId, componentId);

        return ResponseEntity.noContent().build();
    }

}
