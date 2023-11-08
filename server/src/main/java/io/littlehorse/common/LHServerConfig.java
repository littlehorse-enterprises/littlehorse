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
import lombok.extern.slf4j.Slf4j;
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
import org.jetbrains.annotations.Nullable;

@Slf4j
public class LHServerConfig extends ConfigBase {

    // Kafka and Kafka Streams-Specific Configuration Env Vars
    public static final String KAFKA_BOOTSTRAP_KEY = "LHS_KAFKA_BOOTSTRAP_SERVERS";
    public static final String LHS_CLUSTER_ID_KEY = "LHS_CLUSTER_ID";
    public static final String LHS_INSTANCE_ID_KEY = "LHS_INSTANCE_ID";
    public static final String RACK_ID_KEY = "LHS_RACK_ID";

    public static final String REPLICATION_FACTOR_KEY = "LHS_REPLICATION_FACTOR";
    public static final String CLUSTER_PARTITIONS_KEY = "LHS_CLUSTER_PARTITIONS";
    public static final String NUM_STREAM_THREADS_KEY = "LHS_STREAMS_NUM_THREADS";
    public static final String SESSION_TIMEOUT_KEY = "LHS_STREAMS_SESSION_TIMEOUT";
    public static final String COMMIT_INTERVAL_KEY = "LHS_STREAMS_COMMIT_INTERVAL";
    public static final String KAFKA_STATE_DIR_KEY = "LHS_STATE_DIR";
    public static final String NUM_WARMUP_REPLICAS_KEY = "LHS_STREAMS_NUM_WARMUP_REPLICAS";
    public static final String NUM_STANDBY_REPLICAS_KEY = "LHS_STREAMS_NUM_STANDBY_REPLICAS";

    // General LittleHorse Runtime Behavior Config Env Vars
    public static final String NUM_NETWORK_THREADS_KEY = "LHS_NUM_NETWORK_THREADS";
    public static final String DEFAULT_WFRUN_RETENTION_HOURS = "LHS_DEFAULT_WFRUN_RETENTION_HOURS";
    public static final String DEFAULT_EXTERNAL_EVENT_RETENTION_HOURS = "LHS_DEFAULT_EXTERNAL_EVENT_RETENTION_HOURS";
    public static final String INTERNAL_BIND_PORT_KEY = "LHS_INTERNAL_BIND_PORT";
    public static final String INTERNAL_ADVERTISED_HOST_KEY = "LHS_INTERNAL_ADVERTISED_HOST";
    public static final String INTERNAL_ADVERTISED_PORT_KEY = "LHS_INTERNAL_ADVERTISED_PORT";

    public static final String INTERNAL_CA_CERT_KEY = "LHS_INTERNAL_CA_CERT";
    public static final String INTERNAL_SERVER_CERT_KEY = "LHS_INTERNAL_SERVER_CERT";
    public static final String INTERNAL_SERVER_KEY_KEY = "LHS_INTERNAL_SERVER_KEY";

    public static final String KAFKA_TRUSTSTORE_KEY = "LHS_KAFKA_TRUSTSTORE";
    public static final String KAFKA_TRUSTSTORE_PASSWORD_KEY = "LHS_KAFKA_TRUSTSTORE_PASSWORD";
    public static final String KAFKA_TRUSTSTORE_PASSWORD_FILE_KEY = "LHS_KAFKA_TRUSTSTORE_PASSWORD_FILE";
    public static final String KAFKA_KEYSTORE_KEY = "LHS_KAFKA_KEYSTORE";
    public static final String KAFKA_KEYSTORE_PASSWORD_KEY = "LHS_KAFKA_KEYSTORE_PASSWORD";
    public static final String KAFKA_KEYSTORE_PASSWORD_FILE_KEY = "LHS_KAFKA_KEYSTORE_PASSWORD_FILE";

    public static final String SHOULD_CREATE_TOPICS_KEY = "LHS_SHOULD_CREATE_TOPICS";

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

    public String getObservabilityEventTopicName() {
        return getObservabilityEventTopicName(getLHClusterId());
    }

    public static String getObservabilityEventTopicName(String clusterId) {
        return clusterId + "-observability";
    }

    public String getGlobalMetadataCLTopicName() {
        return getGlobalMetadataCLTopicName(getLHClusterId());
    }

