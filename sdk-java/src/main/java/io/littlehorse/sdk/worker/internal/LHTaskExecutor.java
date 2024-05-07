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
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseStub;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * In memory queue and executor for scheduled tasks.
 * An instance of this class will accept scheduled task to be executed from.
 * Multiples tasks can share the same queue of pending tasks.
 * This class will handle retries with a specified delay.
 * A ${@link Semaphore} is used internally in order to prevent overwhelming the in-memory queue,
 * clients can control the allowed size of this by specifying INFLIGHT_PENDING_TASKS.
 * Also, clients can define the number of thread dedicated to execute tasks.
 *
 */
@Slf4j
public class LHTaskExecutor {

    private final ScheduledExecutorService pool;
    private final Semaphore semaphore;
    private static final int TOTAL_RETRIES = 5;

    public LHTaskExecutor() {
        this.pool = Executors.newScheduledThreadPool(8);
        this.semaphore = new Semaphore(1_000);
    }

    public void submitTaskForExecution(
            ScheduledTask scheduledTask,
            LittleHorseStub specificStub,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException exn) {
            throw new RuntimeException(exn);
        }
        this.pool.submit(() -> {
            this.doTask(scheduledTask, specificStub, mappings, executable, taskMethod);
        });
    }

    public boolean close(int timeout, TimeUnit timeUnit) throws InterruptedException {
        log.info("shutting down...");
        pool.shutdown();
        return pool.awaitTermination(timeout, timeUnit);
    }

    private void doTask(
            ScheduledTask scheduledTask,
            LittleHorseGrpc.LittleHorseStub specificStub,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod) {
        ReportTaskRun result = executeTask(
                scheduledTask, LHLibUtil.fromProtoTs(scheduledTask.getCreatedAt()), mappings, executable, taskMethod);
        this.semaphore.release();
        String wfRunId = LHLibUtil.getWfRunId(scheduledTask.getSource()).getId();
        try {
            log.debug("Going to report task for wfRun {}", wfRunId);
            specificStub.reportTask(result, new ReportTaskObserver(result, TOTAL_RETRIES, specificStub));
            log.debug("Successfully contacted LHServer on reportTask for wfRun {}", wfRunId);
        } catch (Exception exn) {
            log.warn("Failed to report task for wfRun {}: {}", wfRunId, exn.getMessage());
            scheduleRetry(result, specificStub, TOTAL_RETRIES);
        }
    }

    private void scheduleRetry(ReportTaskRun reportedTaskRun, LittleHorseStub stub, int retriesLeft) {
        pool.schedule(
                () -> {
                    stub.reportTask(reportedTaskRun, new ReportTaskObserver(reportedTaskRun, retriesLeft, stub));
                },
                500,
                TimeUnit.MILLISECONDS);
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
            log.info("Task executed for: " + scheduledTask.getTaskDefId().getName());
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

    private class ReportTaskObserver implements StreamObserver<Empty> {
        private final ReportTaskRun reportedTaskRun;
        private final int retriesLeft;
        private final LittleHorseStub stub;

        private ReportTaskObserver(ReportTaskRun result, int retriesLeft, LittleHorseStub stub) {
            this.reportedTaskRun = result;
            this.retriesLeft = retriesLeft;
            this.stub = stub;
        }

        @Override
        public void onNext(Empty value) {
            // nothing to do.. maybe metrics?
        }

        @Override
        public void onError(Throwable t) {
            scheduleRetry(reportedTaskRun, stub, retriesLeft);
        }

        @Override
        public void onCompleted() {}
    }
}
