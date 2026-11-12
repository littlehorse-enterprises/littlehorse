package io.littlehorse.sdk.common.auth;

import java.io.File;
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
    private final File caCert;

    /**
     * Creates an OAuth configuration that trusts only the JVM default CAs.
     *
     * @param clientId OAuth client id
     * @param clientSecret OAuth client secret
     * @param tokenEndpointURI OAuth token endpoint URI
     */
    public OAuthConfig(String clientId, String clientSecret, URI tokenEndpointURI) {
        this(clientId, clientSecret, tokenEndpointURI, null);
    }

    /**
     * Explicit all-args constructor used by builder and deserialization.
     *
     * @param clientId OAuth client id
     * @param clientSecret OAuth client secret
     * @param tokenEndpointURI OAuth token endpoint URI
     * @param caCert optional PEM file with CAs trusted for the token endpoint, besides the JVM default CAs
     */
    public OAuthConfig(String clientId, String clientSecret, URI tokenEndpointURI, File caCert) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpointURI = tokenEndpointURI;
        this.caCert = caCert;
    }
}
