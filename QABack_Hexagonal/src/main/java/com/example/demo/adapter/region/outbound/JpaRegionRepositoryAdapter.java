package com.example.demo.adapter.region.outbound;

import com.example.demo.adapter.region.inbound.mapper.RegionMapper;
import com.example.demo.adapter.region.outbound.entities.RegionEntity;
import com.example.demo.adapter.region.outbound.repository.RegionRepository;
import com.example.demo.application.regions.port.out.RegionRepositoryPort;
import com.example.demo.domain.Region;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaRegionRepositoryAdapter implements RegionRepositoryPort {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;


    public JpaRegionRepositoryAdapter(RegionRepository regionRepository, RegionMapper regionMapper) {
        this.regionRepository = regionRepository;
        this.regionMapper = regionMapper;

    }
    @Override
    public Region save(Region region) {
        RegionEntity entity = regionMapper.toEntity(region);
        RegionEntity saved = regionRepository.save(entity);
        return regionMapper.toDomain(saved);
    }

    @Override
    public Optional<Region> findById(Long id) {
        return regionRepository.findById(id)
                .map(regionMapper::toDomain);
    }

    @Override
    public List<Region> findAll() {
        return regionRepository.findAll().stream()
                .map(regionMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        regionRepository.deleteById(id);
    }
}
