package net.prasenjit.poc.encgateway.config;

import net.prasenjit.poc.encgateway.client.PciClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Bean
    public PciClient pciHttpClient() {
        WebClient webClient = WebClient.create("http://localhost:8080");
        HttpExchangeAdapter adaptor = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(adaptor)
                .build();
        return factory.createClient(PciClient.class);
    }
}
