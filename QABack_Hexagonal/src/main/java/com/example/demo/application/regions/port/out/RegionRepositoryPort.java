package com.example.demo.application.regions.port.out;

import com.example.demo.domain.Region;

import java.util.List;
import java.util.Optional;

public interface RegionRepositoryPort {
    Region save(Region region);
    Optional<Region> findById(Long id);
    List<Region> findAll();
    void deleteById(Long id);
}
