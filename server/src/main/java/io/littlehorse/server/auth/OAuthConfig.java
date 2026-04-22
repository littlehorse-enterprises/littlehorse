package io.littlehorse.server.auth;

import java.net.URI;
import java.util.Objects;

public class OAuthConfig {
    private final String clientId;
    private final String clientSecret;
    private final URI introspectionEndpointURI;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthConfig that = (OAuthConfig) o;
        return Objects.equals(clientId, that.clientId)
                && Objects.equals(clientSecret, that.clientSecret)
                && Objects.equals(introspectionEndpointURI, that.introspectionEndpointURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, clientSecret, introspectionEndpointURI);
    }

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

        @java.lang.Override
        public java.lang.String toString() {
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
}
