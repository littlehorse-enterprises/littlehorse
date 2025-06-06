package io.littlehorse.common.util;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskWorkerHeartBeatRequestModel;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LHProducerTest {

    private MockProducer<String, Bytes> prod = new MockProducer<>(
            true, null, Serdes.String().serializer(), Serdes.Bytes().serializer());
    private final LHProducer lhProducer = new LHProducer(prod);
    private final TaskWorkerHeartBeatRequest heartBeat = TaskWorkerHeartBeatRequest.newBuilder()
            .setClientId("worker-id")
            .setListenerName("test")
            .setTaskDefId(TaskDefId.newBuilder().setName("test-task"))
            .build();
    private final ExecutionContext executionContext = Mockito.mock(ExecutionContext.class);
    TaskWorkerHeartBeatRequestModel subCommand =
            LHSerializable.fromProto(heartBeat, TaskWorkerHeartBeatRequestModel.class, executionContext);
    private final CommandModel commandToProduce = new CommandModel(subCommand);

    @Test
    public void shouldSendCommandToKafkaWithCallback() throws Exception {
        CompletableFuture<RecordMetadata> result = lhProducer.send("test", commandToProduce, "test-topic");
        RecordMetadata recordMetadata = result.get(1, TimeUnit.SECONDS);
        assertThat(recordMetadata.topic()).isEqualTo("test-topic");
        assertThat(recordMetadata.partition()).isEqualTo(0);
        assertThat(recordMetadata.offset()).isEqualTo(0);
    }
}
