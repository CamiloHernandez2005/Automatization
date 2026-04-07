package com.example.demo.application.componentsRegions.ports.out;


import com.example.demo.domain.ComponentRegion;


public interface ComponentRegionRepositoryPort {
    void save(ComponentRegion relation);
    void delete(Long regionId, Long componentId);
    boolean exists(Long regionId, Long componentId);
}
