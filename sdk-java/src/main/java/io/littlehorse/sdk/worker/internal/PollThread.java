package io.littlehorse.sdk.worker.internal;

import com.google.common.collect.Iterators;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollThread extends Thread implements Closeable {

    private Iterator<PollTaskStub> activePollClients;
    private final String taskWorkerId;
    private final TaskDefId taskDefId;
    private final String taskWorkerVersion;
    private final Semaphore semaphore = new Semaphore(1000);

    public final LittleHorseGrpc.LittleHorseStub stub;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final Method taskMethod;
    private final ScheduledTaskExecutor taskExecutor;

    private boolean stillRunning = true;
    private final LHConfig config;

    public PollThread(
            String threadName,
            LittleHorseGrpc.LittleHorseStub stub,
            TaskDefId taskDefId,
            String taskWorkerId,
            String taskWorkerVersion,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod,
            ScheduledTaskExecutor taskExecutor,
            LHConfig config) {
        super(threadName);
        this.stub = stub;
        this.taskDefId = taskDefId;
        this.taskWorkerId = taskWorkerId;
        this.taskWorkerVersion = taskWorkerVersion;
        this.mappings = mappings;
        this.executable = executable;
        this.taskMethod = taskMethod;
        this.taskMethod.setAccessible(true);
        this.taskExecutor = taskExecutor;
        this.config = config;
    }

    @Override
    public void run() {
        List<PollTaskStub> pollClients =
                Stream.generate(this::createObserver).limit(1000).collect(Collectors.toList());
        this.activePollClients = Iterators.cycle(pollClients);
        try {
            while (stillRunning) {
                PollTaskStub pollClient = activePollClients.next();
                if (pollClient.isReady()) {
                    pollClient.doNext();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private PollTaskStub createObserver() {
        LittleHorseGrpc.LittleHorseStub specificStub = config.getAsyncStub();
        return new PollTaskStub(
                specificStub,
                semaphore,
                taskExecutor,
                taskWorkerId,
                taskDefId,
                taskWorkerVersion,
                mappings,
                executable,
                taskMethod);
    }

    @Override
    public void close() {
        this.stillRunning = false;
    }

    public boolean isRunning() {
        return this.stillRunning;
    }
}
