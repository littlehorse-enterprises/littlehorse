package io.littlehorse.common;

import com.google.common.base.Strings;
import io.grpc.ChannelCredentials;
import io.grpc.ServerCredentials;
import io.grpc.TlsChannelCredentials;
import io.grpc.TlsServerCredentials;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.Cache;
import org.rocksdb.LRUCache;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteBufferManager;

@Slf4j
public class LHServerConfig extends ConfigBase {

    // Singletons for RocksConfigSetter
    @Getter
    private Cache globalRocksdbBlockCache;

    @Getter
    private WriteBufferManager globalRocksdbWriteBufferManager;

    // Kafka Global Configs
    public static final String KAFKA_BOOTSTRAP_KEY = "LHS_KAFKA_BOOTSTRAP_SERVERS";
    public static final String LHS_CLUSTER_ID_KEY = "LHS_CLUSTER_ID"; // determines application.id
    public static final String LHS_INSTANCE_ID_KEY = "LHS_INSTANCE_ID";
    public static final String REPLICATION_FACTOR_KEY = "LHS_REPLICATION_FACTOR";
    public static final String CLUSTER_PARTITIONS_KEY = "LHS_CLUSTER_PARTITIONS";
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
    public static final String SESSION_TIMEOUT_KEY = "LHS_STREAMS_SESSION_TIMEOUT";
    public static final String KAFKA_STATE_DIR_KEY = "LHS_STATE_DIR";
    public static final String NUM_WARMUP_REPLICAS_KEY = "LHS_STREAMS_NUM_WARMUP_REPLICAS";
    public static final String NUM_STANDBY_REPLICAS_KEY = "LHS_STREAMS_NUM_STANDBY_REPLICAS";

    // General LittleHorse Runtime Behavior Config Env Vars
    public static final String NUM_NETWORK_THREADS_KEY = "LHS_NUM_NETWORK_THREADS";
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
    public static final String KAFKA_TRUSTSTORE_KEY = "LHS_KAFKA_TRUSTSTORE";
    public static final String KAFKA_TRUSTSTORE_PASSWORD_KEY = "LHS_KAFKA_TRUSTSTORE_PASSWORD";
    public static final String KAFKA_TRUSTSTORE_PASSWORD_FILE_KEY = "LHS_KAFKA_TRUSTSTORE_PASSWORD_FILE";
    public static final String KAFKA_KEYSTORE_KEY = "LHS_KAFKA_KEYSTORE";
    public static final String KAFKA_KEYSTORE_PASSWORD_KEY = "LHS_KAFKA_KEYSTORE_PASSWORD";
    public static final String KAFKA_KEYSTORE_PASSWORD_FILE_KEY = "LHS_KAFKA_KEYSTORE_PASSWORD_FILE";

    // PROMETHEUS
    public static final String HEALTH_SERVICE_PORT_KEY = "LHS_HEALTH_SERVICE_PORT";
    public static final String HEALTH_PATH_METRICS_KEY = "LHS_HEALTH_PATH_METRICS";
    public static final String HEALTH_PATH_LIVENESS_KEY = "LHS_HEALTH_PATH_LIVENESS";
    public static final String HEALTH_PATH_STATUS_KEY = "LHS_HEALTH_PATH_STATUS";
    public static final String HEALTH_PATH_DISK_USAGE_KEY = "LHS_HEALTH_PATH_DISK_USAGE";

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

    protected String[] getEnvKeyPrefixes() {
        return new String[] {"LHS_"};
    }

