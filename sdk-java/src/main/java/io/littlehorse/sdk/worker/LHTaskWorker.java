package io.littlehorse.sdk.worker;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.LHTaskSignature;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;
import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * The LHTaskWorker talks to the LH Servers and executes a specified Task Method every time a Task
 * is scheduled.
 */
@Slf4j
public class LHTaskWorker implements Closeable {

    public static HashMap<Class<?>, VariableType> javaTypeToLHType = new HashMap<>() {
        {
            put(Integer.class, VariableType.INT);
            put(Long.class, VariableType.INT);
            put(Boolean.class, VariableType.BOOL);
            put(Double.class, VariableType.DOUBLE);
            put(byte[].class, VariableType.BYTES);
            put(String.class, VariableType.STR);
        }
    };

    private Object executable;
    private LHWorkerConfig config;
    private TaskDef taskDef;
    private Method taskMethod;
    private List<VariableMapping> mappings;
    private LHServerConnectionManager manager;
    private String taskDefName;
    private LHPublicApiBlockingStub grpcClient;

    /**
     * Creates an LHTaskWorker given an Object that has an annotated LHTaskMethod, and a
     * configuration Properties object.
     *
     * @param executable is any Object which has exactly one method annotated with '@LHTaskMethod'.
     *     That method will be used to execute the tasks.
     * @param taskDefName is the name of the `TaskDef` to execute.
     * @param config is a valid LHWorkerConfig.
     */
    public LHTaskWorker(Object executable, String taskDefName, LHWorkerConfig config) throws IOException {
        this.config = config;
        this.executable = executable;
        this.mappings = new ArrayList<>();
        this.taskDefName = taskDefName;
        this.grpcClient = config.getBlockingStub();
    }

    /**
     * `TaskDef` to execute
     *
     * @return the name of the `TaskDef` to execute
     */
    public String getTaskDefName() {
        return taskDefName;
    }

    private void createManager() throws IOException {
        validateTaskDefAndExecutable();
        this.manager = new LHServerConnectionManager(taskMethod, taskDef, config, mappings, executable);
    }

    /**
     * Checks if the TaskDef exists
     *
     * @return true if the task is registered or false otherwise
     * @throws LHApiError if the call fails.
     */
    public boolean doesTaskDefExist() {
        try {
            grpcClient.getTaskDef(TaskDefId.newBuilder().setName(taskDefName).build());
            return true;
        } catch (StatusRuntimeException exn) {
            System.out.println("Status: " + exn.getStatus());
            if (exn.getStatus().getCode() == Code.NOT_FOUND) {
                return false;
            }
            throw exn;
        }
    }

    /**
     * Deploys the TaskDef object to the LH Server. This is a convenience method, generally not
     * recommended for production (in production you should manually use the PutTaskDef).
     *
     * @throws LHApiError if the call fails.
     */
    public void registerTaskDef() {
        registerTaskDef(false);
    }

    /**
     * Deploys the TaskDef object to the LH Server. This is a convenience method, generally not
     * recommended for production (in production you should manually use the PutTaskDef).
     *
     * @param swallowAlreadyExists if true, then ignore grpc ALREADY_EXISTS error when registering
     *     the TaskDef.
     */
    public void registerTaskDef(boolean swallowAlreadyExists) {
        TaskDefBuilder tdb = new TaskDefBuilder(executable, taskDefName);
        log.info("Creating TaskDef: {}", taskDefName);

        try {
            TaskDef result = grpcClient.putTaskDef(tdb.toPutTaskDefRequest());
            log.info("Created TaskDef:\n{}", LHLibUtil.protoToJson(result));

        } catch (StatusRuntimeException exn) {
            if (swallowAlreadyExists && exn.getStatus().getCode() == Code.ALREADY_EXISTS) {
                log.info("TaskDef {} already exists!", taskDefName);
            } else {
                throw exn;
            }
        }
    }

    private void validateTaskDefAndExecutable() throws TaskSchemaMismatchError {
        if (this.taskDef == null) {
            this.taskDef = grpcClient.getTaskDef(
                    TaskDefId.newBuilder().setName(taskDefName).build());
        }
        LHTaskSignature signature = new LHTaskSignature(taskDef.getName(), executable);
        taskMethod = signature.getTaskMethod();

        int numTaskMethodParams = taskMethod.getParameterCount();
        int numTaskDefParams = taskDef.getInputVarsCount();

        boolean wrongNumParams = false;
        if (signature.getHasWorkerContextAtEnd()) {
            if (numTaskMethodParams - 1 != numTaskDefParams) {
                wrongNumParams = true;
            }
        } else if (numTaskDefParams != numTaskMethodParams) {
            wrongNumParams = true;
        }

        if (wrongNumParams) {
            throw new TaskSchemaMismatchError("Number of task method params doesn't match number of taskdef params!");
        }

        for (int i = 0; i < numTaskDefParams; i++) {
            Parameter param = taskMethod.getParameters()[i];
            String javaParamName = param.getName();
            Class<?> paramClass = param.getType();

            if (paramClass.equals(WorkerContext.class)) {
                throw new TaskSchemaMismatchError("Can only have WorkerContext after all required taskDef params.");
            }

            // This line throws a TaskSchemaMismatchError if the param can't
            // be provided properly.
            VariableMapping mapping = new VariableMapping(taskDef, i, paramClass, javaParamName);
            mappings.add(mapping);
        }

        if (signature.getHasWorkerContextAtEnd()) {
            mappings.add(new VariableMapping(taskDef, numTaskMethodParams - 1, WorkerContext.class, null));
        }
    }

    /**
     * Starts polling for and executing tasks.
     *
     * @throws LHApiError if the schema from the TaskDef configured in the configProps is
     *     incompatible with the method signature from the provided executable Java object, or if
     *     the Worker cannot connect to the LH Server.
     */
    public void start() throws IOException {
        if (!doesTaskDefExist()) {
            throw new LHMisconfigurationException("Couldn't find TaskDef: " + taskDefName);
        }
        createManager();
        manager.start();
    }

    /** Cleanly shuts down the Task Worker. */
    public void close() {
        if (manager != null) {
            manager.close();
        }
    }
}
