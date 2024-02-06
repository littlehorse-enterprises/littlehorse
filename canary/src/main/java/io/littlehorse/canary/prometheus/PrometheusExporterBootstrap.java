package io.littlehorse.canary.prometheus;

import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PrometheusExporterBootstrap {

    private final PrometheusMeterRegistry prometheusRegistry;
    private final PrometheusExporterServer prometheusExporterServer;

    public PrometheusExporterBootstrap(
            final int webPort, final String webPath, final boolean metricsFilterEnabled, final String applicationId) {
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        prometheusRegistry.config().commonTags("application_id", applicationId);
        if (metricsFilterEnabled) {
            prometheusRegistry.config().meterFilter(new PrometheusMetricFilter());
        }
        Shutdown.addShutdownHook("Prometheus Exporter", prometheusRegistry::close);

        final JvmMemoryMetrics jvmMeter = new JvmMemoryMetrics();
        jvmMeter.bindTo(prometheusRegistry);

        final JvmThreadMetrics jvmThreadMetrics = new JvmThreadMetrics();
        jvmThreadMetrics.bindTo(prometheusRegistry);

        final ProcessorMetrics processorMetrics = new ProcessorMetrics();
        processorMetrics.bindTo(prometheusRegistry);

        prometheusExporterServer = new PrometheusExporterServer(webPort, webPath, prometheusRegistry);
    }

    public void addMesurable(final Measurable measurable) {
        measurable.bindTo(prometheusRegistry);
    }
}
