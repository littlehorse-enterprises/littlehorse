package io.littlehorse.canary.infra;

import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebServer {

    private final int webPort;
    private final Javalin server;
    private final WebServiceRegistry webServiceRegistry;

    public WebServer(final int webPort) {
        this.webPort = webPort;
        this.server = Javalin.create();
        this.webServiceRegistry = new WebServiceRegistry(server);
        ShutdownHook.add("Web Server", server::stop);
    }

    public void addService(final WebServiceBinder service) {
        service.bindTo(webServiceRegistry);
    }

    public void addServices(final WebServiceBinder... services) {
        for (final WebServiceBinder service : services) {
            addService(service);
        }
    }

    public void start() {
        server.start(webPort);
        log.info("Web Server Started");
    }
}
