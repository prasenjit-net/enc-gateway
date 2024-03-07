package net.prasenjit.poc.encgateway.model;

public record RouteConfig(String id, String[] paths, String destPath, String uri, String[] pciReqPaths, String[] pciRespPaths) {
    public RouteConfig {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("ID cannot be empty");
        if (paths == null || paths.length == 0)
            throw new IllegalArgumentException("Paths cannot be empty");
        if (uri == null || uri.isBlank())
            throw new IllegalArgumentException("URI cannot be empty");
        if (pciReqPaths == null)
            pciReqPaths = new String[0];
        if (pciRespPaths == null)
            pciRespPaths = new String[0];
    }
}
