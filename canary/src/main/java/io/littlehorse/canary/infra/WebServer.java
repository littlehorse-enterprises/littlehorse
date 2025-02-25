package io.littlehorse.canary.infra;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebServer {

    private final int webPort;
    private final Javalin server;

    public WebServer(final int webPort) {
        this.webPort = webPort;
        this.server = Javalin.create();
        ShutdownHook.add("Web Server", server::stop);
    }

    public void addHandler(final HandlerType type, final String path, final Handler handler) {
        server.addHttpHandler(type, path, handler);
    }

    public void start() {
        server.start(webPort);
        log.info("Metrics Server Exporter Started");
    }
}
