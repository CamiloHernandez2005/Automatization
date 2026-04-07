package com.example.demo.application.components.ports.in;

import com.example.demo.domain.Component;

import java.util.Optional;

public interface GetComponentUseCase {
    Optional<Component> getComponent(Long id);
}
