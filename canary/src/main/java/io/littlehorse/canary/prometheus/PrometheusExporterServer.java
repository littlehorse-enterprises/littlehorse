package io.littlehorse.canary.prometheus;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.micrometer.MicrometerPlugin;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusExporterServer {

    public static final String METRICS_PATH = "/metrics";
    private final Javalin server;
    private final PrometheusMeterRegistry prometheusRegistry;

    public PrometheusExporterServer(final int webPort, final PrometheusMeterRegistry prometheusRegistry) {
        this.prometheusRegistry = prometheusRegistry;
        server = Javalin.create(serverConfig -> {
                    serverConfig.registerPlugin(
                            new MicrometerPlugin(pluginConfig -> pluginConfig.registry = prometheusRegistry));
                })
                .get(METRICS_PATH, context -> printMetrics(context))
                .start(webPort);
        Shutdown.addShutdownHook("Prometheus Exporter: Web Server", server::stop);
    }

    private void printMetrics(final Context context) {
        log.trace("Processing metrics request");
        context.contentType(TextFormat.CONTENT_TYPE_004).result(prometheusRegistry.scrape());
    }
}
