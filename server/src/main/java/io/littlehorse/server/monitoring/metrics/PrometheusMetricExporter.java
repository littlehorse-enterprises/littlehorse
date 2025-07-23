package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.StandbyMetrics;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.MetadataCache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.io.Closeable;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;

@Slf4j
public class PrometheusMetricExporter implements Closeable {

    private List<KafkaStreamsMetrics> kafkaStreamsMeters;
    private PrometheusMeterRegistry prometheusRegistry;
    private LHServerConfig config;
    private TaskQueueManagerMetrics taskQueueManagerMetrics;

    public PrometheusMetricExporter(LHServerConfig config) {
        this.config = config;
        this.prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        this.prometheusRegistry.config().commonTags("application_id", config.getLHClusterId());
        new ServerMetricFilter(prometheusRegistry, ServerFilterRules.fromLevel(config.getServerMetricLevel()))
                .initialize();
    }

    public MeterRegistry getMeterRegistry() {
        return prometheusRegistry;
    }

    public void bind(
            KafkaStreams coreStreams,
            KafkaStreams timerStreams,
            TaskQueueManager taskQueueManager,
            MetadataCache metadataCache,
            StandbyMetrics standbyMetrics,
            InstanceState coreState) {

        this.kafkaStreamsMeters = List.of(
                new KafkaStreamsMetrics(coreStreams, Tags.of("topology", "core")),
                new KafkaStreamsMetrics(timerStreams, Tags.of("topology", "timer")));

        for (KafkaStreamsMetrics ksm : kafkaStreamsMeters) {
            ksm.bindTo(prometheusRegistry);
        }

        LHCacheMetrics cacheMetrics = new LHCacheMetrics(metadataCache, "metadata");
        cacheMetrics.bindTo(prometheusRegistry);

        standbyMetrics.bindTo(prometheusRegistry);
        coreState.bindTo(prometheusRegistry);

        taskQueueManagerMetrics = new TaskQueueManagerMetrics(taskQueueManager);
        taskQueueManagerMetrics.bindTo(prometheusRegistry);

        JvmMemoryMetrics jvmMeter = new JvmMemoryMetrics();
        jvmMeter.bindTo(prometheusRegistry);

        JvmThreadMetrics jvmThreadMetrics = new JvmThreadMetrics();
        jvmThreadMetrics.bindTo(prometheusRegistry);

        DiskSpaceMetrics diskSpaceMetrics = new DiskSpaceMetrics(new File(config.getStateDirectory()));
        diskSpaceMetrics.bindTo(prometheusRegistry);

        ProcessorMetrics processorMetrics = new ProcessorMetrics();
        processorMetrics.bindTo(prometheusRegistry);
    }

    public String handleRequest() {
        return prometheusRegistry.scrape();
    }

    @Override
    public void close() {
        kafkaStreamsMeters.forEach(metric -> metric.close());
        prometheusRegistry.close();
        taskQueueManagerMetrics.close();
        log.info("Prometheus stopped");
    }
}
