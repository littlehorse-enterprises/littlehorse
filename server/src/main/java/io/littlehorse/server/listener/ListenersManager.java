package io.littlehorse.server.listener;

import io.grpc.BindableService;
import io.littlehorse.common.LHConfig;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListenersManager implements Closeable {

    private final List<ServerListener> servers;

    public ListenersManager(
        LHConfig config,
        BindableService service,
        MeterRegistry meter
    ) {
        Executor executor = Executors.newFixedThreadPool(16);
        this.servers =
            config
                .getAdvertisedListeners()
                .stream()
                .map(serverListenerConfig ->
                    new ServerListener(serverListenerConfig, executor, service, meter)
                )
                .toList();
    }

    public void start() {
        servers.forEach(ServerListener::start);
    }

    @Override
    public void close() {
        servers.forEach(ServerListener::close);
    }
}
