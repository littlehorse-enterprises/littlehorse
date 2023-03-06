package io.littlehorse.common;

import io.grpc.TlsServerCredentials;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.config.LHServerConfig;
import io.littlehorse.jlib.common.proto.HostInfoPb;
import io.littlehorse.jlib.common.proto.VariableAssignmentPb.SourceCase;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.log4j.Logger;

public class LHConfig extends LHServerConfig {

    private static final Logger log = Logger.getLogger(LHConfig.class);

    private Admin kafkaAdmin;
    private LHProducer producer;
    private LHProducer txnProducer;
    private KafkaConsumer<String, Bytes> kafkaConsumer;

    public int getHotMetadataPartition() {
        return (
            Utils.toPositive(
                Utils.murmur2(LHConstants.META_PARTITION_KEY.getBytes())
            ) %
            getClusterPartitions()
        );
    }

    public String getKafkaTopicPrefix() {
        return getOrSetDefault(LHServerConfig.KAFKA_TOPIC_PREFIX_KEY, "");
    }

    public String getCoreCmdTopicName() {
        return getKafkaTopicPrefix() + "core-cmd";
    }

    public String getRepartitionTopicName() {
        return getKafkaTopicPrefix() + "core-repartition";
    }

    public String getObervabilityEventTopicName() {
        return getKafkaTopicPrefix() + "observability";
    }

    public String getGlobalMetadataCLTopicName() {
        return getKafkaTopicPrefix() + "global-metadata-cl";
    }

    public String getTimerTopic() {
        return getKafkaTopicPrefix() + "timers";
    }

    // TODO: Determine how and where to set the topic names for TaskDef queues

    public String getBootstrapServers() {
        return getOrSetDefault(LHServerConfig.KAFKA_BOOTSTRAP_KEY, "localhost:9092");
    }

    public short getReplicationFactor() {
        return Short.valueOf(
            String.class.cast(
                    props.getOrDefault(LHServerConfig.REPLICATION_FACTOR_KEY, "1")
                )
        );
    }

    public int getClusterPartitions() {
        return Integer.valueOf(
            String.class.cast(
                    props.getOrDefault(LHServerConfig.CLUSTER_PARTITIONS_KEY, "72")
                )
        );
    }

    public String getKafkaGroupId(String component) {
        return getKafkaGroupId() + "-" + component;
    }

    public String getKafkaGroupId() {
        return getOrSetDefault(
            LHServerConfig.LH_CLUSTER_ID_KEY,
            "unset-group-id-bad"
        );
    }

    public String getKafkaInstanceId() {
        return getOrSetDefault(
            LHServerConfig.LH_INSTANCE_ID_KEY,
            "Unset-group-iid-bad"
        );
    }

    public String getStateDirectory() {
        return getOrSetDefault(LHServerConfig.KAFKA_STATE_DIR_KEY, "/tmp/kafkaState");
    }

    public String getInternalAdvertisedHost() {
        return getOrSetDefault(
            LHServerConfig.INTERNAL_ADVERTISED_HOST_KEY,
            "localhost"
        );
    }

    // If INTERNAL_ADVERTISED_PORT isn't set, we return INTERNAL_BIND_PORT.
    public int getInternalAdvertisedPort() {
        return Integer.valueOf(
            getOrSetDefault(
                LHServerConfig.INTERNAL_ADVERTISED_PORT_KEY,
                Integer.valueOf(getInternalBindPort()).toString()
            )
        );
    }

    public int getApiBindPort() {
        return Integer.valueOf(
            getOrSetDefault(LHServerConfig.API_BIND_PORT_KEY, "5000")
        );
    }

    // If INTERNAL_BIND_PORT isn't set, we just return API_BIND_PORT + 1.
    public int getInternalBindPort() {
        return Integer.valueOf(
            getOrSetDefault(
                LHServerConfig.INTERNAL_BIND_PORT_KEY,
                Integer.valueOf(getApiBindPort() + 1).toString()
            )
        );
    }

    private Map<String, HostInfoPb> publicAdvertisedHostMap;

    public Map<String, HostInfoPb> getPublicAdvertisedHostMap() {
        if (publicAdvertisedHostMap != null) {
            return publicAdvertisedHostMap;
        }

        publicAdvertisedHostMap = new HashMap<>();

        String listenerNames = getOrSetDefault(
            LHServerConfig.ADVERTISED_LISTENERS_KEY,
            LHServerConfig.DEFAULT_PUBLIC_LISTENER
        );
        System.out.println("Listener names are " + listenerNames);

        for (String lister : listenerNames.split(",")) {
            publicAdvertisedHostMap.put(lister, getHostForName(lister));
        }

        return publicAdvertisedHostMap;
    }