    public static String getGlobalMetadataCLTopicName(String clusterId) {
        return clusterId + "-global-metadata-cl";
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

        NewTopic observability =
                new NewTopic(getObservabilityEventTopicName(clusterId), clusterPartitions, replicationFactor);

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
                observability,
                timer,
                coreStoreChangelog,
                repartitionStoreChangelog,
                timerStoreChangelog,
                metadataStoreChangelog);
    }

    // TODO: Determine how and where to set the topic names for TaskDef queues

    public String getBootstrapServers() {
        return getOrSetDefault(LHServerConfig.KAFKA_BOOTSTRAP_KEY, "localhost:9092");
    }

    public short getReplicationFactor() {
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
        return getOrSetDefault(LHServerConfig.LHS_INSTANCE_ID_KEY, "server1");
    }

    public String getStateDirectory() {
        return getOrSetDefault(LHServerConfig.KAFKA_STATE_DIR_KEY, "/tmp/kafkaState");
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

    public Properties getKafkaProducerConfig(String component) {
        Properties conf = new Properties();
        conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        conf.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        conf.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                Serdes.Bytes().serializer().getClass());
        // conf.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        conf.put(ProducerConfig.CLIENT_ID_CONFIG, getKafkaGroupId(component));
        conf.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        conf.put(ProducerConfig.ACKS_CONFIG, "all");
        addKafkaSecuritySettings(conf);
        return conf;
    }

    /*
     * EMPLOYEE_TODO: right now, this only supports mtls auth. We want to:
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

    private String loadSettingFromFile(String fileName) {
        try {
            return Files.readString(Path.of(fileName)).trim();
        } catch (IOException e) {
            throw new LHMisconfigurationException("Error loading file: " + fileName, e);
        }
    }

    public Properties getStreamsConfig(String component, boolean exactlyOnce) {
        Properties props = new Properties();
        props.put(
                StreamsConfig.APPLICATION_SERVER_CONFIG,
                this.getInternalAdvertisedHost() + ":" + this.getInternalAdvertisedPort());
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getKafkaGroupId(component));

        // Static membership is utilized
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, this.getLHInstanceId());

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        props.put(StreamsConfig.STATE_DIR_CONFIG, this.getStateDirectory());
        if (exactlyOnce) {
            props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once_v2");
        }
        props.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, "all");

        // Keep retrying kafka requests for 60 seconds, which should be long enough for partition
        // leader to move over in case of broker failure.
        // TODO (LH-149): Ensure this works when we kill a Kafka broker
        props.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000 * 60);

        // TOOD (LH-149): Determine whether a broker failure causes a transaction timeout and then a
        // subsequent state store wipeout and restoration. Additionally, LH-149 should make this
        // value match the request timeout as well.
        props.put(StreamsConfig.producerPrefix(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG), 1000 * 60);

        props.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");

        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, (int) getReplicationFactor());
        props.put(
                StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                org.apache.kafka.streams.errors.LogAndContinueExceptionHandler.class);
        props.put(
                StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
                org.apache.kafka.streams.errors.DefaultProductionExceptionHandler.class);

        // TODO (LH-150): Make this configurable
        props.put(
                StreamsConfig.consumerPrefix(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG),
                Integer.valueOf(getOrSetDefault(LHServerConfig.SESSION_TIMEOUT_KEY, "20000")));

        props.put(
                StreamsConfig.NUM_STREAM_THREADS_CONFIG,
                Integer.valueOf(getOrSetDefault(LHServerConfig.NUM_STREAM_THREADS_KEY, "1")));

        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, this.getStandbyReplicas());
        props.put(StreamsConfig.MAX_WARMUP_REPLICAS_CONFIG, this.getWarmupReplicas());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, getStreamsCommitInterval());
        props.put(StreamsConfig.PROBING_REBALANCE_INTERVAL_MS_CONFIG, 1000 * 60);

        if (getRackId() != null) {
            // This enables high-availability assignment (standby's are scheduled in different)
            // racks than the active tasks
            props.put(StreamsConfig.RACK_AWARE_ASSIGNMENT_TAGS_CONFIG, "availabilityzone");
            props.put(StreamsConfig.CLIENT_TAG_PREFIX + "availabilityzone", getRackId());

            // Enable follower fetching for standby tasks and restoration.
            // Follower fetching increases latency by a few dozen milliseconds for tail reads.
            // Therefore, we only fetch from followers on the standby tasks.
            props.put("restore.consumer." + ConsumerConfig.CLIENT_RACK_CONFIG, getRackId());

            // It's fine to slightly increase latency for the global consumer. Even though the
            // global consumer doesn't read much data, it still sends fetch requests quite often.
            // Those fetch requests can be somewhat costly.
            props.put("global.consumer." + ConsumerConfig.CLIENT_RACK_CONFIG, getRackId());

            // As of Kafka 3.6, there is nothing we can do to optimize the group coordinator traffic.
        }

        props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, RocksConfigSetter.class);

        addKafkaSecuritySettings(props);

        return props;
    }

    public int getNumNetworkThreads() {
        int out = Integer.valueOf(getOrSetDefault(NUM_NETWORK_THREADS_KEY, "2000"));
        if (out < 2) {
            throw new LHMisconfigurationException("Requires at least 2 network threads");
        }
        return out;
    }

    public String getRackId() {
        return getOrSetDefault(LHServerConfig.RACK_ID_KEY, null);
    }

    public int getStreamsCommitInterval() {
        return Integer.valueOf(getOrSetDefault(LHServerConfig.COMMIT_INTERVAL_KEY, "100"));
    }

    public int getDefaultWfRunRetentionHours() {
        return Integer.valueOf(getOrSetDefault(LHServerConfig.DEFAULT_WFRUN_RETENTION_HOURS, "168"));
    }

    public int getDefaultExternalEventRetentionHours() {
        return Integer.valueOf(getOrSetDefault(LHServerConfig.DEFAULT_EXTERNAL_EVENT_RETENTION_HOURS, "168"));
    }

    public int getStandbyReplicas() {
        return Integer.valueOf(getOrSetDefault(LHServerConfig.NUM_STANDBY_REPLICAS_KEY, "0"));
    }

    public int getWarmupReplicas() {
        return Integer.valueOf(getOrSetDefault(LHServerConfig.NUM_WARMUP_REPLICAS_KEY, "12"));
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
    }

    public LHServerConfig(String propertiesPath) {
        super(propertiesPath);
        initKafkaAdmin();
    }

    public LHServerConfig(Properties props) {
        super(props);
        initKafkaAdmin();
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
