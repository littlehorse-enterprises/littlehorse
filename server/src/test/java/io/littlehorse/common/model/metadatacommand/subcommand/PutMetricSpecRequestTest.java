package io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.Duration;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.NodeReference;
import io.littlehorse.sdk.common.proto.PutMetricSpecRequest;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.TestMetadataManager;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PutMetricSpecRequestTest {

    private final LHServerConfig config = mock(LHServerConfig.class);
    private final LHServer server = mock(LHServer.class);
    private final ExecutionContext context = mock(ExecutionContext.class);
    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private MetadataProcessor metadataProcessor;
    private TestMetadataManager metadataManager;
    private final MetadataCache metadataCache = new MetadataCache();
    private final AsyncWaiters asyncWaiters = new AsyncWaiters();
    private final String tenantId = "test-tenant-id";
    Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, LHConstants.ANONYMOUS_PRINCIPAL);

    public static Stream<Arguments> getNodeReferenceValues() {
        Builder<Arguments> builder = Stream.builder();
        builder.add(Arguments.of("EntrypointNode", AggregationType.COUNT));
        builder.add(Arguments.of("ExitNode", AggregationType.COUNT));
        builder.add(Arguments.of("TaskNode", AggregationType.COUNT));
        builder.add(Arguments.of("ExternalEventNode", AggregationType.COUNT));
        builder.add(Arguments.of("StartThreadNode", AggregationType.COUNT));
        builder.add(Arguments.of("WaitForThreadsNode", AggregationType.COUNT));
        builder.add(Arguments.of("NopNode", AggregationType.COUNT));
        builder.add(Arguments.of("SleepNode", AggregationType.COUNT));
        builder.add(Arguments.of("UserTaskNode", AggregationType.COUNT));
        builder.add(Arguments.of("StartMultipleThreadsNode", AggregationType.COUNT));
        builder.add(Arguments.of("ThrowEventNode", AggregationType.COUNT));
        builder.add(Arguments.of("WaitForConditionNode", AggregationType.COUNT));
        return builder.build();
    }

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache, asyncWaiters);
        metadataManager = TestMetadataManager.create(nativeMetadataStore, tenantId, context);
        metadataManager.put(new TenantModel(new TenantIdModel(tenantId)));
    }

    @ParameterizedTest
    @MethodSource("getNodeReferenceValues")
    public void shouldCreateMetricSpec_CountTask(String nodeReferenceType, AggregationType aggregationType) {
        PutMetricSpecRequest request = PutMetricSpecRequest.newBuilder()
                .setWindowLength(Duration.newBuilder().setSeconds(2).build())
                .setAggregationType(aggregationType)
                .setNode(NodeReference.newBuilder().setNodeType("TaskNode").build())
                .build();
        MetricSpec processed = (MetricSpec) send(request);
        assertThat(processed).isNotNull();
        MetricSpecIdModel id = LHSerializable.fromProto(processed.getId(), MetricSpecIdModel.class, context);
        MetricSpecModel storedMetricSpec = metadataManager.get(id);
        assertThat(storedMetricSpec.getCreatedAt()).isNotNull();
        //        assertThat(storedMetricSpec.getWindowLengths())
        //                .hasSize(1)
        //                .allSatisfy(d -> assertThat(d.getSeconds()).isEqualTo(2));
        //        assertThat(storedMetricSpec.getAggregateAs()).hasSize(1).allSatisfy(at ->
        // assertThat(at).isEqualTo(aggregationType));

    }

    private Message send(PutMetricSpecRequest request) {
        String commandId = UUID.randomUUID().toString();
        PutMetricSpecRequestModel requestModel =
                PutMetricSpecRequestModel.fromProto(request, PutMetricSpecRequestModel.class, context);
        MetadataCommandModel command = new MetadataCommandModel(requestModel);
        command.setCommandId(commandId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(commandId, command.toProto().build(), 0L, metadata));
        return asyncWaiters
                .getOrRegisterFuture(commandId, Message.class, new CompletableFuture<>())
                .join();
    }
}
