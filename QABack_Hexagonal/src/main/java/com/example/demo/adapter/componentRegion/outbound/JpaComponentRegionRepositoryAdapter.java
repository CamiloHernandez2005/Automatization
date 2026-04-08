package com.example.demo.adapter.componentRegion.outbound;

import com.example.demo.adapter.componentRegion.inbound.mapper.ComponentRegionMapper;
import com.example.demo.adapter.componentRegion.outbound.entities.ComponentRegionEntity;
import com.example.demo.adapter.componentRegion.outbound.repository.ComponentRegionRepository;
import com.example.demo.application.componentsRegions.ports.out.ComponentRegionRepositoryPort;
import com.example.demo.domain.ComponentRegion;
import com.example.demo.domain.Region;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaComponentRegionRepositoryAdapter implements ComponentRegionRepositoryPort {

    private final ComponentRegionRepository repository;
    private final ComponentRegionMapper mapper;

    public JpaComponentRegionRepositoryAdapter(
            ComponentRegionRepository repository,
            ComponentRegionMapper mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(ComponentRegion componentRegion) {
        ComponentRegionEntity entity = mapper.toEntity(componentRegion);
        repository.save(entity);
    }


    @Override
    public void delete(Long regionId, Long componentId) {
        repository.deleteByIdRegionIdAndIdComponentId(regionId, componentId);
    }


    @Override
    public boolean exists(Long regionId, Long componentId) {
        return repository
                .existsByIdRegionIdAndIdComponentId(regionId, componentId);
    }

    @Override
    public Optional<ComponentRegion> findById(Long regionId, Long componentId) {
        return repository
                .findByIdRegionIdAndIdComponentId(regionId, componentId)
                .map(mapper::toDomain);
    }

}
