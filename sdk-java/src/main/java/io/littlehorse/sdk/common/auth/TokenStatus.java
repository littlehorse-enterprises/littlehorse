package io.littlehorse.sdk.common.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Token metadata used to cache and validate OAuth access tokens.
 */
@Getter
@Builder
@EqualsAndHashCode
public class TokenStatus {

    private String token;
    private Instant expiration;
    private String clientId;
    private String userName;
    private boolean isMachineClient;

    /**
     * Creates a token status object.
     *
     * @param token access token value
     * @param expiration token expiration instant; null means immediately expired
     * @param clientId OAuth client id associated with the token
     * @param userName user name associated with the token when present
     * @param isMachineClient whether the token belongs to a machine client
     */
    public TokenStatus(
            @NonNull String token, Instant expiration, String clientId, String userName, boolean isMachineClient) {
        this.token = token;

        if (expiration == null) {
            expiration = Instant.MIN;
        }
        this.expiration = expiration.truncatedTo(ChronoUnit.SECONDS);
        this.clientId = clientId;
        this.userName = userName;
        this.isMachineClient = isMachineClient;
    }

    /**
     * Returns whether this token is currently valid for authenticated use.
     *
     * @return true when client id is present and expiration is in the future
     */
    public boolean isValid() {
        if (clientId == null) {
            return false;
        }

        return expiration.isAfter(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