    private Admin kafkaAdmin;
    private LHProducer producer;
    private LHProducer txnProducer;

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
        return getAllTopics(getLHClusterId(), getReplicationFactor(), getClusterPartitions());
    }

    // Internal topics are manually created because:
    // 1.- It makes it possible to manage/create topics using an external tool (e.g
    // terraform, strimzi, etc.)
    // 2.- It allows to explicitly manage the configuration of the topics
    // Note: Kafka streams doesn't support disabling automatic internal topic
    // creation. Thus, internal topics that are not explicitly created here will be
    // automatically created by Kafka Stream. Please make sure to manually create all internal topics.
    // Kafka has opened KIP-698 to solve this.
    public static List<NewTopic> getAllTopics(String clusterId, short replicationFactor, int clusterPartitions) {
        HashMap<String, String> compactedTopicConfig = new HashMap<>() {
            {
                put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
            }
        };

        NewTopic coreCommand = new NewTopic(getCoreCmdTopicName(clusterId), clusterPartitions, replicationFactor);

        NewTopic repartition = new NewTopic(getRepartitionTopicName(clusterId), clusterPartitions, replicationFactor);

        NewTopic timer = new NewTopic(getTimerTopic(clusterId), clusterPartitions, replicationFactor);

        NewTopic coreStoreChangelog = new NewTopic(
                        getCoreStoreChangelogTopic(clusterId), clusterPartitions, replicationFactor)
                .configs(compactedTopicConfig);

        NewTopic repartitionStoreChangelog = new NewTopic(
                        getRepartitionStoreChangelogTopic(clusterId), clusterPartitions, replicationFactor)
                .configs(compactedTopicConfig);

        NewTopic timerStoreChangelog = new NewTopic(
                        getTimerStoreChangelogTopic(clusterId), clusterPartitions, replicationFactor)
                .configs(compactedTopicConfig);

        NewTopic metadataStoreChangelog = new NewTopic(getMetadataStoreChangelogTopic(clusterId), 1, replicationFactor)
                .configs(compactedTopicConfig);

        NewTopic metadataCommand =
                new NewTopic(getMetadataCmdTopicName(clusterId), 1, replicationFactor).configs(compactedTopicConfig);

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

    public String getKafkaGroupId(String component) {
        return getLHClusterId() + "-" + component;
    }

    public String getLHClusterId() {
        return getOrSetDefault(LHServerConfig.LHS_CLUSTER_ID_KEY, "cluster1");
    }

    public String getLHInstanceId() {
        return getOrSetDefault(
                LHServerConfig.LHS_INSTANCE_ID_KEY, "unset-" + UUID.randomUUID().toString());
    }

    public String getStateDirectory() {
        return getOrSetDefault(KAFKA_STATE_DIR_KEY, "/tmp/kafkaState");
    }

    public String getInternalAdvertisedHost() {
        return getOrSetDefault(LHServerConfig.INTERNAL_ADVERTISED_HOST_KEY, "localhost");
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

    public String getDiskUsagePath() {
        return getOrSetDefault(LHServerConfig.HEALTH_PATH_DISK_USAGE_KEY, "/diskUsage");
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

    @Nullable
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
        if (this.producer != null) this.producer.close();
        if (this.txnProducer != null) this.txnProducer.close();
    }

    public LHProducer getProducer() {
        if (producer == null) {
            producer = new LHProducer(this);
        }
        return producer;
    }

    public boolean shouldCreateTopics() {
        return Boolean.valueOf(getOrSetDefault(SHOULD_CREATE_TOPICS_KEY, "true"));
    }

    public long getCoreMemtableSize() {
        return Long.valueOf(getOrSetDefault(CORE_MEMTABLE_SIZE_BYTES_KEY, String.valueOf(1024L * 64)));
    }

    // Timer Topology generally has smaller values that are written. The majority of them
    // are LHTimer's with short (i.e. 10-second) TTL's (i.e. TaskRun Timeout timers), so
    // we don't expect the timer memtable to overflow that quickly.
    public long getTimerMemtableSize() {
        return Long.valueOf(getOrSetDefault(TIMER_MEMTABLE_SIZE_BYTES_KEY, String.valueOf(1024L * 32)));
    }

    public Properties getKafkaProducerConfig(String component) {
        Properties conf = new Properties();
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
        String keystoreLoc = getOrSetDefault(KAFKA_KEYSTORE_KEY, null);
        String keystorePassword = getOrSetDefault(KAFKA_KEYSTORE_PASSWORD_KEY, null);
        String keystorePasswordFile = getOrSetDefault(KAFKA_KEYSTORE_PASSWORD_FILE_KEY, null);
        String truststoreLoc = getOrSetDefault(KAFKA_TRUSTSTORE_KEY, null);
        String truststorePassword = getOrSetDefault(KAFKA_TRUSTSTORE_PASSWORD_KEY, null);
        String truststorePasswordFile = getOrSetDefault(KAFKA_TRUSTSTORE_PASSWORD_FILE_KEY, null);

        if (keystorePasswordFile != null) {
            log.info("Loading Keystore Password form file");
            keystorePassword = loadSettingFromFile(keystorePasswordFile);
        }

        if (truststorePasswordFile != null) {
            log.info("Loading Truststore Password form files");
            truststorePassword = loadSettingFromFile(truststorePasswordFile);
        }

        if (keystoreLoc == null && keystorePassword == null && truststoreLoc == null && truststorePassword == null) {
            log.info("Using plaintext kafka access");
            return;
        }

        if (keystoreLoc == null || keystorePassword == null || truststoreLoc == null || truststorePassword == null) {
            throw new RuntimeException("Must provide all or none of the following configs: "
                    + KAFKA_KEYSTORE_KEY
                    + ", "
                    + KAFKA_KEYSTORE_PASSWORD_KEY
                    + ", "
                    + KAFKA_TRUSTSTORE_KEY
                    + ", "
                    + KAFKA_TRUSTSTORE_PASSWORD_KEY);
        }

        conf.put("security.protocol", "SSL");

        conf.put("ssl.keystore.type", "PKCS12");
        conf.put("ssl.keystore.location", keystoreLoc);
        conf.put("ssl.keystore.password", keystorePassword);

        conf.put("ssl.truststore.type", "PKCS12");
        conf.put("ssl.truststore.location", truststoreLoc);
        conf.put("ssl.truststore.password", truststorePassword);
    }

    public Properties getCoreStreamsConfig() {
        Properties props = getBaseStreamsConfig();
        props.put("application.id", getKafkaGroupId("core"));
        props.put("processing.guarantee", "exactly_once_v2");
        props.put("num.stream.threads", Integer.valueOf(getOrSetDefault(CORE_STREAM_THREADS_KEY, "1")));

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
        int commitInterval = Integer.valueOf(getOrSetDefault(LHServerConfig.CORE_STREAMS_COMMIT_INTERVAL_KEY, "2000"));
        props.put("commit.interval", commitInterval);
        props.put(
                "statestore.cache.max.bytes",
                Long.valueOf(getOrSetDefault(CORE_STATESTORE_CACHE_BYTES_KEY, String.valueOf(1024L * 1024L * 32))));
        return props;
    }

    public Properties getTimerStreamsConfig() {
        Properties props = getBaseStreamsConfig();
        props.put("application.id", this.getKafkaGroupId("timer"));
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
        props.put("commit.interval", commitInterval);

        props.put(
                "statestore.cache.max.bytes",
                Long.valueOf(getOrSetDefault(TIMER_STATESTORE_CACHE_BYTES_KEY, String.valueOf(1024L * 1024L * 64))));

        return props;
    }

    private Properties getBaseStreamsConfig() {
        Properties props = new Properties();
        props.put(
                "application.server",
                getOrSetDefault(LHServerConfig.INTERNAL_ADVERTISED_HOST_KEY, "localhost") + ":"
                        + this.getInternalAdvertisedPort());

        props.put("bootstrap.servers", this.getBootstrapServers());
        props.put("state.dir", getStateDirectory());
        props.put("request.timeout.ms", 1000 * 60);
        props.put("producer.transaction.timeout.ms", 1000 * 60);
        props.put("producer.acks", "all");
        props.put("replication.factor", (int) getReplicationFactor());
        props.put("num.standby.replicas", Integer.valueOf(getOrSetDefault(NUM_STANDBY_REPLICAS_KEY, "0")));
        props.put("max.warmup.replicas", Integer.valueOf(getOrSetDefault(NUM_WARMUP_REPLICAS_KEY, "4")));
        props.put("probing.rebalance.interval.ms", 60 * 1000);

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

            // As of Kafka 3.6, there is nothing we can do to optimize the group coordinator traffic.
        }

        RocksConfigSetter.serverConfig = this;
        props.put("rocksdb.config.setter", RocksConfigSetter.class);

        // Until KIP-924 is implemented, for cluster stability it is best to avoid rebalances.
        // 30 seconds of startup is enough time for LH to shut down and be re-spawned during a
        // rolling restart.
        //
        // It also gives enough time for the new server to come up, meaning that
        // in the case of a server failure while a request is being processed, the resulting
        // `Command` should be processed on a new server within a minute. Issue #479
        // should verify this behavior
        props.put(
                "consumer.session.timeout.ms",
                Integer.valueOf(getOrSetDefault(LHServerConfig.SESSION_TIMEOUT_KEY, "40000")));

        // In case we need to authenticate to Kafka, this sets it.
        addKafkaSecuritySettings(props);

        return props;
    }

    public int getNumNetworkThreads() {
        int out = Integer.valueOf(getOrSetDefault(NUM_NETWORK_THREADS_KEY, "2"));
        if (out < 2) {
            throw new LHMisconfigurationException("Requires at least 2 network threads");
        }
        return out;
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

    public LHServerConfig() {
        super();
        initKafkaAdmin();
        initRocksdbSingletons();
    }

    public LHServerConfig(String propertiesPath) {
        super(propertiesPath);
        initKafkaAdmin();
        initRocksdbSingletons();
    }

    public LHServerConfig(Properties props) {
        super(props);
        initKafkaAdmin();
        initRocksdbSingletons();
    }

    private void initRocksdbSingletons() {
        RocksDB.loadLibrary();
        long cacheSize = Long.valueOf(getOrSetDefault(ROCKSDB_TOTAL_BLOCK_CACHE_BYTES_KEY, "-1"));
        if (cacheSize != -1) {
            this.globalRocksdbBlockCache = new LRUCache(cacheSize);
        }

        long totalWriteBufferSize = Long.valueOf(getOrSetDefault(ROCKSDB_TOTAL_MEMTABLE_BYTES_KEY, "-1"));
        if (totalWriteBufferSize != -1) {
            this.globalRocksdbWriteBufferManager =
                    new WriteBufferManager(totalWriteBufferSize, globalRocksdbBlockCache);
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
        return getCreds(caCertFile, serverCertFile, serverKeyFile);
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
            throw new RuntimeException("CA cert file provided but missing cert or key");
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

    private ServerCredentials getCreds(String caCertFile, String serverCertFile, String serverKeyFile) {
        if (caCertFile == null) {
            log.info("No ca cert file found, deploying insecure!");
            return null;
        }

        if (serverCertFile == null || serverKeyFile == null) {
            throw new RuntimeException("CA cert file provided but missing cert or key");
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
