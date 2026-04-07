package com.example.demo.domain;

import java.util.Objects;

public class Region {
    private Long id;
    private String name;

    public Region(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Region(String name) {
        this(null, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }



    public void updateData(String name) {
        this.name = Objects.requireNonNull(name,"name cannot be null");
    }
}
