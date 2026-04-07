package com.example.demo.adapter.vehicle.outbound.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@Table (name = "vehicles")
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int year;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    public VehicleEntity(Long id, String brand, String model, int year, String licensePlate) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
    }
}

