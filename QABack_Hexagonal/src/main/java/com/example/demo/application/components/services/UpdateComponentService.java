package com.example.demo.application.components.services;

import com.example.demo.application.components.ports.in.UpdateComponentUseCase;
import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import com.example.demo.domain.Component;
import org.springframework.stereotype.Service;

@Service
public class UpdateComponentService implements UpdateComponentUseCase {

    private final ComponentRepositoryPort componentRepositoryPort;

    public UpdateComponentService(ComponentRepositoryPort componentRepositoryPort) {
        this.componentRepositoryPort = componentRepositoryPort;
    }

    @Override
    public Component updateComponent(Long id, String name, String description) {
        Component existing = componentRepositoryPort.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Component with id " + id + " not found"
                        )
                );

        existing.updateData(name,description);

        return componentRepositoryPort.save(existing);
    }
}
