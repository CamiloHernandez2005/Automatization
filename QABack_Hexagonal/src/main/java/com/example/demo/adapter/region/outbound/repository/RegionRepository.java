package com.example.demo.adapter.region.outbound.repository;

import com.example.demo.adapter.region.outbound.entities.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<RegionEntity, Long>
{
}
