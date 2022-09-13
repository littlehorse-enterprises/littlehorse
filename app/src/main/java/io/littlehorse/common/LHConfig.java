package io.littlehorse.common;

import io.javalin.Javalin;
import io.littlehorse.common.util.LHApiClient;
import io.littlehorse.common.util.LHKStreamsListener;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHRpcClient;
import io.littlehorse.common.util.LHUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
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
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.HostInfo;

public class LHConfig {

    private Properties props;
    private Admin kafkaAdmin;
    private LHProducer producer;
    private LHProducer txnProducer;
    private KafkaConsumer<String, Bytes> kafkaConsumer;

    public String getBootstrapServers() {
        return getOrSetDefault(
            LHConstants.KAFKA_BOOTSTRAP_KEY,
            "localhost:9092"
        );
    }

    public short getReplicationFactor() {
        return Short.valueOf(
            String.class.cast(
                    props.getOrDefault(LHConstants.REPLICATION_FACTOR_KEY, "1")
                )
        );
    }

    public int getTaskPartitions() {
        return Integer.valueOf(
            String.class.cast(
                    props.getOrDefault(LHConstants.TASK_PARTITIONS_KEY, "12")
                )
        );
    }

    public int getClusterPartitions() {
        return Integer.valueOf(
            String.class.cast(
                    props.getOrDefault(LHConstants.CLUSTER_PARTITIONS_KEY, "72")
                )
        );
    }

    public int getWorkerThreads() {
        return Integer.valueOf(
            getOrSetDefault(LHConstants.NUM_WORKER_THREADS_KEY, "128")
        );
    }

    public String getKafkaGroupId(String component) {
        return getKafkaGroupId() + "-" + component;
    }

    public String getKafkaGroupId() {
        return getOrSetDefault(
            LHConstants.KAFKA_GROUP_ID_KEY,
            "unset-group-id-bad"
        );
    }

    public String getKafkaInstanceId() {
        return getOrSetDefault(
            LHConstants.KAFKA_GROUP_IID_KEY,
            "Unset-group-iid-bad"
        );
    }

    public String getStateDirectory() {
        return getOrSetDefault(
            LHConstants.KAFKA_STATE_DIR_KEY,
            "/tmp/kafkaState"
        );
    }

    public String getAdvertisedProto() {
        return getOrSetDefault(LHConstants.ADVERTISED_PROTOCOL_KEY, "http");
    }

    public String getAdvertisedUrl() {
        return String.format(
            "%s://%s:%d",
            getAdvertisedProto(),
            this.getAdvertisedHost(),
            getAdvertisedPort()
        );
    }

    public String getAdvertisedHost() {
        return getOrSetDefault(LHConstants.ADVERTISED_HOST_KEY, "localhost");
    }

    public int getAdvertisedPort() {
        return Integer.valueOf(
            getOrSetDefault(LHConstants.ADVERTISED_PORT_KEY, "5000")
        );
    }

    public int getExposedPort() {
        return Integer.valueOf(
            getOrSetDefault(LHConstants.EXPOSED_PORT_KEY, "5000")
        );
    }

    public HostInfo getHostInfo() {
        return new HostInfo(getAdvertisedHost(), getAdvertisedPort());
    }

    public HostInfo getApiHostInfo() {
        return new HostInfo(getApiHost(), getApiPort());
    }

    public String getApiHost() {
        return getOrSetDefault(LHConstants.API_HOST_KEY, "localhost");
    }

    public LHApiClient getApiClient() {
        return new LHApiClient(this);
    }

    public int getApiPort() {
        // return Integer.valueOf(getOrSetDefault(LHConstants.API_PORT_KEY, "5000"));

        return Integer.valueOf(
            getOrSetDefault(LHConstants.EXPOSED_PORT_KEY, "5000")
        );
    }

    public void cleanup() {
        if (this.kafkaAdmin != null) this.kafkaAdmin.close();
        if (this.producer != null) this.producer.close();
        if (this.txnProducer != null) this.txnProducer.close();
    }

