package com.example.demo.adapter.component.outbound.repository;

import com.example.demo.adapter.component.outbound.entities.ComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, Long> {
}
