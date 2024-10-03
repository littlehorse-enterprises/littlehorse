package io.littlehorse.server.auth;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import io.littlehorse.sdk.common.auth.TokenStatus;
import io.littlehorse.sdk.common.exception.EntityProviderException;
import java.io.IOException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * Examples:
 * https://connect2id.com/products/nimbus-oauth-openid-connect-sdk/examples/oauth/token-introspection
 * https://www.nimbusds.com/products/nimbus-oauth-openid-connect-sdk/guides/java-cookbook-for-openid-connect-public-clients
 * https://www.oauth.com/oauth2-servers/token-introspection-endpoint/
 */
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
                throw new EntityProviderException("Error getting the token status: "
                        + response.toErrorResponse().getErrorObject());
            }

            TokenIntrospectionSuccessResponse successResponse = response.toSuccessResponse();
            if (!successResponse.isActive()) {
                throw new UnauthenticatedException("Access token is not active");
            }

            String clientId = successResponse.getClientID() == null
                    ? null
                    : successResponse.getClientID().getValue();
            Instant expiration = successResponse.getExpirationTime() == null
                    ? null
                    : successResponse.getExpirationTime().toInstant();

            // This makes the assumption that our human users are using OIDC. However, that decision
            // appears to have been made and is a design decision rather than an implementation detail,
            // so I think that this is safe.
            Scope scope = successResponse.getScope();

            if (scope == null) {
                throw new UnauthenticatedException("Invalid token, scope was not provided");
            }

            boolean isMachineClient = !scope.contains(OIDCScopeValue.OPENID);

            return TokenStatus.builder()
                    .clientId(clientId)
                    .token(token)
                    .userName(successResponse.getUsername())
                    .expiration(expiration)
                    .isMachineClient(isMachineClient)
                    .build();
        } catch (ParseException | IOException e) {
            log.error(e.getMessage(), e);
            throw new EntityProviderException(e);
        }
    }
}
