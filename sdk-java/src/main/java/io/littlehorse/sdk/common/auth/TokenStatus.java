package io.littlehorse.sdk.common.auth;

import java.time.Instant;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class TokenStatus {

    private String token;
    private Instant expiration;

    public boolean isExpired() {
        if (expiration == null) {
            return true;
        }
        return expiration.isBefore(Instant.now());
    }

    @Override
    public String toString() {
        return String.format(
                "IntrospectResponse [expired=%s, expiration date=%s]", isExpired(), expiration);
    }
}
