package io.littlehorse.common;

import com.google.common.base.Strings;
import io.grpc.ChannelCredentials;
import io.grpc.ServerCredentials;
import io.grpc.TlsChannelCredentials;
import io.grpc.TlsServerCredentials;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.RocksConfigSetter;
import io.littlehorse.sdk.common.config.ConfigBase;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.server.auth.AuthorizationProtocol;
import io.littlehorse.server.auth.OAuthConfig;
import io.littlehorse.server.listener.AdvertisedListenerConfig;
import io.littlehorse.server.listener.ListenerProtocol;
import io.littlehorse.server.listener.MTLSConfig;
import io.littlehorse.server.listener.ServerListenerConfig;
import io.littlehorse.server.listener.TLSConfig;
import io.littlehorse.server.streams.ServerTopology;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.rocksdb.Cache;
import org.rocksdb.LRUCache;
import org.rocksdb.RateLimiter;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteBufferManager;

@Slf4j
public class LHServerConfig extends ConfigBase {

    private String instanceName;

    // Kafka Global Configs
    public static final String KAFKA_BOOTSTRAP_KEY = "LHS_KAFKA_BOOTSTRAP_SERVERS";
    public static final String LHS_CLUSTER_ID_KEY = "LHS_CLUSTER_ID"; // determines application.id
    public static final String LHS_INSTANCE_ID_KEY = "LHS_INSTANCE_ID";
    public static final String REPLICATION_FACTOR_KEY = "LHS_REPLICATION_FACTOR";
    public static final String CLUSTER_PARTITIONS_KEY = "LHS_CLUSTER_PARTITIONS";
    public static final String OUTPUT_TOPIC_PARTITIONS_KEY = "LHS_OUTPUT_TOPIC_PARTITIONS";
    public static final String SHOULD_CREATE_TOPICS_KEY = "LHS_SHOULD_CREATE_TOPICS";
    public static final String RACK_ID_KEY = "LHS_RACK_ID";

    // Optional Performance-Related Configs for Kafka Streams
    public static final String CORE_STREAM_THREADS_KEY = "LHS_CORE_STREAM_THREADS";
    public static final String TIMER_STREAM_THREADS_KEY = "LHS_TIMER_STREAM_THREADS";
    public static final String CORE_STREAMS_COMMIT_INTERVAL_KEY = "LHS_CORE_STREAMS_COMMIT_INTERVAL";
    public static final String TIMER_STREAMS_COMMIT_INTERVAL_KEY = "LHS_TIMER_STREAMS_COMMIT_INTERVAL";
    public static final String CORE_MEMTABLE_SIZE_BYTES_KEY = "LHS_CORE_MEMTABLE_SIZE_BYTES";
    public static final String TIMER_MEMTABLE_SIZE_BYTES_KEY = "LHS_TIMER_MEMTABLE_SIZE_BYTES";
    public static final String CORE_STATESTORE_CACHE_BYTES_KEY = "LHS_CORE_STATESTORE_CACHE_BYTES";
    public static final String TIMER_STATESTORE_CACHE_BYTES_KEY = "LHS_TIMER_STATESTORE_CACHE_BYTES";
    public static final String ROCKSDB_TOTAL_BLOCK_CACHE_BYTES_KEY = "LHS_ROCKSDB_TOTAL_BLOCK_CACHE_BYTES";
    public static final String ROCKSDB_TOTAL_MEMTABLE_BYTES_KEY = "LHS_ROCKSDB_TOTAL_MEMTABLE_BYTES";
    public static final String ROCKSDB_USE_DIRECT_IO_KEY = "LHS_ROCKSDB_USE_DIRECT_IO";
    public static final String ROCKSDB_RATE_LIMIT_BYTES_KEY = "LHS_ROCKSDB_RATE_LIMIT_BYTES";
    public static final String SESSION_TIMEOUT_KEY = "LHS_STREAMS_SESSION_TIMEOUT";
    public static final String KAFKA_STATE_DIR_KEY = "LHS_STATE_DIR";
    public static final String NUM_WARMUP_REPLICAS_KEY = "LHS_STREAMS_NUM_WARMUP_REPLICAS";
    public static final String NUM_STANDBY_REPLICAS_KEY = "LHS_STREAMS_NUM_STANDBY_REPLICAS";
    public static final String ROCKSDB_COMPACTION_THREADS_KEY = "LHS_ROCKSDB_COMPACTION_THREADS";
    public static final String STREAMS_METRICS_LEVEL_KEY = "LHS_STREAMS_METRICS_LEVEL";
    public static final String LHS_METRICS_LEVEL_KEY = "LHS_METRICS_LEVEL";
    public static final String LINGER_MS_KEY = "LHS_KAFKA_LINGER_MS";
    public static final String TRANSACTION_TIMEOUT_MS_KEY = "LHS_STREAMS_TRANSACTION_TIMEOUT_MS";
    public static final String STATE_CLEANUP_DELAY_MS_KEY = "LHS_STREAMS_STATE_CLEANUP_DELAY_MS";
    public static final String CORE_KAFKA_STREAMS_OVERRIDE_PREFIX = "LHS_CORE_KS_CONFIG_";

    // General LittleHorse Runtime Behavior Config Env Vars
    public static final String INTERNAL_BIND_PORT_KEY = "LHS_INTERNAL_BIND_PORT";
    public static final String INTERNAL_ADVERTISED_HOST_KEY = "LHS_INTERNAL_ADVERTISED_HOST";
    public static final String INTERNAL_ADVERTISED_PORT_KEY = "LHS_INTERNAL_ADVERTISED_PORT";

    public static final String MAX_BULK_JOB_ITER_DURATION_MS = "LHS_MAX_BULK_JOB_ITER_DURATION_MS";
    public static final String BULK_JOB_DELAY_INTERVAL_SECONDS = "LHS_BULK_JOB_DELAY_INTERVAL_SECONDS";

    // MTLS for internal interactive query port
    public static final String INTERNAL_CA_CERT_KEY = "LHS_INTERNAL_CA_CERT";
    public static final String INTERNAL_SERVER_CERT_KEY = "LHS_INTERNAL_SERVER_CERT";
    public static final String INTERNAL_SERVER_KEY_KEY = "LHS_INTERNAL_SERVER_KEY";

