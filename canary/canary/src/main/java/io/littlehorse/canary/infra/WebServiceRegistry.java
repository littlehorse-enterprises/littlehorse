package io.littlehorse.canary.infra;

import io.javalin.Javalin;
import io.javalin.http.Handler;

public class WebServiceRegistry {

    private final Javalin server;

    public WebServiceRegistry(final Javalin server) {
        this.server = server;
    }

    public void get(final String path, final Handler handler) {
        server.get(path, handler);
    }
}
