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
import lombok.Builder;
import lombok.Getter;

// https://github.com/grpc/grpc-java/blob/master/examples/example-tls/README.md
// https://www.cloudflare.com/learning/access-management/what-is-mutual-tls/

@Getter
@Builder
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
}
