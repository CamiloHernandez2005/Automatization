package com.example.demo.application.components.services;

import com.example.demo.application.components.ports.in.GetAllComponentUseCase;
import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import com.example.demo.domain.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllComponentService implements GetAllComponentUseCase {

    private final ComponentRepositoryPort componentRepositoryPort;

    public GetAllComponentService(ComponentRepositoryPort componentRepositoryPort) {
        this.componentRepositoryPort = componentRepositoryPort;
    }

    @Override
    public List<Component> getAllComponents() {
        return componentRepositoryPort.findAll();
    }
}
