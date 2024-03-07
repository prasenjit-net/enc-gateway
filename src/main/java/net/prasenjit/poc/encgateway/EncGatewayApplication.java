package net.prasenjit.poc.encgateway;

import net.prasenjit.poc.encgateway.model.EncGatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EncGatewayProperties.class)
public class EncGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EncGatewayApplication.class, args);
    }

}
