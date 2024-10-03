package io.littlehorse.server.auth;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.littlehorse.sdk.common.auth.TokenStatus;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Example:
 * https://github.com/grpc/grpc-java/blob/master/examples/example-oauth/src/main/java/io/grpc/examples/oauth/OAuth2ServerInterceptor.java
 */
@Slf4j
public class OAuthServerAuthenticator implements ServerAuthorizer {

    private static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final Cache<String, TokenStatus> tokenCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(4, TimeUnit.HOURS)
            .build();

    private final OAuthClient client;

    public OAuthServerAuthenticator(OAuthConfig config) {
        this.client = new OAuthClient(config);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        try {
            TokenStatus tokenStatus = validateToken(extractAccessToken(headers));
            updateHeaders(headers, tokenStatus);
        } catch (Exception e) {
            log.error("Error authorizing request", e);
            call.close(getStatusByException(e), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        return Contexts.interceptCall(Context.current(), call, headers, next);
    }

    private void updateHeaders(Metadata headers, TokenStatus tokenStatus) {
        // this shouldn't be possible
        if (tokenStatus == null) {
            throw new UnauthenticatedException("Token not found");
        }

        if (tokenStatus.isMachineClient()) {
            headers.put(CLIENT_ID, tokenStatus.getClientId());
        } else {
            headers.put(CLIENT_ID, tokenStatus.getUserName());
        }
    }

    private Status getStatusByException(Exception e) {
        if (e instanceof UnauthenticatedException) {
            return Status.UNAUTHENTICATED.withDescription(e.getMessage());
        } else {
            return Status.ABORTED.withDescription(e.getMessage());
        }
    }

    private String extractAccessToken(Metadata metadata) {
        return Optional.ofNullable(metadata.get(AUTHORIZATION_HEADER_KEY))
                .orElse("")
                .replace("Bearer", "")
                .trim();
    }

    private TokenStatus validateToken(String token) {
        if (Strings.isNullOrEmpty(token)) {
            throw new UnauthenticatedException("Token is empty");
        }

        TokenStatus tokenStatus = tokenCache.getIfPresent(token);

        if (tokenStatus == null) {
            tokenStatus = client.introspect(token);
            tokenCache.put(token, tokenStatus);
        }

        if (!tokenStatus.isValid()) {
            throw new UnauthenticatedException("Token is not active");
        }
        return tokenStatus;
    }
}
