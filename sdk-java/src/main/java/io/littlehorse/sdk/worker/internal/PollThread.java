package io.littlehorse.sdk.worker.internal;

import com.google.common.base.Throwables;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHTaskError;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollThread extends Thread implements Closeable, StreamObserver<PollTaskResponse> {

    private StreamObserver<PollTaskRequest> pollClient;
    private final String taskWorkerId;
    private final TaskDefId taskDefId;
    private final String taskWorkerVersion;
    private final Semaphore semaphore = new Semaphore(1);

    public final LittleHorseGrpc.LittleHorseStub stub;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final Method taskMethod;
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub;
    private final int MAX_RETRY_ATTEMPTS = 5;

    private boolean stillRunning = true;

    public PollThread(
            String threadName,
            LittleHorseGrpc.LittleHorseStub stub,
            LittleHorseGrpc.LittleHorseStub bootstrapStub,
            TaskDefId taskDefId,
            String taskWorkerId,
            String taskWorkerVersion,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod) {
        super(threadName);
        this.stub = stub;
        this.taskDefId = taskDefId;
        this.taskWorkerId = taskWorkerId;
        this.taskWorkerVersion = taskWorkerVersion;
        this.pollClient = stub.pollTask(this);
        this.mappings = mappings;
        this.executable = executable;
        this.taskMethod = taskMethod;
        this.taskMethod.setAccessible(true);
        this.bootstrapStub = bootstrapStub;
    }

    @Override
    public void run() {
        try {
            while (stillRunning) {
                semaphore.acquire();
                pollClient.onNext(PollTaskRequest.newBuilder()
                        .setClientId(taskWorkerId)
                        .setTaskDefId(taskDefId)
                        .setTaskWorkerVersion(taskWorkerVersion)
                        .build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onNext(PollTaskResponse value) {
        if(value.hasResult()) {
            doTask(value.getResult(), stub, mappings, executable, taskMethod);
        } else {
            log.warn("Didn't successfully claim a task");
        }
        semaphore.release();
    }

    @Override
    public void onError(Throwable t) {
        log.error("Unexpected error from server", t);
        this.stillRunning = false;
    }

    @Override
    public void onCompleted() {
        log.error("Unexpected call to onCompleted() in the Server Connection.");
        this.stillRunning = false;
    }

    @Override
    public void close() {
        this.stillRunning = false;
    }

    private void doTask(
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

        WorkerContext wc = new WorkerContext(scheduledTask, scheduleTime);

        try {
            Object rawResult = invoke(scheduledTask, wc, mappings, executable, taskMethod);
            log.debug("Task executed for: " + scheduledTask.getTaskDefId().getName());
            VariableValue serialized = LHLibUtil.objToVarVal(rawResult);
            taskResult.setOutput(serialized.toBuilder()).setStatus(TaskStatus.TASK_SUCCESS);
        } catch (InputVarSubstitutionError exn) {
            log.error("Failed calculating task input variables", exn);
            taskResult.setStatus(TaskStatus.TASK_INPUT_VAR_SUB_ERROR);
            taskResult.setError(exnToTaskError(exn, taskResult.getStatus()));
        } catch (LHSerdeError exn) {
            log.error("Failed serializing Task Output", exn);
            taskResult.setStatus(TaskStatus.TASK_OUTPUT_SERIALIZING_ERROR);
            taskResult.setError(exnToTaskError(exn, taskResult.getStatus()));
        } catch (InvocationTargetException exn) {
            if (exn.getTargetException() instanceof LHTaskException) {
                LHTaskException exception = (LHTaskException) exn.getTargetException();
                log.error("Task Method threw a Business Exception", exn);
                taskResult.setStatus(TaskStatus.TASK_EXCEPTION);
                taskResult.setException(exnToTaskException(exception));
            } else {
                log.error("Task Method threw an exception", exn.getCause());
                taskResult.setStatus(TaskStatus.TASK_FAILED);
                taskResult.setError(exnToTaskError(exn, taskResult.getStatus()));
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
            case TASK_OUTPUT_SERIALIZING_ERROR:
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
            bootstrapStub.reportTask(reportedTaskRun, new ReportTaskObserver(reportedTaskRun, --retriesLeft));
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
