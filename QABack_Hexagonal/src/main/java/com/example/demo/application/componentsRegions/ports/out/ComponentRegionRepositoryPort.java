package com.example.demo.application.componentsRegions.ports.out;


import com.example.demo.adapter.componentRegion.outbound.entities.ComponentRegionEntity;
import com.example.demo.domain.ComponentRegion;

import java.util.Optional;


public interface ComponentRegionRepositoryPort {
    void save(ComponentRegion relation);
    void delete(Long regionId, Long componentId);
    boolean exists(Long regionId, Long componentId);
    Optional<ComponentRegion> findById(Long regionId, Long componentId);}
