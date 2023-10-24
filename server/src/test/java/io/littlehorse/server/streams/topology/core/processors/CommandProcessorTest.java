package io.littlehorse.server.streams.topology.core.processors;

import static org.mockito.Mockito.*;

import com.google.protobuf.Message;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.MetadataCache;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandProcessorTest {
    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();

    @InjectMocks
    private final CommandProcessor commandProcessor = new CommandProcessor(config, server, metadataCache);

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.CORE_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final ModelStore defaultStore = ModelStore.defaultStore(nativeInMemoryStore);

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
    }

    @Test
    void supportTaskQueueRehydrationOnInitialization() {
        UserTaskRunModel userTaskRunModel =
                TestUtil.userTaskRun(UUID.randomUUID().toString());
        CoreProcessorDAO mockDao = mock(CoreProcessorDAO.class);
        when(mockDao.get(any())).thenReturn(TestUtil.nodeRun());
        userTaskRunModel.setDao(mockDao);
        final ScheduledTaskModel scheduledTask =
                new ScheduledTaskModel(TestUtil.taskDef("my-task").getObjectId(), List.of(), userTaskRunModel);
        defaultStore.put(scheduledTask);
        commandProcessor.init(mockProcessorContext);
        verify(server, times(1)).onTaskScheduled(eq(scheduledTask.getTaskDefId()), any());
    }

    @Test
    void shouldProcessGenericCommandWithResponse() {
        final String commandId = UUID.randomUUID().toString();
        CommandModel genericCommand = mock(CommandModel.class);
        Message genericResponse = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
        when(genericCommand.process(any(), any())).thenReturn(genericResponse);
        when(genericCommand.hasResponse()).thenReturn(true);
        when(genericCommand.getCommandId()).thenReturn(commandId);
        Record<String, CommandModel> recordToBeProcessed = new Record<>(commandId, genericCommand, 0L);
        commandProcessor.init(mockProcessorContext);
        commandProcessor.process(recordToBeProcessed);
        verify(server).onResponseReceived(anyString(), any());
    }
}
