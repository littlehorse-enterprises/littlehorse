package io.littlehorse.sdk.common.config;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.CompositeCallCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.littlehorse.sdk.common.adapter.LHTypeAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.auth.OAuthClient;
import io.littlehorse.sdk.common.auth.OAuthConfig;
import io.littlehorse.sdk.common.auth.OAuthCredentialsProvider;
import io.littlehorse.sdk.common.auth.TenantMetadataProvider;
import io.littlehorse.sdk.common.config.retryinterceptor.ResourceExhaustedRetryInterceptor;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseFutureStub;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseStub;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TenantId;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/** This class is used to configure the LHClient class. */
@Slf4j
public class LHConfig extends ConfigBase {

    private static final ResourceExhaustedRetryInterceptor RESOURCE_EXHAUSTED_RETRY_INTERCEPTOR =
            new ResourceExhaustedRetryInterceptor();

    enum ConfigKeys {
        LHC_API_HOST,
        LHC_API_PORT,
        LHC_API_PROTOCOL,
        LHC_GRPC_RESOURCE_EXHAUSTED_RETRY,
        LHC_TENANT_ID,
        LHC_CLIENT_CERT,
        LHC_CLIENT_KEY,
        LHC_CA_CERT,
        LHC_OAUTH_CLIENT_ID,
        LHC_OAUTH_CLIENT_SECRET,
        LHC_OAUTH_ACCESS_TOKEN_URL,
        LHW_TASK_WORKER_ID,
        LHW_TASK_WORKER_VERSION,
        LHW_NUM_WORKER_THREADS,
        LHC_GRPC_KEEPALIVE_TIME_MS,
        LHC_GRPC_KEEPALIVE_TIMEOUT_MS,
        LHC_INFLIGHT_TASKS
    }

    /** The bootstrap host for the LH Server. */
    public static final String API_HOST_KEY = ConfigKeys.LHC_API_HOST.name();

    /** The bootstrap port for the LH Server. */
    public static final String API_PORT_KEY = ConfigKeys.LHC_API_PORT.name();

    /** The bootstrap protocol for the LH Server. */
    public static final String API_PROTOCOL_KEY = ConfigKeys.LHC_API_PROTOCOL.name();

    /** Enables transparent retries for RESOURCE_EXHAUSTED unary gRPC calls. */
    public static final String GRPC_RESOURCE_EXHAUSTED_RETRY_KEY = ConfigKeys.LHC_GRPC_RESOURCE_EXHAUSTED_RETRY.name();

    /** The Client Id. */
    public static final String TASK_WORKER_ID_KEY = ConfigKeys.LHW_TASK_WORKER_ID.name();

    /** Define a version for current worker instance for debugging purposes. */
    public static final String TASK_WORKER_VERSION_KEY = ConfigKeys.LHW_TASK_WORKER_VERSION.name();

    /** The Tenant Id for this client, null will be used if not set. */
    public static final String TENANT_ID_KEY = ConfigKeys.LHC_TENANT_ID.name();

    /** Optional location of Client Cert file. */
    public static final String CLIENT_CERT_KEY = ConfigKeys.LHC_CLIENT_CERT.name();

    /** Optional location of Client Private Key File. */
    public static final String CLIENT_KEY_KEY = ConfigKeys.LHC_CLIENT_KEY.name();

    /** Optional location of CA Cert File. */
    public static final String CA_CERT_KEY = ConfigKeys.LHC_CA_CERT.name();

    /** OAuth client id. */
    public static final String OAUTH_CLIENT_ID_KEY = ConfigKeys.LHC_OAUTH_CLIENT_ID.name();

    /** OAuth client secret. */
    public static final String OAUTH_CLIENT_SECRET_KEY = ConfigKeys.LHC_OAUTH_CLIENT_SECRET.name();

    /** OAuth access token url. */
    public static final String OAUTH_ACCESS_TOKEN_URL_KEY = ConfigKeys.LHC_OAUTH_ACCESS_TOKEN_URL.name();

    /** The number of worker threads to run. */
    public static final String NUM_WORKER_THREADS_KEY = ConfigKeys.LHW_NUM_WORKER_THREADS.name();

    /** GRPC Connection Keepalive Interval */
    public static final String GRPC_KEEPALIVE_TIME_MS_KEY = ConfigKeys.LHC_GRPC_KEEPALIVE_TIME_MS.name();

