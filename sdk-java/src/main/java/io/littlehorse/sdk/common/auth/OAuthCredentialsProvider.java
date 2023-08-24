package io.littlehorse.sdk.common.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuthCredentialsProvider extends CallCredentials {

    private static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    private final OAuthClient oauthClient;
    private TokenStatus currentToken;

    public OAuthCredentialsProvider(OAuthClient oauthClient) {
        this.oauthClient = oauthClient;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        executor.execute(() -> {
            try {
                if (currentToken == null || currentToken.isExpired()) {
                    log.debug("Token expired, requesting a new one");
                    currentToken = oauthClient.getAccessToken();
                } else {
                    log.debug("Using cached token");
                }

                Metadata headers = new Metadata();
                headers.put(AUTHORIZATION_HEADER_KEY, String.format("Bearer %s", currentToken.getToken()));
                metadataApplier.apply(headers);
            } catch (Exception e) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }
}
