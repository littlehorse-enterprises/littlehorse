package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.server.streams.taskqueue.OneTaskQueue;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskQueueManagerMetrics implements MeterBinder, Closeable {

    public static final String METRIC_NAME = "lh_task_queue_size";
    public static final String TENANT_ID_TAG = "tenant_id";
    public static final String TASK_NAME_TAG = "task_name";
    private final TaskQueueManager taskQueueManager;
    private final ScheduledExecutorService mainExecutor;

    public TaskQueueManagerMetrics(TaskQueueManager taskQueueManager) {
        this.taskQueueManager = taskQueueManager;
        mainExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        mainExecutor.scheduleAtFixedRate(() -> updateMetrics(registry), 30, 30, TimeUnit.SECONDS);
    }

    private void updateMetrics(MeterRegistry registry) {
        for (OneTaskQueue queue : taskQueueManager.all()) {
            if (!wasRegistered(registry, queue)) {
                log.trace("Adding new metric for queue {}", queue.getTaskDefName());
                Gauge.builder(METRIC_NAME, queue, OneTaskQueue::size)
                        .tag(TENANT_ID_TAG, queue.getTenantId().getId())
                        .tag(TASK_NAME_TAG, queue.getTaskDefName())
                        .register(registry);
            }
        }
    }

    private boolean wasRegistered(MeterRegistry registry, OneTaskQueue queue) {
        return registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals(METRIC_NAME))
                .filter(meter -> meter.getId().getTag(TENANT_ID_TAG) != null)
                .anyMatch(meter -> meter.getId().getTag(TASK_NAME_TAG) != null);
    }

    @Override
    public void close() {
        try {
            mainExecutor.shutdownNow();
            mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("Error when closing meter {}", ex.getMessage(), ex);
        }
    }
}
