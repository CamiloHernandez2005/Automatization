package com.example.demo.adapter.region.inbound.mapper;

import com.example.demo.adapter.region.inbound.dto.RegionRequest;
import com.example.demo.adapter.region.inbound.dto.RegionResponse;
import com.example.demo.adapter.region.outbound.entities.RegionEntity;
import com.example.demo.domain.Region;
import org.springframework.stereotype.Component;

@Component
public class RegionMapper {

    // DTO Request -> Dominio
    public Region toDomain(RegionRequest request) {
        return new Region(
                request.name()
        );
    }

    // Entidad -> Dominio
    public Region toDomain(RegionEntity entity) {
        return new Region(
                entity.getId(),
                entity.getName()
        );
    }

    // Dominio -> Entidad
    public RegionEntity toEntity(Region region) {
        return RegionEntity.builder()
                .id(region.getId())
                .name(region.getName())
                .build();
    }

    // Dominio -> DTO Response
    public RegionResponse toResponse(Region region) {
        return new RegionResponse(
                region.getId(),
                region.getName()
        );
    }

}
