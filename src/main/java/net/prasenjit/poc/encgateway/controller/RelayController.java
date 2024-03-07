package net.prasenjit.poc.encgateway.controller;

import lombok.extern.log4j.Log4j2;
import net.prasenjit.poc.encgateway.model.Person;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class RelayController {

    @PostMapping("/post-map")
    public Person postMap(@RequestBody Person person) {
        log.info("Thread name is {}", Thread.currentThread().getName());
        return person;
    }
}
