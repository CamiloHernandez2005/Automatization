package com.example.demo.application.regions.services;

import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import com.example.demo.application.regions.port.in.CreateRegionUseCase;
import com.example.demo.application.regions.port.out.RegionRepositoryPort;
import com.example.demo.domain.Region;
import org.springframework.stereotype.Service;

@Service
public class CreateRegionService implements CreateRegionUseCase {

    private final RegionRepositoryPort regionRepositoryPort;

    public CreateRegionService(RegionRepositoryPort regionRepositoryPort, ComponentRepositoryPort componentRepositoryPort) {
        this.regionRepositoryPort = regionRepositoryPort;
    }

    @Override
    public Region createRegion(Region region) {
        return regionRepositoryPort.save(region);
    }
}
