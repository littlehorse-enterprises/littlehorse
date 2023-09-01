package io.littlehorse.sdk.common.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@EqualsAndHashCode
public class TokenStatus {

    private String token;
    private Instant expiration;
    private String clientId;

    public TokenStatus(@NonNull String token, @NonNull Instant expiration, String clientId) {
        this.token = token;
        this.expiration = expiration.truncatedTo(ChronoUnit.SECONDS);
        this.clientId = clientId;
    }

    public boolean isExpired() {
        return expiration.isBefore(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1));
    }
}
