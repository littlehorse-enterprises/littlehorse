package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.InputVarSubstitutionError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.sdk.worker.internal.util.ReportTaskObserver;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    private boolean running;
    private List<LHServerConnection> runningConnections;
    private LHPublicApiStub bootstrapStub;
    private ExecutorService threadPool;
    private Semaphore workerSemaphore;
    private Thread rebalanceThread;

    private static final int TOTAL_RETRIES = 5;

    public LHServerConnectionManager(
            Method taskMethod, TaskDef taskDef, LHConfig config, List<VariableMapping> mappings, Object executable)
            throws IOException {
        this.executable = executable;
        this.taskMethod = taskMethod;
        taskMethod.setAccessible(true);
        this.config = config;
        this.mappings = mappings;
        this.taskDef = taskDef;

        this.bootstrapStub = config.getAsyncStub();

        this.running = false;
        this.runningConnections = new ArrayList<>();
        this.workerSemaphore = new Semaphore(config.getWorkerThreads());
        this.threadPool = Executors.newFixedThreadPool(config.getWorkerThreads());

        this.rebalanceThread = new Thread(() -> {
            while (this.running) {
                doHeartbeat();
                try {
                    Thread.sleep(5000);
                } catch (Exception ignored) {
                    // Ignored
                }
            }
        });
    }

    public void submitTaskForExecution(ScheduledTask scheduledTask, LHPublicApiStub specificStub) {
        try {
            this.workerSemaphore.acquire();
        } catch (InterruptedException exn) {
            throw new RuntimeException(exn);
        }
        this.threadPool.submit(() -> {
            this.doTask(scheduledTask, specificStub);
        });
    }

    private void doTask(ScheduledTask scheduledTask, LHPublicApiStub specificStub) {
        ReportTaskRun result = executeTask(scheduledTask, LHLibUtil.fromProtoTs(scheduledTask.getCreatedAt()));
        this.workerSemaphore.release();
        String wfRunId = LHLibUtil.getWfRunId(scheduledTask.getSource());
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
        for (LHHostInfo host : next.getYourHostsList()) {
            if (!isAlreadyRunning(host)) {
                try {
                    runningConnections.add(new LHServerConnection(this, host));
                    log.info(
                            "Adding connection to: {}:{} for taskdef {}",
                            host.getHost(),
                            host.getPort(),
                            taskDef.getName());
                } catch (IOException exn) {
                    log.error("Yikes, caught IOException in onNext", exn);
                    throw new RuntimeException(exn);
                }
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
        this.running = false;
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
                        .setTaskDefName(taskDef.getName())
                        .setClientId(config.getClientId())
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

    public void start() {
        this.running = true;
        this.rebalanceThread.start();
    }

    public void close() {
        this.running = false;
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

            if (wc.getLogOutput() != null) {
                taskResult.setLogOutput(VariableValue.newBuilder().setStr(wc.getLogOutput()));
            }
        } catch (InputVarSubstitutionError exn) {
            log.error("Failed calculating task input variables", exn);
            taskResult.setLogOutput(exnToVarVal(exn, wc));
            taskResult.setStatus(TaskStatus.TASK_INPUT_VAR_SUB_ERROR);
        } catch (LHSerdeError exn) {
            log.error("Failed serializing Task Output", exn);
            taskResult.setLogOutput(exnToVarVal(exn, wc));
            taskResult.setStatus(TaskStatus.TASK_OUTPUT_SERIALIZING_ERROR);
        } catch (InvocationTargetException exn) {
            log.error("Task Method threw an exception", exn.getCause());
            taskResult.setLogOutput(exnToVarVal(exn.getCause(), wc));
            taskResult.setStatus(TaskStatus.TASK_FAILED);
        } catch (Exception exn) {
            log.error("Unexpected exception during task execution", exn);
            taskResult.setLogOutput(exnToVarVal(exn, wc));
            taskResult.setStatus(TaskStatus.TASK_FAILED);
        }

        taskResult.setTime(LHLibUtil.fromDate(new Date()));
        return taskResult.build();
    }

    private Object invoke(ScheduledTask scheduledTask, WorkerContext context)
            throws InputVarSubstitutionError, Exception {
        List<Object> inputs = new ArrayList<>();
        for (VariableMapping mapping : this.mappings) {
            inputs.add(mapping.assign(scheduledTask, context));
        }

        return this.taskMethod.invoke(this.executable, inputs.toArray());
    }

    private VariableValue.Builder exnToVarVal(Throwable exn, WorkerContext ctx) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exn.printStackTrace(pw);
        String output = sw.toString();
        if (ctx.getLogOutput() != null) {
            output += "\n\n\n\n" + ctx.getLogOutput();
        }

        return VariableValue.newBuilder().setStr(output).setType(VariableType.STR);
    }

    public int getNumThreads() {
        return config.getWorkerThreads();
    }
}
