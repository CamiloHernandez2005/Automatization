package com.example.demo.adapter.componentRegion.outbound.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "component_region")
public class ComponentRegionEntity {

    @EmbeddedId
    private ComponentRegionId id;

    @Column(nullable = false)
    private String port;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String dns;

}
