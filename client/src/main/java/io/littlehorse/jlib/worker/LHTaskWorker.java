package io.littlehorse.jlib.worker;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.exception.TaskSchemaMismatchError;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.TaskDefPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.jlib.wfsdk.internal.taskdefutil.LHTaskSignature;
import io.littlehorse.jlib.wfsdk.internal.taskdefutil.TaskDefBuilder;
import io.littlehorse.jlib.worker.internal.LHServerConnectionManager;
import io.littlehorse.jlib.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LHTaskWorker talks to the LH Servers and executes a specified Task Method every
 * time a Task is scheduled.
 */
public class LHTaskWorker implements Closeable {

    private Logger log = LoggerFactory.getLogger(LHTaskWorker.class);

    public static HashMap<Class<?>, VariableTypePb> javaTypeToLHType = new HashMap<>() {
        {
            put(Integer.class, VariableTypePb.INT);
            put(Long.class, VariableTypePb.INT);
            put(Boolean.class, VariableTypePb.BOOL);
            put(Double.class, VariableTypePb.DOUBLE);
            put(byte[].class, VariableTypePb.BYTES);
            put(String.class, VariableTypePb.STR);
        }
    };

    private Object executable;
    private LHWorkerConfig config;
    private TaskDefPb taskDef;
    private Method taskMethod;
    private List<VariableMapping> mappings;
    private LHServerConnectionManager manager;
    private LHClient lhClient;
    private String taskDefName;

    /**
     * Creates an LHTaskWorker given an Object that has an annotated LHTaskMethod,
     * and a configuration Properties object.
     * @param executable is any Object which has exactly one method annotated with
     * '@LHTaskMethod'. That method will be used to execute the tasks.
     * @param taskDefName is the name of the `TaskDef` to execute.
     * @param config is a valid LHWorkerConfig.
     */
    public LHTaskWorker(
        Object executable,
        String taskDefName,
        LHWorkerConfig config
    ) {
        this.config = config;
        this.executable = executable;
        this.mappings = new ArrayList<>();
        this.lhClient = new LHClient(config);
        this.taskDefName = taskDefName;
    }

    private void createManager() throws LHApiError {
        try {
            validateTaskDefAndExecutable();

            this.manager =
                new LHServerConnectionManager(
                    taskMethod,
                    taskDef,
                    config,
                    mappings,
                    executable
                );
        } catch (TaskSchemaMismatchError exn) {
            throw new LHApiError(
                exn,
                "Provided java method does not match registered task!",
                LHResponseCodePb.BAD_REQUEST_ERROR
            );
        } catch (IOException exn) {
            throw new LHApiError(exn, "Couldn't create connection to LH");
        }
    }

    private void checkTaskDefExists() throws LHApiError {
        this.taskDef = lhClient.getTaskDef(taskDefName);

        if (this.taskDef == null) {
            throw new LHApiError(
                "Couldn't find TaskDef: " + taskDefName,
                LHResponseCodePb.NOT_FOUND_ERROR
            );
        }
    }

    /**
     * Deploys the  TaskDef object to the LH Server.
     * This is a convenience method, generally not recommended for
     * production (in production you should manually use the PutTaskDef).
     * @param swallowAlreadyExists if true, then ignore ALREADY_EXISTS_ERROR when
     * registering the TaskDef.
     * @throws LHApiError if the call fails.
     */
    public void registerTaskDef(boolean swallowAlreadyExists) throws LHApiError {
        try {
            TaskDefBuilder tdb = new TaskDefBuilder(executable, taskDefName);
            log.info(
                "Creating TaskDef:\n {}",
                LHLibUtil.protoToJson(
                    lhClient.putTaskDef(tdb.toPutTaskDefPb(), swallowAlreadyExists)
                )
            );
        } catch (TaskSchemaMismatchError exn) {
            log.error("Error registering task", exn);
            throw new LHApiError(
                exn,
                exn.getMessage(),
                LHResponseCodePb.VALIDATION_ERROR
            );
        }
    }

    private void validateTaskDefAndExecutable() throws TaskSchemaMismatchError {
        LHTaskSignature signature = new LHTaskSignature(
            taskDef.getName(),
            executable
        );
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
            throw new TaskSchemaMismatchError(
                "Number of task method params doesn't match number of taskdef params!"
            );
        }

        for (int i = 0; i < numTaskDefParams; i++) {
            Parameter param = taskMethod.getParameters()[i];
            String javaParamName = param.getName();
            Class<?> paramClass = param.getType();

            if (paramClass.equals(WorkerContext.class)) {
                throw new TaskSchemaMismatchError(
                    "Can only have WorkerContext after all required taskDef params."
                );
            }

            // This line throws a TaskSchemaMismatchError if the param can't
            // be provided properly.
            VariableMapping mapping = new VariableMapping(
                taskDef,
                i,
                paramClass,
                javaParamName
            );
            mappings.add(mapping);
        }

        if (signature.getHasWorkerContextAtEnd()) {
            mappings.add(
                new VariableMapping(
                    taskDef,
                    numTaskMethodParams - 1,
                    WorkerContext.class,
                    null
                )
            );
        }
    }

    /**
     * Starts polling for and executing tasks.
     * @throws LHApiError if the schema from the TaskDef configured in
     * the configProps is incompatible with the method signature from the provided
     * executable Java object, or if the Worker cannot connect to the LH Server.
     */
    public void start() throws LHApiError {
        checkTaskDefExists();
        createManager();
        manager.start();
    }

    /**
     * Cleanly shuts down the Task Worker.
     */
    public void close() {
        manager.close();
    }
}
