package io.littlehorse.sdk.worker.internal;

import com.google.common.base.Throwables;
import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LHTaskError;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseStub;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHTaskWorkerHealth;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.sdk.worker.internal.util.ReportTaskObserver;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHServerConnectionManager implements StreamObserver<RegisterTaskWorkerResponse>, Closeable {

    public Object executable;
    public Method taskMethod;
    public LHConfig config;
    public List<VariableMapping> mappings;
    public TaskDef taskDef;

    private List<LHServerConnection> runningConnections;
    private LittleHorseStub bootstrapStub;
    private ExecutorService threadPool;
    private Semaphore workerSemaphore;
    private Thread rebalanceThread;

    private static final long HEARTBEAT_INTERVAL_MS = 5000L;

    private final LHLivenessController livenessController;

    private static final int TOTAL_RETRIES = 5;

    public LHServerConnectionManager(
            Method taskMethod,
            TaskDef taskDef,
            LHConfig config,
            List<VariableMapping> mappings,
            Object executable,
            LHLivenessController livenessController) {
        this.executable = executable;
        this.taskMethod = taskMethod;
        taskMethod.setAccessible(true);
        this.config = config;
        this.mappings = mappings;
        this.taskDef = taskDef;

        this.bootstrapStub = config.getAsyncStub();

        this.runningConnections = new ArrayList<>();
        this.workerSemaphore = new Semaphore(config.getWorkerThreads());
        this.threadPool = Executors.newFixedThreadPool(config.getWorkerThreads());
        this.livenessController = livenessController;
        this.rebalanceThread = new Thread(() -> {
            while (this.livenessController.keepWorkerRunning()) {
                doHeartbeat();
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                } catch (Exception ignored) {
                    // Ignored
                }
            }
        });
    }

    public void submitTaskForExecution(ScheduledTask scheduledTask, LittleHorseStub specificStub) {
        try {
            this.workerSemaphore.acquire();
        } catch (InterruptedException exn) {
            throw new RuntimeException(exn);
        }
        this.threadPool.submit(() -> {
            this.doTask(scheduledTask, specificStub);
        });
    }

    public void maybeExecuteTask(PollTaskResponse taskToDo, LittleHorseStub specificStub) {
        if (taskToDo.hasResult()) {
            ScheduledTask scheduledTask = taskToDo.getResult();
            String wfRunId = LHLibUtil.getWfRunId(scheduledTask.getSource()).getId();
            log.debug("Received task schedule request for wfRun {}", wfRunId);

            this.submitTaskForExecution(scheduledTask, specificStub);

            log.debug("Scheduled task on threadpool for wfRun {}", wfRunId);
        } else {
            this.workerSemaphore.release();
            log.error("Didn't successfully claim task, likely due to server restart.");
        }
    }

    private void doTask(ScheduledTask scheduledTask, LittleHorseStub specificStub) {
        ReportTaskRun result = executeTask(scheduledTask, LHLibUtil.fromProtoTs(scheduledTask.getCreatedAt()));
        this.workerSemaphore.release();
        String wfRunId = LHLibUtil.getWfRunId(scheduledTask.getSource()).getId();
        try {
            log.debug("Going to report task for wfRun {}", wfRunId);
            specificStub.reportTask(result, new ReportTaskObserver(this, result, TOTAL_RETRIES));
            log.debug("Successfully contacted LHServer on reportTask for wfRun {}", wfRunId);
        } catch (Exception exn) {
            log.warn("Failed to report task for wfRun {}: {}", wfRunId, exn.getMessage());
            retryReportTask(result, TOTAL_RETRIES);
        }
    }

    @Override
    public void onNext(RegisterTaskWorkerResponse next) {
        // Reconcile what's running
        livenessController.notifySuccessCall(next);

        for (LHHostInfo host : next.getYourHostsList()) {
            if (!isAlreadyRunning(host)) {
                runningConnections.add(new LHServerConnection(this, host));
                log.info(
                        "Adding connection to: {}:{} for taskdef {}",
                        host.getHost(),
                        host.getPort(),
                        taskDef.getId().getName());
            }
        }

        for (int i = runningConnections.size() - 1; i >= 0; i--) {
            LHServerConnection runningThread = runningConnections.get(i);
            if (!shouldBeRunning(runningThread, next.getYourHostsList())) {
                log.info(
                        "Stopping worker thread for host {}:{}",
                        runningThread.getHostInfo().getHost(),
                        runningThread.getHostInfo().getPort());
                runningThread.close();
                runningConnections.remove(i);
            }
        }
    }

    private boolean shouldBeRunning(LHServerConnection ssc, List<LHHostInfo> hosts) {
        for (LHHostInfo h : hosts) {
            if (ssc.isSameAs(h)) return true;
        }
        return false;
    }

    private boolean isAlreadyRunning(LHHostInfo host) {
        for (LHServerConnection ssc : runningConnections) {
            if (ssc.isSameAs(host)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onError(Throwable t) {
        log.error(
                "Failed contacting bootstrap host {}:{}",
                config.getApiBootstrapHost(),
                config.getApiBootstrapPort(),
                t);
        livenessController.notifyWorkerFailure();
        // We don't close the connections to other hosts here since they will do
        // that themselves if they can't connect.
    }

    @Override
    public void onCompleted() {
        // nothing to do
    }

    private void doHeartbeat() {
        bootstrapStub.registerTaskWorker(
                RegisterTaskWorkerRequest.newBuilder()
                        .setTaskDefId(taskDef.getId())
                        .setTaskWorkerId(config.getTaskWorkerId())
                        .setListenerName(config.getConnectListener())
                        .build(),
                this // the callbacks come back to this manager.
                );
    }

    public void retryReportTask(ReportTaskRun result, int retriesLeft) {
        // EMPLOYEE_TODO: create a queue or something that has delay and multiple
        // retries. This thing just tries again with the bootstrap host and hopes
        // that the request finds the right

        // The second arg is null so that we don't get into infinite retry loop.
        // That's why we need an employee to fix it ;)

        threadPool.submit(() -> {
            log.debug("Retrying reportTask rpc on taskRun {}", LHLibUtil.taskRunIdToString(result.getTaskRunId()));
            try {
                // This should also slow down progress on tasks too, which should
                // help prevent tons of overflow.
                // EMPLOYEE_TODO: make this a bit better oops
                Thread.sleep(500);
            } catch (Exception ignored) {
            }
            bootstrapStub.reportTask(result, new ReportTaskObserver(this, result, retriesLeft - 1));
        });
    }

    public void onConnectionClosed(LHServerConnection connection) {
        // TODO: remove from the list
        runningConnections.removeIf(thing -> {
            if (thing == connection) {
                return true;
            }
            return false;
        });
    }

    public boolean isAlive() {
        return rebalanceThread.isAlive();
    }

    public void start() {
        rebalanceThread.start();
    }

    public void close() {
        livenessController.stop();
    }

    // Below is actual task execution logic

    private ReportTaskRun executeTask(ScheduledTask scheduledTask, Date scheduleTime) {
        ReportTaskRun.Builder taskResult = ReportTaskRun.newBuilder()
                .setTaskRunId(scheduledTask.getTaskRunId())
                .setAttemptNumber(scheduledTask.getAttemptNumber());

        WorkerContext wc = new WorkerContext(scheduledTask, scheduleTime);

        try {
            Object rawResult = invoke(scheduledTask, wc);
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

    private Object invoke(ScheduledTask scheduledTask, WorkerContext context) throws Exception {
        List<Object> inputs = new ArrayList<>();
        for (VariableMapping mapping : this.mappings) {
            inputs.add(mapping.assign(scheduledTask, context));
        }

        return this.taskMethod.invoke(this.executable, inputs.toArray());
    }

    private io.littlehorse.sdk.common.proto.LHTaskException exnToTaskException(LHTaskException exn) {
        return io.littlehorse.sdk.common.proto.LHTaskException.newBuilder()
                .setName(exn.getName())
                .setContent(exn.getContent())
                .setMessage(exn.getMessage())
                .build();
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

    public int getNumThreads() {
        return config.getWorkerThreads();
    }

    public LHTaskWorkerHealth healthStatus() {
        return livenessController.healthStatus();
    }
}
