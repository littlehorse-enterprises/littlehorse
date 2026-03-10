package io.littlehorse.sdk.common.auth;

import java.net.URI;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable OAuth client configuration.
 */
@Getter
@Builder
@EqualsAndHashCode
public class OAuthConfig {

    private final String clientId;
    private final String clientSecret;
    private final URI tokenEndpointURI;

    /**
     * Explicit all-args constructor used by builder and deserialization.
     *
     * @param clientId OAuth client id
     * @param clientSecret OAuth client secret
     * @param tokenEndpointURI OAuth token endpoint URI
     */
    public OAuthConfig(String clientId, String clientSecret, URI tokenEndpointURI) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpointURI = tokenEndpointURI;
    }
}
