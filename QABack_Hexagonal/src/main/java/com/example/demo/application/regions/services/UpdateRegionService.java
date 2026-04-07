package com.example.demo.application.regions.services;

import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import com.example.demo.application.regions.port.in.UpdateRegionUseCase;
import com.example.demo.application.regions.port.out.RegionRepositoryPort;
import com.example.demo.domain.Region;
import org.springframework.stereotype.Service;

@Service
public class UpdateRegionService implements UpdateRegionUseCase {

    private final RegionRepositoryPort regionRepositoryPort;
    private final ComponentRepositoryPort componentRepositoryPort;

    public UpdateRegionService(RegionRepositoryPort regionRepositoryPort, ComponentRepositoryPort componentRepositoryPort) {
        this.regionRepositoryPort = regionRepositoryPort;
        this.componentRepositoryPort = componentRepositoryPort;
    }

    @Override
    public Region updateRegion(Long id, String name) {

        Region existing = regionRepositoryPort.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Region with id " + id + " not found"
                        )
                );

            existing.updateData(name);

            return regionRepositoryPort.save(existing);
        }
}
