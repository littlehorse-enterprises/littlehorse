package io.littlehorse.examples;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingClientInterceptor;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This example shows how to plug a custom gRPC ClientInterceptor into the
 * LittleHorse Java client SDK.
 *
 * The LHConfig constructor accepts a varargs list of io.grpc.ClientInterceptor.
 * Every gRPC channel created by the SDK (for the blocking stub used to register
 * metadata as well as the task worker's stubs) will be wrapped with the provided
 * interceptors.
 *
 * Here we use Micrometer's MetricCollectingClientInterceptor to expose gRPC client
 * metrics, and we serve them over an HTTP /metrics endpoint (via Javalin) that
 * Prometheus can scrape.
 */
public class GrpcInterceptorExample {

    private static final Logger log = LoggerFactory.getLogger(GrpcInterceptorExample.class);

    private static final int METRICS_PORT = 8080;
    private static final String METRICS_PATH = "/metrics";

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-grpc-interceptor", wf -> {
            WfRunVariable theName = wf.declareStr("input-name").searchable();
            wf.execute("greet", theName);
        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static LHTaskWorker getTaskWorker(LHConfig config) {
        MyWorker executable = new MyWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "greet", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();

        // 1. Create a Micrometer registry and a gRPC client interceptor that records metrics.
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        MetricCollectingClientInterceptor interceptor = new MetricCollectingClientInterceptor(prometheusRegistry);

        // 2. Pass the interceptor into LHConfig. The SDK applies it to every gRPC channel it creates.
        LHConfig config = new LHConfig(props, interceptor);

        // 3. Expose the collected gRPC client metrics over an HTTP endpoint Prometheus can scrape.
        Javalin app = Javalin.create()
                .get(METRICS_PATH, ctx -> ctx.contentType(ContentType.PLAIN).result(prometheusRegistry.scrape()));
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        app.start(METRICS_PORT);
        log.info("Serving Prometheus metrics at http://localhost:{}{}", METRICS_PORT, METRICS_PATH);

        // New workflow
        Workflow workflow = getWorkflow();
        // New worker
        LHTaskWorker worker = getTaskWorker(config);

        // Register task
        worker.registerTaskDef();

        // Register a workflow
        workflow.registerWfSpec(config.getBlockingStub());

        // Run the worker
        worker.start();
    }
}
