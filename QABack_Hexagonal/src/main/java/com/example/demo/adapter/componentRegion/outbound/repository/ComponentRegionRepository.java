package com.example.demo.adapter.componentRegion.outbound.repository;

import com.example.demo.adapter.component.outbound.entities.ComponentEntity;
import com.example.demo.adapter.componentRegion.outbound.entities.ComponentRegionEntity;
import com.example.demo.adapter.componentRegion.outbound.entities.ComponentRegionId;
import com.example.demo.adapter.region.outbound.entities.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComponentRegionRepository
        extends JpaRepository<ComponentRegionEntity, ComponentRegionId> {

    boolean existsByIdRegionIdAndIdComponentId(Long regionId, Long componentId);

    void deleteByIdRegionIdAndIdComponentId(Long regionId, Long componentId);

    @Query("""
       SELECT c
       FROM RegionEntity c
       JOIN ComponentRegionEntity cr
           ON cr.id.regionId = c.id
       WHERE cr.id.componentId = :componentId
       """)
    List<RegionEntity> findRegionsByComponentId(@Param("componentId") Long componentId);

    @Query("""
       SELECT c
       FROM ComponentEntity c
       JOIN ComponentRegionEntity cr
           ON cr.id.componentId = c.id
       WHERE cr.id.regionId = :regionId
       """)
    List<ComponentEntity> findComponentsByRegionId(@Param("regionId") Long regionId);

    Optional<ComponentRegionEntity> findByIdRegionIdAndIdComponentId(Long regionId, Long componentId);

}
