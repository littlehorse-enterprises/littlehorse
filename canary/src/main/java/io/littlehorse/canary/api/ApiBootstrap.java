package io.littlehorse.canary.api;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.util.Shutdown;

public class ApiBootstrap implements Bootstrap {

    private final ApiServer apiServer;

    public ApiBootstrap(final int webPort) {
        apiServer = new ApiServer(webPort);
        Shutdown.addShutdownHook(apiServer);
    }
}
