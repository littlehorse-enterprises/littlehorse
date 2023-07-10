package io.littlehorse.sdk.common.config;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.littlehorse.sdk.common.auth.OAuthClient;
import io.littlehorse.sdk.common.auth.OAuthConfig;
import io.littlehorse.sdk.common.auth.OAuthCredentialsProvider;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to configure the LHClient class.
 */
@Slf4j
public class LHClientConfig extends ConfigBase {

    /**
     * The bootstrap host for the LH Server.
     */
    public static final String API_HOST_KEY = "LHC_API_HOST";

    /**
     * The bootstrap port for the LH Server.
     */
    public static final String API_PORT_KEY = "LHC_API_PORT";

    /**
     * The Client Id.
     */
    public static final String CLIENT_ID_KEY = "LHC_CLIENT_ID";

    /**
     * Optional location of Client Cert file.
     */
    public static final String CLIENT_CERT_KEY = "LHC_CLIENT_CERT";

    /**
     * Optional location of Client Private Key File.
     */
    public static final String CLIENT_KEY_KEY = "LHC_CLIENT_KEY";

    /**
     * Optional location of CA Cert File.
     */
    public static final String CA_CERT_KEY = "LHC_CA_CERT";

    public static final String OAUTH_CLIENT_ID_KEY = "LHC_OAUTH_CLIENT_ID";
    public static final String OAUTH_CLIENT_SECRET_KEY = "LHC_OAUTH_CLIENT_SECRET";
    public static final String OAUTH_AUTHORIZATION_SERVER_KEY =
        "LHC_OAUTH_AUTHORIZATION_SERVER";

    private static final Set<String> configNames = Collections.unmodifiableSet(
        Set.of(
            LHClientConfig.API_HOST_KEY,
            LHClientConfig.API_PORT_KEY,
            LHClientConfig.CLIENT_ID_KEY,
            LHClientConfig.CLIENT_CERT_KEY,
            LHClientConfig.CLIENT_KEY_KEY,
            LHClientConfig.CA_CERT_KEY,
            LHClientConfig.OAUTH_AUTHORIZATION_SERVER_KEY,
            LHClientConfig.OAUTH_CLIENT_ID_KEY,
            LHClientConfig.OAUTH_CLIENT_SECRET_KEY
        )
    );

    /**
     * Returns a set of all config names.
     * @return A Set of all config names.
     */
    public static Set<String> configNames() {
        return LHClientConfig.configNames;
    }

    private Map<String, Channel> createdChannels;

    private OAuthClient oauthClient;
    private OAuthConfig oauthConfig;
    private OAuthCredentialsProvider oauthCredentialsProvider;

