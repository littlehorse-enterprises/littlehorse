package io.littlehorse.server.listener;

import io.grpc.BindableService;
import io.grpc.Context;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executor;

public class ListenersManager implements Closeable {

    private final List<ServerListener> servers;

    public ListenersManager(
            LHServerConfig config,
            BindableService service,
            Executor threadpool,
            MeterRegistry meter,
            MetadataCache metadataCache,
            Context.Key<RequestExecutionContext> executionContextKey,
            CoreStoreProvider coreStoreProvider) {
        this.servers = config.getListeners().stream()
                .map(serverListenerConfig -> new ServerListener(
                        serverListenerConfig,
                        threadpool,
                        service,
                        meter,
                        metadataCache,
                        executionContextKey,
                        coreStoreProvider))
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
