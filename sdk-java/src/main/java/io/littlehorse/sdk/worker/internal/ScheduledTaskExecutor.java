package io.littlehorse.sdk.worker.internal;

import com.google.common.base.Throwables;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionException;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHTaskError;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduledTaskExecutor {

    private final int MAX_RETRY_ATTEMPTS = 5;
    private final LittleHorseGrpc.LittleHorseStub retriesStub;
    private final LittleHorseGrpc.LittleHorseBlockingStub blockingStub;
    private final LHTypeAdapterRegistry typeAdapterRegistry;
    private final TaskDef taskDef;

    public ScheduledTaskExecutor(
            final LittleHorseGrpc.LittleHorseStub retriesStub, final LittleHorseBlockingStub blockingStub) {
        this(retriesStub, blockingStub, LHTypeAdapterRegistry.empty(), null);
    }

    public ScheduledTaskExecutor(
            final LittleHorseGrpc.LittleHorseStub retriesStub,
            final LittleHorseBlockingStub blockingStub,
            final List<LHTypeAdapter<?>> typeAdapters) {
        this(retriesStub, blockingStub, LHTypeAdapterRegistry.from(typeAdapters), null);
    }

    public ScheduledTaskExecutor(
            final LittleHorseGrpc.LittleHorseStub retriesStub,
            final LittleHorseBlockingStub blockingStub,
            final LHTypeAdapterRegistry typeAdapterRegistry) {
        this(retriesStub, blockingStub, typeAdapterRegistry, null);
    }

    public ScheduledTaskExecutor(
            final LittleHorseGrpc.LittleHorseStub retriesStub,
            final LittleHorseBlockingStub blockingStub,
            final LHTypeAdapterRegistry typeAdapterRegistry,
            final TaskDef taskDef) {
        this.retriesStub = retriesStub;
        this.blockingStub = blockingStub;
        this.typeAdapterRegistry = Objects.requireNonNull(typeAdapterRegistry, "Type adapter registry cannot be null");
        this.taskDef = taskDef;
    }

    public void doTask(
            ScheduledTask scheduledTask,
            LittleHorseGrpc.LittleHorseStub specificStub,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod) {
        ReportTaskRun result = executeTask(
                scheduledTask, LHLibUtil.fromProtoTs(scheduledTask.getCreatedAt()), mappings, executable, taskMethod);
        String wfRunId = LHLibUtil.getWfRunId(scheduledTask.getSource()).getId();
        try {
            log.debug("Going to report task for wfRun {}", wfRunId);
            specificStub.reportTask(result, new ReportTaskObserver(result, 2));
            log.debug("Successfully contacted LHServer on reportTask for wfRun {}", wfRunId);
        } catch (Exception exn) {
            log.warn("Failed to report task for wfRun {}: {}", wfRunId, exn.getMessage());
            retry(result, MAX_RETRY_ATTEMPTS);
        }
    }

    private ReportTaskRun executeTask(
            ScheduledTask scheduledTask,
            Date scheduleTime,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod) {
        ReportTaskRun.Builder taskResult = ReportTaskRun.newBuilder()
                .setTaskRunId(scheduledTask.getTaskRunId())
                .setAttemptNumber(scheduledTask.getAttemptNumber());

        WorkerContext wc = new WorkerContext(scheduledTask, scheduleTime, blockingStub, typeAdapterRegistry);

        try {
            Object rawResult = invoke(scheduledTask, wc, mappings, executable, taskMethod);
            log.debug("Task executed for: " + scheduledTask.getTaskDefId().getName());
            VariableValue serialized = serializeResult(rawResult, taskMethod);
            taskResult.setOutput(serialized.toBuilder()).setStatus(TaskStatus.TASK_SUCCESS);
        } catch (InputVarSubstitutionException exn) {
            log.error("Failed calculating task input variables", exn);
            taskResult.setStatus(TaskStatus.TASK_INPUT_VAR_SUB_ERROR);
            taskResult.setError(exnToTaskError(exn, taskResult.getStatus()));
        } catch (LHSerdeException exn) {
            log.error("Failed serializing Task Output", exn);
            taskResult.setStatus(TaskStatus.TASK_OUTPUT_SERDE_ERROR);
            taskResult.setError(exnToTaskError(exn, taskResult.getStatus()));
        } catch (InvocationTargetException exn) {
            if (exn.getTargetException() instanceof LHTaskException) {
                LHTaskException exception = (LHTaskException) exn.getTargetException();
                log.error("Task Method threw a Business Exception", exn);
                taskResult.setStatus(TaskStatus.TASK_EXCEPTION);
                taskResult.setException(exnToTaskException(exception));
            } else {
                log.error("Task Method threw an exception", exn.getTargetException());
                taskResult.setStatus(TaskStatus.TASK_FAILED);
                taskResult.setError(exnToTaskError(exn.getTargetException(), taskResult.getStatus()));
            }
        } catch (Exception exn) {
            log.error("Unexpected exception during task execution", exn);
            taskResult.setStatus(TaskStatus.TASK_FAILED);
            taskResult.setError(exnToTaskError(exn, taskResult.getStatus()));
        }
        if (wc.getLogOutput() != null) {
            taskResult.setLogOutput(VariableValue.newBuilder().setStr(wc.getLogOutput()));
        }
        taskResult.setTime(LHLibUtil.fromDate(new Date()));
        return taskResult.build();
    }

    private Object invoke(
            ScheduledTask scheduledTask,
            WorkerContext context,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod)
            throws Exception {
        List<Object> inputs = new ArrayList<>();
        for (VariableMapping mapping : mappings) {
            inputs.add(mapping.assign(scheduledTask, context));
        }
        return taskMethod.invoke(executable, inputs.toArray());
    }

    /**
     * Bridges the reflection boundary: Method.getReturnType() is a raw Class<?>, so
     * the cast to Class<T> is unchecked but safe—returnType always matches the actual object.
     */
    private VariableValue serializeResult(Object result, Method taskMethod) {
        Class<?> returnType = taskMethod.getReturnType();
        if (InlineStruct.class.equals(returnType)) {
            return serializeInlineStructResult(result);
        }
        return LHLibUtil.objToVarVal(result, returnType, typeAdapterRegistry);
    }

    private VariableValue serializeInlineStructResult(Object result) {
        if (result == null) {
            return VariableValue.newBuilder().build();
        }

        if (!(result instanceof InlineStruct)) {
            throw new LHSerdeException("Task returned a non-InlineStruct value for an InlineStruct task method.");
        }

        if (taskDef == null
                || !taskDef.hasReturnType()
                || !taskDef.getReturnType().hasReturnType()) {
            throw new LHSerdeException("InlineStruct task return requires a StructDef return type in TaskDef.");
        }

        StructDefId structDefId = taskDef.getReturnType().getReturnType().getStructDefId();
        return LHLibUtil.inlineStructToVarVal((InlineStruct) result, structDefId);
    }

    private LHTaskError exnToTaskError(Throwable throwable, TaskStatus taskStatus) {
        return LHTaskError.newBuilder()
                .setType(getFailureCodeFor(taskStatus))
                .setMessage(Throwables.getStackTraceAsString(throwable))
                .build();
    }

    private LHErrorType getFailureCodeFor(TaskStatus status) {
        switch (status) {
            case TASK_FAILED:
                return LHErrorType.TASK_FAILURE;
            case TASK_TIMEOUT:
                return LHErrorType.TIMEOUT;
            case TASK_OUTPUT_SERDE_ERROR:
                return LHErrorType.VAR_MUTATION_ERROR;
            case TASK_INPUT_VAR_SUB_ERROR:
                return LHErrorType.VAR_SUB_ERROR;
            case TASK_RUNNING:
            case TASK_SCHEDULED:
            case TASK_SUCCESS:
            case TASK_PENDING:
            case TASK_EXCEPTION: // TASK_EXCEPTION is NOT a technical ERROR, so this fails.
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("Unexpected task status: " + status);
    }

    private io.littlehorse.sdk.common.proto.LHTaskException exnToTaskException(LHTaskException exn) {
        return io.littlehorse.sdk.common.proto.LHTaskException.newBuilder()
                .setName(exn.getName())
                .setContent(exn.getContent())
                .setMessage(exn.getMessage())
                .build();
    }

    private void retry(ReportTaskRun reportedTaskRun, int retriesLeft) {
        if (retriesLeft > 0) {
            retriesStub.reportTask(reportedTaskRun, new ReportTaskObserver(reportedTaskRun, --retriesLeft));
        }
    }

    private class ReportTaskObserver implements StreamObserver<Empty> {
        private final ReportTaskRun reportedTaskRun;
        private final int retriesLeft;

        private ReportTaskObserver(ReportTaskRun result, int retriesLeft) {
            this.reportedTaskRun = result;
            this.retriesLeft = retriesLeft;
        }

        @Override
        public void onNext(Empty value) {
            // nothing to do.. maybe metrics?
        }

        @Override
        public void onError(Throwable t) {
            retry(reportedTaskRun, retriesLeft);
        }

        @Override
        public void onCompleted() {}
    }
}
