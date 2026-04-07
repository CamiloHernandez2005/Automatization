package com.example.demo.domain;

import java.util.Objects;

public class Vehicle {
    private Long id;
    private String brand;
    private String model;
    private int year;
    private String licensePlate;

    public Vehicle(Long id, String brand, String model, int year, String licensePlate) {
        this.id = id;
        this.brand = Objects.requireNonNull(brand,"brand cannot be null");
        this.model = Objects.requireNonNull(model, "model cannot be null");
        this.year = year;
        this.licensePlate = Objects.requireNonNull(licensePlate, "licensePlate cannot be null");
    }

    public Vehicle(String brand, String model, int year, String licensePlate) {
        this(null, brand, model, year, licensePlate);
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void updateData(String brand, String model, int year, String licensePlate) {
        this.brand= Objects.requireNonNull(brand, "brand cannot be null");
        this.model= Objects.requireNonNull(model, "model cannot be null");
        this.year= year;
        this.licensePlate= Objects.requireNonNull(licensePlate, "licensePlate cannot be null");
    }
}
