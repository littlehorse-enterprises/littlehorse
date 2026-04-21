package io.littlehorse.server.auth;

import java.net.URI;

public class OAuthConfig {
    private final String clientId;
    private final String clientSecret;
    private final URI introspectionEndpointURI;

    OAuthConfig(final String clientId, final String clientSecret, final URI introspectionEndpointURI) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.introspectionEndpointURI = introspectionEndpointURI;
    }

    public static class OAuthConfigBuilder {
        private String clientId;
        private String clientSecret;
        private URI introspectionEndpointURI;

        OAuthConfigBuilder() {}

        /**
         * @return {@code this}.
         */
        public OAuthConfig.OAuthConfigBuilder clientId(final String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public OAuthConfig.OAuthConfigBuilder clientSecret(final String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public OAuthConfig.OAuthConfigBuilder introspectionEndpointURI(final URI introspectionEndpointURI) {
            this.introspectionEndpointURI = introspectionEndpointURI;
            return this;
        }

        public OAuthConfig build() {
            return new OAuthConfig(this.clientId, this.clientSecret, this.introspectionEndpointURI);
        }

        @Override
        public String toString() {
            return "OAuthConfig.OAuthConfigBuilder(clientId=" + this.clientId + ", clientSecret=" + this.clientSecret
                    + ", introspectionEndpointURI=" + this.introspectionEndpointURI + ")";
        }
    }

    public static OAuthConfig.OAuthConfigBuilder builder() {
        return new OAuthConfig.OAuthConfigBuilder();
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public URI getIntrospectionEndpointURI() {
        return this.introspectionEndpointURI;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof OAuthConfig)) return false;
        final OAuthConfig other = (OAuthConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$clientId = this.getClientId();
        final Object other$clientId = other.getClientId();
        if (this$clientId == null ? other$clientId != null : !this$clientId.equals(other$clientId)) return false;
        final Object this$clientSecret = this.getClientSecret();
        final Object other$clientSecret = other.getClientSecret();
        if (this$clientSecret == null ? other$clientSecret != null : !this$clientSecret.equals(other$clientSecret))
            return false;
        final Object this$introspectionEndpointURI = this.getIntrospectionEndpointURI();
        final Object other$introspectionEndpointURI = other.getIntrospectionEndpointURI();
        if (this$introspectionEndpointURI == null
                ? other$introspectionEndpointURI != null
                : !this$introspectionEndpointURI.equals(other$introspectionEndpointURI)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OAuthConfig;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $clientId = this.getClientId();
        result = result * PRIME + ($clientId == null ? 43 : $clientId.hashCode());
        final Object $clientSecret = this.getClientSecret();
        result = result * PRIME + ($clientSecret == null ? 43 : $clientSecret.hashCode());
        final Object $introspectionEndpointURI = this.getIntrospectionEndpointURI();
        result = result * PRIME + ($introspectionEndpointURI == null ? 43 : $introspectionEndpointURI.hashCode());
        return result;
    }
}
