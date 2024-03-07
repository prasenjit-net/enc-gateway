package net.prasenjit.poc.encgateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import net.prasenjit.poc.encgateway.client.PciClient;
import net.prasenjit.poc.encgateway.model.EncGatewayProperties;
import net.prasenjit.poc.encgateway.model.PciString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Function;

@Log4j2
@Configuration
public class RouteConfig {

    @Autowired
    private PciClient pciClient;

    @Bean
    RouteLocator myRoutes(RouteLocatorBuilder builder, EncGatewayProperties properties) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        properties.routes().forEach(config -> routes.route(config.id(), r -> r
                .path(config.paths())
                .filters(f -> f
                        .setPath(config.destPath())
                        .modifyRequestBody(JsonNode.class, JsonNode.class, handlePciOperation(config.pciReqPaths(), this::encodeOperation))
                        .modifyResponseBody(JsonNode.class, JsonNode.class, handlePciOperation(config.pciRespPaths(), this::decodeOperation))
                )
                .uri(config.uri())
        ));
        return routes.build();
    }

    private Mono<String> decodeOperation(String value) {
        return pciClient.decode(new PciString(value)).map(PciString::value);
    }

    private Mono<String> encodeOperation(String value) {
        return pciClient.encode(new PciString(value)).map(PciString::value);
    }

    private RewriteFunction<JsonNode, JsonNode> handlePciOperation(String[] pciPaths, Function<String, Mono<String>> pciOperation) {
        return (exchange, jsonNode) -> {
            log.info("Request: {}", jsonNode);
            log.info("Thread name is {}", Thread.currentThread().getName());
            return Flux.fromArray(pciPaths)
                    .flatMap(path -> replaceValue(jsonNode,
                            StringUtils.tokenizeToStringArray(path, ".", true, true),
                            pciOperation))
                    .then(Mono.just(jsonNode));
        };
    }

    private Mono<Void> replaceValue(JsonNode node, String[] tokens, Function<String, Mono<String>> replace) {
        if (node instanceof ArrayNode arrayNode) {
            return Flux.fromIterable(arrayNode)
                    .flatMap(child -> replaceValue(child, tokens, replace))
                    .then();
        } else if (tokens.length == 1) {
            if (node instanceof ObjectNode objectNode) {
                if (objectNode.hasNonNull(tokens[0])) {
                    String text = objectNode.get(tokens[0]).asText();
                    return replace.apply(text)
                            .map(tr -> objectNode.put(tokens[0], tr))
                            .then();
                }
            }
            return Mono.empty();
        } else {
            String token = tokens[0];
            if (node instanceof ObjectNode objectNode) {
                String[] nextTokens = Arrays.copyOfRange(tokens, 1, tokens.length);
                JsonNode child = objectNode.get(token);
                return replaceValue(child, nextTokens, replace);
            } else {
                return Mono.empty();
            }
        }
    }
}
