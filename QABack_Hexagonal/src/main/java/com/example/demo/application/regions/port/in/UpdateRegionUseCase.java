package com.example.demo.application.regions.port.in;


import com.example.demo.domain.Region;

public interface UpdateRegionUseCase {
    Region updateRegion(Long id, String name);
}
