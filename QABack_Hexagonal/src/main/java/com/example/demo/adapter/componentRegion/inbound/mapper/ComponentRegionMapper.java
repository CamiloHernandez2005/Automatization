package com.example.demo.adapter.componentRegion.inbound.mapper;

import com.example.demo.adapter.componentRegion.inbound.dto.ComponentRegionRequest;
import com.example.demo.adapter.componentRegion.inbound.dto.ComponentRegionResponse;
import com.example.demo.adapter.componentRegion.outbound.entities.ComponentRegionEntity;
import com.example.demo.adapter.componentRegion.outbound.entities.ComponentRegionId;
import com.example.demo.domain.ComponentRegion;
import org.springframework.stereotype.Component;


@Component
public class ComponentRegionMapper {

    // DTO Request -> Dominio
    public ComponentRegion toDomain(
            Long regionId,
            Long componentId,
            ComponentRegionRequest request){

        return new ComponentRegion(
                regionId,
                componentId,
                request.port(),
                request.link(),
                request.dns()
        );
    }

    // Entidad -> Dominio
    public ComponentRegion toDomain(ComponentRegionEntity entity){
        return new ComponentRegion(
                entity.getId().getRegionId(),
                entity.getId().getComponentId(),
                entity.getPort(),
                entity.getLink(),
                entity.getDns()
        );
    }

    // Dominio -> Entidad
    public ComponentRegionEntity toEntity(ComponentRegion componentRegion){

        return ComponentRegionEntity.builder()
                .id(new ComponentRegionId(
                        componentRegion.getRegionId(),
                        componentRegion.getComponentId()
                ))
                .port(componentRegion.getPort())
                .link(componentRegion.getLink())
                .dns(componentRegion.getDns())
                .build();
    }

    // Dominio -> DTO Response
    public ComponentRegionResponse toResponse(ComponentRegion componentRegion){
        return new ComponentRegionResponse(
                componentRegion.getRegionId(),
                componentRegion.getComponentId(),
                componentRegion.getPort(),
                componentRegion.getLink(),
                componentRegion.getDns()
        );
    }


}
