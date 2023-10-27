package io.littlehorse.server.auth;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.littlehorse.sdk.common.auth.TokenStatus;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuthServerAuthorizer implements ServerAuthorizer {

    private static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final Cache<String, TokenStatus> tokenCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(4, TimeUnit.HOURS)
            .build();

    private final OAuthClient client;

    public OAuthServerAuthorizer(OAuthConfig config) {
        this.client = new OAuthClient(config);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String token = extractAccessToken(headers);

        try {
            validateToken(token);
        } catch (Exception e) {
            log.error("Error authorizing request", e);
            call.close(getStatusByException(e), headers);
        }

        return next.startCall(call, headers);
    }

    private Status getStatusByException(Exception e) {
        if (e instanceof PermissionDeniedException) {
            return Status.PERMISSION_DENIED.withDescription(e.getMessage());
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

    private void validateToken(String token) {
        if (Strings.isNullOrEmpty(token)) {
            throw new PermissionDeniedException("Token is empty");
        }

        TokenStatus tokenStatus = tokenCache.getIfPresent(token);

        if (tokenStatus == null) {
            tokenStatus = client.introspect(token);
            tokenCache.put(token, tokenStatus);
        }

        if (!tokenStatus.isValid()) {
            throw new PermissionDeniedException("Token is not active");
        }
    }
}
