package io.littlehorse.server.streams;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.server.streams.util.AsyncWaiters;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

class ProducerCommandCallbackTest {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AsyncWaiters commandWaiters = new AsyncWaiters();
    private final StreamObserver<WaitForCommandResponse> responseObserver = mock();
    private final AbstractCommand<?> command = mock(AbstractCommand.class);
    private final KafkaStreams coreStreams = mock();
    private final HostInfo hostInfo = new HostInfo("localhost", 2023);
    private final LHInternalsGrpc.LHInternalsStub stub = mock();
    private final Function<KeyQueryMetadata, LHInternalsGrpc.LHInternalsStub> stubProvider = (meta) -> stub;
    private final ProducerCommandCallback producerCallback = new ProducerCommandCallback(
            responseObserver, command, coreStreams, hostInfo, stubProvider, commandWaiters, executor);
    private final RecordMetadata metadata = new RecordMetadata(new TopicPartition("my-topic", 2), 0L, 0, 0L, 0, 0);
    private final WaitForCommandResponse response = mock();
    private final KeyQueryMetadata keyQueryMetadata = new KeyQueryMetadata(hostInfo, Collections.emptySet(), 2);

    @BeforeEach
    public void setup() {
        when(command.getCommandId()).thenReturn("123");
        when(command.getPartitionKey()).thenReturn("123");
        when(coreStreams.queryMetadataForKey(anyString(), anyString(), any(Serializer.class)))
                .thenReturn(keyQueryMetadata);
    }

    @ParameterizedTest
    @EnumSource(value = LHStoreType.class, mode = EnumSource.Mode.EXCLUDE, names = "UNRECOGNIZED")
    public void shouldWaitForCommandAfterCompletion(LHStoreType storeType) throws InterruptedException {
        when(command.getStore()).thenReturn(storeType);
        producerCallback.onCompletion(metadata, null);
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        commandWaiters.registerCommandProcessed("123", response);
        verify(responseObserver).onNext(response);
        verify(responseObserver).onCompleted();
    }

    @Test
    public void shouldHandleTransientErrorsFromKafkaStreamsMetadata() throws InterruptedException {
        when(command.getStore()).thenReturn(LHStoreType.CORE);
        when(coreStreams.queryMetadataForKey(anyString(), anyString(), any(Serializer.class)))
                .thenThrow(new IllegalStateException("invalid state store"));
        producerCallback.onCompletion(metadata, null);
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        ArgumentCaptor<LHApiException> errorCaptor = ArgumentCaptor.forClass(LHApiException.class);
        verify(responseObserver).onError(errorCaptor.capture());
        LHApiException apiException = errorCaptor.getValue();
        assertThat(apiException)
                .hasMessageContaining("invalid state store")
                .extracting(StatusRuntimeException::getStatus)
                .extracting(Status::getCode)
                .isEqualTo(Status.UNAVAILABLE.getCode());
    }
}
