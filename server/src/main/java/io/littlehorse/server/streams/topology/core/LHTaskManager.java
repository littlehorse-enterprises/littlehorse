package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.storeinternals.TaskQueueHintModel;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

/**
 * This class provides useful methods for managing LH tasks
 */
@Slf4j
public class LHTaskManager {

    private final List<LHTimer> timersToSchedule = new ArrayList<>();

    private final Map<String, ScheduledTaskModel> scheduledTaskPuts = new HashMap<>();

    private final String timerTopicName;
    private final String commandTopicName;
    private final AuthorizationContext authContext;

    private final ProcessorContext<String, CommandProcessorOutput> processorContext;
    private final TaskQueueManager taskQueueManager;
    private final TenantScopedStore coreStore;

    private Date latestClearedTask;

    public LHTaskManager(
            String timerTopicName,
            String commandTopicName,
            AuthorizationContext authContext,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            TaskQueueManager taskQueueManager,
            TenantScopedStore coreStore) {
        this.timerTopicName = timerTopicName;
        this.commandTopicName = commandTopicName;
        this.authContext = authContext;
        this.processorContext = processorContext;
        this.taskQueueManager = taskQueueManager;
        this.coreStore = coreStore;
    }

    /**
     * Clear pending timers and scheduled tasks
     */
    public void clearBuffer() {
        timersToSchedule.clear();
        scheduledTaskPuts.clear();
    }

    public void scheduleTask(ScheduledTaskModel scheduledTask) {
        scheduledTaskPuts.put(scheduledTask.getStoreKey(), scheduledTask);
    }

    // @Override
    public void scheduleTimer(LHTimer timer) {
        timersToSchedule.add(timer);
    }

    public ScheduledTaskModel markTaskAsClaimed(TaskRunModel taskRun) {
        boolean isLegacy = false;
        ScheduledTaskModel scheduledTask =
                this.coreStore.get(ScheduledTaskModel.getScheduledTaskKey(taskRun), ScheduledTaskModel.class);

        if (scheduledTask == null) {
            isLegacy = true;
            scheduledTask = coreStore.get(ScheduledTaskModel.getLegacyKey(taskRun), ScheduledTaskModel.class);
        }

        if (scheduledTask != null) {
            scheduledTaskPuts.put(scheduledTask.getStoreKey(), null);
            if (!isLegacy && (latestClearedTask == null || latestClearedTask.compareTo(taskRun.getCreatedAt()) < 0)) {
                latestClearedTask = taskRun.getCreatedAt();
            }
        }

        return scheduledTask;
    }

    void forwardPendingTimers() {
        for (LHTimer lhTimer : timersToSchedule) {
            forwardTimer(lhTimer);
        }
    }

    void forwardPendingTasks() {
        TaskId taskId = processorContext.taskId();
        for (Map.Entry<String, ScheduledTaskModel> entry : scheduledTaskPuts.entrySet()) {
            String scheduledTaskId = entry.getKey();
            ScheduledTaskModel scheduledTask = entry.getValue();
            if (scheduledTask != null) {
                this.coreStore.put(scheduledTask);
                taskQueueManager.onTaskScheduled(
                        taskId, scheduledTask.getTaskDefId(), scheduledTask, authContext.tenantId());
            } else {
                this.coreStore.delete(scheduledTaskId, StoreableType.SCHEDULED_TASK);
            }
        }

        if (latestClearedTask != null) {
            // TODO: Refactor the LHTaskManager so that we can do this every 1,000 TaskRun's rather than
            // every single TaskRun. In the grand scheme of things, most of these writes will go to the
            // Write Buffer, but it will still be slightly better for performance to do it less
            // frequently.

            coreStore.put(new TaskQueueHintModel(latestClearedTask));
        }
    }

    void forwardTimer(LHTimer timer) {
        timer.setTenantId(authContext.tenantId());
        timer.setPrincipalId(authContext.principalId());
        timer.topic = commandTopicName;
        CommandProcessorOutput output = new CommandProcessorOutput(timerTopicName, timer, timer.partitionKey);
        Headers headers = HeadersUtil.metadataHeadersFor(authContext.tenantId(), authContext.principalId());
        log.trace("forwarding lh timer: {}", timer);
        processorContext.forward(new Record<>(timer.partitionKey, output, System.currentTimeMillis(), headers));
    }
}