    /**
     * Creates an LHClientConfig. Loads default values for config from env vars.
     */
    public LHClientConfig() {
        super();
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with provided config values.
     * @param props configuration values.
     */
    public LHClientConfig(Properties props) {
        super(props);
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with config props in a specified .properties file.
     * @param propLocation the location of the .properties file.
     */
    public LHClientConfig(String propLocation) {
        super(propLocation);
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with provided config values.
     * @param configs configuration values.
     */
    public LHClientConfig(Map<String, Object> configs) {
        super(configs);
        createdChannels = new HashMap<>();
    }

    /**
     * Gets a Blocking gRPC stub for the LH Public API on the configured host/port,
     * which is generally the loadbalancer url.
     * @return a blocking gRPC stub for the configured host/port.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiBlockingStub getBlockingStub() throws IOException {
        return getBlockingStub(getApiBootstrapHost(), getApiBootstrapPort());
    }

    /**
     * Gets an Async gRPC stub for the LH Public API on the configured host/port,
     * which is generally the loadbalancer url.
     * @return an async gRPC stub for the configured host/port.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiStub getAsyncStub() throws IOException {
        return getAsyncStub(getApiBootstrapHost(), getApiBootstrapPort());
    }

    /**
     * Gets an Async gRPC stub for the LH Public API on a specified host and port.
     * Generally used by the Task Worker, which needs to connect directly to a
     * specific LH Server rather than the bootstrap host (loadbalancer).
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @return an async gRPC stub for that host/port combo.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiStub getAsyncStub(String host, int port) throws IOException {
        if (isOauth()) {
            return LHPublicApiGrpc
                .newStub(getChannel(host, port))
                .withCallCredentials(oauthCredentialsProvider);
        }
        return LHPublicApiGrpc.newStub(getChannel(host, port));
    }

    /**
     * Gets a Blocking gRPC stub for the LH Public API on a specified host and port.
     * Generally used by the Task Worker, which needs to connect directly to a
     * specific LH Server rather than the bootstrap host (loadbalancer).
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @return a blocking gRPC stub for that host/port combo.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiBlockingStub getBlockingStub(String host, int port)
        throws IOException {
        if (isOauth()) {
            return LHPublicApiGrpc
                .newBlockingStub(getChannel(host, port))
                .withCallCredentials(oauthCredentialsProvider);
        }
        return LHPublicApiGrpc.newBlockingStub(getChannel(host, port));
    }

    /**
     * Returns a gRPC channel for the specified host/port combination.
     * @param host The host to connect to.
     * @param port the port to connect to.
     * @return a gRPC channel for that specified host/port combo.
     * @throws IOException if we can't connect.
     */
    private Channel getChannel(String host, int port) throws IOException {
        String hostKey = host + ":" + port;
        if (createdChannels.containsKey(hostKey)) {
            return createdChannels.get(hostKey);
        }

        Channel out;

        String caCertFile = getOrSetDefault(CA_CERT_KEY, null);
        String clientCertFile = getOrSetDefault(CLIENT_CERT_KEY, null);
        String clientKeyFile = getOrSetDefault(CLIENT_KEY_KEY, null);

        if (caCertFile == null) {
            log.warn("Using insecure channel!");
            out = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        } else {
            log.info("Using secure connection!");
            TlsChannelCredentials.Builder tlsBuilder = TlsChannelCredentials
                .newBuilder()
                .trustManager(new File(caCertFile));

            if (clientCertFile != null && clientKeyFile != null) {
                log.info("Using mtls!");
                tlsBuilder.keyManager(
                    new File(clientCertFile),
                    new File(clientKeyFile)
                );
            }

            out =
                Grpc
                    .newChannelBuilderForAddress(host, port, tlsBuilder.build())
                    .build();
        }

        createdChannels.put(hostKey, out);
        return out;
    }

    public String getApiBootstrapHost() {
        return getOrSetDefault(API_HOST_KEY, "localhost");
    }

    public int getApiBootstrapPort() {
        return Integer.valueOf(getOrSetDefault(API_PORT_KEY, "5000"));
    }

    public String getClientId() {
        return getOrSetDefault(
            CLIENT_ID_KEY,
            "client-" + UUID.randomUUID().toString().replaceAll("-", "")
        );
    }

    public boolean isOauth() {
        String clientId = getOrSetDefault(OAUTH_CLIENT_ID_KEY, null);
        String clientSecret = getOrSetDefault(OAUTH_CLIENT_SECRET_KEY, null);
        String authServer = getOrSetDefault(OAUTH_AUTHORIZATION_SERVER_KEY, null);

        if (clientId == null && clientSecret == null && authServer == null) {
            log.info("OAuth is disable");
            return false;
        }

        log.info("OAuth is enable");

        if (oauthConfig == null) {
            oauthConfig =
                OAuthConfig
                    .builder()
                    .authorizationServer(URI.create(authServer))
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
        }

        if (oauthClient == null) {
            oauthClient = new OAuthClient(oauthConfig);
        }

        if (oauthCredentialsProvider == null) {
            oauthCredentialsProvider = new OAuthCredentialsProvider(oauthClient);
        }

        log.info("OAuth initialized");
        return true;
    }

    @Override
    protected String[] getEnvKeyPrefixes() {
        return new String[] { "LHC_" };
    }
}
