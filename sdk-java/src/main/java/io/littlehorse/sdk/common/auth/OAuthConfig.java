package io.littlehorse.sdk.common.auth;

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
    private final URI authorizationServer;
}
