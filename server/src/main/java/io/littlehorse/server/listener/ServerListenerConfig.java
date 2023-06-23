package io.littlehorse.server.listener;

import com.google.common.base.Objects;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerCredentials;
import io.grpc.TlsServerCredentials;
import io.littlehorse.common.LHConfig;
import io.littlehorse.server.auth.AuthorizationProtocol;
import io.littlehorse.server.auth.InsecureServerAuthorizer;
import io.littlehorse.server.auth.OAuthServerAuthorizer;
import io.littlehorse.server.auth.ServerAuthorizer;
import java.io.IOException;
import lombok.Builder;
import lombok.Getter;

// read https://github.com/grpc/grpc-java/blob/master/examples/example-tls/README.md

@Getter
@Builder
public class ServerListenerConfig {

    private String name;
    private String host;
    private int port;
    private ListenerProtocol protocol;
    private AuthorizationProtocol authorizationProtocol;
    private LHConfig config;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerListenerConfig that = (ServerListenerConfig) o;
        return (
            port == that.port &&
            Objects.equal(name, that.name) &&
            Objects.equal(host, that.host) &&
            protocol == that.protocol &&
            authorizationProtocol == that.authorizationProtocol
        );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, host, port, protocol, authorizationProtocol);
    }

    public ServerCredentials getCredentials() {
        try {
            return switch (protocol) {
                case TLS -> {
                    TlsConfig tlsConfig = config.getTlsConfigByAdvertisedListenerName(
                        name
                    );
                    yield TlsServerCredentials
                        .newBuilder()
                        .keyManager(tlsConfig.getCert(), tlsConfig.getKey())
                        .build();
                }
                case MTLS -> {
                    TlsConfig mtlsConfig = config.getTlsConfigByAdvertisedListenerName(
                        name
                    );
                    yield TlsServerCredentials
                        .newBuilder()
                        .keyManager(mtlsConfig.getCert(), mtlsConfig.getKey())
                        .trustManager(mtlsConfig.getCaCert())
                        .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
                        .build();
                }
                default -> InsecureServerCredentials.create();
            };
        } catch (IOException e) {
            throw new ServerListenerInitializationException(e);
        }
    }

    public ServerAuthorizer getAuthorizer() {
        return switch (authorizationProtocol) {
            case OAUTH -> new OAuthServerAuthorizer(
                config.getOAuthConfigByAdvertisedListenerName(name)
            );
            default -> InsecureServerAuthorizer.create();
        };
    }
}
