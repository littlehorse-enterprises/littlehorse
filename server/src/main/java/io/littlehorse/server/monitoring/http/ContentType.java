package io.littlehorse.server.monitoring.http;

/**
 * Enum representing common HTTP Content-Type headers used in HTTP responses.
 */
public enum ContentType {
    TEXT("text/plain"),
    JSON("application/json");

    /**
     * The MIME type string representation of the content type.
     */
    private final String type;

    ContentType(String type) {
        this.type = type;
    }

    /**
     * Returns the MIME type string for this content type.
     *
     * @return the MIME type string representation
     */
    public String getType() {
        return type;
    }
}