    // Kafka authentication/security
    public static final String KAFKA_SECURITY_PROTOCOL_KEY = "LHS_KAFKA_SECURITY_PROTOCOL";
    public static final String KAFKA_TRUSTSTORE_KEY = "LHS_KAFKA_TRUSTSTORE";
    public static final String KAFKA_TRUSTSTORE_PASSWORD_KEY = "LHS_KAFKA_TRUSTSTORE_PASSWORD";
    public static final String KAFKA_TRUSTSTORE_PASSWORD_FILE_KEY = "LHS_KAFKA_TRUSTSTORE_PASSWORD_FILE";
    public static final String KAFKA_KEYSTORE_KEY = "LHS_KAFKA_KEYSTORE";
    public static final String KAFKA_KEYSTORE_PASSWORD_KEY = "LHS_KAFKA_KEYSTORE_PASSWORD";
    public static final String KAFKA_KEYSTORE_PASSWORD_FILE_KEY = "LHS_KAFKA_KEYSTORE_PASSWORD_FILE";
    public static final String KAFKA_SASL_MECHANISM_KEY = "LHS_KAFKA_SASL_MECHANISM";
    public static final String KAFKA_SASL_JAAS_CONFIG_KEY = "LHS_KAFKA_SASL_JAAS_CONFIG";
    public static final String KAFKA_SASL_JAAS_CONFIG_FILE_KEY = "LHS_KAFKA_SASL_JAAS_CONFIG_FILE";

    // PROMETHEUS
    public static final String HEALTH_SERVICE_PORT_KEY = "LHS_HEALTH_SERVICE_PORT";
    public static final String HEALTH_PATH_METRICS_KEY = "LHS_HEALTH_PATH_METRICS";
    public static final String HEALTH_PATH_LIVENESS_KEY = "LHS_HEALTH_PATH_LIVENESS";
    public static final String HEALTH_PATH_STATUS_KEY = "LHS_HEALTH_PATH_STATUS";
    public static final String HEALTH_PATH_DISK_USAGE_KEY = "LHS_HEALTH_PATH_DISK_USAGE";
    public static final String HEALTH_PATH_STANDBY_KEY = "HEALTH_PATH_STANDBY";

    // ADVERTISED LISTENERS
    public static final String ADVERTISED_LISTENERS_KEY = "LHS_ADVERTISED_LISTENERS";
    public static final String LISTENERS_KEY = "LHS_LISTENERS";
    public static final String LISTENERS_PROTOCOL_MAP_KEY = "LHS_LISTENERS_PROTOCOL_MAP";
    public static final String LISTENERS_AUTHENTICATION_MAP_KEY = "LHS_LISTENERS_AUTHENTICATION_MAP";
    public static final String CA_CERT = "LHS_CA_CERT";
    public static final String OAUTH_CLIENT_ID = "LHS_OAUTH_CLIENT_ID";
    public static final String OAUTH_CLIENT_SECRET = "LHS_OAUTH_CLIENT_SECRET";
    public static final String OAUTH_INTROSPECT_URL = "LHS_OAUTH_INTROSPECT_URL";
    public static final String OAUTH_CLIENT_ID_FILE = "LHS_OAUTH_CLIENT_ID_FILE";
    public static final String OAUTH_CLIENT_SECRET_FILE = "LHS_OAUTH_CLIENT_SECRET_FILE";
    private List<ServerListenerConfig> listenerConfigs;
    private List<AdvertisedListenerConfig> advertisedListenerConfigs;
    private Map<String, ListenerProtocol> listenersProtocolMap;
    private Map<String, AuthorizationProtocol> listenersAuthorizationMap;

    // EXPERIMENTAL Internal configs. Should not be used by real users; only for testing.
    public static final String ROCKSDB_USE_LEVEL_COMPACTION_KEY = "LHS_X_ROCKSDB_USE_LEVEL_COMPACTION";
    public static final String X_ENABLE_STRUCT_DEFS_KEY = "LHS_X_ENABLE_STRUCT_DEFS";

    // Instance configs
    private final String lhsMetricsLevel;

    // Singletons for RocksConfigSetter
    @Getter
    private Cache globalRocksdbBlockCache;

    @Getter
    private WriteBufferManager globalRocksdbWriteBufferManager;

    @Getter
    private RateLimiter globalRocksdbRateLimiter;

    public LHServerConfig() {
        super();
        initKafkaAdmin();
        initRocksdbSingletons();
        lhsMetricsLevel = getServerMetricLevel();
    }

    public LHServerConfig(String propertiesPath) {
        super(propertiesPath);
        initKafkaAdmin();
        initRocksdbSingletons();
        lhsMetricsLevel = getServerMetricLevel();
    }

    public LHServerConfig(Properties props) {
        super(props);
        initKafkaAdmin();
        initRocksdbSingletons();
        lhsMetricsLevel = getServerMetricLevel();
    }

    protected String[] getEnvKeyPrefixes() {
        return new String[] {"LHS_"};
    }

    private Admin kafkaAdmin;
    private LHProducer commandProducer;
    private LHProducer taskClaimProducer;

    public int getHotMetadataPartition() {
        return (Utils.toPositive(Utils.murmur2(LHConstants.META_PARTITION_KEY.getBytes())) % getClusterPartitions());
    }

    /*
     * Kafka Streams does not currently expose any way to control the prefix of
     * internal Streams topics. The prefixes are automatically set to the
     * application.id + "-".
     */
    public String getCoreCmdTopicName() {
        return getCoreCmdTopicName(getLHClusterId());
    }

    public static String getCoreCmdTopicName(String clusterId) {
        return clusterId + "-core-cmd";
    }

    public String getMetadataCmdTopicName() {
        return getMetadataCmdTopicName(getLHClusterId());
    }

    public static String getMetadataCmdTopicName(String clusterId) {
        return clusterId + "-metadata-cmd";
    }

    public String getRepartitionTopicName() {
        return getRepartitionTopicName(getLHClusterId());
    }

    public static String getRepartitionTopicName(String clusterId) {
        return clusterId + "-core-repartition";
    }

    public String getTimerTopic() {
        return getTimerTopic(getLHClusterId());
    }

    public static String getTimerTopic(String clusterId) {
        return clusterId + "-timers";
    }

    public static String getCoreStoreChangelogTopic(String clusterId) {
        return clusterId + "-core-" + ServerTopology.CORE_STORE + "-changelog";
    }

    public String getCoreStoreChangelogTopic() {
        return getCoreStoreChangelogTopic(getLHClusterId());
    }

    public static String getTimerStoreChangelogTopic(String clusterId) {
        return (clusterId + "-timer-" + ServerTopology.TIMER_STORE + "-changelog");
    }

    public static String getMetadataStoreChangelogTopic(String clusterId) {
        return (clusterId + "-core-" + ServerTopology.METADATA_STORE + "-changelog");
    }

    public String getTimerStoreChangelogTopic() {
        return getTimerStoreChangelogTopic(getLHClusterId());
    }

    public static String getRepartitionStoreChangelogTopic(String clusterId) {
        return (clusterId + "-core-" + ServerTopology.CORE_REPARTITION_STORE + "-changelog");
    }

    public String getRepartitionStoreChangelogTopic() {
        return getRepartitionStoreChangelogTopic(getLHClusterId());
    }

    public List<NewTopic> getAllTopics() {
        return getAllTopics(getLHClusterId(), getReplicationFactor(), partitionsByTopic());
    }

