package com.example.demo.application.components.ports.out;

import com.example.demo.domain.Component;

import java.util.List;
import java.util.Optional;

public interface ComponentRepositoryPort {
    Component save(Component component);
    Optional<Component> findById(Long id);
    List<Component> findAll();
    void deleteById(Long id);
}
