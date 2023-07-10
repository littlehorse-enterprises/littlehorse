package io.littlehorse.jlib.common.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

public class TokenStatusTest {

    @Test
    void doesNotThrowExceptionWhenFormattingToString() {
        TokenStatus status = TokenStatus.builder().build();
        status.toString();
    }

    @Test
    void getExpiredWithNull() {
        TokenStatus status = TokenStatus.builder().build();
        assertTrue(status.isExpired());
    }

    @Test
    void getExpired() {
        TokenStatus status = TokenStatus.builder().expiration(Instant.MIN).build();
        assertTrue(status.isExpired());
    }

    @Test
    void getNotExpired() {
        TokenStatus status = TokenStatus.builder().expiration(Instant.MAX).build();
        assertFalse(status.isExpired());
    }
}
