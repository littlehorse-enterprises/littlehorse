package io.littlehorse.server.listener;

import com.google.common.base.Objects;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerCredentials;
import io.grpc.TlsServerCredentials;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.auth.AuthorizationProtocol;
import io.littlehorse.server.auth.LHServerInterceptor;
import io.littlehorse.server.auth.authenticators.InsecureAuthenticator;
import io.littlehorse.server.auth.authenticators.MTLSAuthenticator;
import io.littlehorse.server.auth.authenticators.OAuthAuthenticator;
import java.io.IOException;
// https://github.com/grpc/grpc-java/blob/master/examples/example-tls/README.md
// https://www.cloudflare.com/learning/access-management/what-is-mutual-tls/

public class ServerListenerConfig {
    private String name;
    private int port;
    private ListenerProtocol protocol;
    private AuthorizationProtocol authorizationProtocol;
    private LHServerConfig config;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerListenerConfig that = (ServerListenerConfig) o;
        return (port == that.port
                && Objects.equal(name, that.name)
                && protocol == that.protocol
                && authorizationProtocol == that.authorizationProtocol);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, port, protocol, authorizationProtocol);
    }

    public ServerCredentials getCredentials() {
        try {
            return switch (protocol) {
                case TLS -> {
                    TLSConfig tlsConfig = config.getTLSConfiguration(name);
                    yield TlsServerCredentials.newBuilder()
                            .keyManager(tlsConfig.getCertChain(), tlsConfig.getPrivateKey())
                            .build();
                }
                case MTLS -> {
                    MTLSConfig mtlsConfig = config.getMTLSConfiguration(name);
                    yield TlsServerCredentials.newBuilder()
                            .keyManager(mtlsConfig.getCertChain(), mtlsConfig.getPrivateKey())
                            .trustManager(mtlsConfig.getCaCertificate())
                            .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
                            .build();
                }
                default -> InsecureServerCredentials.create();
            };
        } catch (IOException e) {
            throw new ServerListenerInitializationException(e);
        }
    }

    public LHServerInterceptor getRequestAuthenticator() {
        return switch (authorizationProtocol) {
            case OAUTH -> new OAuthAuthenticator(config.getOAuthConfig());
            case MTLS -> new MTLSAuthenticator();
            default -> InsecureAuthenticator.create();
        };
    }

    ServerListenerConfig(
            final String name,
            final int port,
            final ListenerProtocol protocol,
            final AuthorizationProtocol authorizationProtocol,
            final LHServerConfig config) {
        this.name = name;
        this.port = port;
        this.protocol = protocol;
        this.authorizationProtocol = authorizationProtocol;
        this.config = config;
    }

    public static class ServerListenerConfigBuilder {
        private String name;
        private int port;
        private ListenerProtocol protocol;
        private AuthorizationProtocol authorizationProtocol;
        private LHServerConfig config;

        ServerListenerConfigBuilder() {}

        /**
         * @return {@code this}.
         */
        public ServerListenerConfig.ServerListenerConfigBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ServerListenerConfig.ServerListenerConfigBuilder port(final int port) {
            this.port = port;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ServerListenerConfig.ServerListenerConfigBuilder protocol(final ListenerProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ServerListenerConfig.ServerListenerConfigBuilder authorizationProtocol(
                final AuthorizationProtocol authorizationProtocol) {
            this.authorizationProtocol = authorizationProtocol;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ServerListenerConfig.ServerListenerConfigBuilder config(final LHServerConfig config) {
            this.config = config;
            return this;
        }

        public ServerListenerConfig build() {
            return new ServerListenerConfig(
                    this.name, this.port, this.protocol, this.authorizationProtocol, this.config);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "ServerListenerConfig.ServerListenerConfigBuilder(name=" + this.name + ", port=" + this.port
                    + ", protocol=" + this.protocol + ", authorizationProtocol=" + this.authorizationProtocol
                    + ", config=" + this.config + ")";
        }
    }

    public static ServerListenerConfig.ServerListenerConfigBuilder builder() {
        return new ServerListenerConfig.ServerListenerConfigBuilder();
    }

    public String getName() {
        return this.name;
    }

    public int getPort() {
        return this.port;
    }

    public ListenerProtocol getProtocol() {
        return this.protocol;
    }

    public AuthorizationProtocol getAuthorizationProtocol() {
        return this.authorizationProtocol;
    }

    public LHServerConfig getConfig() {
        return this.config;
    }
}
