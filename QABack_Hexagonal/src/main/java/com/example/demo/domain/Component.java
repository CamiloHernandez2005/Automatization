package com.example.demo.domain;

import java.util.Objects;

public class Component {
    private Long id;
    private String name;
    private String description;

    public Component(Long id, String name, String description) {
        this.id = id;
        this.name = Objects.requireNonNull(name , "name cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
    }

    public Component(String name, String description){
        this(null, name, description);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void updateData(String name, String description) {
        this.name = Objects.requireNonNull(name , "name cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
    }
}
