package io.littlehorse.common;

import java.time.Duration;

public class LHConstants {
    public static final String KAFKA_BOOTSTRAP_KEY = "LHORSE_KAFKA_BOOTSTRAP";
    public static final String KAFKA_GROUP_ID_KEY = "LHORSE_KAFKA_GROUP_ID";
    public static final String KAFKA_GROUP_IID_KEY = "LHORSE_KAFKA_GROUP_IID";

    public static final String REPLICATION_FACTOR_KEY = "LHORSE_REPLICATION_FACTOR";
    public static final String TASK_PARTITIONS_KEY = "LHORSE_TASK_PARTITIONS";
    public static final String CLUSTER_PARTITIONS_KEY = "LHORSE_CLUSTER_PARTITIONS";
    public static final String NUM_STREAM_THREADS_KEY = "LHORSE_NUM_STREAM_THREADS";
    public static final String NUM_STANDBY_REPLICAS_KEY = "LHORSE_NUM_STANDBY_REPLICAS";
    public static final String COMMIT_INTERVAL_KEY = "LHORSE_COMMIT_INTERVAL";
    public static final String API_HOST_KEY = "LHORSE_API_HOST";
    public static final String API_PORT_KEY = "LHORSE_API_PORT";
    public static final String DEFAULT_TIMEOUT_KEY = "LHORSE_DEFAULT_TIMEOUT";
    public static final String NUM_WORKER_THREADS_KEY = "LHORSE_NUM_WORKER_THREADS";
    public static final String RACK_ID_KEY = "LHORSE_RACK_ID";

    public static final String WF_RUN_EVENT_TOPIC = "WFRun_Event";
    public static final String TIMER_TOPIC_NAME = "Timer";
    public static final String WF_RUN_OBSERVABILITY_TOPIC = "WFRun_Observability";

    public static final String SCHED_WF_RUN_STORE_NAME = "Scheduler_WfRunState_Store";
    public static final String TIMER_STORE_NAME = "Timer_Store";
    public static final String INDEX_STORE_NAME = "Core_Server_Index_Store";
    public static final String INDEX_TOPIC_NAME = "Index_Entry_Actions";

    public static final String ADVERTISED_PROTOCOL_KEY = "LHORSE_ADVERTISED_PROTOCOL";
    public static final String ADVERTISED_HOST_KEY = "LHORSE_ADVERTISED_HOST";
    public static final String ADVERTISED_PORT_KEY = "LHORSE_ADVERTISED_PORT";
    public static final String EXPOSED_PORT_KEY = "LHORSE_EXPOSED_PORT";
    public static final String KAFKA_STATE_DIR_KEY = "LHORSE_KAFKA_STREAMS_STATE_DIR";

    public static final Duration PUNCTUATOR_INERVAL = Duration.ofSeconds(4);
    public static final Duration TICKER_INTERVAL = Duration.ofSeconds(4);

    public static final String OBJECT_ID_HEADER = "STORE_KEY";
}

// NOTE: Use m6a.4xlarge