package io.littlehorse.common;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LHConstants {

    // Kafka and Kafka Streams-Specific Configuration Env Vars
    public static final String KAFKA_BOOTSTRAP_KEY = "LHORSE_KAFKA_BOOTSTRAP";
    public static final String KAFKA_GROUP_ID_KEY = "LHORSE_KAFKA_GROUP_ID";
    public static final String KAFKA_GROUP_IID_KEY = "LHORSE_KAFKA_GROUP_IID";

    public static final String REPLICATION_FACTOR_KEY = "LHORSE_REPLICATION_FACTOR";
    public static final String TASK_PARTITIONS_KEY = "LHORSE_TASK_PARTITIONS";
    public static final String CLUSTER_PARTITIONS_KEY = "LHORSE_CLUSTER_PARTITIONS";
    public static final String NUM_STREAM_THREADS_KEY = "LHORSE_NUM_STREAM_THREADS";
    public static final String COMMIT_INTERVAL_KEY = "LHORSE_COMMIT_INTERVAL";
    public static final String RACK_ID_KEY = "LHORSE_RACK_ID";
    public static final String KAFKA_STATE_DIR_KEY = "LHORSE_KAFKA_STREAMS_STATE_DIR";
    public static final String NUM_STANDBY_REPLICAS_KEY =
        "LHORSE_NUM_STANDBY_REPLICAS";

    // General LittleHorse Runtime Behavior Config Env Vars
    public static final String DEFAULT_TIMEOUT_KEY = "LHORSE_DEFAULT_TIMEOUT";
    public static final String KAFKA_TOPIC_PREFIX_KEY = "LHORSE_KAFKA_TOPIC_PREFIX";

    // Host and Port Configuration Env Vars
    public static final String BIND_HOST_KEY = "LHORSE_BIND_HOST";
    public static final String ADVERTISED_HOST_KEY = "LHORSE_ADVERTISED_HOST";
    public static final String ADVERTISED_PROTOCOL_KEY = "LHORSE_ADVERTISED_PROTOCOL";
    public static final String API_ADVERTISED_PORT_KEY = "LHORSE_API_ADVERTISED_PORT";
    public static final String API_BIND_PORT_KEY = "LHORSE_API_BIND_PORT";
    public static final String INTERNAL_BIND_PORT_KEY = "LHORSE_INTERNAL_BIND_PORT";
    public static final String INTERNAL_ADVERTISED_PORT_KEY =
        "LHORSE_INTERNAL_ADVERTISED_PORT";

    // Other various constants used by code
    public static final Duration PUNCTUATOR_INERVAL = Duration.ofSeconds(4);
    public static final Duration TICKER_INTERVAL = Duration.ofSeconds(4);
    public static final String EXT_EVT_HANDLER_VAR = "INPUT";

    // Make all global metadata use the same partition key so that they're processed
    // on the same node. This guarantees ordering. Note that metadata is low
    // throughput and low volume so partitioning is not needed.
    public static final String META_PARTITION_KEY = "METADATA";

    // Reserved `FailureDef` names
    public static final String CHILD_FAILURE = "CHILD_FAILURE";
    public static final String VAR_SUB_ERROR = "VAR_SUB_ERROR";
    public static final String VAR_MUTATION_ERROR = "VAR_MUTATION_ERROR";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String TASK_FAILURE = "TASK_FAILURE";
    public static final String VAR_ERROR = "VAR_ERROR";
    public static final String TASK_ERROR = "TASK_ERROR";
    public static final Set<String> RESERVED_EXCEPTION_NAMES = new HashSet<>(
        Arrays.asList(
            CHILD_FAILURE,
            VAR_SUB_ERROR,
            VAR_MUTATION_ERROR,
            TASK_FAILURE,
            TIMEOUT,
            VAR_ERROR,
            TASK_ERROR
        )
    );

    public static final int DEFAULT_LIMIT = 1000;
}
// NOTE: Use m6a.4xlarge
