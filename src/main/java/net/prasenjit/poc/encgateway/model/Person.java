package net.prasenjit.poc.encgateway.model;

import java.util.List;

public record Person(String name, List<Address> address) {
}
