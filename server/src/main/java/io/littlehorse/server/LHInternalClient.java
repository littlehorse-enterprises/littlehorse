package io.littlehorse.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.WaitForCommandRequest;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.server.auth.internalport.InternalCallCredentials;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.HostInfo;

@Slf4j
public final class LHInternalClient {

    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final ChannelCredentials clientCreds;
    private final ExecutorService networkThreads;

    public LHInternalClient(ChannelCredentials clientCreds, ExecutorService networkThreads) {
        this.clientCreds = clientCreds;
        this.networkThreads = networkThreads;
    }

    public <T extends Message> CompletableFuture<T> remoteWaitForCommand(
            WaitForCommandRequest request,
            Class<T> responseCls,
            HostInfo host,
            RequestExecutionContext clientExecutionContext) {
        try {
            WaitForCommandResponse waitForCommandResponse =
                    getInternalAsyncClient(host, clientExecutionContext).waitForCommand(request);
            return CompletableFuture.completedFuture(waitForCommandResponse)
                    .thenApply(r -> this.buildResponseFromWaitForCommand(r, responseCls));
        } catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private <T extends Message> T buildResponseFromWaitForCommand(
            WaitForCommandResponse response, Class<T> responseCls) {
        try {
            ByteString bytes = response.getResult();

            return (T) responseCls.getMethod("parseFrom", ByteString.class).invoke(null, bytes);
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new RuntimeException("Not possible");
        }
    }

    private LHInternalsGrpc.LHInternalsBlockingStub getInternalAsyncClient(
            HostInfo host, RequestExecutionContext clientExecutionContext) {
        if (host.port() == -1) {
            throw new LHApiException(
                    Status.UNAVAILABLE, "Kafka Streams not ready or invalid server cluster configuration");
        }
        return LHInternalsGrpc.newBlockingStub(getChannel(host))
                .withCallCredentials(InternalCallCredentials.forContext(clientExecutionContext));
    }

    private ManagedChannel getChannel(HostInfo host) {
        String key = host.host() + ":" + host.port();
        ManagedChannel channel = channels.get(key);
        if (channel == null) {
            if (clientCreds == null) {
                channel = ManagedChannelBuilder.forAddress(host.host(), host.port())
                        .usePlaintext()
                        .executor(networkThreads)
                        .build();
            } else {
                channel = Grpc.newChannelBuilderForAddress(host.host(), host.port(), clientCreds)
                        .executor(networkThreads)
                        .build();
            }
            channels.put(key, channel);
        }
        return channel;
    }
}
