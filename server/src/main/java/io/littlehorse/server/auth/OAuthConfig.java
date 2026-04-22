package io.littlehorse.server.auth;

import java.net.URI;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthConfig {

    private final String clientId;
    private final String clientSecret;
    private final URI introspectionEndpointURI;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthConfig that = (OAuthConfig) o;
        return Objects.equals(clientId, that.clientId)
                && Objects.equals(clientSecret, that.clientSecret)
                && Objects.equals(introspectionEndpointURI, that.introspectionEndpointURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, clientSecret, introspectionEndpointURI);
    }
}
