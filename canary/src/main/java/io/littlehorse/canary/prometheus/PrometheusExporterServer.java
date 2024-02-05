package io.littlehorse.canary.prometheus;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusExporterServer {

    public static final String METRICS_PATH = "/metrics";
    private final Javalin server;
    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public PrometheusExporterServer(final int webPort, final PrometheusMeterRegistry prometheusRegistry) {
        prometheusMeterRegistry = prometheusRegistry;

        server = Javalin.create();
        Shutdown.addShutdownHook(server::stop);
        server.get(METRICS_PATH, context -> printMetrics(context));
        server.start(webPort);
    }

    private void printMetrics(final Context context) {
        log.trace("Processing metrics request");
        context.result(prometheusMeterRegistry.scrape());
    }
}
