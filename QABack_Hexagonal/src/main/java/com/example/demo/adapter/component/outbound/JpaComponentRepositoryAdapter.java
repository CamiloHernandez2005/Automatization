package com.example.demo.adapter.component.outbound;
import com.example.demo.adapter.component.inbound.mapper.ComponentMapper;
import com.example.demo.adapter.component.outbound.entities.ComponentEntity;
import com.example.demo.adapter.component.outbound.repository.ComponentRepository;
import com.example.demo.application.components.ports.out.ComponentRepositoryPort;
import com.example.demo.domain.Component;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Component
public class JpaComponentRepositoryAdapter implements ComponentRepositoryPort {

    private final ComponentRepository componentRepository;
    private final ComponentMapper componentMapper;

    public JpaComponentRepositoryAdapter(ComponentRepository componentRepository, ComponentMapper componentMapper) {
        this.componentRepository = componentRepository;
        this.componentMapper = componentMapper;
    }

    @Override
    public Component save(Component component) {
        ComponentEntity entity = componentMapper.toEntity(component);
        ComponentEntity saved = componentRepository.save(entity);
        return componentMapper.toDomain(saved);
    }

    @Override
    public Optional<Component> findById(Long id) {
        return componentRepository.findById(id)
                .map(componentMapper::toDomain);
    }

    @Override
    public List<Component> findAll() {
        return componentRepository.findAll().stream()
                .map(componentMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        componentRepository.deleteById(id);
    }
}
