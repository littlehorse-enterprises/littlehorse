package io.littlehorse.canary.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.io.Closeable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ApiServer implements Closeable {

    public static final String METRICS_PATH = "/metrics";
    private final Javalin server;

    public ApiServer(final int webPort) {
        server = Javalin.create();
        server.get(METRICS_PATH, context -> printMetrics(context));
        server.start(webPort);
    }

    @NotNull
    private static void printMetrics(final Context context) {
        log.trace("Processing metrics request");
        context.result("Hola");
    }

    @Override
    public void close() {
        server.stop();
    }
}
