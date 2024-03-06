package net.prasenjit.poc.encgateway.client;

import net.prasenjit.poc.encgateway.model.PciString;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface PciClient {

    @PostExchange("/encode")
    Mono<PciString> encode(@RequestBody PciString value);

    @PostExchange("/decode")
    Mono<PciString> decode(@RequestBody PciString value);
}
