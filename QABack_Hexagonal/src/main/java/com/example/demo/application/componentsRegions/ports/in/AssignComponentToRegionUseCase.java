package com.example.demo.application.componentsRegions.ports.in;

public interface AssignComponentToRegionUseCase {
    void assign(Long regionId, Long componentId, String port, String link, String dns);
}
