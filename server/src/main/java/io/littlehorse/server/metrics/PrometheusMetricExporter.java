package io.littlehorse.server.metrics;

import io.javalin.http.Handler;
import io.littlehorse.common.LHConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.Closeable;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;

@Slf4j
public class PrometheusMetricExporter implements Closeable {

    private List<KafkaStreamsMetrics> kafkaStreamsMeters;
    private PrometheusMeterRegistry prometheusRegistry;
    private LHConfig config;

    public PrometheusMetricExporter(LHConfig config) {
        this.config = config;
        this.prometheusRegistry =
            new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        new ServerMetricFilter(prometheusRegistry, ServerFilterRules.RULES)
            .initialize();
    }

    public MeterRegistry getMeterRegistry() {
        return prometheusRegistry;
    }

    public void bind(KafkaStreams coreStreams, KafkaStreams timerStreams) {
        this.kafkaStreamsMeters =
            List.of(
                new KafkaStreamsMetrics(coreStreams, Tags.of("topology", "core")),
                new KafkaStreamsMetrics(timerStreams, Tags.of("topology", "timer"))
            );
        for (KafkaStreamsMetrics ksm : kafkaStreamsMeters) {
            ksm.bindTo(prometheusRegistry);
        }

        JvmMemoryMetrics jvmMeter = new JvmMemoryMetrics();
        jvmMeter.bindTo(prometheusRegistry);

        JvmThreadMetrics jvmThreadMetrics = new JvmThreadMetrics();
        jvmThreadMetrics.bindTo(prometheusRegistry);

        DiskSpaceMetrics diskSpaceMetrics = new DiskSpaceMetrics(
            new File(config.getStateDirectory())
        );
        diskSpaceMetrics.bindTo(prometheusRegistry);

        ProcessorMetrics processorMetrics = new ProcessorMetrics();
        processorMetrics.bindTo(prometheusRegistry);
    }

    public Handler handleRequest() {
        return ctx -> {
            log.trace("Processing metrics request");
            ctx.result(prometheusRegistry.scrape());
        };
    }

    @Override
    public void close() {
        kafkaStreamsMeters.stream().forEach(metric -> metric.close());
        prometheusRegistry.close();
        log.info("Prometheus stopped");
    }
}
