package io.littlehorse.common.model.metadatacommand.subcommand;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.TestMetadataManager;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
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

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    @Mock
    private LHServerConfig config;

    @Mock
    private LHServer server;

    private final MetadataCache metadataCache = new MetadataCache();
    private final String tenantId = LHConstants.DEFAULT_TENANT;
    private TestMetadataManager metadataManager;

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
        metadataManager = TestMetadataManager.create(nativeMetadataStore, tenantId, executionContext);
    }

    @Test
    public void shouldStoreValidWorkflowEventDef() {
        sendCommand(putWorkflowEventDef);
        WorkflowEventDefModel storedEventDef = metadataManager.get(new WorkflowEventDefIdModel("user-created"));
        Assertions.assertThat(storedEventDef).isNotNull();
    }

    @Test
    public void shouldValidateDuplicatedWorkflowEvents() {
        sendCommand(putWorkflowEventDef);
        WorkflowEventDefModel storedEventDef = metadataManager.get(new WorkflowEventDefIdModel("user-created"));
        Assertions.assertThat(storedEventDef).isNotNull();
        sendCommand(new PutWorkflowEventDefRequestModel("user-created", new ReturnTypeModel(VariableType.INT)));
        verify(server, times(1)).sendErrorToClient(anyString(), any());
        PutWorkflowEventDefRequestModel userUpdatedCommand =
                new PutWorkflowEventDefRequestModel("user-updated", new ReturnTypeModel(VariableType.STR));
        reset(server);
        sendCommand(userUpdatedCommand);
        verify(server, never()).sendErrorToClient(anyString(), any());
        WorkflowEventDefModel userUpdatedEventDef = metadataManager.get(new WorkflowEventDefIdModel("user-updated"));
        Assertions.assertThat(userUpdatedEventDef).isNotNull();
    }

    private PutWorkflowEventDefRequestModel createSubCommand() {
        return new PutWorkflowEventDefRequestModel("user-created", new ReturnTypeModel(VariableType.STR));
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
