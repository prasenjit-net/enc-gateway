package net.prasenjit.poc.encgateway.controller;

import lombok.extern.log4j.Log4j2;
import net.prasenjit.poc.encgateway.model.PciString;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@Log4j2
@RestController
public class ReplaceController {
    @PostMapping("/encode")
    public PciString encode(@RequestBody PciString pciString) {
        log.info("Encoding {}", pciString);
        log.info("Thread name is {}", Thread.currentThread().getName());
        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(pciString.value().getBytes());
        return new PciString(encoded);
    }

    @PostMapping("/decode")
    public PciString decode(@RequestBody PciString pciString) {
        log.info("Decoding {}", pciString);
        log.info("Thread name is {}", Thread.currentThread().getName());
        String encoded = new String(Base64.getUrlDecoder().decode(pciString.value()));
        return new PciString(encoded);
    }
}
