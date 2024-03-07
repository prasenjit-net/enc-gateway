package net.prasenjit.poc.encgateway.model;

public record RouteConfig(String id, String[] paths, String destPath, String uri, String[] pciReqPaths, String[] pciRespPaths) {
}
