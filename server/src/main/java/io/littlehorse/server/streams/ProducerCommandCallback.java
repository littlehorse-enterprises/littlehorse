package io.littlehorse.server.streams;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.WaitForCommandRequest;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.AsyncWaiters;
import java.util.function.Function;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;

public class ProducerCommandCallback implements Callback {
    private final StreamObserver<WaitForCommandResponse> observer;
    private final AbstractCommand<?> command;
    private final RequestExecutionContext context;
    private final KafkaStreams coreStreams;
    private final HostInfo thisHost;
    private final Function<KeyQueryMetadata, LHInternalsGrpc.LHInternalsStub> internalStub;
    private final AsyncWaiters asyncWaiters;

    public ProducerCommandCallback(
            StreamObserver<WaitForCommandResponse> observer,
            AbstractCommand<?> command,
            RequestExecutionContext context,
            KafkaStreams coreStreams,
            HostInfo thisHost,
            Function<KeyQueryMetadata, LHInternalsGrpc.LHInternalsStub> internalStub,
            AsyncWaiters asyncWaiters) {
        this.observer = observer;
        this.command = command;
        this.context = context;
        this.coreStreams = coreStreams;
        this.thisHost = thisHost;
        this.internalStub = internalStub;
        this.asyncWaiters = asyncWaiters;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
            observer.onError(new LHApiException(Status.UNAVAILABLE, "Failed recording command to Kafka"));
        } else {
            waitForCommand(command, observer, context);
        }
    }

    private void waitForCommand(
            AbstractCommand<?> command,
            StreamObserver<WaitForCommandResponse> observer,
            RequestExecutionContext requestCtx) {
        String storeName =
                switch (command.getStore()) {
                    case CORE -> ServerTopology.CORE_STORE;
                    case METADATA -> ServerTopology.METADATA_STORE;
                    case REPARTITION -> ServerTopology.CORE_REPARTITION_STORE;
                    case UNRECOGNIZED -> throw new LHApiException(Status.INTERNAL);
                };
        try {
            KeyQueryMetadata meta = lookupPartitionKey(storeName, command.getPartitionKey());
            /*
             * As a prerequisite to this method being called, the command has already
             * been recorded into the CoreCommand Kafka Topic (and ack'ed by all
             * of the in-sync replicas).
             */
            if (meta.activeHost().equals(thisHost)) {
                localWaitForCommand(command.getCommandId(), meta.partition(), observer);
            } else {
                WaitForCommandRequest req = WaitForCommandRequest.newBuilder()
                        .setCommandId(command.getCommandId())
                        .setPartition(meta.partition())
                        .build();
                internalStub.apply(meta).waitForCommand(req, observer);
            }
        } catch (LHApiException ex) {
            observer.onError(ex);
        }
    }

    private void localWaitForCommand(String commandId, int partition, StreamObserver<WaitForCommandResponse> observer) {
        asyncWaiters.registerObserverWaitingForCommand(commandId, partition, observer);
    }

    private KeyQueryMetadata lookupPartitionKey(String storeName, String partitionKey) {
        try {
            KeyQueryMetadata metadata = coreStreams.queryMetadataForKey(
                    storeName, partitionKey, Serdes.String().serializer());
            if (metadata.activeHost().port() == -1
                    && metadata.activeHost().host().equals("unavailable")) {
                throw new LHApiException(Status.UNAVAILABLE, "Kafka Streams not ready yet");
            }
            return metadata;
        } catch (IllegalStateException ex) {
            throw new LHApiException(Status.UNAVAILABLE, ex.getMessage());
        }
    }
}
