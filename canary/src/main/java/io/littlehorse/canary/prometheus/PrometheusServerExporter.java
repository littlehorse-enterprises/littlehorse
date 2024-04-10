package io.littlehorse.canary.prometheus;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.littlehorse.canary.util.ShutdownHook;
import io.prometheus.client.exporter.common.TextFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusServerExporter {

    private final PrometheusExporter prometheusExporter;

    public PrometheusServerExporter(
            final int webPort, final String webPath, final PrometheusExporter prometheusExporter) {
        this.prometheusExporter = prometheusExporter;
        final Javalin server = Javalin.create().get(webPath, this::printMetrics).start(webPort);
        ShutdownHook.add("Prometheus Exporter: Web Server", server::stop);
    }

    private void printMetrics(final Context context) {
        log.trace("Processing metrics request");
        context.contentType(TextFormat.CONTENT_TYPE_004).result(prometheusExporter.scrape());
    }
}