    private HostInfoPb getHostForName(String listenerName) {
        String fullHost = getOrSetDefault(listenerName, "localhost:5000");

        HostInfoPb.Builder out = HostInfoPb.newBuilder();
        int colonIndex = fullHost.indexOf(":");
        if (colonIndex == -1) {
            throw new RuntimeException(
                "Listener " + listenerName + " set to invalid host " + fullHost
            );
        }

        out.setHost(fullHost.substring(0, colonIndex));
        try {
            out.setPort(
                Integer.valueOf(fullHost.substring(colonIndex + 1, fullHost.length()))
            );
        } catch (Exception exn) {
            throw new RuntimeException(exn);
        }
        return out.build();
    }

    public HostInfo getInternalHostInfo() {
        return new HostInfo(getInternalAdvertisedHost(), getInternalAdvertisedPort());
    }

    public void cleanup() {
        if (this.kafkaAdmin != null) this.kafkaAdmin.close();
        if (this.producer != null) this.producer.close();
        if (this.txnProducer != null) this.txnProducer.close();
    }

    public LHProducer getProducer() {
        if (producer == null) {
            producer = new LHProducer(this, false);
        }
        return producer;
    }

    public LHProducer getTxnProducer() {
        if (txnProducer == null) {
            txnProducer = new LHProducer(this, true);
        }
        return txnProducer;
    }

