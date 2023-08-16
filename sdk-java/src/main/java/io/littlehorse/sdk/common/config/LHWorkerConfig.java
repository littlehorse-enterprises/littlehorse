package io.littlehorse.sdk.common.config;

import io.littlehorse.sdk.common.proto.GetTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.TaskDefIdPb;
import io.littlehorse.sdk.common.proto.TaskDef;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is a configuration class for the Task Workers.
 */
public class LHWorkerConfig extends LHClientConfig {

    /**
     * The number of worker threads to run.
     */
    public static final String NUM_WORKER_THREADS_KEY = "LHW_NUM_WORKER_THREADS";

    /**
     * Listener to connect to.
     */
    public static final String SERVER_CONNECT_LISTENER_KEY =
        "LHW_SERVER_CONNECT_LISTENER";
    public static final String TASK_WORKER_VERSION_KEY = "LHW_TASK_WORKER_VERSION";
    public static final String DEFAULT_PUBLIC_LISTENER = "PLAIN";

    private static final Set<String> configNames = Collections.unmodifiableSet(
        Set.of(
            LHWorkerConfig.NUM_WORKER_THREADS_KEY,
            LHWorkerConfig.SERVER_CONNECT_LISTENER_KEY,
            LHWorkerConfig.TASK_WORKER_VERSION_KEY
        )
    );

    public static Set<String> configNames() {
        return LHWorkerConfig.configNames;
    }

    /**
     * Creates an LHWorkerConfig using the environment variables as defaults.
     */
    public LHWorkerConfig() {
        super();
    }

    /**
     * Creates an LHWorkerConfig with provided properties.
     * @param props config values.
     */
    public LHWorkerConfig(Properties props) {
        super(props);
    }

    /**
     * Creates an LHWorkerConfig with a .properties file.
     * @param propLocation is the location of the .properties file.
     */
    public LHWorkerConfig(String propLocation) {
        super(propLocation);
    }

    /**
     * Creates an LHWorkerConfig with provided config values.
     * @param configs configuration values.
     */
    public LHWorkerConfig(Map<String, Object> configs) {
        super(configs);
    }

    /**
     * Gets the `TaskDefPb` for a given taskDefName.
     * @param taskDefName is the TaskDef's name.
     * @return the specified TaskDefPb.
     */
    public TaskDef getTaskDef(String taskDefName) {
        try {
            GetTaskDefReplyPb reply = getBlockingStub()
                .getTaskDef(TaskDefIdPb.newBuilder().setName(taskDefName).build());
            if (reply.getCode() != LHResponseCodePb.OK) {
                throw new RuntimeException(
                    "Failed loading taskDef: " + reply.getMessage()
                );
            }

            return reply.getResult();
        } catch (Exception exn) {
            throw new RuntimeException(exn);
        }
    }

    /**
     * Returns the TaskWorker Version of this worker.
     * @return Task Worker Version.
     */
    public String getTaskWorkerVersion() {
        return getOrSetDefault(TASK_WORKER_VERSION_KEY, "");
    }

    /**
     * Returns the API BootStrap Host.
     * @return the API Bootstrap Host.
     */
    public String getApiBootstrapHost() {
        return getOrSetDefault(API_HOST_KEY, "localhost");
    }

    /**
     * Returns the API Bootstrap Port.
     * @return the API Bootstrap port.
     */
    public int getApiBootstrapPort() {
        return Integer.valueOf(String.valueOf(getOrSetDefault(API_PORT_KEY, "2023")));
    }

    /**
     * Returns the name of the listener to connect to.
     * @return the name of the listener on the LH Server to connect to.
     */
    public String getConnectListener() {
        return getOrSetDefault(
            SERVER_CONNECT_LISTENER_KEY,
            LHWorkerConfig.DEFAULT_PUBLIC_LISTENER
        );
    }

    /**
     * Returns the prefixes of environment variables for the LHWorkerConfig class.
     */
    protected String[] getEnvKeyPrefixes() {
        return new String[] { "LHC_", "LHW_" };
    }

    /**
     * Returns the number of worker threads to run.
     * @return the number of worker threads to run.
     */
    public int getWorkerThreads() {
        return Integer.valueOf(getOrSetDefault(NUM_WORKER_THREADS_KEY, "8"));
    }
}
