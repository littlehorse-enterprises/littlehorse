package io.littlehorse.sdk.worker.internal;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.List;

public class PollThreadFactory {
    private final LHConfig config;
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub;
    private final TaskDefId targetTaskId;
    private final String taskWorkerId;
    private final List<VariableMapping> mappings;
    private final Object taskWorkerInstance;
    private final Method taskWorkerMethod;

    public PollThreadFactory(
            LHConfig config,
            LittleHorseGrpc.LittleHorseStub bootstrapStub,
            TaskDefId targetTaskId,
            String taskWorkerId,
            List<VariableMapping> mappings,
            Object taskWorkerInstance,
            Method taskWorkerMethod) {
        this.config = config;
        this.bootstrapStub = bootstrapStub;
        this.targetTaskId = targetTaskId;
        this.taskWorkerId = taskWorkerId;
        this.mappings = mappings;
        this.taskWorkerInstance = taskWorkerInstance;
        this.taskWorkerMethod = taskWorkerMethod;
    }

    PollThread create(String threadName, LHHostInfo host) {
        LittleHorseGrpc.LittleHorseStub specificStub = config.getAsyncStub(host.getHost(), host.getPort());
        return new PollThread(
                threadName,
                config.getInflightTasks(),
                bootstrapStub,
                specificStub,
                targetTaskId,
                taskWorkerId,
                config.getTaskWorkerVersion(),
                mappings,
                taskWorkerInstance,
                taskWorkerMethod,
                new ScheduledTaskExecutor(bootstrapStub));
    }
}
