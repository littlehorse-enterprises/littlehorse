package io.littlehorse.jlib.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.InputVarSubstitutionError;
import io.littlehorse.jlib.common.exception.LHSerdeError;
import io.littlehorse.jlib.common.proto.HostInfoPb;
import io.littlehorse.jlib.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.RegisterTaskWorkerPb;
import io.littlehorse.jlib.common.proto.RegisterTaskWorkerReplyPb;
import io.littlehorse.jlib.common.proto.ScheduledTaskPb;
import io.littlehorse.jlib.common.proto.TaskDefPb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.TaskResultEventPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.jlib.common.proto.VariableValuePb;
import io.littlehorse.jlib.worker.WorkerContext;
import io.littlehorse.jlib.worker.internal.util.ReportTaskObserver;
import io.littlehorse.jlib.worker.internal.util.VariableMapping;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LHServerConnectionManager
    implements StreamObserver<RegisterTaskWorkerReplyPb>, Closeable {

    private Logger log = LoggerFactory.getLogger(LHServerConnectionManager.class);

    public Object executable;
    public Method taskMethod;
    public LHWorkerConfig config;
    public List<VariableMapping> mappings;
    public TaskDefPb taskDef;

    private boolean running;
    private List<LHServerConnection> runningConnections;
    private LHPublicApiStub bootstrapStub;
    private ExecutorService threadPool;
    private Semaphore workerSemaphore;
    private Thread rebalanceThread;

    private static final int TOTAL_RETRIES = 5;

    public LHServerConnectionManager(
        Method taskMethod,
        TaskDefPb taskDef,
        LHWorkerConfig config,
        List<VariableMapping> mappings,
        Object executable
    ) throws IOException {
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

        this.rebalanceThread =
            new Thread(() -> {
                while (this.running) {
                    askForMetadata();
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ignored) {
                        // Ignored
                    }
                }
            });
    }

    public void submitTaskForExecution(
        ScheduledTaskPb scheduledTask,
        LHPublicApiStub specificStub
    ) {
        try {
            this.workerSemaphore.acquire();
        } catch (InterruptedException exn) {
            throw new RuntimeException(exn);
        }
        this.threadPool.submit(() -> {
                this.doTask(scheduledTask, specificStub);
            });
    }

    private void doTask(ScheduledTaskPb scheduleTask, LHPublicApiStub specificStub) {
        TaskResultEventPb result = executeTask(
            scheduleTask,
            LHLibUtil.fromProtoTs(scheduleTask.getCreatedAt())
        );
        this.workerSemaphore.release();
        try {
            log.debug("Going to report task for wfRun {}", scheduleTask.getWfRunId());
            specificStub.reportTask(
                result,
                new ReportTaskObserver(this, result, TOTAL_RETRIES)
            );
            log.debug(
                "Successfully reported task for wfRun {}",
                scheduleTask.getWfRunId()
            );
        } catch (Exception exn) {
            log.warn(
                "Failed to report task for wfRun {}: {}",
                scheduleTask.getWfRunId(),
                exn.getMessage()
            );
            retryReportTask(result, TOTAL_RETRIES);
        }
    }

    @Override
    public void onNext(RegisterTaskWorkerReplyPb next) {
        if (next.getCode() == LHResponseCodePb.BAD_REQUEST_ERROR) {
            throw new RuntimeException("Invalid configuration: " + next.getMessage());
        }
        // Reconcile what's running
        for (HostInfoPb host : next.getYourHostsList()) {
            if (!isAlreadyRunning(host)) {
                try {
                    runningConnections.add(new LHServerConnection(this, host));
                    log.info(
                        "Adding connection to: {}:{} for taskdef {}",
                        host.getHost(),
                        host.getPort(),
                        taskDef.getName()
                    );
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
                    runningThread.getHostInfo().getPort()
                );
                runningThread.close();
                runningConnections.remove(i);
            }
        }
    }

    private boolean shouldBeRunning(LHServerConnection ssc, List<HostInfoPb> hosts) {
        for (HostInfoPb h : hosts) {
            if (ssc.isSameAs(h)) return true;
        }
        return false;
    }

    private boolean isAlreadyRunning(HostInfoPb host) {
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
            t
        );
        // We don't close the connections to other hosts here since they will do
        // that themselves if they can't connect.
    }

    @Override
    public void onCompleted() {
        // nothing to do
    }

    private void askForMetadata() {
        bootstrapStub.registerTaskWorker(
            RegisterTaskWorkerPb
                .newBuilder()
                .setTaskDefName(taskDef.getName())
                .setClientId(config.getClientId())
                .setListenerName(config.getConnectListener())
                .build(),
            this // the callbacks come back to this manager.
        );
    }

    public void retryReportTask(TaskResultEventPb result, int retriesLeft) {
        // EMPLOYEE_TODO: create a queue or something that has delay and multiple
        // retries. This thing just tries again with the bootstrap host and hopes
        // that the request finds the right

        // The second arg is null so that we don't get into infinite retry loop.
        // That's why we need an employee to fix it ;)

        threadPool.submit(() -> {
            log.debug("Retrying reportTask rpc on wfRun {}", result.getWfRunId());
            try {
                // This should also slow down progress on tasks too, which should
                // help prevent tons of overflow.
                // EMPLOYEE_TODO: make this a bit better oops
                Thread.sleep(500);
            } catch (Exception ignored) {}
            bootstrapStub.reportTask(
                result,
                new ReportTaskObserver(this, result, retriesLeft - 1)
            );
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

    private TaskResultEventPb executeTask(
        ScheduledTaskPb scheduledTask,
        Date scheduleTime
    ) {
        TaskResultEventPb.Builder taskResult = TaskResultEventPb
            .newBuilder()
            .setWfRunId(scheduledTask.getWfRunId())
            .setTaskRunPosition(scheduledTask.getTaskRunPosition())
            .setThreadRunNumber(scheduledTask.getThreadRunNumber());

        if (scheduledTask.hasUtaTaskId()) {
            taskResult.setUtaTaskId(scheduledTask.getUtaTaskId());
        }

        WorkerContext wc = new WorkerContext(scheduledTask, scheduleTime);

        try {
            Object rawResult = invoke(scheduledTask, wc);
            VariableValuePb serialized = LHLibUtil.objToVarVal(rawResult);
            taskResult.setResultCode(TaskResultCodePb.SUCCESS);
            taskResult.setOutput(serialized.toBuilder());
            if (wc.getLogOutput() != null) {
                taskResult.setLogOutput(
                    VariableValuePb.newBuilder().setStr(wc.getLogOutput())
                );
            }
        } catch (InputVarSubstitutionError exn) {
            exn.printStackTrace();
            taskResult.setLogOutput(exnToVarVal(exn, wc));
            taskResult.setResultCode(TaskResultCodePb.VAR_SUB_ERROR);
        } catch (LHSerdeError exn) {
            exn.printStackTrace();
            taskResult.setLogOutput(exnToVarVal(exn, wc));
            taskResult.setResultCode(TaskResultCodePb.VAR_SUB_ERROR);
        } catch (InvocationTargetException exn) {
            exn.getCause().printStackTrace();
            taskResult.setLogOutput(exnToVarVal(exn.getCause(), wc));
            taskResult.setResultCode(TaskResultCodePb.FAILED);
        } catch (Exception exn) {
            exn.printStackTrace();
            taskResult.setLogOutput(exnToVarVal(exn, wc));
            taskResult.setResultCode(TaskResultCodePb.FAILED);
        }

        taskResult.setTime(LHLibUtil.fromDate(new Date()));
        return taskResult.build();
        // TODO: Use the client to send the request.
    }

    private Object invoke(ScheduledTaskPb scheduledTask, WorkerContext context)
        throws InputVarSubstitutionError, Exception {
        List<Object> inputs = new ArrayList<>();
        for (VariableMapping mapping : this.mappings) {
            inputs.add(mapping.assign(scheduledTask, context));
        }

        return this.taskMethod.invoke(this.executable, inputs.toArray());
    }

    private VariableValuePb.Builder exnToVarVal(Throwable exn, WorkerContext ctx) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exn.printStackTrace(pw);
        String output = sw.toString();
        if (ctx.getLogOutput() != null) {
            output += "\n\n\n\n" + ctx.getLogOutput();
        }

        return VariableValuePb
            .newBuilder()
            .setStr(output)
            .setType(VariableTypePb.STR);
    }

    public int getNumThreads() {
        return config.getWorkerThreads();
    }
}
