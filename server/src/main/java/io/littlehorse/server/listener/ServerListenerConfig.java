package io.littlehorse.server.listener;

import com.google.common.base.Objects;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerCredentials;
import io.grpc.TlsServerCredentials;
import io.littlehorse.common.LHConfig;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.server.auth.AuthorizationProtocol;
import io.littlehorse.server.auth.InsecureServerAuthorizer;
import io.littlehorse.server.auth.OAuthServerAuthorizer;
import io.littlehorse.server.auth.ServerAuthorizer;
import java.io.File;
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
    private File clientsCACert;
    private File certificate;
    private File certificateKey;
    private LHConfig config;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerListenerConfig that = (ServerListenerConfig) o;
        return (port == that.port
                && Objects.equal(name, that.name)
                && protocol == that.protocol
                && Objects.equal(clientsCACert, that.clientsCACert)
                && Objects.equal(certificate, that.certificate)
                && Objects.equal(certificateKey, that.certificateKey)
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
                    if (certificate == null || certificateKey == null) {
                        throw new LHMisconfigurationException("Invalid configuration: Listener " + name
                                + " was configured to use TLS but certificate and/or key are missing");
                    }
                    yield TlsServerCredentials.newBuilder()
                            .keyManager(certificate, certificateKey)
                            .build();
                }
                case MTLS -> {
                    if (certificate == null || certificateKey == null | clientsCACert == null) {
                        throw new LHMisconfigurationException("Invalid configuration: Listener " + name
                                + " was configured to use MTLS but certificate, key and/or client CA cert are missing");
                    }
                    yield TlsServerCredentials.newBuilder()
                            .keyManager(certificate, certificateKey)
                            .trustManager(clientsCACert)
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
            case OAUTH -> new OAuthServerAuthorizer(config.getOAuthConfigByListener(name));
            default -> InsecureServerAuthorizer.create();
        };
    }
}
