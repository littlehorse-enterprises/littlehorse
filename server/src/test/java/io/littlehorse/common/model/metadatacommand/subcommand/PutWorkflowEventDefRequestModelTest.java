package io.littlehorse.common.model.metadatacommand.subcommand;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.TestMetadataManager;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PutWorkflowEventDefRequestModelTest {

    private final PutWorkflowEventDefRequestModel putWorkflowEventDef = createSubCommand();

    private MetadataProcessor metadataProcessor;

    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private ExecutionContext executionContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);

    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();
    private final String tenantId = LHConstants.DEFAULT_TENANT;
    private TenantScopedStore tenantStore;
    private TestMetadataManager metadataManager;

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
        TenantScopedStore.newInstance(nativeMetadataStore, new TenantIdModel(tenantId), executionContext);
        metadataManager = TestMetadataManager.create(nativeMetadataStore, tenantId, executionContext);
    }

    @Test
    public void shouldStoreValidWorkflowEventDef() {
        sendCommand(putWorkflowEventDef);
        WorkflowEventDefModel storedEventDef = metadataManager.get(new WorkflowEventDefIdModel("user-created"));
        Assertions.assertThat(storedEventDef).isNotNull();
    }

    private PutWorkflowEventDefRequestModel createSubCommand() {
        WorkflowEventDefIdModel id = new WorkflowEventDefIdModel("user-created");
        return new PutWorkflowEventDefRequestModel(id, VariableType.STR);
    }

    private MetadataCommandModel sendCommand(MetadataSubCommand<?> putPrincipalRequest) {
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, LHConstants.ANONYMOUS_PRINCIPAL);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(
                new Record<>(LHConstants.ANONYMOUS_PRINCIPAL, command.toProto().build(), 0L, metadata));
        return command;
    }
}
