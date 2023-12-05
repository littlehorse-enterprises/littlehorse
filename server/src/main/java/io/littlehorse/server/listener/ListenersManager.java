package io.littlehorse.server.listener;

import io.grpc.BindableService;
import io.grpc.Context;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class ListenersManager implements Closeable {

    private final List<ServerListener> servers;

    public ListenersManager(
            LHServerConfig config,
            BindableService service,
            Executor threadpool,
            MeterRegistry meter,
            MetadataCache metadataCache,
            Context.Key<RequestExecutionContext> executionContextKey,
            BiFunction<Integer, String, ReadOnlyKeyValueStore<String, Bytes>> storeProvider) {
        this.servers = config.getListeners().stream()
                .map(serverListenerConfig -> new ServerListener(
                        serverListenerConfig,
                        threadpool,
                        service,
                        meter,
                        metadataCache,
                        executionContextKey,
                        storeProvider))
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
