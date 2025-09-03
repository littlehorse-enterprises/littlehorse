package io.littlehorse.sdk.worker.internal;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollThreadFactory {
    private final LHConfig config;
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub;
    private final TaskDefId targetTaskId;
    private final String taskWorkerId;
    private final List<VariableMapping> mappings;
    private final Object taskWorkerInstance;
    private final Method taskWorkerMethod;
    private final ScheduledTaskExecutor executor;

    public PollThreadFactory(
            LHConfig config,
            LittleHorseGrpc.LittleHorseStub bootstrapStub,
            TaskDefId targetTaskId,
            String taskWorkerId,
            List<VariableMapping> mappings,
            Object taskWorkerInstance,
            Method taskWorkerMethod,
            ScheduledTaskExecutor executor) {
        this.config = config;
        this.bootstrapStub = bootstrapStub;
        this.targetTaskId = targetTaskId;
        this.taskWorkerId = taskWorkerId;
        this.mappings = mappings;
        this.taskWorkerInstance = taskWorkerInstance;
        this.taskWorkerMethod = taskWorkerMethod;
        this.executor = executor;
        this.taskWorkerMethod.setAccessible(true);
    }

    PollThread create(String threadName, LHHostInfo host) {
        log.info("Connecting PollThread to LH Server Instance on host {}:{}", host.getHost(), host.getPort());
        int inflightRequests = config.getInflightTasks();
        Semaphore availableInflightRequests = new Semaphore(inflightRequests);
        var pollClients = Stream.generate(() -> new PollTaskStub(
                        bootstrapStub,
                        config.getAsyncStub(host.getHost(), host.getPort()),
                        availableInflightRequests,
                        executor,
                        taskWorkerId,
                        targetTaskId,
                        config.getTaskWorkerVersion(),
                        mappings,
                        taskWorkerInstance,
                        taskWorkerMethod))
                .limit(inflightRequests)
                .collect(Collectors.toList());
        return new PollThread(threadName, inflightRequests, pollClients);
    }
}
