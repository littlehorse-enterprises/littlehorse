package io.littlehorse.sdk.common.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class TokenStatus {

    private String token;
    private Instant expiration;
    private String clientId;

    public boolean isExpired() {
        if (expiration == null) {
            return true;
        }
        return expiration.isBefore(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1));
    }

    @Override
    public String toString() {
        return String.format(
                "TokenStatus [client id=%s, expired=%s, expiration date=%s]", clientId, isExpired(), expiration);
    }
}
