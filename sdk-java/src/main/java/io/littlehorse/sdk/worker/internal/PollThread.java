package io.littlehorse.sdk.worker.internal;

import com.google.common.collect.Iterators;
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

    private final Iterator<PollTaskStub> activePollClients;
    private final Semaphore availableInflightRequests;

    public final LittleHorseGrpc.LittleHorseStub stub;
    public final LittleHorseGrpc.LittleHorseStub bootstrapStub;
    private boolean stillRunning = true;
    private final boolean requireConcurrency;

    PollThread(
            String threadName,
            int inflightRequests,
            LittleHorseGrpc.LittleHorseStub stub,
            LittleHorseGrpc.LittleHorseStub bootstrapStub,
            TaskDefId taskDefId,
            String taskWorkerId,
            String taskWorkerVersion,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod,
            ScheduledTaskExecutor taskExecutor) {
        super(threadName);
        this.stub = stub;
        this.bootstrapStub = bootstrapStub;
        this.availableInflightRequests = new Semaphore(inflightRequests);
        taskMethod.setAccessible(true);
        this.requireConcurrency = inflightRequests > 1;
        List<PollTaskStub> pollClients = Stream.generate(() -> new PollTaskStub(
                        bootstrapStub,
                        stub,
                        availableInflightRequests,
                        taskExecutor,
                        taskWorkerId,
                        taskDefId,
                        taskWorkerVersion,
                        mappings,
                        executable,
                        taskMethod))
                .limit(inflightRequests)
                .collect(Collectors.toList());
        this.activePollClients = Iterators.cycle(pollClients);
    }

    @Override
    public void run() {
        try {
            while (stillRunning) {
                PollTaskStub pollClient = activePollClients.next();
                if (!requireConcurrency || pollClient.isReady()) {
                    pollClient.doNext();
                }
                if (pollClient.isClosed()) {
                    stillRunning = false;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        this.stillRunning = false;
    }

    public boolean isRunning() {
        return this.stillRunning;
    }
}
