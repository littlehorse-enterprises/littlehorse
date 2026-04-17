package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionMetricsMemoryStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandProcessorTest {
    @Mock
    private LHServerConfig config;

    private LHServer server = mock();

    @Mock
    private TaskQueueManager taskQueueManager;

    private ExecutionContext executionContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);

    private final MetadataCache metadataCache = new MetadataCache();

    private CommandProcessor commandProcessor;

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

    private TestCoreProcessorContext tenantProcessorContext;
    private TestCoreProcessorContext defaultProcessorContext;

    @BeforeEach
    public void setup() {
        commandProcessor = new CommandProcessor(config, server, metadataCache, taskQueueManager, mock());
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        globalInMemoryStore.init(mockProcessorContext.getStateStoreContext(), globalInMemoryStore);
    }

    @Test
    void supportTaskQueueRehydrationOnInitialization() {
        RunWfRequest runWfSubCommand =
                RunWfRequest.newBuilder().setWfSpecName("name").build();
        Command commandToExecute =
                Command.newBuilder().setRunWf(runWfSubCommand).build();

        tenantProcessorContext = TestCoreProcessorContext.create(
                commandToExecute, HeadersUtil.metadataHeadersFor("my-tenant", "tyler"), mockProcessorContext);
        defaultProcessorContext = new TestCoreProcessorContext(
                commandToExecute,
                HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.DEFAULT_TENANT),
                tenantProcessorContext.getLhConfig(),
                mockProcessorContext,
                tenantProcessorContext.getGlobalTaskQueueManager(),
                tenantProcessorContext.getMetadataCache(),
                tenantProcessorContext.getServer(),
                tenantProcessorContext.getPartitionMetricsMemoryStore());
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
        verify(server, times(2)).onTaskScheduled(any(), eq(scheduledTask.getTaskDefId()), any(), any());
    }

    @Test
    void shouldForwardMetricsTimerWithTenantInTimerAndHeaders() throws Exception {
        when(config.getCoreCmdTopicName()).thenReturn("core-cmd");
        commandProcessor.init(mockProcessorContext);
        setField(commandProcessor, "shouldUseMetricsHint", false);

        TenantIdModel tenantId = new TenantIdModel("metrics-tenant");
        PartitionMetricWindowModel metricWindow = new PartitionMetricWindowModel(
                new MetricWindowIdModel(tenantId, new WfSpecIdModel("my-wf", 1, 0), new Date(0)));
        metricWindow.incrementCount("started");
        getPartitionMetricsMemoryStore().put(metricWindow);

        mockProcessorContext.scheduledPunctuators().get(0).getPunctuator().punctuate(System.currentTimeMillis());

        assertThat(mockProcessorContext.forwarded()).hasSize(2);
        Record<? extends String, ? extends CommandProcessorOutput> forwardedRecord =
                mockProcessorContext.forwarded().get(1).record();
        assertThat(HeadersUtil.tenantIdFromMetadata(forwardedRecord.headers()).getId())
                .isEqualTo(tenantId.getId());

        CommandProcessorOutput output = forwardedRecord.value();
        assertThat(output.partitionKey)
                .isEqualTo(metricWindow.getId().getPartitionKey().orElseThrow());
        assertThat(output.topic).isEqualTo("core-cmd");
        assertThat(output.payload).isInstanceOf(LHTimer.class);

        LHTimer timer = (LHTimer) output.payload;
        assertThat(timer.getTenantId().getId()).isEqualTo(tenantId.getId());
        assertThat(timer.isRepartition()).isTrue();
        assertThat(timer.topic).isEqualTo("core-cmd");
    }

    private PartitionMetricsMemoryStore getPartitionMetricsMemoryStore() throws Exception {
        return (PartitionMetricsMemoryStore) getField(commandProcessor, "partitionMetricsMemoryStore");
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
