package io.littlehorse.server.monitoring.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TaskMetadata;
import org.apache.kafka.streams.ThreadMetadata;

@Slf4j
public class InstanceState implements MeterBinder, KafkaStreams.StateListener {
    private final KafkaStreams streams;

    @Getter
    private KafkaStreams.State currentState;

    private static final String METRIC_NAME = "active_tasks_count";

    @Getter
    private Map<String, Integer> activeTaskPerPartition = new HashMap<>();

    public InstanceState(KafkaStreams streams) {
        this.streams = streams;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("registering metric " + METRIC_NAME);
        activeTaskPerPartition.put(ServerTopologyDescriptor.CORE.topologyName, 0);
        activeTaskPerPartition.put(ServerTopologyDescriptor.TIMER.topologyName, 0);
        Gauge.builder(METRIC_NAME + "_" + ServerTopologyDescriptor.CORE.topologyName, activeTaskPerPartition, value -> {
                    return value.get(ServerTopologyDescriptor.CORE.topologyName);
                })
                .register(registry);
        Gauge.builder(
                        METRIC_NAME + "_" + ServerTopologyDescriptor.TIMER.topologyName,
                        activeTaskPerPartition,
                        value -> value.get(ServerTopologyDescriptor.TIMER.topologyName))
                .register(registry);
    }

    @Override
    public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
        this.currentState = newState;
        activeTaskPerPartition.put(ServerTopologyDescriptor.TIMER.topologyName, 0);
        activeTaskPerPartition.put(ServerTopologyDescriptor.CORE.topologyName, 0);
        for (ThreadMetadata metadataForLocalThread : streams.metadataForLocalThreads()) {
            for (TaskMetadata activeTask : metadataForLocalThread.activeTasks()) {
                ServerTopologyDescriptor descriptor =
                        getDescriptor(activeTask.taskId().subtopology());
                if (descriptor != null) {
                    int currentCount = activeTaskPerPartition.get(descriptor.topologyName);
                    activeTaskPerPartition.put(descriptor.topologyName, ++currentCount);
                }
            }
        }
    }

    private ServerTopologyDescriptor getDescriptor(int id) {
        return switch (id) {
            case 1 -> ServerTopologyDescriptor.CORE;
            case 2 -> ServerTopologyDescriptor.TIMER;
            default -> null;
        };
    }

    private enum ServerTopologyDescriptor {
        CORE("core", 1),
        TIMER("timer", 2);
        private final String topologyName;
        private final int id;

        ServerTopologyDescriptor(String topologyName, int id) {
            this.topologyName = topologyName;
            this.id = id;
        }
    }
}
