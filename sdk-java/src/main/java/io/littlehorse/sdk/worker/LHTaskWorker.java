package io.littlehorse.sdk.worker;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.LHTaskSignature;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;
import io.littlehorse.sdk.worker.internal.LHLivenessController;
import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private LHConfig config;
    private TaskDef taskDef;
    private Method taskMethod;
    private List<VariableMapping> mappings;
    private LHServerConnectionManager manager;
    private String taskDefName;
    private String lhTaskMethodAnnotationValue;
    private LittleHorseBlockingStub grpcClient;

    /**
     * Creates an LHTaskWorker given an Object that has an annotated LHTaskMethod, and a
     * configuration Properties object.
     *
     * @param executable  is any Object which has exactly one method annotated with '@LHTaskMethod'.
     *                    That method will be used to execute the tasks.
     * @param taskDefName is the name of the `TaskDef` to execute.
     * @param config      is a valid LHConfig.
     */
    public LHTaskWorker(Object executable, String taskDefName, LHConfig config) {
        this.config = config;
        this.executable = executable;
        this.mappings = new ArrayList<>();
        this.taskDefName = taskDefName;
        this.lhTaskMethodAnnotationValue = taskDefName;
        this.grpcClient = config.getBlockingStub();
    }

    /**
     *  Creates an LHTaskWorker given an Object that has an annotated LHTaskMethod, and a
     *  configuration Properties object. You can have placeholders in the taskDefName in the form of:
     *  a-task-name-${PLACEHOLDER_1}-${PLACEHOLDER-2}.
     *  Each placeholder should be replaced by its corresponding value coming from the valuesForPlaceHolders map.
     *  PLACEHOLDER_1: VALUE_1
     *  PLACEHOLDER_2: VALUE_2
     *  So after the values are replaced, you will have a taskDefName like: a-task-name-VALUE_1-VALUE_2
     *
     * @param executable is any Object which has exactly one method annotated with '@LHTaskMethod'.
     *      *                    That method will be used to execute the tasks.
     * @param taskDefNameTemplate is the name of the `TaskDef` to execute. May contain placeholders.
     * @param config is a valid LHConfig.
     * @param valuesForPlaceholders map of values that will replace the placeholders on the taskDefNameTemplate.
     */
    public LHTaskWorker(
            Object executable, String taskDefNameTemplate, LHConfig config, Map<String, String> valuesForPlaceholders) {
        this.config = config;
        this.executable = executable;
        this.mappings = new ArrayList<>();
        this.taskDefName = replacePlaceholdersInTaskDefName(taskDefNameTemplate, valuesForPlaceholders);
        this.lhTaskMethodAnnotationValue = taskDefNameTemplate;
        this.grpcClient = config.getBlockingStub();
    }

    public LHTaskWorker(
            Object executable,
            String taskDefName,
            Map<String, String> valuesForPlaceHolders,
            LHConfig config,
            LHServerConnectionManager manager) {
        this(executable, taskDefName, config, valuesForPlaceHolders);
        this.manager = manager;
    }

    /**
     * `TaskDef` to execute
     *
     * @return the name of the `TaskDef` to execute
     */
    public String getTaskDefName() {
        return taskDefName;
    }

    private void createManager() {
        validateTaskDefAndExecutable();
        if (this.manager == null) {
            this.manager = new LHServerConnectionManager(
                    taskDef,
                    config.getAsyncStub(),
                    config.getTaskWorkerId(),
                    new LHLivenessController(),
                    taskMethod,
                    mappings,
                    executable,
                    config);
        }
    }

    /**
     * Checks if the TaskDef exists
     *
     * @return true if the task is registered or false otherwise
     */
    public boolean doesTaskDefExist() {
        try {
            grpcClient.getTaskDef(TaskDefId.newBuilder().setName(taskDefName).build());
            return true;
        } catch (StatusRuntimeException exn) {
            if (exn.getStatus().getCode() == Code.NOT_FOUND) {
                return false;
            }
            throw exn;
        }
    }

    /**
     * Deploys the TaskDef object to the LH Server. This is a convenience method, generally not
     * recommended for production (in production you should manually use the PutTaskDef).
     */
    public void registerTaskDef() {
        TaskDefBuilder tdb = new TaskDefBuilder(executable, this.taskDefName, this.lhTaskMethodAnnotationValue);
        TaskDef result = grpcClient.putTaskDef(tdb.toPutTaskDefRequest());
        log.info("Created TaskDef:\n{}", LHLibUtil.protoToJson(result));
    }

    private void validateTaskDefAndExecutable() throws TaskSchemaMismatchError {
        if (this.taskDef == null) {
            long start = System.currentTimeMillis();
            long timeout = start + Duration.ofSeconds(2).toMillis();
            do {
                try {
                    TaskDef taskDef = grpcClient.getTaskDef(
                            TaskDefId.newBuilder().setName(taskDefName).build());
                    if (taskDef != null) {
                        this.taskDef = taskDef;
                        break;
                    }
                } catch (StatusRuntimeException exn) {
                    if (!(exn.getStatus().getCode() == Code.NOT_FOUND)) {
                        throw exn;
                    }
                }

            } while (System.currentTimeMillis() < timeout);
        }

        LHTaskSignature signature =
                new LHTaskSignature(taskDef.getId().getName(), executable, this.lhTaskMethodAnnotationValue);
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
     */
    public void start() {
        createManager();
        manager.start();
    }

    /**
     * Cleanly shuts down the Task Worker.
     */
    public void close() {
        if (manager != null) {
            manager.close();
        }
    }

    public LHTaskWorkerHealth healthStatus() {
        if (manager == null) {
            throw new IllegalStateException("Worker not started");
        }
        return manager.healthStatus();
    }

    private static String replacePlaceholdersInTaskDefName(String template, Map<String, String> values) {
        final StringBuilder resultingText = new StringBuilder();

        final Pattern placeholderPattern = Pattern.compile("\\$\\{(.*?)\\}", Pattern.DOTALL);

        final Matcher matcher = placeholderPattern.matcher(template);

        while (matcher.find()) {
            final String placeholderKey = matcher.group(1);
            final String replacement = values.get(placeholderKey);

            if (replacement == null) {
                throw new IllegalArgumentException(
                        "No value has been provided for the placeholder with key: " + placeholderKey);
            }
            matcher.appendReplacement(resultingText, replacement);
        }

        matcher.appendTail(resultingText);
        return resultingText.toString();
    }
}
