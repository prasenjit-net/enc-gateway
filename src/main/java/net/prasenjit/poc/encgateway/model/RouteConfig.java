package net.prasenjit.poc.encgateway.model;

public record RouteConfig(String[] paths, String destPath, String uri, String[] pciReqPaths, String[] pciRespPaths) {
}
