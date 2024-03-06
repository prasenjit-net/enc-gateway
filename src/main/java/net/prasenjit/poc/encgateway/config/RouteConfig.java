package net.prasenjit.poc.encgateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import net.prasenjit.poc.encgateway.client.PciClient;
import net.prasenjit.poc.encgateway.model.PciString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
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
    private static final String zip_path = "address.zip";

    @Autowired
    private PciClient pciClient;

    private Function<PredicateSpec, Buildable<Route>> loopRoute() {
        return p -> p
                .path("/pm").and().method("POST")
                .filters(f -> f
                        .setPath("/post-map")
                        .modifyRequestBody(JsonNode.class, JsonNode.class, handleRequestBody())
                        .modifyResponseBody(JsonNode.class, JsonNode.class, handleResponseBody())
                ).uri("http://localhost:8080/");
    }

    private RewriteFunction<JsonNode, JsonNode> handleRequestBody() {
        return (exchange, s) -> {
            log.info("Request: {}", s);
            String[] tokens = StringUtils.tokenizeToStringArray(zip_path, ".", true, true);
            return replaceValue(s, tokens, old -> pciClient.encode(new PciString(old)).map(PciString::value))
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

    private RewriteFunction<JsonNode, JsonNode> handleResponseBody() {
        return (exchange, s) -> {
            log.info("Response: {}", s);
            String[] tokens = StringUtils.tokenizeToStringArray(zip_path, ".", true, true);
            return replaceValue(s, tokens, old -> pciClient.decode(new PciString(old)).map(PciString::value))
                    .then(Mono.just(s));
        };
    }

    @Bean
    RouteLocator myRoutes(RouteLocatorBuilder builder, PciClient pciClient) {
        return builder.routes()
                .route("post", p -> p
                        .path("/post")
                        .and().method("POST")
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .modifyRequestBody(JsonNode.class, JsonNode.class, handleRequestBody())
                                .modifyResponseBody(JsonNode.class, JsonNode.class, handleResponseBody())
                        )
                        .uri("https://httpbin.org:80"))
                .route("loop", loopRoute())
                .build();
    }
}
