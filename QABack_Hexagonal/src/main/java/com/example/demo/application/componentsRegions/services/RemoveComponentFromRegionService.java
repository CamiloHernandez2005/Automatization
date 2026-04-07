package com.example.demo.application.componentsRegions.services;

import com.example.demo.application.componentsRegions.ports.in.RemoveComponentFromRegionUseCase;
import com.example.demo.application.componentsRegions.ports.out.ComponentRegionRepositoryPort;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RemoveComponentFromRegionService implements RemoveComponentFromRegionUseCase {

    private final ComponentRegionRepositoryPort componentRegionRepositoryPort;

    public RemoveComponentFromRegionService(ComponentRegionRepositoryPort componentRegionRepositoryPort) {
        this.componentRegionRepositoryPort = componentRegionRepositoryPort;
    }


    @Override
    public void remove(Long regionId, Long componentId) {

        if (!componentRegionRepositoryPort.exists(regionId, componentId)) {
            throw new IllegalArgumentException("Relation does not exist");
        }

        componentRegionRepositoryPort.delete(regionId, componentId);
    }
}

