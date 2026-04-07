package com.example.demo.application.components.services;

import com.example.demo.application.components.ports.in.GetComponentUseCase;
import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import com.example.demo.domain.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetComponentService implements GetComponentUseCase {

    private final ComponentRepositoryPort componentRepositoryPort;

    public GetComponentService(ComponentRepositoryPort componentRepositoryPort) {
        this.componentRepositoryPort = componentRepositoryPort;
    }

    @Override
    public Optional<Component> getComponent(Long id) {
        return componentRepositoryPort.findById(id);
    }
}
