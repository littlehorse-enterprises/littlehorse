package io.littlehorse.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.HostInfo;
import io.javalin.Javalin;
import io.littlehorse.common.util.KStreamsStateListener;
import io.littlehorse.common.util.LHApiClient;

public class LHConfig {
    private Properties props;
    private HashSet<String> seenConsumerTypes;
    private HashSet<String> seenProducerTypes;
    private Admin kafkaAdmin;

    private HashSet<KafkaProducer<?, ?>> producersToClose;
    private HashSet<KafkaConsumer<?, ?>> consumersToClose;

    public String getBootstrapServers() {
        return getOrSetDefault(LHConstants.KAFKA_BOOTSTRAP_KEY, "localhost:9092");
    }

    public short getReplicationFactor() {
        return Short.valueOf(String.class.cast(props.getOrDefault(
            LHConstants.REPLICATION_FACTOR_KEY, "1"
        )));
    }

    public int getTaskPartitions() {
        return Integer.valueOf(String.class.cast(props.getOrDefault(
            LHConstants.TASK_PARTITIONS_KEY, "12")
        ));
    }

    public int getClusterPartitions() {
        return Integer.valueOf(String.class.cast(props.getOrDefault(
            LHConstants.CLUSTER_PARTITIONS_KEY, "72")
        ));
    }

    public String getKafkaGroupId() {
        return getOrSetDefault(LHConstants.KAFKA_GROUP_ID_KEY, "unset-group-id-bad");
    }

    public String getKafkaInstanceId() {
        return getOrSetDefault(LHConstants.KAFKA_GROUP_IID_KEY, "Unset-group-iid-bad");
    }

    public String getStateDirectory() {
        return getOrSetDefault(LHConstants.KAFKA_STATE_DIR_KEY, "/tmp/kafkaState");
    }

    public String getAdvertisedProto() {
        return getOrSetDefault(
            LHConstants.ADVERTISED_PROTOCOL_KEY, "http"
        );
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
        return getOrSetDefault(
            LHConstants.ADVERTISED_HOST_KEY, "localhost"
        );
    }

    public int getAdvertisedPort() {
        return Integer.valueOf(getOrSetDefault(
            LHConstants.ADVERTISED_PORT_KEY, "5000"
        ));
    }

    public int getExposedPort() {
        return Integer.valueOf(getOrSetDefault(
            LHConstants.EXPOSED_PORT_KEY, "5000"
        ));
    }

    public HostInfo getHostInfo() {
        return new HostInfo(getAdvertisedHost(), getAdvertisedPort());
    }

    public void cleanup() {
        if (this.kafkaAdmin != null) this.kafkaAdmin.close();

        for (KafkaProducer<?, ?> p: producersToClose) p.close();
        for (KafkaConsumer<?, ?> c: consumersToClose) c.close();
    }

    public LHApiClient getApiClient() {
        return new LHApiClient();
    }

    public <U, T extends Serializer<U>> KafkaProducer<String, U> getKafkaProducer(
        Class<T> serializerClass
    ) {
        if (seenProducerTypes == null) seenProducerTypes = new HashSet<>();

        if (seenProducerTypes.contains(serializerClass.getCanonicalName())) {
            throw new RuntimeException(
                "Twice got consumer with " + serializerClass.getCanonicalName()
            );
        }
        seenProducerTypes.add(serializerClass.getCanonicalName());

        Properties conf = new Properties();
        conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        conf.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        conf.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass);
        conf.put(
            ProducerConfig.CLIENT_ID_CONFIG,
            getKafkaGroupId() + "-" + getKafkaInstanceId()
        );
        conf.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringSerializer.class
        );

        KafkaProducer<String, U> prod = new KafkaProducer<String, U>(conf);
        producersToClose.add(prod);
        return prod;
    }

    public <U, T extends Deserializer<U>> KafkaConsumer<String, U> getKafkaConsumer(
        Class<T> deserializerClass
    ) {
        if (seenConsumerTypes == null) {
            seenConsumerTypes = new HashSet<>();
        }

        if (seenConsumerTypes.contains(deserializerClass.getCanonicalName())) {
            throw new RuntimeException(
                "Twice got consumer with " + deserializerClass.getCanonicalName()
            );
        }
        seenConsumerTypes.add(deserializerClass.getCanonicalName());

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
        conf.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass);

        // Uncomment when done with production.
        // conf.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        KafkaConsumer<String, U> cons = new KafkaConsumer<String, U>(conf);
        consumersToClose.add(cons);
        return cons;
    }

    public Properties getStreamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getKafkaGroupId());
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, this.getKafkaInstanceId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.STATE_DIR_CONFIG, this.getStateDirectory());
        props.put(StreamsConfig.METADATA_MAX_AGE_CONFIG, 4000);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once_v2");
        props.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, "all");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, getReplicationFactor());
        props.put(
            StreamsConfig.consumerPrefix(ConsumerConfig.METADATA_MAX_AGE_CONFIG), 4000
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
            StreamsConfig.NUM_STREAM_THREADS_CONFIG,
            Integer.valueOf(
                getOrSetDefault(LHConstants.NUM_STREAM_THREADS_KEY, "1")
            )
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
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            Serdes.StringSerde.class.getName()
        );
        props.put(
            StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
            getStreamsCommitInterval()
        );
        return props;
    }

    public int getStreamsCommitInterval() {
        return Integer.valueOf(getOrSetDefault(
            LHConstants.STREAMS_COMMIT_INTERVAL,
            "50"
        ));
    }

    public int getStandbyReplicas() {
        return Integer.valueOf(
            getOrSetDefault(LHConstants.NUM_STANDBY_REPLICAS_KEY, "1")
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
            if (e.getCause() != null && e.getCause() instanceof TopicExistsException) {
                System.out.println("Topic " + topic.name() + " already exists.");
            } else {
                throw e;
            }
        }
    }

    private void initialize(Properties overrides) {
        Properties origDefaults = getEnvDefaults();
        for (String k: overrides.stringPropertyNames()) {
            origDefaults.setProperty(k, overrides.getProperty(k));
        }
        props = origDefaults;

        Properties akProperties = new Properties();
        akProperties.put(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
            getBootstrapServers()
        );
        consumersToClose = new HashSet<>();
        producersToClose = new HashSet<>();
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

        for (Map.Entry<String, String> entry: System.getenv().entrySet()) {
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

    public static Javalin createAppWithHealth(KStreamsStateListener listener) {
        Javalin app = Javalin.create(javalinConf -> {
            javalinConf.prefer405over404 = true;
            javalinConf.enableCorsForAllOrigins();
        });

        app.get("/health", (ctx) -> {
            if (listener.getState() == KafkaStreams.State.RUNNING) {
                ctx.status(200);
                ctx.result("OK");
            } else {
                ctx.status(500);
                ctx.result(listener.getState().toString());
            }
        });
        return app;
    }
}
