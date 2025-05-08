package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetadataProcessorTest {

    @Mock
    private LHServerConfig config;

    @Mock
    private LHServer server;

    private final MetadataCache metadataCache = new MetadataCache();

    private MetadataProcessor metadataProcessor;

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final Headers metadata =
            HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache, null);
    }

    /*@Test
    public void shouldProcessMetadataCommand() {
        String commandId = UUID.randomUUID().toString();
        MetadataCommand genericMetadataCommand = mock(Answers.RETURNS_DEEP_STUBS);
        Message genericResponse = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
        when(genericMetadataCommand.get).thenReturn(true);
        when(genericMetadataCommand.getCommandId()).thenReturn(commandId);
        when(genericMetadataCommand.process(any())).thenReturn(genericResponse);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(commandId, genericMetadataCommand, 0L, metadata));
        verify(server).onResponseReceived(eq(commandId), any());
    }*/
}
