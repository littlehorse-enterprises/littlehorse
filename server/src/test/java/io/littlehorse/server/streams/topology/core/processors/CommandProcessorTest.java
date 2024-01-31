package io.littlehorse.server.streams.topology.core.processors;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandProcessorTest {
    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    @Mock
    private TaskQueueManager taskQueueManager;

    @Mock
    private ExecutionContext executionContext;

    private final MetadataCache metadataCache = new MetadataCache();

    @InjectMocks
    private final CommandProcessor commandProcessor =
            new CommandProcessor(config, server, metadataCache, taskQueueManager);

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.CORE_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final KeyValueStore<String, Bytes> globalInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.GLOBAL_METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final TenantScopedStore defaultStore = TenantScopedStore.newInstance(
            nativeInMemoryStore, new TenantIdModel(LHConstants.DEFAULT_TENANT), executionContext);
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private TestProcessorExecutionContext tenantProcessorContext;
    private TestProcessorExecutionContext defaultProcessorContext;

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
    }

    @Test
    void supportTaskQueueRehydrationOnInitialization() {
        RunWfRequest runWfSubCommand =
                RunWfRequest.newBuilder().setWfSpecName("name").build();
        Command commandToExecute =
                Command.newBuilder().setRunWf(runWfSubCommand).build();

        tenantProcessorContext = TestProcessorExecutionContext.create(
                commandToExecute, HeadersUtil.metadataHeadersFor("my-tenant", "tyler"), mockProcessorContext);
        defaultProcessorContext = new TestProcessorExecutionContext(
                commandToExecute,
                HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.DEFAULT_TENANT),
                tenantProcessorContext.getLhConfig(),
                mockProcessorContext,
                tenantProcessorContext.getGlobalTaskQueueManager(),
                tenantProcessorContext.getMetadataCache(),
                tenantProcessorContext.getServer());
        defaultProcessorContext.getableManager();
        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(
                mockProcessorContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE), executionContext);
        NodeRunModel nodeRun = TestUtil.nodeRun();
        UserTaskRunModel userTaskRunModel =
                TestUtil.userTaskRun(UUID.randomUUID().toString(), nodeRun, tenantProcessorContext);
        tenantProcessorContext.getableManager().put(nodeRun);
        final ScheduledTaskModel scheduledTask = new ScheduledTaskModel(
                TestUtil.taskDef("my-task").getObjectId(), List.of(), userTaskRunModel, tenantProcessorContext);
        tenantProcessorContext.getTaskManager().scheduleTask(scheduledTask);
        defaultProcessorContext.getTaskManager().scheduleTask(scheduledTask);
        tenantProcessorContext.endExecution();
        defaultProcessorContext.endExecution();
        defaultStore.put(scheduledTask);
        clusterStore.put(new StoredGetable<>(new TenantModel("my-tenant")));
        commandProcessor.init(mockProcessorContext);
        verify(server, times(2))
                .onTaskScheduled(
                        eq(scheduledTask.getTaskDefId()), any(), eq(new TenantIdModel(LHConstants.DEFAULT_TENANT)));
    }

    /*@Test
    void shouldProcessGenericCommandWithResponse() {
        final String commandId = UUID.randomUUID().toString();
        CommandModel genericCommand = mock(CommandModel.class);
        Message genericResponse = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
        Headers metadata = HeadersUtil.metadataHeadersFor("my-tenant-id", "my-principal-id");
        when(genericCommand.process(any(), any())).thenReturn(genericResponse);
        when(genericCommand.hasResponse()).thenReturn(true);
        when(genericCommand.getCommandId()).thenReturn(commandId);
        Record<String, CommandModel> recordToBeProcessed = new Record<>(commandId, genericCommand, 0L, metadata);
        commandProcessor.init(mockProcessorContext);
        commandProcessor.process(recordToBeProcessed);
        verify(server).onResponseReceived(anyString(), any());
    }*/
}
