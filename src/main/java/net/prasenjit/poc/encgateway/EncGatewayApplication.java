package net.prasenjit.poc.encgateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Function;

@SpringBootApplication
public class EncGatewayApplication {

    private static final String zip_path = "address.zip";

    public static void main(String[] args) {
        SpringApplication.run(EncGatewayApplication.class, args);
    }

    private static Function<PredicateSpec, Buildable<Route>> loopRoute() {
        return p -> p
                .path("/pm").and().method("POST")
                .filters(f -> f
                        .setPath("/post-map")
                        .modifyRequestBody(JsonNode.class, JsonNode.class, handleRequestBody())
                        .modifyResponseBody(JsonNode.class, JsonNode.class, handleResponseBody())
                ).uri("http://localhost:8080/");
    }

    private static RewriteFunction<JsonNode, JsonNode> handleRequestBody() {
        return (exchange, s) -> {
            System.out.println("Request: " + s);
            String[] tokens = StringUtils.tokenizeToStringArray(zip_path, ".", true, true);
            replaceValue(s, tokens, old -> "12345");
            return Mono.just(s);
        };
    }

    /**
     * recursively fins the JsonNode and replace the value.
     * Considering the Object type and array time node.
     * Multiple result is possible especially for Array type node.
     *
     * @param node
     * @param tokens
     * @param replace
     */
    private static void replaceValue(JsonNode node, String[] tokens, Function<String, String> replace) {
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode child : arrayNode) {
                replaceValue(child, tokens, replace);
            }
        } else if (tokens.length == 1) {
            if (node instanceof ObjectNode objectNode) {
                if (objectNode.hasNonNull(tokens[0])) {
                    objectNode.put(tokens[0], replace.apply(objectNode.get(tokens[0]).asText()));
                }
            }
        } else {
            String token = tokens[0];
            if (node instanceof ObjectNode objectNode) {
                String[] nextTokens = Arrays.copyOfRange(tokens, 1, tokens.length);
                JsonNode child = objectNode.get(token);
                replaceValue(child, nextTokens, replace);
            }
        }
    }

    static RewriteFunction<JsonNode, JsonNode> handleResponseBody() {
        return (exchange, s) -> {
            System.out.println("Response: " + s);
            String[] tokens = StringUtils.tokenizeToStringArray(zip_path, ".", true, true);
            replaceValue(s, tokens, old -> "60001");
            return Mono.just(s);
        };
    }

    @Bean
    RouteLocator myRoutes(RouteLocatorBuilder builder) {
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
