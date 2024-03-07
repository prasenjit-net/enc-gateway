package net.prasenjit.poc.encgateway.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gateway")
public record EncGatewayProperties(List<RouteConfig> routes) {
}
