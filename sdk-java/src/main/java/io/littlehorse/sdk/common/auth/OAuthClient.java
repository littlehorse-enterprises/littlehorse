package io.littlehorse.sdk.common.auth;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    public TokenStatus getAccessToken() {
        try {
            TokenRequest request =
                    new TokenRequest(config.getTokenEndpointURI(), credentials, new ClientCredentialsGrant());

            TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                throw new AuthorizationServerException("Error getting the token status: "
                        + response.toErrorResponse().getErrorObject());
            }

            AccessTokenResponse successResponse = response.toSuccessResponse();
            AccessToken accessToken = successResponse.getTokens().getAccessToken();
            Instant expiration = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(accessToken.getLifetime());

            return TokenStatus.builder()
                    .clientId(config.getClientId())
                    .token(accessToken.getValue())
                    .expiration(expiration)
                    .build();
        } catch (ParseException | IOException e) {
            log.error(e.getMessage(), e);
            throw new AuthorizationServerException(e);
        }
    }
}