    public Properties getKafkaProducerConfig() {
        Properties conf = new Properties();
        conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        conf.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        conf.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            Serdes.Bytes().serializer().getClass()
        );
        // conf.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        conf.put(
            ProducerConfig.CLIENT_ID_CONFIG,
            getKafkaGroupId() + "-" + getKafkaInstanceId()
        );
        conf.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringSerializer.class
        );
        conf.put(ProducerConfig.ACKS_CONFIG, "all");
        return conf;
    }

    public Properties getKafkaTxnProducerConfig() {
        Properties conf = getKafkaProducerConfig();
        conf.put(
            ProducerConfig.TRANSACTIONAL_ID_CONFIG,
            getKafkaGroupId() + "__" + getKafkaInstanceId() + "__transactional"
        );
        conf.put(
            ProducerConfig.CLIENT_ID_CONFIG,
            getKafkaGroupId() + "-" + getKafkaInstanceId() + "__transactional"
        );
        return conf;
    }

    public KafkaConsumer<String, Bytes> getKafkaConsumer(List<String> topics) {
        if (kafkaConsumer != null) {
            throw new RuntimeException("Tried to initialize consumer twice!");
        }

        Properties conf = new Properties();
        conf.put(ConsumerConfig.GROUP_ID_CONFIG, getKafkaGroupId());
        conf.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, getKafkaInstanceId());
        conf.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        conf.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        conf.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringDeserializer.class
        );
        conf.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.BytesDeserializer.class
        );
        conf.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        kafkaConsumer = new KafkaConsumer<>(conf);
        kafkaConsumer.subscribe(topics);
        return kafkaConsumer;
    }

    public Properties getStreamsConfig(String component, boolean exactlyOnce) {
        Properties props = new Properties();
        props.put(
            StreamsConfig.APPLICATION_SERVER_CONFIG,
            this.getInternalAdvertisedHost() + ":" + this.getInternalAdvertisedPort()
        );
        props.put(
            StreamsConfig.APPLICATION_ID_CONFIG,
            this.getKafkaGroupId(component)
        );
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, this.getKafkaInstanceId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        props.put(StreamsConfig.STATE_DIR_CONFIG, this.getStateDirectory());
        if (exactlyOnce) {
            props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once_v2");
        }
        props.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, "all");
        props.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(
            StreamsConfig.producerPrefix(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG),
            1000 * 15
        );
        props.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");

        props.put(
            StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_RECORDS_CONFIG),
            10000 // 10,000 instead of 1,000 to improve throughput
        );
        props.put(
            StreamsConfig.consumerPrefix(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG),
            20
        );

        props.put(
            StreamsConfig.REPLICATION_FACTOR_CONFIG,
            (int) getReplicationFactor()
        );
        props.put(
            StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
            org.apache.kafka.streams.errors.LogAndContinueExceptionHandler.class
        );
        props.put(
            StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
            org.apache.kafka.streams.errors.DefaultProductionExceptionHandler.class
        );
        props.put(
            StreamsConfig.consumerPrefix(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG),
            Integer.valueOf(
                getOrSetDefault(LHServerConfig.SESSION_TIMEOUT_KEY, "30000")
            )
        );
        props.put(StreamsConfig.METADATA_MAX_AGE_CONFIG, 1000 * 30);
        props.put(
            StreamsConfig.NUM_STREAM_THREADS_CONFIG,
            Integer.valueOf(
                getOrSetDefault(LHServerConfig.NUM_STREAM_THREADS_KEY, "1")
            )
        );
        props.put(StreamsConfig.TASK_TIMEOUT_MS_CONFIG, 10 * 1000);
        props.put(
            StreamsConfig.producerPrefix(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG),
            10 * 1000
        );
        props.put(
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            Serdes.StringSerde.class.getName()
        );
        props.put(
            StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG,
            this.getStandbyReplicas()
        );
        props.put(StreamsConfig.MAX_WARMUP_REPLICAS_CONFIG, this.getWarmupReplicas());
        props.put(
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
            Serdes.StringSerde.class.getName()
        );
        props.put(
            StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
            getStreamsCommitInterval()
        );
        props.put(StreamsConfig.PROBING_REBALANCE_INTERVAL_MS_CONFIG, 1000 * 60);

        // props.put(StreamsConfig.RACK_AWARE_ASSIGNMENT_TAGS_CONFIG, "rack");
        // props.put(StreamsConfig.CLIENT_TAG_PREFIX + "rack", getRackId());

        return props;
    }

    public String getRackId() {
        return getOrSetDefault(LHServerConfig.RACK_ID_KEY, "unset-rack-id-bad-bad");
    }

    public int getStreamsCommitInterval() {
        return Integer.valueOf(
            getOrSetDefault(LHServerConfig.COMMIT_INTERVAL_KEY, "100")
        );
    }

    public VariableAssignment getDefaultTaskTimeout() {
        int timeout = Integer.valueOf(
            getOrSetDefault(LHServerConfig.DEFAULT_TIMEOUT_KEY, "10")
        );

        VariableValue val = new VariableValue(timeout);
        VariableAssignment out = new VariableAssignment();
        out.rhsSourceType = SourceCase.LITERAL_VALUE;
        out.rhsLiteralValue = val;
        return out;
    }

    public int getStandbyReplicas() {
        return Integer.valueOf(
            getOrSetDefault(LHServerConfig.NUM_STANDBY_REPLICAS_KEY, "0")
        );
    }

    public int getWarmupReplicas() {
        return Integer.valueOf(
            getOrSetDefault(LHServerConfig.NUM_WARMUP_REPLICAS_KEY, "12")
        );
    }

    public void createKafkaTopic(NewTopic topic)
        throws InterruptedException, ExecutionException {
        CreateTopicsResult result = kafkaAdmin.createTopics(
            Collections.singleton(topic)
        );
        KafkaFuture<Void> future = result.values().get(topic.name());
        try {
            future.get();
        } catch (Exception e) {
            if (
                e.getCause() != null && e.getCause() instanceof TopicExistsException
            ) {
                LHUtil.log("Topic " + topic.name() + " already exists.");
            } else {
                throw e;
            }
        }
    }

    public LHConfig(Properties props) {
        this.props = props;

        Properties akProperties = new Properties();
        akProperties.put(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
            getBootstrapServers()
        );
        this.kafkaAdmin = Admin.create(akProperties);
    }

    public TlsServerCredentials.Builder getServerCreds() {
        String caCertFile = getOrSetDefault(CA_CERT_KEY, null);
        String serverCertFile = getOrSetDefault(SERVER_CERT_KEY, null);
        String serverKeyFile = getOrSetDefault(SERVER_KEY_KEY, null);
        if (caCertFile == null) {
            log.info("No ca cert file found, deploying insecure!");
            return null;
        }

        if (serverCertFile == null || serverKeyFile == null) {
            throw new RuntimeException(
                "CA cert file provided but missing cert or key"
            );
        }
        File serverCert = new File(serverCertFile);
        File serverKey = new File(serverKeyFile);
        File rootCA = new File(caCertFile);

        try {
            TlsServerCredentials.Builder out = TlsServerCredentials
                .newBuilder()
                .keyManager(serverCert, serverKey)
                .trustManager(rootCA)
                .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE);

            return out;
        } catch (IOException exn) {
            throw new RuntimeException(exn);
        }
    }
}