    /** GRPC Connection Keepalive Interval */
    public static final String GRPC_KEEPALIVE_TIMEOUT_MS_KEY = ConfigKeys.LHC_GRPC_KEEPALIVE_TIMEOUT_MS.name();

    /** Defines inflight request per polling thread */
    public static final String INFLIGHT_TASKS_KEY = ConfigKeys.LHC_INFLIGHT_TASKS.name();

    private static final String DEFAULT_PROTOCOL = "PLAINTEXT";

    /**
     * Returns a set of all config names.
     *
     * @return A Set of all config names.
     */
    public static Set<String> configNames() {
        return Arrays.stream(ConfigKeys.values()).map(Enum::name).collect(Collectors.toUnmodifiableSet());
    }

    private Map<String, Channel> createdChannels;

    private OAuthClient oauthClient;
    private OAuthConfig oauthConfig;
    private OAuthCredentialsProvider oauthCredentialsProvider;
    private final LHTypeAdapterRegistry typeAdapterRegistry;

    /** Creates an LHConfig. Loads default values for config from env vars. */
    public LHConfig() {
        super();
        createdChannels = new HashMap<>();
        typeAdapterRegistry = LHTypeAdapterRegistry.empty();
    }

    /**
     * Creates an LHConfig with provided config values.
     *
     * @param props configuration values.
     */
    public LHConfig(Properties props) {
        super(props);
        createdChannels = new HashMap<>();
        typeAdapterRegistry = LHTypeAdapterRegistry.empty();
    }

    /**
     * Creates an LHConfig with config props in a specified .properties file.
     *
     * @param propLocation the location of the .properties file.
     */
    public LHConfig(Path propLocation) {
        super(propLocation);
        createdChannels = new HashMap<>();
        typeAdapterRegistry = LHTypeAdapterRegistry.empty();
    }

    /**
     * Creates an LHConfig with config props in a specified .properties file.
     *
     * @param propLocation the location of the .properties file.
     */
    public LHConfig(String propLocation) {
        super(propLocation);
        createdChannels = new HashMap<>();
        typeAdapterRegistry = LHTypeAdapterRegistry.empty();
    }

