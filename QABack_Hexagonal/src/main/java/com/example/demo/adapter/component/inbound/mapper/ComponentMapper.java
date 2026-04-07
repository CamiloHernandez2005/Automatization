package com.example.demo.adapter.component.inbound.mapper;

import com.example.demo.adapter.component.inbound.dto.ComponentRequest;
import com.example.demo.adapter.component.inbound.dto.ComponentResponse;
import com.example.demo.adapter.component.outbound.entities.ComponentEntity;
import com.example.demo.domain.Component;

@org.springframework.stereotype.Component
public class ComponentMapper {

    // DTO Request -> Dominio
    public Component toDomain(ComponentRequest request){
        return new Component(
                request.name(),
                request.description()
        );
    }


    // Entidad -> Dominio
    public Component toDomain(ComponentEntity entity){
        return new Component(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }

    // Dominio -> Entidad
    public ComponentEntity toEntity(Component component){
        return ComponentEntity.builder()
                .id(component.getId())
                .name(component.getName())
                .description(component.getDescription())
                .build();
    }

    // Dominio -> DTO Response
    public ComponentResponse toResponse(Component component){
        return new ComponentResponse(
                component.getId(),
                component.getName(),
                component.getDescription()
        );
    }

}
