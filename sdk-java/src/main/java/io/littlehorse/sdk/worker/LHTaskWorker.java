package io.littlehorse.sdk.worker;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.LHTaskSignature;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.TaskDefBuilder;
import io.littlehorse.sdk.worker.internal.LHLivenessController;
import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import io.littlehorse.sdk.worker.internal.util.PlaceholderUtil;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * The LHTaskWorker talks to the LH Servers and executes a specified Task Method every time a Task
 * is scheduled.
 */
@Slf4j
public class LHTaskWorker implements Closeable {
    private Object executable;
    private LHConfig config;
    private TaskDef taskDef;
    private Method taskMethod;
    private List<VariableMapping> mappings;
    private LHServerConnectionManager manager;
    private String taskDefName;
    private String lhTaskMethodAnnotationValue;
    private LittleHorseBlockingStub grpcClient;
    private TaskDefBuilder tdb;
    private Map<String, String> placeholderValues;

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
        this.placeholderValues = Map.of();
        this.taskDefName = taskDefName;
        this.lhTaskMethodAnnotationValue = taskDefName;
        this.grpcClient = config.getBlockingStub();
        this.tdb = new TaskDefBuilder(
                executable,
                this.taskDefName,
                this.lhTaskMethodAnnotationValue,
                config.getTypeAdapterRegistry(),
                this.placeholderValues);
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
        this.placeholderValues = valuesForPlaceholders == null ? Map.of() : Map.copyOf(valuesForPlaceholders);
        this.taskDefName = replacePlaceholders(taskDefNameTemplate, this.placeholderValues);
        this.lhTaskMethodAnnotationValue = taskDefNameTemplate;
        this.grpcClient = config.getBlockingStub();
        this.tdb = new TaskDefBuilder(
                executable,
                this.taskDefName,
                this.lhTaskMethodAnnotationValue,
                config.getTypeAdapterRegistry(),
                this.placeholderValues);
    }

    /**
     * Creates a task worker with a pre-created server connection manager.
     *
     * @param executable is any Object which has exactly one method annotated with '@LHTaskMethod'.
     *                   That method will be used to execute the tasks.
     * @param taskDefNameTemplate is the name of the `TaskDef` to execute. May contain placeholders.
     * @param valuesForPlaceholders map of values that will replace the placeholders on the taskDefNameTemplate.
     * @param config      is a valid LHConfig.
     * @param manager server connection manager to use
     */
    public LHTaskWorker(
            Object executable,
            String taskDefNameTemplate,
            Map<String, String> valuesForPlaceholders,
            LHConfig config,
            LHServerConnectionManager manager) {
        this(executable, taskDefNameTemplate, config, valuesForPlaceholders);
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
        validateStructDefs(StructDefCompatibilityType.NO_SCHEMA_UPDATES);

        TaskDef result = grpcClient.putTaskDef(tdb.toPutTaskDefRequest());
        log.info("Created TaskDef:\n{}", LHLibUtil.protoToJson(result));
    }

    /**
     * Validates StructDef classes used in your Task Definitions against StructDefs on the server.
     *
     * @param compatibilityType The server will validate the given StructDef schema against
     *                          the existing StructDef schema based on this compatibility type.
     */
    public void validateStructDefs(StructDefCompatibilityType compatibilityType) {
        if (tdb.getStructDefDependencies().isEmpty()) return;

        for (LHStructDefType lhStructDefType : tdb.getStructDefDependencies()) {
            validateStructDef(lhStructDefType, compatibilityType);
        }
    }

    /**
     * Validates whether or not you can evolve your StructDef with the selected compatibility type
     *
     * @param structClass       The class for your StructDef
     * @param compatibilityType The server will validate the given StructDef schemas against
     *                          their existing StructDef schemas based on this compatibility type.
     */
    public void validateStructDef(Class<?> structClass, StructDefCompatibilityType compatibilityType) {
        LHStructDefType lhStructDefType = new LHStructDefType(structClass, config.getTypeAdapterRegistry());

        validateStructDef(lhStructDefType, compatibilityType);
    }

    private void validateStructDef(LHStructDefType lhStructDefType, StructDefCompatibilityType compatibilityType) {
        StructDef structDef = lhStructDefType.toStructDef();

        ValidateStructDefEvolutionRequest.Builder validateStructDefRequest =
                ValidateStructDefEvolutionRequest.newBuilder();
        validateStructDefRequest.setStructDefId(structDef.getId());
        validateStructDefRequest.setStructDef(structDef.getStructDef());
        validateStructDefRequest.setCompatibilityType(compatibilityType);

        ValidateStructDefEvolutionResponse resp =
                grpcClient.validateStructDefEvolution(validateStructDefRequest.build());

        if (!resp.getIsValid()) {
            throw new IllegalArgumentException(String.format(
                    "Unable to evolve StructDef %s using StructDefCompatibilityType %s",
                    structDef.getId().getName(), compatibilityType));
        }
    }

    private void registerStructDef(LHStructDefType lhStructDefType, StructDefCompatibilityType compatibilityType) {
        StructDef structDef = lhStructDefType.toStructDef();
        PutStructDefRequest.Builder putStructDefRequest = PutStructDefRequest.newBuilder();
        putStructDefRequest.setName(structDef.getId().getName());
        putStructDefRequest.setDescription(structDef.getDescription());
        putStructDefRequest.setStructDef(structDef.getStructDef());
        putStructDefRequest.setAllowedUpdates(compatibilityType);

        grpcClient.putStructDef(putStructDefRequest.build());
    }

    /**
     * Registers a single StructDef based on the StructDef class
     *
     * Note: If your StructDef depends on other StructDefs, ensure you register them
     * in the right order. This method does not handle registering StructDef dependencies.
     *
     * @param structClass       The class for your StructDef
     * @param compatibilityType The server will try to register the given StructDef
     *                          according to this compatibility type.
     */
    public void registerStructDef(Class<?> structClass, StructDefCompatibilityType compatibilityType) {
        LHStructDefType lhStructDefType = new LHStructDefType(structClass, config.getTypeAdapterRegistry());

        registerStructDef(lhStructDefType, compatibilityType);
    }

    /**
     * Validates StructDef classes used in your Task Definitions against StructDefs on the server.
     *
     * @param compatibilityType The server will try to register the given StructDefs
     *                          according to this compatibility type.
     */
    public void registerStructDefs(StructDefCompatibilityType compatibilityType) {
        List<LHStructDefType> lhStructDefTypes = tdb.getStructDefDependencies();

        if (lhStructDefTypes.isEmpty()) return;

        for (LHStructDefType lhStructDefType : lhStructDefTypes) {
            registerStructDef(lhStructDefType, compatibilityType);
        }
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
                    Thread.sleep(10);
                } catch (StatusRuntimeException exn) {
                    if (!(exn.getStatus().getCode() == Code.NOT_FOUND)) {
                        throw exn;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            } while (System.currentTimeMillis() < timeout);

            if (this.taskDef == null) {
                throw new IllegalStateException("TaskDef '" + taskDefName
                        + "' was not found on the server. Register it before starting this worker.");
            }
        }

        LHTaskSignature signature = new LHTaskSignature(
                taskDef.getId().getName(),
                executable,
                this.lhTaskMethodAnnotationValue,
                config.getTypeAdapterRegistry(),
                placeholderValues);
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
            LHType lhTypeAnnotation = param.getAnnotation(LHType.class);
            String inlineStructDefName = null;
            if (lhTypeAnnotation != null && !lhTypeAnnotation.structDefName().isBlank()) {
                inlineStructDefName = replacePlaceholders(lhTypeAnnotation.structDefName(), placeholderValues);
            }

            if (paramClass.equals(WorkerContext.class)) {
                throw new TaskSchemaMismatchError("Can only have WorkerContext after all required taskDef params.");
            }

            // This line throws a TaskSchemaMismatchError if the param can't
            // be provided properly.
            VariableMapping mapping = new VariableMapping(
                    taskDef, i, paramClass, javaParamName, inlineStructDefName, config.getTypeAdapterRegistry());
            mappings.add(mapping);
        }

        if (signature.getHasWorkerContextAtEnd()) {
            mappings.add(new VariableMapping(
                    taskDef, numTaskMethodParams - 1, WorkerContext.class, null, config.getTypeAdapterRegistry()));
        }
    }

    /**
     * Starts polling for and executing tasks.
     */
    public void start() {
        log.info("Starting task worker for TaskDef {}", taskDefName);
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

    /**
     * Tests if this worker is alive. A worker is alive if it has been started and has not yet terminated.
     * @return true if this thread is not alive; false otherwise.
     */
    public boolean isClosed() {
        return manager.isClosed();
    }

    /**
     * Determine if a worker is healthy. A worker could be running but not healthy.
     * @return LHTaskWorkerHealth
     */
    public LHTaskWorkerHealth healthStatus() {
        if (manager == null) {
            throw new IllegalStateException("Worker not started");
        }
        return manager.healthStatus();
    }

    private static String replacePlaceholders(String template, Map<String, String> values) {
        return PlaceholderUtil.replacePlaceholders(template, values);
    }
}
