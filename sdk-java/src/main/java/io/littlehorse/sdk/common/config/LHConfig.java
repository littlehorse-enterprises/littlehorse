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
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
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

/** This class is used to configure the LHClient class. */
@Slf4j
public class LHConfig extends ConfigBase {

    /** The bootstrap host for the LH Server. */
    public static final String API_HOST_KEY = "LHC_API_HOST";

    /** The bootstrap port for the LH Server. */
    public static final String API_PORT_KEY = "LHC_API_PORT";

    /** The bootstrap protocol for the LH Server. */
    public static final String API_PROTOCOL_KEY = "LHC_API_PROTOCOL";

    /** The Client Id. */
    public static final String CLIENT_ID_KEY = "LHC_CLIENT_ID";

    /** Optional location of Client Cert file. */
    public static final String CLIENT_CERT_KEY = "LHC_CLIENT_CERT";

    /** Optional location of Client Private Key File. */
    public static final String CLIENT_KEY_KEY = "LHC_CLIENT_KEY";

    /** Optional location of CA Cert File. */
    public static final String CA_CERT_KEY = "LHC_CA_CERT";

    public static final String OAUTH_CLIENT_ID_KEY = "LHC_OAUTH_CLIENT_ID";
    public static final String OAUTH_CLIENT_SECRET_KEY = "LHC_OAUTH_CLIENT_SECRET";
    public static final String OAUTH_ACCESS_TOKEN_URL = "LHC_OAUTH_ACCESS_TOKEN_URL";

    /** The number of worker threads to run. */
    public static final String NUM_WORKER_THREADS_KEY = "LHW_NUM_WORKER_THREADS";

    /** Listener to connect to. */
    public static final String SERVER_CONNECT_LISTENER_KEY = "LHW_SERVER_CONNECT_LISTENER";

    public static final String TASK_WORKER_VERSION_KEY = "LHW_TASK_WORKER_VERSION";
    public static final String DEFAULT_PUBLIC_LISTENER = "PLAIN";
    public static final String DEFAULT_PROTOCOL = "PLAINTEXT";

    private static final Set<String> configNames = Collections.unmodifiableSet(Set.of(
            LHConfig.API_HOST_KEY,
            LHConfig.API_PORT_KEY,
            LHConfig.API_PROTOCOL_KEY,
            LHConfig.CLIENT_ID_KEY,
            LHConfig.CLIENT_CERT_KEY,
            LHConfig.CLIENT_KEY_KEY,
            LHConfig.CA_CERT_KEY,
            LHConfig.OAUTH_ACCESS_TOKEN_URL,
            LHConfig.OAUTH_CLIENT_ID_KEY,
            LHConfig.OAUTH_CLIENT_SECRET_KEY,
            LHConfig.NUM_WORKER_THREADS_KEY,
            LHConfig.SERVER_CONNECT_LISTENER_KEY,
            LHConfig.TASK_WORKER_VERSION_KEY));

    /**
     * Returns a set of all config names.
     *
     * @return A Set of all config names.
     */
    public static Set<String> configNames() {
        return LHConfig.configNames;
    }

    private Map<String, Channel> createdChannels;

    private OAuthClient oauthClient;
    private OAuthConfig oauthConfig;
    private OAuthCredentialsProvider oauthCredentialsProvider;