    private LHConfig(ConfigSource configSource, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(configSource);
        createdChannels = new HashMap<>();
        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry, "Type adapter registry cannot be null");
    }

    /**
     * Creates a new builder for constructing an LHConfig.
     *
     * @return a new LHConfigBuilder
     */
    public static LHConfigBuilder newBuilder() {
        return new LHConfigBuilder();
    }

    /**
     * Fluent builder for composing configuration from multiple sources.
     */
    public static class LHConfigBuilder {

        private final ConfigSource configSource = ConfigSource.newSource();
        private Map<Class<?>, LHTypeAdapter<?>> typeAdaptersByClass = new LinkedHashMap<>();

        /** Default constructor for the builder. */
        public LHConfigBuilder() {}

        /**
         * Loads key/value pairs into this builder.
         *
         * @param map config values to load
         * @return this builder
         */
        public LHConfigBuilder loadFromMap(Map<?, ?> map) {
            configSource.loadFromMap(map);
            return this;
        }

        /**
         * Loads values from another ConfigSource.
         *
         * @param configSource source to load from
         * @return this builder
         */
        public LHConfigBuilder loadFromConfigSource(ConfigSource configSource) {
            configSource.loadFromConfigSource(configSource);
            return this;
        }

        /**
         * Loads Java properties into this builder.
         *
         * @param properties properties to load
         * @return this builder
         */
        public LHConfigBuilder loadFromProperties(Properties properties) {
            configSource.loadFromProperties(properties);
            return this;
        }

        /**
         * Loads properties from a file path.
         *
         * @param path path to a properties file
         * @return this builder
         */
        public LHConfigBuilder loadFromPropertiesFile(Path path) {
            configSource.loadFromPropertiesFile(path);
            return this;
        }

        /**
         * Loads properties from a file path string.
         *
         * @param path path to a properties file
         * @return this builder
         */
        public LHConfigBuilder loadFromPropertiesFile(String path) {
            configSource.loadFromPropertiesFile(path);
            return this;
        }

        /**
         * Loads properties from a file.
         *
         * @param file properties file
         * @return this builder
         */
        public LHConfigBuilder loadFromPropertiesFile(File file) {
            configSource.loadFromPropertiesFile(file);
            return this;
        }

        /**
         * Loads environment variables into this builder.
         *
         * @return this builder
         */
        public LHConfigBuilder loadFromEnvVariables() {
            configSource.loadFromEnvVariables();
            return this;
        }

        /**
         * Registers a type adapter to this config. Type adapters registered to the config will be used by
         * the SDK for type conversions anywhere that user defined classes can be found.
         *
         * Examples of where these Type Adapters may be used include TaskDef input and output variables and StructDef fields.
         *
         * @param <T> the custom Java type handled by this adapter
         * @param adapter the type adapter to register
         * @return this builder
         */
        public <T> LHConfigBuilder addTypeAdapter(LHTypeAdapter<T> adapter) {
            Objects.requireNonNull(adapter, "Type adapter cannot be null");
            if (typeAdaptersByClass.containsKey(adapter.getTypeClass())) {
                throw new IllegalArgumentException("A type adapter for "
                        + adapter.getTypeClass().getName() + " is already registered to this worker");
            }

            typeAdaptersByClass.put(adapter.getTypeClass(), adapter);

            return this;
        }

        /**
         * Builds a new LHConfig from all loaded sources.
         *
         * @return a new LHConfig instance
         */
        public LHConfig build() {
            return new LHConfig(configSource, LHTypeAdapterRegistry.from(new LinkedHashMap<>(typeAdaptersByClass)));
        }
    }

    /**
     * Creates an LHConfig with provided config values.
     *
     * @param configs configuration values.
     */
    public LHConfig(Map<String, Object> configs) {
        super(configs);
        createdChannels = new HashMap<>();
        typeAdapterRegistry = LHTypeAdapterRegistry.empty();
    }

    /**
     * Gets a Blocking gRPC stub for the LH Public API on the configured host/port, which is
     * generally the loadbalancer url.
     *
     * @return a blocking gRPC stub for the configured host/port.
     */
    public LittleHorseBlockingStub getBlockingStub() {
        return getBlockingStub(getApiBootstrapHost(), getApiBootstrapPort());
    }

    /**
     * Gets a Future gRPC stub for the LH Public API on the bootstrap host.
     * @return a future gRPC stub for that host/port combo.
     */
    public LittleHorseFutureStub getFutureStub() {
        return getFutureStub(getApiBootstrapHost(), getApiBootstrapPort());
    }

    /**
     * Gets an Async gRPC stub for the LH Public API on the configured host/port, which is generally
     * the loadbalancer url.
     *
     * @return an async gRPC stub for the configured host/port.
     */
    public LittleHorseStub getAsyncStub() {
        return getAsyncStub(getApiBootstrapHost(), getApiBootstrapPort());
    }

    /**
     * Gets the `TaskDefPb` for a given taskDefName.
     *
     * @param taskDefName is the TaskDef's name.
     * @return the specified TaskDefPb.
     */
    public TaskDef getTaskDef(String taskDefName) {
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
     * Gets an Async gRPC stub for the LH Public API on a specified host and port. Generally used by
     * the Task Worker, which needs to connect directly to a specific LH Server rather than the
     * bootstrap host (loadbalancer).
     *
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @return an async gRPC stub for that host/port combo.
     */
    public LittleHorseStub getAsyncStub(String host, int port) {
        return getBaseAsyncStub(host, port).withCallCredentials(getCredentials());
    }

    /**
     * Gets an Async gRPC stub for the LH Public API on a specified host and port. Generally used by
     * the Task Worker, which needs to connect directly to a specific LH Server rather than the
     * bootstrap host (loadbalancer).
     *
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @param tenantId is the current authenticated tenant.
     * @return an async gRPC stub for that host/port combo.
     */
    public LittleHorseStub getAsyncStub(String host, int port, TenantId tenantId) {
        return getBaseAsyncStub(host, port).withCallCredentials(getCredentials(tenantId));
    }

    /**
     * Gets a Blocking gRPC stub for the LH Public API on a specified host and port. Generally used
     * by the Task Worker, which needs to connect directly to a specific LH Server rather than the
     * bootstrap host (loadbalancer).
     *
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @return a blocking gRPC stub for that host/port combo.
     */
    public LittleHorseBlockingStub getBlockingStub(String host, int port) {
        return getBaseBlockingStub(host, port).withCallCredentials(getCredentials());
    }

    /**
     * Gets a Blocking gRPC stub for the LH Public API on a specified host and port. Generally used
     * by the Task Worker, which needs to connect directly to a specific LH Server rather than the
     * bootstrap host (loadbalancer).
     *
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @param tenantId is the current authenticated tenant.
     * @return a blocking gRPC stub for that host/port combo.
     */
    public LittleHorseBlockingStub getBlockingStub(String host, int port, TenantId tenantId) {
        return getBaseBlockingStub(host, port).withCallCredentials(getCredentials(tenantId));
    }

    /**
     * Gets a Future gRPC stub for the LH Public API on a specified host and port.
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @return a future gRPC stub for that host/port combo.
     */
    public LittleHorseFutureStub getFutureStub(String host, int port) {
        return getBaseFutureStub(host, port).withCallCredentials(getCredentials());
    }

    /**
     * Gets a Future gRPC stub for the LH Public API on a specified host and port.
     * @param host is the host that the LH Server lives on.
     * @param port is the port that the LH Server lives on.
     * @param tenantId is the current authenticated tenant.
     * @return a future gRPC stub for that host/port combo.
     */
    public LittleHorseFutureStub getFutureStub(String host, int port, TenantId tenantId) {
        return getBaseFutureStub(host, port).withCallCredentials(getCredentials(tenantId));
    }

    private CallCredentials getCredentials(TenantId tenantId) {
        if (isOauth()) {
            return new CompositeCallCredentials(oauthCredentialsProvider, new TenantMetadataProvider(tenantId));
        } else {
            return new TenantMetadataProvider(tenantId);
        }
    }

    private CallCredentials getCredentials() {
        return getCredentials(getTenantId());
    }

    /**
     * Returns a gRPC channel for the specified host/port combination.
     *
     * @param host The host to connect to.
     * @param port the port to connect to.
     * @return a gRPC channel for that specified host/port combo.
     */
    private Channel getChannel(String host, int port) {
        String hostKey = host + ":" + port;
        if (createdChannels.containsKey(hostKey)) {
            return createdChannels.get(hostKey);
        }

        ManagedChannelBuilder<?> builder;

        String caCertFile = getOrSetDefault(CA_CERT_KEY, null);
        String clientCertFile = getOrSetDefault(CLIENT_CERT_KEY, null);
        String clientKeyFile = getOrSetDefault(CLIENT_KEY_KEY, null);

        if (DEFAULT_PROTOCOL.equals(getApiProtocol())) {
            log.warn("Using insecure channel!");
            builder = ManagedChannelBuilder.forAddress(host, port).usePlaintext();
        } else {
            log.info("Using secure connection!");
            TlsChannelCredentials.Builder tlsBuilder = TlsChannelCredentials.newBuilder();

            if (caCertFile != null) {
                try {
                    tlsBuilder.trustManager(new File(caCertFile));
                } catch (IOException e) {
                    throw new LHMisconfigurationException("Error accessing to certificate", e);
                }
            }

            if (clientCertFile != null && clientKeyFile != null) {
                log.info("Using mtls!");
                try {
                    tlsBuilder.keyManager(new File(clientCertFile), new File(clientKeyFile));
                } catch (IOException e) {
                    throw new LHMisconfigurationException("Error accessing to certificate", e);
                }
            }

            builder = Grpc.newChannelBuilderForAddress(host, port, tlsBuilder.build());
        }
        builder = builder.keepAliveTime(getKeepaliveTimeMs(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(getKeepaliveTimeoutMs(), TimeUnit.MILLISECONDS)
                .keepAliveWithoutCalls(true);

        Channel out = builder.build();
        if (shouldRetryOnResourceExhausted()) {
            out = ClientInterceptors.intercept(out, RESOURCE_EXHAUSTED_RETRY_INTERCEPTOR);
        }
        createdChannels.put(hostKey, out);
        return out;
    }

    /**
     * Get a blocking stub with the application defaults
     */
    private LittleHorseBlockingStub getBaseBlockingStub(String host, int port) {
        return LittleHorseGrpc.newBlockingStub(getChannel(host, port));
    }

    private LittleHorseGrpc.LittleHorseFutureStub getBaseFutureStub(String host, int port) {
        return LittleHorseGrpc.newFutureStub(getChannel(host, port));
    }

    /**
     * Get a async stub with the application defaults
     */
    private LittleHorseStub getBaseAsyncStub(String host, int port) {
        return LittleHorseGrpc.newStub(getChannel(host, port));
    }

    /**
     * Returns the configured gRPC keepalive time in milliseconds.
     *
     * @return keepalive interval in milliseconds
     */
    public long getKeepaliveTimeMs() {
        return Long.valueOf(getOrSetDefault(GRPC_KEEPALIVE_TIME_MS_KEY, "45000"));
    }

    /**
     * Returns the configured gRPC keepalive timeout in milliseconds.
     *
     * @return keepalive timeout in milliseconds
     */
    public long getKeepaliveTimeoutMs() {
        return Long.valueOf(getOrSetDefault(GRPC_KEEPALIVE_TIMEOUT_MS_KEY, "5000"));
    }

    /**
     * Returns whether RESOURCE_EXHAUSTED unary gRPC calls should be transparently retried.
     *
     * @return true when the retry interceptor is enabled
     */
    public boolean shouldRetryOnResourceExhausted() {
        return Boolean.parseBoolean(getOrSetDefault(GRPC_RESOURCE_EXHAUSTED_RETRY_KEY, "true"));
    }

    /**
     * Returns the configured bootstrap API host.
     *
     * @return bootstrap host
     */
    public String getApiBootstrapHost() {
        return getOrSetDefault(API_HOST_KEY, "localhost");
    }

    /**
     * Returns the configured bootstrap API port.
     *
     * @return bootstrap port
     */
    public int getApiBootstrapPort() {
        return Integer.valueOf(getOrSetDefault(API_PORT_KEY, "2023"));
    }

    /**
     * Returns the configured API protocol.
     *
     * @return API protocol value
     */
    public String getApiProtocol() {
        String protocol = getOrSetDefault(API_PROTOCOL_KEY, DEFAULT_PROTOCOL);
        if (!protocol.equals(DEFAULT_PROTOCOL) && !protocol.equals("TLS")) {
            throw new IllegalArgumentException("Invalid protocol: " + protocol);
        }
        return protocol;
    }

    /**
     * Returns the configured task worker id.
     *
     * @return task worker id
     */
    public String getTaskWorkerId() {
        return getOrSetDefault(
                TASK_WORKER_ID_KEY, "worker-" + UUID.randomUUID().toString().replaceAll("-", ""));
    }

    /**
     * Returns the configured tenant id.
     *
     * @return tenant id used for API calls
     */
    public TenantId getTenantId() {
        String tenantId = getOrSetDefault(TENANT_ID_KEY, "default");
        return TenantId.newBuilder().setId(tenantId).build();
    }

    /**
     * Returns the configured number of in-flight tasks per polling thread.
     *
     * @return in-flight task count
     */
    public Integer getInflightTasks() {
        return Integer.valueOf(getOrSetDefault(INFLIGHT_TASKS_KEY, "1"));
    }

    /**
     * Returns whether OAuth is configured and initializes OAuth helpers when enabled.
     *
     * @return true when OAuth config is complete; false otherwise
     */
    public boolean isOauth() {
        String clientId = getOrSetDefault(OAUTH_CLIENT_ID_KEY, null);
        String clientSecret = getOrSetDefault(OAUTH_CLIENT_SECRET_KEY, null);
        String tokenEndpointUrl = getOrSetDefault(OAUTH_ACCESS_TOKEN_URL_KEY, null);

        if (clientId == null && clientSecret == null && tokenEndpointUrl == null) {
            log.debug("OAuth is disable");
            return false;
        }

        if (clientId == null || clientSecret == null || tokenEndpointUrl == null) {
            throw new IllegalArgumentException("OAuth Configuration is Missing");
        }

        log.debug("OAuth is enable");

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
        return Integer.valueOf(getOrSetDefault(NUM_WORKER_THREADS_KEY, "2"));
    }

    /**
     * Returns an immutable registry view of all currently registered type adapters.
     *
     * <p>This accessor is primarily intended for SDK internal use and wiring between SDK
     * components.
     *
     * @return a type adapter registry for the current configuration
     */
    public LHTypeAdapterRegistry getTypeAdapterRegistry() {
        return typeAdapterRegistry;
    }
}
