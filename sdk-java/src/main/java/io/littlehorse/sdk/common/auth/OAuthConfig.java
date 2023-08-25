package io.littlehorse.sdk.common.auth;

import java.net.URI;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class OAuthConfig {

    final private String clientId;
    final private String clientSecret;
    final private URI authorizationServer;
}
