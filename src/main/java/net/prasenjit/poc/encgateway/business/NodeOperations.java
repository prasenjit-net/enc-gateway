package net.prasenjit.poc.encgateway.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.prasenjit.poc.encgateway.client.PciClient;
import net.prasenjit.poc.encgateway.model.PciString;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Function;

@Component
public class NodeOperations {
    private final PciClient pciClient;

    public NodeOperations(PciClient pciClient) {
        this.pciClient = pciClient;
    }

    public Mono<Void> replaceValue(JsonNode node, String[] tokens, Function<String, Mono<String>> replace) {
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

    public Mono<String> decodeOperation(String value) {
        return pciClient.decode(new PciString(value)).map(PciString::value);
    }

    public Mono<String> encodeOperation(String value) {
        return pciClient.encode(new PciString(value)).map(PciString::value);
    }
}
