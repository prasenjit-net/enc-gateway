package net.prasenjit.poc.encgateway.controller;

import net.prasenjit.poc.encgateway.model.Person;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RelayController {

    @PostMapping("/post-map")
    public Person postMap(@RequestBody Person person) {
        return person;
    }
}
