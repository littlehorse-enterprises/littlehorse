package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class TaskQueueManagerMetrics implements MeterBinder {

    private final TaskQueueManager taskQueueManager;

    public TaskQueueManagerMetrics(TaskQueueManager taskQueueManager) {
        this.taskQueueManager = taskQueueManager;
    }

    @Override
    public void bindTo(MeterRegistry registry) {}
}
