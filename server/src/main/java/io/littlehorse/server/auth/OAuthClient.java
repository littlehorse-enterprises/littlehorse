package io.littlehorse.server.auth;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import io.littlehorse.server.auth.TokenStatus.TokenStatusBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.URIUtil;

// https://connect2id.com/products/nimbus-oauth-openid-connect-sdk/examples/oauth/token-introspection
// https://www.nimbusds.com/products/nimbus-oauth-openid-connect-sdk/guides/java-cookbook-for-openid-connect-public-clients
// https://www.oauth.com/oauth2-servers/token-introspection-endpoint/

@Slf4j
public class OAuthClient {

    private final OIDCProviderMetadata providerMetadata;
    private final OAuthConfig config;

    public OAuthClient(OAuthConfig config) {
        this.config = config;
        this.providerMetadata = getProviderMetadata(config.getAuthorizationServer());
    }

    private OIDCProviderMetadata getProviderMetadata(URI path) {
        try {
            URI fullPath = URIUtil.addPath(path, "/.well-known/openid-configuration");
            HttpRequest request = HttpRequest.newBuilder(fullPath).build();
            HttpResponse<String> response = HttpClient
                .newHttpClient()
                .send(request, BodyHandlers.ofString());

            return OIDCProviderMetadata.parse(response.body());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UnexpectedAuthorizationServerException(e);
        }
    }

    public TokenStatus introspect(String token) {
        try {
            TokenIntrospectionRequest request = new TokenIntrospectionRequest(
                providerMetadata.getIntrospectionEndpointURI(),
                getCredentials(),
                new BearerAccessToken(token)
            );
            TokenIntrospectionResponse response = TokenIntrospectionResponse.parse(
                request.toHTTPRequest().send()
            );

            if (!response.indicatesSuccess()) {
                throw new UnexpectedAuthorizationServerException(
                    "Error getting the token status: " +
                    response.toErrorResponse().getErrorObject()
                );
            }

            TokenIntrospectionSuccessResponse successResponse = response.toSuccessResponse();

            TokenStatusBuilder statusBuilder = TokenStatus
                .builder()
                .active(successResponse.isActive());

            if (successResponse.getExpirationTime() != null) {
                statusBuilder.exp(successResponse.getExpirationTime().toInstant());
            }

            return statusBuilder.build();
        } catch (ParseException | IOException e) {
            log.error(e.getMessage(), e);
            throw new UnexpectedAuthorizationServerException(e);
        }
    }

    private ClientAuthentication getCredentials() {
        return new ClientSecretBasic(
            new ClientID(config.getClientId()),
            new Secret(config.getClientSecret())
        );
    }
}