    public Map<String, Integer> partitionsByTopic() {
        Map<String, Integer> out = new HashMap<>();
        String clusterId = getLHClusterId();
        int clusterPartitions = getClusterPartitions();
        out.put(getCoreCmdTopicName(clusterId), clusterPartitions);
        out.put(getRepartitionTopicName(clusterId), clusterPartitions);
        out.put(getTimerTopic(clusterId), clusterPartitions);
        out.put(getCoreStoreChangelogTopic(clusterId), clusterPartitions);
        out.put(getRepartitionStoreChangelogTopic(clusterId), clusterPartitions);
        out.put(getTimerStoreChangelogTopic(clusterId), clusterPartitions);
        out.put(getMetadataStoreChangelogTopic(clusterId), 1); // global store
        out.put(getMetadataCmdTopicName(clusterId), 1); // global store
        return out;
    }

    // Internal topics are manually created because:
    // 1.- It makes it possible to manage/create topics using an external tool (e.g
    // terraform, strimzi, etc.)
    // 2.- It allows to explicitly manage the configuration of the topics
    // Note: Kafka streams doesn't support disabling automatic internal topic
    // creation. Thus, internal topics that are not explicitly created here will be
    // automatically created by Kafka Stream. Please make sure to manually create all internal topics.
    // Kafka has opened KIP-698 to solve this.
    public static List<NewTopic> getAllTopics(
            String clusterId, short replicationFactor, Map<String, Integer> partitionsByTopic) {
        HashMap<String, String> compactedTopicConfig = new HashMap<>() {
            {
                put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
            }
        };
        String coreCommandTopicName = getCoreCmdTopicName(clusterId);
        NewTopic coreCommand =
                new NewTopic(coreCommandTopicName, partitionsByTopic.get(coreCommandTopicName), replicationFactor);

        String repartitionTopicName = getRepartitionTopicName(clusterId);
        NewTopic repartition =
                new NewTopic(repartitionTopicName, partitionsByTopic.get(repartitionTopicName), replicationFactor);

        String timerTopicName = getTimerTopic(clusterId);
        NewTopic timer = new NewTopic(timerTopicName, partitionsByTopic.get(timerTopicName), replicationFactor);

        String coreChangelogTopicName = getCoreStoreChangelogTopic(clusterId);
        NewTopic coreStoreChangelog = new NewTopic(
                        coreChangelogTopicName, partitionsByTopic.get(coreChangelogTopicName), replicationFactor)
                .configs(compactedTopicConfig);

        String repartitionStoreChangelogTopicName = getRepartitionStoreChangelogTopic(clusterId);
        NewTopic repartitionStoreChangelog = new NewTopic(
                        repartitionStoreChangelogTopicName,
                        partitionsByTopic.get(repartitionStoreChangelogTopicName),
                        replicationFactor)
                .configs(compactedTopicConfig);

        String timerStoreChangelogTopicName = getTimerStoreChangelogTopic(clusterId);
        NewTopic timerStoreChangelog = new NewTopic(
                        timerStoreChangelogTopicName,
                        partitionsByTopic.get(timerStoreChangelogTopicName),
                        replicationFactor)
                .configs(compactedTopicConfig);

        String metadataStoreChangelogTopicName = getMetadataStoreChangelogTopic(clusterId);
        NewTopic metadataStoreChangelog = new NewTopic(
                        metadataStoreChangelogTopicName,
                        partitionsByTopic.get(metadataStoreChangelogTopicName),
                        replicationFactor)
                .configs(compactedTopicConfig);

        String metadataCommandTopicName = getMetadataCmdTopicName(clusterId);
        NewTopic metadataCommand = new NewTopic(
                        metadataCommandTopicName, partitionsByTopic.get(metadataCommandTopicName), replicationFactor)
                .configs(compactedTopicConfig);

        return List.of(
                coreCommand,
                metadataCommand,
                repartition,
                timer,
                coreStoreChangelog,
                repartitionStoreChangelog,
                timerStoreChangelog,
                metadataStoreChangelog);
    }

    public String getBootstrapServers() {
        return getOrSetDefault(LHServerConfig.KAFKA_BOOTSTRAP_KEY, "localhost:9092");
    }

    private short getReplicationFactor() {
        return Short.valueOf(String.class.cast(props.getOrDefault(LHServerConfig.REPLICATION_FACTOR_KEY, "1")));
    }

    public int getClusterPartitions() {
        return Integer.valueOf(String.class.cast(props.getOrDefault(LHServerConfig.CLUSTER_PARTITIONS_KEY, "12")));
    }

    public int getOutputToppicPartitions() {
        return Integer.valueOf(String.class.cast(props.getOrDefault(
                LHServerConfig.OUTPUT_TOPIC_PARTITIONS_KEY, String.valueOf(getClusterPartitions()))));
    }

    public static String getExecutionOutputTopicName(String clusterId, TenantIdModel tenantId) {
        return clusterId + "_" + tenantId.toString() + "_execution";
    }

    public static String getMetadataOutputTopicName(String clusterId, TenantIdModel tenantId) {
        return clusterId + "_" + tenantId.toString() + "_metadata";
    }

    public String getExecutionOutputTopicName(TenantIdModel tenant) {
        return getExecutionOutputTopicName(getLHClusterId(), tenant);
    }

    public String getMetadataOutputTopicName(TenantIdModel tenant) {
        return getMetadataOutputTopicName(getLHClusterId(), tenant);
    }

    public Pair<NewTopic, NewTopic> getOutputTopicsFor(TenantModel tenant) {
        String executionTopicName = getExecutionOutputTopicName(tenant.getId());
        String metadataTopicName = getMetadataOutputTopicName(tenant.getId());

        // metadata topic is compacted
        Map<String, String> compactedTopicConfig =
                Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);

