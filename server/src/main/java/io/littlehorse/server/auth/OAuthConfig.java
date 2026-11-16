package io.littlehorse.server.auth;

import java.io.File;
import java.net.URI;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class OAuthConfig {

    private final String clientId;
    private final String clientSecret;
    private final URI introspectionEndpointURI;
    private final File caCert;
}
