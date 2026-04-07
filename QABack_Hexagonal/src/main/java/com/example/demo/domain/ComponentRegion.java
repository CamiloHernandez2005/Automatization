package com.example.demo.domain;

public class ComponentRegion {

    private Long regionId;
    private Long componentId;
    private String port;
    private String link;
    private String dns;

    public ComponentRegion(Long regionId, Long componentId, String port, String link, String dns) {
        this.regionId = regionId;
        this.componentId = componentId;
        this.port = port;
        this.link = link;
        this.dns = dns;
    }

    public Long getRegionId() {
        return regionId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public String getPort() {
        return port;
    }

    public String getLink() {
        return link;
    }

    public String getDns() {
        return dns;
    }
}