        return Pair.of(
                new NewTopic(metadataTopicName, 1, getReplicationFactor()).configs(compactedTopicConfig),
                new NewTopic(executionTopicName, getClusterPartitions(), getReplicationFactor()));
    }

    public String getKafkaGroupId(String component) {
        return getLHClusterId() + "-" + component;
    }

    public String getLHClusterId() {
        return getOrSetDefault(LHServerConfig.LHS_CLUSTER_ID_KEY, "cluster1");
    }

    public Optional<Short> getLHInstanceId() {
        String instanceId = getOrSetDefault(LHS_INSTANCE_ID_KEY, null);
        if (instanceId == null) return Optional.empty();

        short ordinalVal = Short.valueOf(instanceId);
        if (ordinalVal < 0) {
            throw new LHMisconfigurationException("LHS_INSTANCE_ID cannot be negative");
        }
        return Optional.of(ordinalVal);
    }

    public String getLHInstanceName() {
        if (instanceName != null) return instanceName;

        instanceName = getLHInstanceId().isPresent()
                ? getLHInstanceId().get().toString()
                : UUID.randomUUID().toString();

        return instanceName;
    }

    public String getStateDirectory() {
        return getOrSetDefault(KAFKA_STATE_DIR_KEY, "/tmp/kafkaState");
    }

    public String getInternalAdvertisedHost() {
        return getOrSetDefault(LHServerConfig.INTERNAL_ADVERTISED_HOST_KEY, "localhost");
    }

    public String getServerMetricLevel() {
        if (lhsMetricsLevel != null) {
            return lhsMetricsLevel;
        }
        String metricLevel = getOrSetDefault(LHS_METRICS_LEVEL_KEY, "INFO").toUpperCase();
        List<String> allowedValues = List.of("INFO", "DEBUG", "TRACE");
        if (!allowedValues.contains(metricLevel)) {
            throw new LHMisconfigurationException("Unrecognized metric level: " + metricLevel);
        }
        return metricLevel.toUpperCase();
    }

    // If INTERNAL_ADVERTISED_PORT isn't set, we return INTERNAL_BIND_PORT.
    public int getInternalAdvertisedPort() {
        return Integer.valueOf(
                getOrSetDefault(LHServerConfig.INTERNAL_ADVERTISED_PORT_KEY, String.valueOf(getInternalBindPort())));
    }

    public int getHealthServicePort() {
        return Integer.parseInt(getOrSetDefault(HEALTH_SERVICE_PORT_KEY, "1822"));
    }

    public String getPrometheusExporterPath() {
        return getOrSetDefault(LHServerConfig.HEALTH_PATH_METRICS_KEY, "/metrics");
    }

    public String getLivenessPath() {
        return getOrSetDefault(LHServerConfig.HEALTH_PATH_LIVENESS_KEY, "/liveness");
    }

    public String getStatusPath() {
        return getOrSetDefault(LHServerConfig.HEALTH_PATH_STATUS_KEY, "/status");
    }

    public long getCoreStoreRateLimitBytes() {
        return Long.valueOf(getOrSetDefault(LHServerConfig.ROCKSDB_RATE_LIMIT_BYTES_KEY, "-1"));
    }

    public String getDiskUsagePath() {
        return getOrSetDefault(LHServerConfig.HEALTH_PATH_DISK_USAGE_KEY, "/diskUsage");
    }

    public String getStandbyStatusPath() {
        return getOrSetDefault(LHServerConfig.HEALTH_PATH_STANDBY_KEY, "/standby-status");
    }

    public int getInternalBindPort() {
        return Integer.parseInt(getOrSetDefault(LHServerConfig.INTERNAL_BIND_PORT_KEY, "2011"));
    }

    public OAuthConfig getOAuthConfig() {

        String clientId = getOrSetDefault(OAUTH_CLIENT_ID, null);
        String clientSecret = getOrSetDefault(OAUTH_CLIENT_SECRET, null);
        String introspectionEndpoint = getOrSetDefault(OAUTH_INTROSPECT_URL, null);

        String clientIdFile = getOrSetDefault(OAUTH_CLIENT_ID_FILE, null);
        String clientSecretFile = getOrSetDefault(OAUTH_CLIENT_SECRET_FILE, null);

        if (clientSecretFile != null) {
            log.info("Loading OAuth2 Client Secret from file");
            clientSecret = loadSettingFromFile(clientSecretFile);
        }

        if (clientIdFile != null) {
            log.info("Loading OAuth2 Client Id from file");
            clientId = loadSettingFromFile(clientIdFile);
        }

        if (clientId == null || clientSecret == null || introspectionEndpoint == null) {
            throw new LHMisconfigurationException(
                    "OAuth configuration called but not provided. Check missing client id, client secret or introspection endpoint url");
        }

        final URI parsedUrl;
        try {
            parsedUrl = URI.create(introspectionEndpoint);
        } catch (IllegalArgumentException e) {
            throw new LHMisconfigurationException("Malformed URL check " + OAUTH_INTROSPECT_URL);
        }

        return OAuthConfig.builder()
                .introspectionEndpointURI(parsedUrl)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    public MTLSConfig getMTLSConfiguration(String listenerName) {
        String keyConfigName = "LHS_LISTENER_" + listenerName + "_KEY";
        String certConfigName = "LHS_LISTENER_" + listenerName + "_CERT";

        File certChain = getFile(certConfigName);
        File privateKey = getFile(keyConfigName);
        File caCertificate = getFile(CA_CERT);

        if (certChain == null || privateKey == null || caCertificate == null) {
            throw new LHMisconfigurationException(
                    "Invalid configuration: Listener " + listenerName + " was configured to use MTLS but "
                            + keyConfigName + ", " + certConfigName + " and/or " + CA_CERT + " are missing");
        }

        return new MTLSConfig(caCertificate, certChain, privateKey);
    }

    public TLSConfig getTLSConfiguration(String listenerName) {
        String keyConfigName = "LHS_LISTENER_" + listenerName + "_KEY";
        String certConfigName = "LHS_LISTENER_" + listenerName + "_CERT";

        File certChain = getFile(certConfigName);
        File privateKey = getFile(keyConfigName);

        if (certChain == null || privateKey == null) {
            throw new LHMisconfigurationException("Invalid configuration: Listener " + listenerName
                    + " was configured to use TLS but " + keyConfigName + " and/or " + certConfigName + " are missing");
        }

        return new TLSConfig(certChain, privateKey);
    }

    @SuppressWarnings("null")
    private File getFile(String configName) {
        String fileLocation = getOrSetDefault(configName, null);

        if (Strings.isNullOrEmpty(fileLocation)) {
            return null;
        }

        File file = new File(fileLocation);

        if (!file.isFile()) {
            throw new LHMisconfigurationException(
                    "Invalid configuration: File location specified on " + configName + " is invalid");
        }

        return file;
    }

    @SuppressWarnings("null")
    public Map<String, AuthorizationProtocol> getListenersAuthorizationMap() {
        if (listenersAuthorizationMap != null) {
            return listenersAuthorizationMap;
        }

        String rawAuthProtocolMap = getOrSetDefault(LHServerConfig.LISTENERS_AUTHENTICATION_MAP_KEY, null);

        if (Strings.isNullOrEmpty(rawAuthProtocolMap)) {
            return listenersAuthorizationMap = Map.of();
        }

        String regexAllAuthProtocols =
                Arrays.stream(AuthorizationProtocol.values()).map(Enum::name).collect(Collectors.joining("|"));

        if (!rawAuthProtocolMap.matches("([a-zA-Z0-9_-]+:(" + regexAllAuthProtocols + ")+,?)+")) {
            throw new LHMisconfigurationException(
                    "Invalid configuration: " + LHServerConfig.LISTENERS_AUTHENTICATION_MAP_KEY);
        }

        List<String> rawAuthProtocols = Arrays.asList(rawAuthProtocolMap.split(","));

        return (listenersAuthorizationMap = rawAuthProtocols.stream()
                .map(protocolMap -> protocolMap.split(":"))
                .collect(
                        Collectors.toMap(strings -> strings[0], strings -> AuthorizationProtocol.valueOf(strings[1]))));
    }

    public Map<String, ListenerProtocol> getListenersProtocolMap() {
        if (listenersProtocolMap != null) {
            return listenersProtocolMap;
        }

        String rawProtocolMap = getOrSetDefault(LHServerConfig.LISTENERS_PROTOCOL_MAP_KEY, "PLAIN:PLAIN");

        String regexAllProtocols =
                Arrays.stream(ListenerProtocol.values()).map(Enum::name).collect(Collectors.joining("|"));

        if (!rawProtocolMap.matches("([a-zA-Z0-9_-]+:(" + regexAllProtocols + ")+,?)+")) {
            throw new LHMisconfigurationException(
                    "Invalid configuration: " + LHServerConfig.LISTENERS_PROTOCOL_MAP_KEY);
        }

        List<String> rawProtocols = Arrays.asList(rawProtocolMap.split(","));

        return (listenersProtocolMap = rawProtocols.stream()
                .map(protocolMap -> protocolMap.split(":"))
                .collect(Collectors.toMap(strings -> strings[0], strings -> ListenerProtocol.valueOf(strings[1]))));
    }

    public List<ServerListenerConfig> getListeners() {
        if (listenerConfigs != null) {
            return listenerConfigs;
        }

        String rawListenersConfig = getOrSetDefault(LHServerConfig.LISTENERS_KEY, "PLAIN:2023");
        Map<String, ListenerProtocol> protocolMap = getListenersProtocolMap();
        Map<String, AuthorizationProtocol> authMap = getListenersAuthorizationMap();

        if (!rawListenersConfig.matches("([a-zA-Z0-9_-]+:\\d+,?)+")) {
            throw new LHMisconfigurationException("Invalid configuration: " + LHServerConfig.LISTENERS_KEY);
        }

        List<String> rawListenersConfigs = Arrays.asList(rawListenersConfig.split(","));

        listenerConfigs = rawListenersConfigs.stream()
                .map(listener -> {
                    String[] split = listener.split(":");
                    String name = split[0].trim();
                    String port = split[1].trim();
                    ListenerProtocol protocol =
                            protocolMap.get(name) == null ? ListenerProtocol.PLAIN : protocolMap.get(name);
                    AuthorizationProtocol authProtocol =
                            authMap.get(name) == null ? AuthorizationProtocol.NONE : authMap.get(name);

                    if (authProtocol == AuthorizationProtocol.MTLS && protocol != ListenerProtocol.MTLS) {
                        throw new LHMisconfigurationException(
                                "Invalid configuration: Listener " + name
                                        + " LHS_LISTENERS_PROTOCOL_MAP has to be MTLS in order to support MTLS for authentication");
                    }

                    return ServerListenerConfig.builder()
                            .name(name)
                            .port(Integer.parseInt(port))
                            .protocol(protocol)
                            .authorizationProtocol(authProtocol)
                            .config(this)
                            .build();
                })
                .toList();

        int totalDifferentPorts = listenerConfigs.stream()
                .map(ServerListenerConfig::getPort)
                .collect(Collectors.toSet())
                .size();

        if (totalDifferentPorts != listenerConfigs.size()) {
            listenerConfigs = null;
            throw new LHMisconfigurationException(
                    "Invalid configuration: " + LHServerConfig.LISTENERS_KEY + ". Ports should be different");
        }

        return listenerConfigs;
    }

    public List<AdvertisedListenerConfig> getAdvertisedListeners() {
        if (advertisedListenerConfigs != null) {
            return advertisedListenerConfigs;
        }

        String rawListenersConfig = getOrSetDefault(LHServerConfig.ADVERTISED_LISTENERS_KEY, "PLAIN://localhost:2023");

        if (!rawListenersConfig.matches("([a-zA-Z0-9_-]+://[a-zA-Z0-9.\\-]+:\\d+,?)+")) {
            throw new LHMisconfigurationException("Invalid configuration: " + LHServerConfig.ADVERTISED_LISTENERS_KEY);
        }

        List<String> rawAdvertisedListenerConfigs = Arrays.asList(rawListenersConfig.split(","));

        advertisedListenerConfigs = rawAdvertisedListenerConfigs.stream()
                .map(listener -> {
                    String[] split = listener.split(":(//)?");
                    String name = split[0].trim();
                    String host = split[1].trim();
                    String port = split[2].trim();

                    return AdvertisedListenerConfig.builder()
                            .name(name)
                            .port(Integer.parseInt(port))
                            .host(host)
                            .build();
                })
                .toList();

        return advertisedListenerConfigs;
    }

    public void cleanup() {
        if (this.kafkaAdmin != null) this.kafkaAdmin.close();
        if (this.commandProducer != null) this.commandProducer.close();
        if (this.taskClaimProducer != null) this.taskClaimProducer.close();
    }

    public LHProducer getCommandProducer() {
        if (commandProducer == null) {
            commandProducer = new LHProducer(this.getKafkaProducerConfig(this.getLHInstanceName()));
        }
        return commandProducer;
    }

    public LHProducer getTaskClaimProducer() {
        if (taskClaimProducer == null) {
            taskClaimProducer = new LHProducer(this.getKafkaProducerConfig(this.getLHInstanceName()));
        }
        return taskClaimProducer;
    }

    public boolean shouldCreateTopics() {
        return Boolean.valueOf(getOrSetDefault(SHOULD_CREATE_TOPICS_KEY, "true"));
    }

    public int getRocksDBCompactionThreads() {
        return Integer.valueOf(getOrSetDefault(ROCKSDB_COMPACTION_THREADS_KEY, "1"));
    }

    public boolean getRocksDBUseLevelCompaction() {
        return Boolean.valueOf(getOrSetDefault(ROCKSDB_USE_LEVEL_COMPACTION_KEY, "false"));
    }

    public long getCoreMemtableSize() {
        // 64MB default
        return Long.valueOf(getOrSetDefault(CORE_MEMTABLE_SIZE_BYTES_KEY, String.valueOf(1024L * 1024L * 64)));
    }

    public boolean useDirectIOForRocksDB() {
        return Boolean.valueOf(getOrSetDefault(ROCKSDB_USE_DIRECT_IO_KEY, "false"));
    }

    // Timer Topology generally has smaller values that are written. The majority of them
    // are LHTimer's with short (i.e. 10-second) TTL's (i.e. TaskRun Timeout timers), so
    // we don't expect the timer memtable to overflow that quickly.
    public long getTimerMemtableSize() {
        // 32MB default
        return Long.valueOf(getOrSetDefault(TIMER_MEMTABLE_SIZE_BYTES_KEY, String.valueOf(1024L * 1024L * 32)));
    }

    public Properties getKafkaProducerConfig(String component) {
        Properties conf = new Properties();
        conf.put("client.id", this.getClientId(component));
        conf.put(CommonClientConfigs.METADATA_RECOVERY_STRATEGY_CONFIG, "rebootstrap");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
        props.put(ProducerConfig.COMPRESSION_ZSTD_LEVEL_CONFIG, 8);
        conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        conf.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        conf.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                Serdes.Bytes().serializer().getClass());
        conf.put(ProducerConfig.CLIENT_ID_CONFIG, getKafkaGroupId(component));
        conf.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        conf.put(ProducerConfig.ACKS_CONFIG, "all");
        conf.put(ProducerConfig.LINGER_MS_CONFIG, getOrSetDefault(LINGER_MS_KEY, "0"));
        addKafkaSecuritySettings(conf);
        return conf;
    }

    /*
     * Right now, this only supports mtls auth. We want to:
     * 1. Support other auth types, especially SCRAM-SHA-512
     * 2. Clean up the code, because currently it's just kludgey.
     *
     * For part 2), we maybe want to factor out the Kafka configuration to
     * its own class. Maybe. Or maybe not.
     *
     * Either way, this all should be configurable.
     */
    private void addKafkaSecuritySettings(Properties conf) {
        String securityProtocol = getOrSetDefault(KAFKA_SECURITY_PROTOCOL_KEY, "PLAINTEXT");

        String keystoreLoc = getOrSetDefault(KAFKA_KEYSTORE_KEY, null);
        String keystorePassword =
                getFromConfigOrFile(KAFKA_KEYSTORE_PASSWORD_KEY, KAFKA_KEYSTORE_PASSWORD_FILE_KEY, null);

        String truststoreLoc = getOrSetDefault(KAFKA_TRUSTSTORE_KEY, null);
        String truststorePassword =
                getFromConfigOrFile(KAFKA_TRUSTSTORE_PASSWORD_KEY, KAFKA_TRUSTSTORE_PASSWORD_FILE_KEY, null);

        String saslMechanism = getOrSetDefault(KAFKA_SASL_MECHANISM_KEY, null);
        String jaasConfig = getFromConfigOrFile(KAFKA_SASL_JAAS_CONFIG_KEY, KAFKA_SASL_JAAS_CONFIG_FILE_KEY, null);

        conf.put("security.protocol", securityProtocol);
        if (securityProtocol.equals("PLAINTEXT")) {
            if (keystoreLoc != null
                    || keystorePassword != null
                    || truststoreLoc != null
                    || truststorePassword != null
                    || jaasConfig != null
                    || saslMechanism != null) {
                throw new LHMisconfigurationException(
                        "Check your LHS_KAFKA_SECURITY_PROTOCOL. Cannot have PLAINTEXT with other security configs.");
            }
            log.info("Connecting to Kafka with PLAINTEXT");

        } else if (securityProtocol.equals("SSL")) {
            if (keystoreLoc != null) {
                if (keystorePassword == null) {
                    throw new LHMisconfigurationException(
                            "Must set LHS_KAFKA_KEYSTORE_PASSWORD or LHS_KAFKA_KEYSTORE_PASSWORD_FILE if"
                                    + " LHS_KAFKA_KEYSTORE location is set");
                }
                conf.put("ssl.keystore.type", "PKCS12");
                conf.put("ssl.keystore.location", keystoreLoc);
                conf.put("ssl.keystore.password", keystorePassword);
                log.info("Connecting to Kafka with MTLS.");
            } else {
                log.info("Connecting to Kafka with TLS.");
            }

        } else if (securityProtocol.equals("SASL_SSL")) {
            if (saslMechanism == null || jaasConfig == null) {
                throw new LHMisconfigurationException("Must set SASL mechanism and Jaas Config using SASL_SSL");
            }
            conf.put("sasl.mechanism", saslMechanism);
            conf.put("sasl.jaas.config", jaasConfig);
        } else {
            throw new LHMisconfigurationException(
                    "Only SASL_SSL, PLAINTEXT, and SSL supported for LHS_KAFKA_SECURITY_PROTOCOL");
        }

        if (truststoreLoc != null) {
            if (truststorePassword == null) {
                throw new LHMisconfigurationException("LHS_KAFKA_TRUSTORE set but no password provided");
            }
            conf.put("ssl.truststore.type", "PKCS12");
            conf.put("ssl.truststore.location", truststoreLoc);
            conf.put("ssl.truststore.password", truststorePassword);
        }
    }

    /**
     * Some configs can be set either directly or via a file (generally, passwords).
     * @param primary is the config which *might* have the config set.
     * @param fileLocation is the file which *might* have the config set.
     * @param defaultVal is the value to return if none are set.
     * @return the config if set or the default.
     * @throws LHMisconfigurationException if both primary and fileLocation are set.
     */
    private String getFromConfigOrFile(String primary, String fileLocation, String defaultVal) {
        String primaryVal = getOrSetDefault(primary, null);
        String fileLocationVal = getOrSetDefault(fileLocation, null);
        if (primaryVal != null && fileLocationVal != null) {
            throw new LHMisconfigurationException("Cannot set both %s and %s".formatted(primary, fileLocation));
        }
        if (primaryVal != null) return primaryVal;
        if (fileLocationVal != null) return loadSettingFromFile(fileLocationVal);
        return defaultVal;
    }

    public Properties getCoreStreamsConfig() {
        Properties result = getBaseStreamsConfig();
        result.put("application.id", getKafkaGroupId("core"));
        result.put("client.id", this.getClientId("core"));

        result.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);

        result.put("num.stream.threads", Integer.valueOf(getOrSetDefault(CORE_STREAM_THREADS_KEY, "1")));
        // The Core Topology is EOS. Note that we have engineered the application to not be sensitive
        // to commit latency (long story). The only thing that is affected by commit latency is the
        // time at which metrics updates are processed by the repartition processor, but those
        // are on a 10-second punctuator anyways.
        //
        // This allows us to increase the default config for commit interval (Streams sets it to 100),
        // which allows us to further reduce the number of records sent to the changelog by relying on
        // the Kafka Streams Statestore Cache. Additionally, larger transactions with more
        // records perform better (because starting + committing a transaction is expensive, not writing
        // records to it).
        //
        // 3 seconds should be long enough for a significant amount of TaskRun's to be scheduled, started, and
        // completed. If all three `Command`s are processed within one commit, then we save 6 writes to the
        // changelog (the WfRun, NodeRun, and TaskRun each are saved on the first two commands).
        //
        // That's not to mention that we will be writing fewer times to RocksDB. Huge win.
        int commitInterval = Integer.valueOf(getOrSetDefault(LHServerConfig.CORE_STREAMS_COMMIT_INTERVAL_KEY, "500"));
        result.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, commitInterval);
        result.put(
                "statestore.cache.max.bytes",
                Long.valueOf(getOrSetDefault(CORE_STATESTORE_CACHE_BYTES_KEY, String.valueOf(1024L * 1024L * 32))));

        // Kafka Streams calls KafkaProducer#commitTransaction() which flushes messages upon committing the kafka
        // transaction. We _could_ linger.ms to the commit interval; however, the problem with this is that the
        // timer topology needs to be able to read the records. The Timer Topology is set to read_uncommitted and
        // has a requirement that all Command's in the Timer Topology are idempotent, so this is okay.
        //
        // We can make some of our end-to-end tests fail if we set linger.ms to something big (i.e. 3,000ms) because
        // it delays the sending of timers to the Timer Topology, so certain time-triggered events that are expected
        // to happen end up not happening (eg. in RetryTest, exponential-backoff retries are not scheduled on time).
        //
        // We set linger.ms to the same interval as the Timer Punctuator interval (500ms). This gives us approximately
        // 1-second precision on timers.
        result.put(StreamsConfig.producerPrefix("linger.ms"), LHConstants.TIMER_PUNCTUATOR_INTERVAL.toMillis());

        for (Object keyObj : props.keySet()) {
            String key = (String) keyObj;
            if (key.startsWith(CORE_KAFKA_STREAMS_OVERRIDE_PREFIX)) {
                String kafkaKey = key.substring(CORE_KAFKA_STREAMS_OVERRIDE_PREFIX.length())
                        .replace("_", ".")
                        .toLowerCase();
                result.put(kafkaKey, props.get(key));
            }
        }
        result.put(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_RECORDS_CONFIG), "100");
        return result;
    }

    public Properties getTimerStreamsConfig() {
        Properties props = getBaseStreamsConfig();
        props.put("application.id", this.getKafkaGroupId("timer"));
        props.put("client.id", this.getClientId("timer"));
        props.put("processing.guarantee", "at_least_once");
        props.put("consumer.isolation.level", "read_uncommitted");
        props.put("num.stream.threads", Integer.valueOf(getOrSetDefault(TIMER_STREAM_THREADS_KEY, "1")));

        // The timer topology is ALOS, so we can have a larger commit interval with less of a problem. Looking at the
        // workload of the timer topology, the majority is TaskRun timeouts, which is as follows:
        //
        // - ScheduleTimer command comes in, we write a LHTimer to the state store
        // - 10 seconds later, we punctuate and delete the LHTimer
        //
        // With a large state store cache and a commit interval of 30 seconds (which is streams default), we can
        // drastically reduce the amount of records written to the changelog. It is possible that a majority of the
        // LHTimer's never actually get written to the changelog topic.
        //
        // Due to the larger commit interval for the Timer Topology, we recommend setting the
        // LHS_TIMER_STATESTORE_CACHE_BYTES config to be big (i.e. 128MB), but it is fine to leave
        // the LHS_CORE_STATESTORE_CACHE_BYTES config smaller (i.e. 16MB) due to the smaller commit interval.
        int commitInterval =
                Integer.valueOf(getOrSetDefault(LHServerConfig.TIMER_STREAMS_COMMIT_INTERVAL_KEY, "30000"));
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, commitInterval);

        props.put(
                "statestore.cache.max.bytes",
                Long.valueOf(getOrSetDefault(TIMER_STATESTORE_CACHE_BYTES_KEY, String.valueOf(1024L * 1024L * 64))));

        // For the Timer, which is ALOS, the linger ms does potentially impact the latency of a timer coming in.
        // Future work might allow this to be a separate config from the linger ms used for the GRPC server.
        props.put(StreamsConfig.producerPrefix("linger.ms"), getOrSetDefault(LINGER_MS_KEY, "0"));

        return props;
    }

    private Properties getBaseStreamsConfig() {
        Properties props = new Properties();

        props.put(StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_TYPE_CONFIG), "zstd");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_ZSTD_LEVEL_CONFIG), 8);
        props.put(StreamsConfig.producerPrefix(CommonClientConfigs.METADATA_RECOVERY_STRATEGY_CONFIG), "rebootstrap");
        props.put(StreamsConfig.consumerPrefix(CommonClientConfigs.METADATA_RECOVERY_STRATEGY_CONFIG), "rebootstrap");
        props.put(
                StreamsConfig.restoreConsumerPrefix(CommonClientConfigs.METADATA_RECOVERY_STRATEGY_CONFIG),
                "rebootstrap");
        props.put(
                StreamsConfig.globalConsumerPrefix(CommonClientConfigs.METADATA_RECOVERY_STRATEGY_CONFIG),
                "rebootstrap");
        props.put(
                StreamsConfig.adminClientPrefix(CommonClientConfigs.METADATA_RECOVERY_STRATEGY_CONFIG), "rebootstrap");

        // Unfortunately, there is a bug that means the only way to do this is with an "internal" config.
        props.put(StreamsConfig.consumerPrefix("internal.leave.group.on.close"), true);

        props.put(
                "application.server",
                getOrSetDefault(LHServerConfig.INTERNAL_ADVERTISED_HOST_KEY, "localhost") + ":"
                        + this.getInternalAdvertisedPort());

        props.put("bootstrap.servers", this.getBootstrapServers());
        props.put("state.dir", getStateDirectory());

        // We want a request to be able to fail and be handled (if non-fatal) before a transaction times out.
        // Therefore, request timeout should be less than transaction timeout.
        props.put("request.timeout.ms", (int) (getTransactionTimeoutMs() * 0.75));

        props.put("producer.acks", "all");
        props.put("replication.factor", (int) getReplicationFactor());
        props.put("num.standby.replicas", Integer.valueOf(getOrSetDefault(NUM_STANDBY_REPLICAS_KEY, "0")));
        props.put("max.warmup.replicas", Integer.valueOf(getOrSetDefault(NUM_WARMUP_REPLICAS_KEY, "4")));
        props.put("probing.rebalance.interval.ms", 60 * 1000);
        props.put("metrics.recording.level", getServerMetricLevel().toUpperCase());
        props.put(StreamsConfig.producerPrefix(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG), getTransactionTimeoutMs());

        // Configs required by KafkaStreams. Some of these are overriden by the application logic itself.
        props.put("default.deserialization.exception.handler", LogAndContinueExceptionHandler.class);
        props.put("default.production.exception.handler", DefaultProductionExceptionHandler.class);
        props.put("default.value.serde", Serdes.StringSerde.class.getName());
        props.put("default.key.serde", Serdes.StringSerde.class.getName());

        if (getRackId() != null) {
            // This enables high-availability assignment (standby's are scheduled in different
            // racks than the active tasks)
            props.put("rack.aware.assignment.tags", "lhrack");
            props.put("client.tag.lhrack", getRackId());

            // Enable follower fetching for standby tasks and restoration.
            // Follower fetching increases latency by a few dozen milliseconds for tail reads.
            // Therefore, we only fetch from followers on the standby tasks.
            props.put("restore.consumer.client.rack", getRackId());

            // It's fine to slightly increase latency for the global consumer. Even though the
            // global consumer doesn't read much data, it still sends fetch requests quite often.
            // Those fetch requests can be somewhat costly.
            props.put("global.consumer.client.rack", getRackId());

            // As of Kafka 3.9, there is nothing we can do to optimize the group coordinator traffic.
        }

        // Set the RocksDB Config Setter, and inject this LHServerConfig into the options set
        // into it.
        props.put(RocksConfigSetter.LH_SERVER_CONFIG_KEY, this);
        props.put("rocksdb.config.setter", RocksConfigSetter.class);

        props.put("consumer.session.timeout.ms", getStreamsSessionTimeout());

        // The delay before the state cleanup thread runs. This is used to clean up state stores
        props.put("state.cleanup.delay.ms", getStreamsStateCleanupDelayMs());

        // In case we need to authenticate to Kafka, this sets it.
        addKafkaSecuritySettings(props);

        return props;
    }

    private int getTransactionTimeoutMs() {
        // Default 60 second transaction timeout.
        return Integer.valueOf(getOrSetDefault(LHServerConfig.TRANSACTION_TIMEOUT_MS_KEY, "60000"));
    }

    private String getClientId(String component) {
        return this.getLHClusterId() + "-" + this.getLHInstanceName() + "-" + component;
    }

    public int getStreamsSessionTimeout() {
        return Integer.valueOf(
                getOrSetDefault(LHServerConfig.SESSION_TIMEOUT_KEY, String.valueOf(getTransactionTimeoutMs())));
    }

    public int getStreamsStateCleanupDelayMs() {
        return Integer.valueOf(getOrSetDefault(LHServerConfig.STATE_CLEANUP_DELAY_MS_KEY, "600000"));
    }

    public boolean areStructDefsEnabled() {
        return Boolean.valueOf(getOrSetDefault(LHServerConfig.X_ENABLE_STRUCT_DEFS_KEY, "false"));
    }

    public String getRackId() {
        return getOrSetDefault(LHServerConfig.RACK_ID_KEY, null);
    }

    public int getMaxBulkJobIterDurationMs() {
        return Integer.valueOf(getOrSetDefault(MAX_BULK_JOB_ITER_DURATION_MS, "20"));
    }

    public int getBulkJobDelayIntervalSeconds() {
        return Integer.valueOf(getOrSetDefault(BULK_JOB_DELAY_INTERVAL_SECONDS, "0"));
    }

    /**
     * Creates a Kafka Topic if it doesn't already exist. Returns true if the topic was created,
     * false if it already existed.
     * @param topic is the topic to create.
     * @return true if topic was created, false if it already existed
     * @throws InterruptedException if interrupted when waiting for the topic creation callback.
     * @throws ExecutionException if the topic creation callback fails.
     */
    public boolean createKafkaTopic(NewTopic topic) throws InterruptedException, ExecutionException {
        try {
            kafkaAdmin.createTopics(Collections.singleton(topic)).all().get();
            log.info("Topic {} created.", topic.name());
            return true;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof TopicExistsException) {
                log.info("Topic {} already exists.", topic.name());
                return false;
            } else {
                throw e;
            }
        }
    }

    private void initRocksdbSingletons() {
        RocksDB.loadLibrary();
        long cacheSize = Long.valueOf(getOrSetDefault(ROCKSDB_TOTAL_BLOCK_CACHE_BYTES_KEY, "-1"));
        if (cacheSize != -1) {
            // The global RocksDB cache is shared across multiple state stores, requiring strict size limits to prevent
            // unexpected out-of-memory errors
            this.globalRocksdbBlockCache = new LRUCache(cacheSize, -1, true);
        }

        long totalWriteBufferSize = Long.valueOf(getOrSetDefault(ROCKSDB_TOTAL_MEMTABLE_BYTES_KEY, "-1"));
        if (totalWriteBufferSize != -1) {
            this.globalRocksdbWriteBufferManager =
                    new WriteBufferManager(totalWriteBufferSize, globalRocksdbBlockCache, true);
        }

        long rateLimit = Long.valueOf(getOrSetDefault(ROCKSDB_RATE_LIMIT_BYTES_KEY, "-1"));
        if (rateLimit > 0) {
            this.globalRocksdbRateLimiter = new RateLimiter(
                    rateLimit,
                    RateLimiter.DEFAULT_REFILL_PERIOD_MICROS,
                    RateLimiter.DEFAULT_FAIRNESS,
                    RateLimiter.DEFAULT_MODE,
                    false);
        }
    }

    private void initKafkaAdmin() {
        Properties kafkaSettings = new Properties();
        kafkaSettings.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        addKafkaSecuritySettings(kafkaSettings);
        kafkaAdmin = Admin.create(kafkaSettings);
    }

    public ServerCredentials getInternalServerCreds() {
        String caCertFile = getOrSetDefault(INTERNAL_CA_CERT_KEY, null);
        String serverCertFile = getOrSetDefault(INTERNAL_SERVER_CERT_KEY, null);
        String serverKeyFile = getOrSetDefault(INTERNAL_SERVER_KEY_KEY, null);

        if (caCertFile == null) {
            log.info("No ca cert file found, deploying insecure!");
            return null;
        }

        if (serverCertFile == null || serverKeyFile == null) {
            throw new LHMisconfigurationException("CA cert file provided but missing cert or key");
        }
        File serverCert = new File(serverCertFile);
        File serverKey = new File(serverKeyFile);
        File rootCA = new File(caCertFile);

        try {
            return TlsServerCredentials.newBuilder()
                    .keyManager(serverCert, serverKey)
                    .trustManager(rootCA)
                    .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
                    .build();
        } catch (IOException exn) {
            throw new RuntimeException(exn);
        }
    }

    public ChannelCredentials getInternalClientCreds() {
        String caCertFile = getOrSetDefault(INTERNAL_CA_CERT_KEY, null);
        String serverCertFile = getOrSetDefault(INTERNAL_SERVER_CERT_KEY, null);
        String serverKeyFile = getOrSetDefault(INTERNAL_SERVER_KEY_KEY, null);
        if (caCertFile == null) {
            log.info("No ca cert file, using plaintext internal client");
            return null;
        }
        if (serverCertFile == null || serverKeyFile == null) {
            throw new LHMisconfigurationException("CA cert file provided but missing cert or key");
        }
        File serverCert = new File(serverCertFile);
        File serverKey = new File(serverKeyFile);
        File rootCA = new File(caCertFile);

        try {
            return TlsChannelCredentials.newBuilder()
                    .keyManager(serverCert, serverKey)
                    .trustManager(rootCA)
                    .build();
        } catch (IOException exn) {
            throw new RuntimeException(exn);
        }
    }

    private String loadSettingFromFile(String fileName) {
        try {
            return Files.readString(Path.of(fileName)).trim();
        } catch (IOException e) {
            throw new LHMisconfigurationException("Error loading file: " + fileName, e);
        }
    }

    @Override
    public String toString() {
        return props.entrySet().stream()
                .map(entry -> String.format(
                        "%s=%s",
                        entry.getKey(),
                        entry.getKey().toString().matches(".*(PASSWORD|CLIENT_SECRET).*")
                                ? "*********"
                                : entry.getValue()))
                .sorted()
                .collect(Collectors.joining("\n"));
    }
}
