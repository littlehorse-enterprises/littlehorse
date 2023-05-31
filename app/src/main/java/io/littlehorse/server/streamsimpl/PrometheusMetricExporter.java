package io.littlehorse.server.streamsimpl;

import com.sun.net.httpserver.HttpServer;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.log4j.Logger;

public class PrometheusMetricExporter implements Closeable {

    private static final Logger log = Logger.getLogger(
        PrometheusMetricExporter.class
    );

    private HttpServer server;
    private List<KafkaStreamsMetrics> streamsMetrics;
    private PrometheusMeterRegistry prometheusRegistry;
    private LHConfig config;

    public PrometheusMetricExporter(LHConfig config, List<KafkaStreams> streams) {
        this.config = config;
        this.prometheusRegistry =
            new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        this.streamsMetrics =
            streams
                .stream()
                .map(stream -> {
                    KafkaStreamsMetrics metric = new KafkaStreamsMetrics(stream);
                    metric.bindTo(prometheusRegistry);
                    return metric;
                })
                .toList();
    }

    public void start() throws IOException {
        int port = config.getPrometheusExporterPort();
        log.info("Starting prometheus service at: " + port);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(
            LHConstants.PROMETHEUS_EXPORTER_PATH,
            httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(
                    HttpResponseStatus.OK.code(),
                    response.getBytes().length
                );
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        );
        server.start();

        log.info("Prometheus started");
    }

    @Override
    public void close() {
        streamsMetrics.stream().forEach(metric -> metric.close());
        server.stop(1);
    }
}
