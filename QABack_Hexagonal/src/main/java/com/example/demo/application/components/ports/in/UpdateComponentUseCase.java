package com.example.demo.application.components.ports.in;

import com.example.demo.domain.Component;

public interface UpdateComponentUseCase {
    Component updateComponent(Long id, String name, String description);
}
