package io.littlehorse.server.monitoring.http;

public enum ContentType {
    TEXT("text/plain"),
    JSON("application/json");

    private final String type;

    ContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
