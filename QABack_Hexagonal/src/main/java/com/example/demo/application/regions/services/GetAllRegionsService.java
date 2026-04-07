package com.example.demo.application.regions.services;

import com.example.demo.application.regions.port.in.GetAllRegionsUseCase;
import com.example.demo.application.regions.port.out.RegionRepositoryPort;
import com.example.demo.domain.Region;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllRegionsService implements GetAllRegionsUseCase {

    private final RegionRepositoryPort regionRepositoryPort;

    public GetAllRegionsService(RegionRepositoryPort regionRepositoryPort) {
        this.regionRepositoryPort = regionRepositoryPort;
    }

    @Override
    public List<Region> getAllRegions() {
        return regionRepositoryPort.findAll();
    }
}
