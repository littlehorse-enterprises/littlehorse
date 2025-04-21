package io.littlehorse.server.streams;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.WaitForCommandResponse;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;

public class ProducerCommandCallback implements Callback {
    private final StreamObserver<WaitForCommandResponse> observer;
    private final AbstractCommand<?> command;
    private final KafkaStreams coreStreams;
    private final HostInfo thisHost;
    private final Function<KeyQueryMetadata, LHInternalsGrpc.LHInternalsStub> internalStub;
    private final Executor networkThreadPool;

    public ProducerCommandCallback(
            StreamObserver<WaitForCommandResponse> observer,
            AbstractCommand<?> command,
            KafkaStreams coreStreams,
            HostInfo thisHost,
            Function<KeyQueryMetadata, LHInternalsGrpc.LHInternalsStub> internalStub,
            Executor networkThreadPool) {
        this.observer = observer;
        this.command = command;
        this.coreStreams = coreStreams;
        this.thisHost = thisHost;
        this.internalStub = internalStub;
        this.networkThreadPool = networkThreadPool;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        this.networkThreadPool.execute(() -> {
            try {
                if (exception != null) {
                    command.getCommandId().notify();
                    observer.onError(new LHApiException(Status.UNAVAILABLE, "Failed recording command to Kafka"));
                }
            } catch (LHApiException ex) {
                observer.onError(ex);
            }
        });
    }
}
