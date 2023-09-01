package io.littlehorse.server.auth;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import io.littlehorse.sdk.common.auth.AuthorizationServerException;
import io.littlehorse.sdk.common.auth.TokenStatus;
import java.io.IOException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

// https://connect2id.com/products/nimbus-oauth-openid-connect-sdk/examples/oauth/token-introspection
// https://www.nimbusds.com/products/nimbus-oauth-openid-connect-sdk/guides/java-cookbook-for-openid-connect-public-clients
// https://www.oauth.com/oauth2-servers/token-introspection-endpoint/

@Slf4j
public class OAuthClient {

    private final OAuthConfig config;
    private final ClientAuthentication credentials;

    public OAuthClient(OAuthConfig config) {
        this.config = config;
        this.credentials =
                new ClientSecretBasic(new ClientID(config.getClientId()), new Secret(config.getClientSecret()));
    }

    public TokenStatus introspect(String token) {
        try {
            TokenIntrospectionRequest request = new TokenIntrospectionRequest(
                    config.getIntrospectionEndpointURI(), credentials, new BearerAccessToken(token));

            TokenIntrospectionResponse response =
                    TokenIntrospectionResponse.parse(request.toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                throw new AuthorizationServerException("Error getting the token status: "
                        + response.toErrorResponse().getErrorObject());
            }

            TokenIntrospectionSuccessResponse successResponse = response.toSuccessResponse();
            if (!successResponse.isActive()) {
                log.warn("Received Access Token is Not Active");
            }

            String clientId = successResponse.getClientID() == null
                    ? null
                    : successResponse.getClientID().getValue();
            Instant expiration = successResponse.getExpirationTime() == null
                    ? null
                    : successResponse.getExpirationTime().toInstant();

            return TokenStatus.builder()
                    .clientId(clientId)
                    .token(token)
                    .expiration(expiration)
                    .build();
        } catch (ParseException | IOException e) {
            log.error(e.getMessage(), e);
            throw new AuthorizationServerException(e);
        }
    }
}
