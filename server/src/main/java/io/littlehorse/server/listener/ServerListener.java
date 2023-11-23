package io.littlehorse.server.listener;

import io.grpc.BindableService;
import io.grpc.Context;
import io.grpc.Grpc;
import io.grpc.Server;
import io.littlehorse.server.auth.RequestAuthorizer;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class ServerListener implements Closeable {

    private final ServerListenerConfig config;
    private final Server server;

    // storeProvider: RequestAuthorizer queries the store in order to get principals
    public ServerListener(
            ServerListenerConfig config,
            Executor executor,
            BindableService service,
            MeterRegistry meter,
            MetadataCache metadataCache,
            Context.Key<RequestExecutionContext> executionContextKey,
            BiFunction<Integer, String, ReadOnlyKeyValueStore<String, Bytes>> storeProvider) {
        this.config = config;
        this.server = Grpc.newServerBuilderForPort(config.getPort(), config.getCredentials())
                .permitKeepAliveTime(15, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .addService(service)
                .intercept(new MetricCollectingServerInterceptor(meter))
                .intercept(new RequestAuthorizer(
                        service, executionContextKey, metadataCache, storeProvider, config.getConfig()))
                .intercept(config.getAuthorizer())
                .executor(executor)
                .build();
    }

    public void start() {
        try {
            server.start();
        } catch (IOException e) {
            throw new ServerListenerInitializationException(e);
        }
        log.info("Server {} was started at: {}", config.getName(), server.getPort());
    }

    @Override
    public void close() {
        // This forcibly closes all grpc connections rather than waiting for them to
        // complete. This cuts off all streaming connections too.
        server.shutdownNow();
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            log.warn("InterruptedException Ignored", e);
        }
        log.info("Server {} was stopped", config.getName());
    }
}
