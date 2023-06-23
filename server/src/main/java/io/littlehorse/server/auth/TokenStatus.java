package io.littlehorse.server.auth;

import java.time.Instant;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class TokenStatus {

    private boolean active;
    private Instant exp;

    public boolean isExpired() {
        if (exp == null) {
            return true;
        }
        return exp.isBefore(Instant.now());
    }

    @Override
    public String toString() {
        return String.format(
            "IntrospectResponse [active=%s, expired=%s, expiration date=%s]",
            isActive(),
            isExpired(),
            exp
        );
    }
}
