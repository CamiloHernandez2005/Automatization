package com.example.demo.application.regions.services;

import com.example.demo.application.regions.port.in.DeleteRegionUseCase;
import com.example.demo.application.regions.port.out.RegionRepositoryPort;
import org.springframework.stereotype.Service;


@Service
public class DeleteRegionService implements DeleteRegionUseCase {

    private final RegionRepositoryPort regionRepositoryPort;

    public DeleteRegionService(RegionRepositoryPort regionRepositoryPort) {
        this.regionRepositoryPort = regionRepositoryPort;
    }

    @Override
    public void deleteRegion(Long id) {
        regionRepositoryPort.deleteById(id);
    }
}
