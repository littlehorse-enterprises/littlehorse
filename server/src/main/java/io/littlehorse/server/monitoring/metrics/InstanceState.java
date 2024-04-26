package io.littlehorse.server.monitoring.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TaskMetadata;
import org.apache.kafka.streams.ThreadMetadata;

@Slf4j
public class InstanceState implements MeterBinder, Closeable, KafkaStreams.StateListener {
    private final KafkaStreams streams;

    @Getter
    private KafkaStreams.State currentState;

    private final ScheduledExecutorService mainExecutor;
    private static final String METRIC_NAME = "active_tasks_count";

    @Getter
    private int currentAssignedTasks;

    public InstanceState(KafkaStreams streams) {
        this.streams = streams;
        mainExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("registering metric " + METRIC_NAME);
        Gauge.builder(METRIC_NAME, () -> currentAssignedTasks).register(registry);
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

    @Override
    public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
        this.currentState = newState;
        currentAssignedTasks = 0;
        for (ThreadMetadata metadataForLocalThread : streams.metadataForLocalThreads()) {
            for (TaskMetadata activeTask : metadataForLocalThread.activeTasks()) {
                log.info(activeTask.taskId().toString());
            }
            currentAssignedTasks += metadataForLocalThread.activeTasks().size();
        }
    }
}
