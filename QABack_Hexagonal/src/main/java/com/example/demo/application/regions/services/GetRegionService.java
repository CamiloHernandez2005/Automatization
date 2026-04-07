package com.example.demo.application.regions.services;

import com.example.demo.application.regions.port.in.GetRegionUseCase;
import com.example.demo.application.regions.port.out.RegionRepositoryPort;
import com.example.demo.domain.Region;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetRegionService implements GetRegionUseCase {

    private final RegionRepositoryPort regionRepositoryPort;

    public GetRegionService(RegionRepositoryPort regionRepositoryPort) {
        this.regionRepositoryPort = regionRepositoryPort;
    }

    @Override
    public Optional<Region> getRegion(Long id) {
        return regionRepositoryPort.findById(id);
    }
}