    public LHRpcClient getRpcClient() {
        return new LHRpcClient();
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
        conf.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            getBootstrapServers()
        );
        conf.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        conf.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            Serdes.Bytes().serializer().getClass()
        );
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
        conf.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            getBootstrapServers()
        );
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

        // TODO: Make this more graceful
        conf.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, getWorkerThreads());

        kafkaConsumer = new KafkaConsumer<>(conf);
        kafkaConsumer.subscribe(topics);
        return kafkaConsumer;
    }

    public Properties getStreamsConfig(String component) {
        Properties props = new Properties();
        props.put(
            StreamsConfig.APPLICATION_SERVER_CONFIG,
            this.getAdvertisedHost() + ":" + this.getAdvertisedPort()
        );
        props.put(
            StreamsConfig.APPLICATION_ID_CONFIG,
            this.getKafkaGroupId(component)
        );
        props.put(
            ConsumerConfig.GROUP_INSTANCE_ID_CONFIG,
            this.getKafkaInstanceId()
        );
        props.put(
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
            this.getBootstrapServers()
        );
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.STATE_DIR_CONFIG, this.getStateDirectory());
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once_v2");
        props.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, "all");
        props.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(
            StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG),
            "all"
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
            StreamsConfig.consumerPrefix(
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG
            ),
            10000
        );
        props.put(StreamsConfig.METADATA_MAX_AGE_CONFIG, "4000");
        props.put(
            StreamsConfig.NUM_STREAM_THREADS_CONFIG,
            Integer.valueOf(
                getOrSetDefault(LHConstants.NUM_STREAM_THREADS_KEY, "1")
            )
        );
        props.put(StreamsConfig.TASK_TIMEOUT_MS_CONFIG, 0);
        props.put(
            StreamsConfig.producerPrefix(
                ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG
            ),
            10000
        );
        props.put(
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            Serdes.StringSerde.class.getName()
        );
        props.put(
            StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG,
            this.getStandbyReplicas()
        );
        props.put(
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
            Serdes.StringSerde.class.getName()
        );
        props.put(
            StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
            getStreamsCommitInterval()
        );
        props.put(StreamsConfig.RACK_AWARE_ASSIGNMENT_TAGS_CONFIG, "rack");
        props.put(StreamsConfig.CLIENT_TAG_PREFIX + "rack", getRackId());

        return props;
    }

    public String getRackId() {
        return getOrSetDefault(
            LHConstants.RACK_ID_KEY,
            "unset-rack-id-bad-bad"
        );
    }

    public int getStreamsCommitInterval() {
        return Integer.valueOf(
            getOrSetDefault(LHConstants.COMMIT_INTERVAL_KEY, "50")
        );
    }

    public int getDefaultTaskTimeout() {
        return Integer.valueOf(
            getOrSetDefault(LHConstants.DEFAULT_TIMEOUT_KEY, "10")
        );
    }

    public int getStandbyReplicas() {
        return Integer.valueOf(
            getOrSetDefault(LHConstants.NUM_STANDBY_REPLICAS_KEY, "0")
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
                e.getCause() != null &&
                e.getCause() instanceof TopicExistsException
            ) {
                LHUtil.log("Topic " + topic.name() + " already exists.");
            } else {
                throw e;
            }
        }
    }

    private void initialize(Properties overrides) {
        Properties origDefaults = getEnvDefaults();
        for (String k : overrides.stringPropertyNames()) {
            origDefaults.setProperty(k, overrides.getProperty(k));
        }
        props = origDefaults;

        Properties akProperties = new Properties();
        akProperties.put(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
            getBootstrapServers()
        );
        this.kafkaAdmin = Admin.create(akProperties);
    }

    public LHConfig() {
        initialize(new Properties());
    }

    public LHConfig(Properties overrides) {
        initialize(overrides);
    }

    private Properties getEnvDefaults() {
        Properties props = new Properties();

        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            if (entry.getKey().startsWith("LHORSE")) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
        }
        return props;
    }

    private String getOrSetDefault(String key, String defaultVal) {
        String result = String.class.cast(props.get(key));

        if (result == null) {
            props.setProperty(key, defaultVal);
            return defaultVal;
        } else {
            return result;
        }
    }

    public Javalin createAppWithHealth(LHKStreamsListener listener) {
        Javalin app = Javalin.create(javalinConf -> {
            javalinConf.prefer405over404 = true;
            javalinConf.enableCorsForAllOrigins();
        });

        app.get(
            "/live",
            ctx -> {
                LHKStreamsListener.ProbeResponse resp = listener.getResponse();

                KafkaStreams.State state = resp.state;
                if (
                    state == KafkaStreams.State.NOT_RUNNING ||
                    state == KafkaStreams.State.PENDING_ERROR ||
                    state == KafkaStreams.State.PENDING_SHUTDOWN ||
                    state == KafkaStreams.State.ERROR
                ) {
                    ctx.status(500);
                } else if (
                    state == KafkaStreams.State.REBALANCING &&
                    resp.activeRestores == 0
                ) {
                    // ctx.status(500);
                    resp.message =
                        "Yikes, rebalancing but zero active restores!";
                } else {
                    ctx.status(200);
                }
                ctx.json(resp);
            }
        );

        app.get(
            "/ready",
            ctx -> {
                LHKStreamsListener.ProbeResponse resp = listener.getResponse();
                if (resp.state == KafkaStreams.State.RUNNING) {
                    ctx.status(200);
                } else {
                    ctx.status(500);
                }
                ctx.json(resp);
            }
        );

        app.get(
            "/diskUsage",
            ctx -> {
                Long size = FileUtils.sizeOfDirectory(
                    FileUtils.getFile(getStateDirectory())
                );
                ctx.result(size.toString());
            }
        );

        return app;
    }
}
