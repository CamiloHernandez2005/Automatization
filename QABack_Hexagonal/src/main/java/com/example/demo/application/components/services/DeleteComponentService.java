package com.example.demo.application.components.services;

import com.example.demo.application.components.ports.in.DeleteComponentUseCase;
import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class DeleteComponentService implements DeleteComponentUseCase {

    private final ComponentRepositoryPort componentRepositoryPort;

    public DeleteComponentService(ComponentRepositoryPort componentRepositoryPort) {
        this.componentRepositoryPort = componentRepositoryPort;
    }

    @Override
    public void deleteComponent(Long id) {
        componentRepositoryPort.deleteById(id);
    }
}
