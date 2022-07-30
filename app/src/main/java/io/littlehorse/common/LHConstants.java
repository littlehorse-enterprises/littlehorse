package io.littlehorse.common;

import java.time.Duration;

public class LHConstants {
    public static String KAFKA_BOOTSTRAP_KEY = "LHORSE_KAFKA_BOOTSTRAP";
    public static String KAFKA_GROUP_ID_KEY = "LHORSE_KAFKA_GROUP_ID";
    public static String KAFKA_GROUP_IID_KEY = "LHORSE_KAFKA_GROUP_IID";
    public static String KAFKA_STATE_DIR_KEY = "LHORSE_KAFKA_STATE_DIR";

    public static String REPLICATION_FACTOR_KEY = "LHORSE_REPLICATION_FACTOR";
    public static String TASK_PARTITIONS_KEY = "LHORSE_TASK_PARTITIONS";
    public static String CLUSTER_PARTITIONS_KEY = "LHORSE_CLUSTER_PARTITIONS";
    public static String NUM_STREAM_THREADS_KEY = "LHORSE_NUM_STREAM_THREADS";
    public static String NUM_STANDBY_REPLICAS_KEY = "LHORSE_NUM_STANDBY_REPLICAS";
    public static String STREAMS_COMMIT_INTERVAL = "LHORSE_STREAMS_COMMIT_INTERVAL";

    public static String WF_RUN_EVENT_TOPIC = "WFRun_Event";
    public static String WF_RUN_ENTITY_TOPIC = "WFRun_Entity";
    public static String WF_RUN_STORE_NAME = "wfrun";
    public static String WF_SPEC_STORE_NAME = "wfSpec";
    public static String WF_SPEC_ENTITY_TOPIC = "WFSpec_Entity";
    public static String INDEX_STORE_NAME = "INDEX_STORE";

    public static String ADVERTISED_PROTOCOL_KEY = "LHORSE_ADVERTISED_PROTOCOL";
    public static String ADVERTISED_HOST_KEY = "LHORSE_ADVERTISED_HOST";
    public static String ADVERTISED_PORT_KEY = "LHORSE_ADVERTISED_PORT";
    public static String EXPOSED_PORT_KEY = "LHORSE_EXPOSED_PORT";
    public static String STATE_DIR_KEY = "LHORSE_KAFKA_STREAMS_STATE_DIR";

    public static Duration PUNCTUATOR_INERVAL = Duration.ofSeconds(4);
}

// NOTE: Use m6a.4xlarge