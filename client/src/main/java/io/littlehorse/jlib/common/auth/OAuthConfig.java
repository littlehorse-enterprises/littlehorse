package io.littlehorse.jlib.common.auth;

import java.net.URI;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class OAuthConfig {

    private String clientId;
    private String clientSecret;
    private URI authorizationServer;
}
