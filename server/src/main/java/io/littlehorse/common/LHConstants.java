package io.littlehorse.common;

import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.ServerACL;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LHConstants {

    // Other various constants used by code
    public static final Duration TIMER_PUNCTUATOR_INTERVAL = Duration.ofMillis(500);
    public static final String EXT_EVT_HANDLER_VAR = "INPUT";

    // Make all global metadata use the same partition key so that they're processed
    // on the same node. This guarantees ordering. Note that metadata is low
    // throughput and low volume so partitioning is not needed.
    public static final String META_PARTITION_KEY = "METADATA";
    public static final String PARTITION_CLAIM_KEY = "PARTITION_CLAIM";
    public static final String PARTITION_CLAIM_GUID_HEADER = "PARTITION_CLAIM_GUID";

    // Reserved `FailureDef` names
    public static final String CHILD_FAILURE = "CHILD_FAILURE";
    public static final String VAR_SUB_ERROR = "VAR_SUB_ERROR";
    public static final String VAR_MUTATION_ERROR = "VAR_MUTATION_ERROR";
    public static final String USER_TASK_CANCELLED = "USER_TASK_CANCELLED";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String TASK_FAILURE = "TASK_FAILURE";
    public static final String VAR_ERROR = "VAR_ERROR";
    public static final String TASK_ERROR = "TASK_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final Set<String> RESERVED_EXCEPTION_NAMES = new HashSet<>(Arrays.asList(
            CHILD_FAILURE,
            VAR_SUB_ERROR,
            VAR_MUTATION_ERROR,
            TASK_FAILURE,
            TIMEOUT,
            VAR_ERROR,
            TASK_ERROR,
            INTERNAL_ERROR,
            USER_TASK_CANCELLED));

    public static final int DEFAULT_TASK_TIMEOUT_SECONDS = 60;

    public static final int DEFAULT_LIMIT = 1000;
    public static final int MIN_WORKER_ASSIGNMENT_BY_SERVER = 2;
    public static final long MAX_TASK_WORKER_INACTIVITY = 15L;

    public static final String CLUSTER_LEVEL_METRIC = "CLUSTER_LEVEL_METRIC";

    public static final int INFINITE_RETENTION = -1;

    public static final String DEFAULT_TENANT = "default";
    public static final String ANONYMOUS_PRINCIPAL = "anonymous";

    public static final ServerACL ADMIN_ACL = ServerACL.newBuilder()
            .addAllowedActions(ACLAction.ALL_ACTIONS)
            .addResources(ACLResource.ACL_ALL_RESOURCES)
            .build();

    public static final String TENANT_ID_HEADER_NAME = "tenantid";

    public static final String PRINCIPAL_ID_HEADER_NAME = "principalId";

    // Store key for metric cache
    public static final String PARTITION_METRICS_KEY = "partitionMetrics";

    public static final Duration MAX_INCOMING_REQUEST_IDLE_TIME = Duration.ofSeconds(60);
    public static final int MAX_TASKRUNS_IN_ONE_TASKQUEUE = 4;

    public static final String STRING_MASK = "*****";
}
// NOTE: Use m6a.4xlarge
