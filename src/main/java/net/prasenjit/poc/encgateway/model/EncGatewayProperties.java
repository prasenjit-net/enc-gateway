package net.prasenjit.poc.encgateway.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "gateway")
public record EncGatewayProperties(Map<String, RouteConfig> routes) {
}
