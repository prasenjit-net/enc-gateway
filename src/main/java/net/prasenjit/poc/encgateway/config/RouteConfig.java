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
        properties.routes().forEach((k, v) -> {
            routes.route(k, r -> r
                    .path(v.paths())
                    .filters(f -> f
                            .setPath(v.destPath())
                            .modifyRequestBody(JsonNode.class, JsonNode.class, handleRequestBody(v.pciReqPaths()))
                            .modifyResponseBody(JsonNode.class, JsonNode.class, handleResponseBody(v.pciRespPaths()))
                    )
                    .uri(v.uri())
            );
        });
        return routes.build();
    }

    private RewriteFunction<JsonNode, JsonNode> handleRequestBody(String[] pciReqPaths) {
        return (exchange, s) -> {
            log.info("Request: {}", s);
            return Flux.fromArray(pciReqPaths)
                    .flatMap(path -> replaceValue(s,
                            StringUtils.tokenizeToStringArray(path, ".", true, true),
                            old -> pciClient.encode(new PciString(old)).map(PciString::value)))
                    .then(Mono.just(s));
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

    private RewriteFunction<JsonNode, JsonNode> handleResponseBody(String[] pciRespPaths) {
        return (exchange, s) -> {
            log.info("Response: {}", s);
            return Flux.fromArray(pciRespPaths)
                    .flatMap(path -> replaceValue(s,
                            StringUtils.tokenizeToStringArray(path, ".", true, true),
                            old -> pciClient.decode(new PciString(old)).map(PciString::value)))
                    .then(Mono.just(s));
        };
    }
}
