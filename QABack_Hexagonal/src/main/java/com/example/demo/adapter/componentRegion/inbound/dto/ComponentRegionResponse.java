package com.example.demo.adapter.componentRegion.inbound.dto;

public record ComponentRegionResponse(
        Long regionId,
        Long componentId,
        String port,
        String link,
        String dns
) {}
