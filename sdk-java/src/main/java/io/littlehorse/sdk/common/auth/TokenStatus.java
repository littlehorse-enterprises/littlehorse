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
    private String userName;

    public TokenStatus(@NonNull String token, Instant expiration, String clientId, String userName) {
        this.token = token;

        if (expiration == null) {
            expiration = Instant.MIN;
        }
        this.expiration = expiration.truncatedTo(ChronoUnit.SECONDS);
        this.clientId = clientId;
        this.userName = userName;
    }

    public boolean isValid() {
        if (clientId == null) {
            return false;
        }

        return expiration.isAfter(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
