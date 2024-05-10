package io.littlehorse.server.monitoring.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TaskMetadata;
import org.apache.kafka.streams.ThreadMetadata;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class InstanceState implements MeterBinder, KafkaStreams.StateListener, Closeable {
    private final KafkaStreams streams;

    @Getter
    private KafkaStreams.State currentState;

    private static final String METRIC_NAME = "active_tasks_count";

    @Getter
    private final Map<Integer, Integer> activeTaskBySubTopology = new HashMap<>();

    private static final int GLOBAL_SUB_TOPOLOGY_ID = 0;
    private static final int CORE_SUB_TOPOLOGY_ID = 1;
    private static final int REPARTITION_SUB_TOPOLOGY_ID = 2;

    public InstanceState(KafkaStreams streams) {
        this.streams = streams;
    }

    @Override
    public void bindTo(@NotNull MeterRegistry registry) {
        Gauge.builder(METRIC_NAME + "_global", activeTaskBySubTopology, value -> {
                    return value.getOrDefault(GLOBAL_SUB_TOPOLOGY_ID, 0);
                })
                .register(registry);
        Gauge.builder(METRIC_NAME + "_core", activeTaskBySubTopology, value -> {
                    return value.getOrDefault(CORE_SUB_TOPOLOGY_ID, 0);
                })
                .register(registry);
        Gauge.builder(METRIC_NAME + "_repartition", activeTaskBySubTopology, value -> {
                    return value.getOrDefault(REPARTITION_SUB_TOPOLOGY_ID, 0);
                })
                .register(registry);
    }

    @Override
    public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
        this.currentState = newState;
        activeTaskBySubTopology.put(GLOBAL_SUB_TOPOLOGY_ID, 0);
        activeTaskBySubTopology.put(CORE_SUB_TOPOLOGY_ID, 0);
        activeTaskBySubTopology.put(REPARTITION_SUB_TOPOLOGY_ID, 0);
        for (ThreadMetadata metadataForLocalThread : streams.metadataForLocalThreads()) {
            for (TaskMetadata activeTask : metadataForLocalThread.activeTasks()) {
                final int subtopology = activeTask.taskId().subtopology();
                int currentCount = activeTaskBySubTopology.getOrDefault(subtopology, 0);
                activeTaskBySubTopology.put(subtopology, ++currentCount);
            }
        }
    }

    @Override
    public void close() throws IOException {}
}
