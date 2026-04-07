package com.example.demo.application.componentsRegions.services;

import com.example.demo.application.componentsRegions.ports.in.AssignComponentToRegionUseCase;
import com.example.demo.application.componentsRegions.ports.out.ComponentRegionRepositoryPort;
import com.example.demo.domain.ComponentRegion;
import org.springframework.stereotype.Service;

@Service
public class AssignComponentToRegionService implements AssignComponentToRegionUseCase {

    private final ComponentRegionRepositoryPort componentRegionRepositoryPort;

    public AssignComponentToRegionService(ComponentRegionRepositoryPort componentRegionRepositoryPort) {
        this.componentRegionRepositoryPort = componentRegionRepositoryPort;
    }

    @Override
    public void assign(Long regionId, Long componentId, String port, String link, String dns) {

        if (componentRegionRepositoryPort.exists(regionId, componentId)) {
            throw new IllegalStateException("Relation already exists");
        }

        ComponentRegion relation = new ComponentRegion(regionId, componentId, port, link, dns);
        componentRegionRepositoryPort.save(relation);
    }

}
