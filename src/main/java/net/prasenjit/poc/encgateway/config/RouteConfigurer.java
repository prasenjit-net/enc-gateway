package net.prasenjit.poc.encgateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import net.prasenjit.poc.encgateway.business.NodeOperations;
import net.prasenjit.poc.encgateway.model.EncGatewayProperties;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Log4j2
@Configuration
public class RouteConfigurer {
    private final NodeOperations nodeOperations;

    public RouteConfigurer(NodeOperations nodeOperations) {
        this.nodeOperations = nodeOperations;
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, EncGatewayProperties properties) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        properties.routes().forEach(config -> routes.route(config.id(), r -> r
                .path(config.paths())
                .filters(f -> f
                        .setPath(config.destPath())
                        .modifyRequestBody(JsonNode.class, JsonNode.class, handlePciOperation(config.pciReqPaths(), nodeOperations::encodeOperation))
                        .modifyResponseBody(JsonNode.class, JsonNode.class, handlePciOperation(config.pciRespPaths(), nodeOperations::decodeOperation))
                )
                .uri(config.uri())
        ));
        return routes.build();
    }

    private RewriteFunction<JsonNode, JsonNode> handlePciOperation(String[] pciPaths, Function<String, Mono<String>> pciOperation) {
        return (exchange, jsonNode) -> {
            log.info("Request: {}", jsonNode);
            log.info("Thread name is {}", Thread.currentThread().getName());
            return Flux.fromArray(pciPaths)
                    .flatMap(path -> nodeOperations.replaceValue(jsonNode,
                            StringUtils.tokenizeToStringArray(path, ".", true, true),
                            pciOperation))
                    .then(Mono.just(jsonNode));
        };
    }

}
