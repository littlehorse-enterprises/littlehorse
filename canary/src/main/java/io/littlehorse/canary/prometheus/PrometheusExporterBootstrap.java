package io.littlehorse.canary.prometheus;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PrometheusExporterBootstrap extends Bootstrap {

    private final PrometheusMeterRegistry prometheusRegistry;

    public PrometheusExporterBootstrap(final CanaryConfig config) {
        super(config);

        prometheusRegistry = initializeRegistry();

        new PrometheusExporterServer(config.getMetricsPort(), config.getMetricsPath(), prometheusRegistry);
    }

    private PrometheusMeterRegistry initializeRegistry() {
        final PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Shutdown.addShutdownHook("Prometheus Exporter", prometheusRegistry::close);

        prometheusRegistry.config().commonTags("application_id", config.getId());
        if (config.isMetricsFilterEnabled()) {
            prometheusRegistry.config().meterFilter(new PrometheusMetricFilter(config.getEnabledMetrics()));
        }

        final JvmMemoryMetrics jvmMeter = new JvmMemoryMetrics();
        jvmMeter.bindTo(prometheusRegistry);

        final JvmThreadMetrics jvmThreadMetrics = new JvmThreadMetrics();
        jvmThreadMetrics.bindTo(prometheusRegistry);

        final ProcessorMetrics processorMetrics = new ProcessorMetrics();
        processorMetrics.bindTo(prometheusRegistry);

        return prometheusRegistry;
    }

    public void addMesurable(final MeterBinder measurable) {
        measurable.bindTo(prometheusRegistry);
    }
}
