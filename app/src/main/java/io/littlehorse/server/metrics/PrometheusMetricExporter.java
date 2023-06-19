package io.littlehorse.server.metrics;

import io.javalin.Javalin;
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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusMetricExporter implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(
        PrometheusMetricExporter.class
    );

    private Javalin server;
    private List<KafkaStreamsMetrics> kafkaStreamsMeters;
    private PrometheusMeterRegistry prometheusRegistry;
    private LHConfig config;

    public PrometheusMetricExporter(LHConfig config) {
        this.config = config;
        this.prometheusRegistry =
            new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        new ServerMetricFilter(prometheusRegistry, ServerFilterRules.RULES);
    }

    public MeterRegistry getRegistry() {
        return prometheusRegistry;
    }

    public void bind(Map<String, KafkaStreams> streams) {
        this.kafkaStreamsMeters =
            streams
                .entrySet()
                .stream()
                .map(entry -> {
                    KafkaStreamsMetrics metric = new KafkaStreamsMetrics(
                        entry.getValue(),
                        Tags.of("topology", entry.getKey())
                    );
                    metric.bindTo(prometheusRegistry);
                    return metric;
                })
                .toList();

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

    public void start() throws IOException {
        int port = config.getPrometheusExporterPort();
        String path = config.getPrometheusExporterPath();

        log.info("Starting prometheus service at :{}{}", port, path);
        server = Javalin.create().get(path, handleRequest()).start(port);
        log.info("Prometheus started");
    }

    private Handler handleRequest() {
        return ctx -> {
            log.debug(
                "Request [from={}, path={}, method={}]",
                ctx.req().getRemoteHost(),
                ctx.path(),
                ctx.method()
            );
            ctx.result(prometheusRegistry.scrape());
        };
    }

    @Override
    public void close() {
        if (server != null) {
            server.stop();
        }
        kafkaStreamsMeters.stream().forEach(metric -> metric.close());
        prometheusRegistry.close();
        log.info("Prometheus stopped");
    }
}
