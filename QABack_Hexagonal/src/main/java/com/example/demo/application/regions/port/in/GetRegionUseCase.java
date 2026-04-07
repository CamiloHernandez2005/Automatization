package com.example.demo.application.regions.port.in;

import com.example.demo.domain.Region;

import java.util.Optional;

public interface GetRegionUseCase {
    Optional<Region> getRegion(Long id);
}
