package com.example.demo.adapter.componentRegion.outbound.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComponentRegionId implements Serializable {

    private Long regionId;
    private Long componentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComponentRegionId that)) return false;
        return Objects.equals(regionId, that.regionId) &&
                Objects.equals(componentId, that.componentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regionId, componentId);
    }
}