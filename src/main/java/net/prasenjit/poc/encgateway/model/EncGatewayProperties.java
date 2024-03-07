package net.prasenjit.poc.encgateway.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "gateway")
public record EncGatewayProperties(List<RouteConfig> routes) {
    public EncGatewayProperties {
        if (routes == null)
            routes = Collections.emptyList();
    }
}
