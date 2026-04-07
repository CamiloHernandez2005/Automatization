package com.example.demo.application.components.services;

import com.example.demo.application.components.ports.in.CreateComponentUseCase;
import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import com.example.demo.domain.Component;
import org.springframework.stereotype.Service;

@Service
public class CreateComponentService implements CreateComponentUseCase {

    private final ComponentRepositoryPort componentRepositoryPort;

    public CreateComponentService(ComponentRepositoryPort componentRepositoryPort) {
        this.componentRepositoryPort = componentRepositoryPort;
    }

    @Override
    public Component createComponent(Component component) {
        return componentRepositoryPort.save(component);
    }
}