    /** Creates an LHClientConfig. Loads default values for config from env vars. */
    public LHConfig() {
        super();
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with provided config values.
     *
     * @param props configuration values.
     */
    public LHConfig(Properties props) {
        super(props);
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with config props in a specified .properties file.
     *
     * @param propLocation the location of the .properties file.
     */
    public LHConfig(String propLocation) {
        super(propLocation);
        createdChannels = new HashMap<>();
    }

    /**
     * Creates an LHClientConfig with provided config values.
     *
     * @param configs configuration values.
     */
    public LHConfig(Map<String, Object> configs) {
        super(configs);
        createdChannels = new HashMap<>();
    }

    /**
     * Gets a Blocking gRPC stub for the LH Public API on the configured host/port, which is
     * generally the loadbalancer url.
     *
     * @return a blocking gRPC stub for the configured host/port.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiBlockingStub getBlockingStub() throws IOException {
        return getBlockingStub(getApiBootstrapHost(), getApiBootstrapPort());
    }

    /**
     * Gets an Async gRPC stub for the LH Public API on the configured host/port, which is generally
     * the loadbalancer url.
     *
     * @return an async gRPC stub for the configured host/port.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiStub getAsyncStub() throws IOException {
        return getAsyncStub(getApiBootstrapHost(), getApiBootstrapPort());
    }

    /**
     * Gets the `TaskDefPb` for a given taskDefName.
     *
     * @param taskDefName is the TaskDef's name.
     * @return the specified TaskDefPb.
     */
    public TaskDef getTaskDef(String taskDefName) throws IOException {
        return getBlockingStub()
                .getTaskDef(TaskDefId.newBuilder().setName(taskDefName).build());
    }

    /**
     * Returns the TaskWorker Version of this worker.
     *
     * @return Task Worker Version.
     */
    public String getTaskWorkerVersion() {
        return getOrSetDefault(TASK_WORKER_VERSION_KEY, "");
    }

    /**
     * Returns the name of the listener to connect to.
     *
     * @return the name of the listener on the LH Server to connect to.
     */
    public String getConnectListener() {
        return getOrSetDefault(SERVER_CONNECT_LISTENER_KEY, LHConfig.DEFAULT_PUBLIC_LISTENER);
    }

    /**
     * Gets an Async gRPC stub for the LH Public API on a specified host and port. Generally used by
     * the Task Worker, which needs to connect directly to a specific LH Server rather than the
     * bootstrap host (loadbalancer).
     *
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @return an async gRPC stub for that host/port combo.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiStub getAsyncStub(String host, int port) throws IOException {
        if (isOauth()) {
            return LHPublicApiGrpc.newStub(getChannel(host, port)).withCallCredentials(oauthCredentialsProvider);
        }
        return LHPublicApiGrpc.newStub(getChannel(host, port));
    }

    /**
     * Gets a Blocking gRPC stub for the LH Public API on a specified host and port. Generally used
     * by the Task Worker, which needs to connect directly to a specific LH Server rather than the
     * bootstrap host (loadbalancer).
     *
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @return a blocking gRPC stub for that host/port combo.
     * @throws IOException if stub creation fails.
     */
    public LHPublicApiBlockingStub getBlockingStub(String host, int port) throws IOException {
        if (isOauth()) {
            return LHPublicApiGrpc.newBlockingStub(getChannel(host, port))
                    .withCallCredentials(oauthCredentialsProvider);
        }
        return LHPublicApiGrpc.newBlockingStub(getChannel(host, port));
    }

    /**
     * Returns a gRPC channel for the specified host/port combination.
     *
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

        if (DEFAULT_PROTOCOL.equals(getApiProtocol())) {
            log.warn("Using insecure channel!");
            out = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        } else {
            log.info("Using secure connection!");
            TlsChannelCredentials.Builder tlsBuilder = TlsChannelCredentials.newBuilder();

            if (caCertFile != null) {
                tlsBuilder.trustManager(new File(caCertFile));
            }

            if (clientCertFile != null && clientKeyFile != null) {
                log.info("Using mtls!");
                tlsBuilder.keyManager(new File(clientCertFile), new File(clientKeyFile));
            }

            out = Grpc.newChannelBuilderForAddress(host, port, tlsBuilder.build())
                    .build();
        }

        createdChannels.put(hostKey, out);
        return out;
    }

    public String getApiBootstrapHost() {
        return getOrSetDefault(API_HOST_KEY, "localhost");
    }

    public int getApiBootstrapPort() {
        return Integer.valueOf(getOrSetDefault(API_PORT_KEY, "2023"));
    }

    public String getApiProtocol() {
        String protocol = getOrSetDefault(API_PROTOCOL_KEY, "PLAINTEXT");
        if (!protocol.equals("PLAINTEXT") && !protocol.equals("TLS")) {
            throw new IllegalArgumentException("Invalid protocol: " + protocol);
        }
        return protocol;
    }

    public String getClientId() {
        return getOrSetDefault(
                CLIENT_ID_KEY, "client-" + UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public boolean isOauth() {
        String clientId = getOrSetDefault(OAUTH_CLIENT_ID_KEY, null);
        String clientSecret = getOrSetDefault(OAUTH_CLIENT_SECRET_KEY, null);
        String tokenEndpointUrl = getOrSetDefault(OAUTH_ACCESS_TOKEN_URL, null);

        if (clientId == null && clientSecret == null && tokenEndpointUrl == null) {
            log.info("OAuth is disable");
            return false;
        }

        if (clientId == null || clientSecret == null || tokenEndpointUrl == null) {
            throw new IllegalArgumentException("OAuth Configuration is Missing");
        }

        log.info("OAuth is enable");

        if (oauthConfig == null) {
            oauthConfig = OAuthConfig.builder()
                    .tokenEndpointURI(URI.create(tokenEndpointUrl))
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
        return new String[] {"LHC_", "LHW_"};
    }

    /**
     * Returns the number of worker threads to run.
     *
     * @return the number of worker threads to run.
     */
    public int getWorkerThreads() {
        return Integer.valueOf(getOrSetDefault(NUM_WORKER_THREADS_KEY, "8"));
    }
}
