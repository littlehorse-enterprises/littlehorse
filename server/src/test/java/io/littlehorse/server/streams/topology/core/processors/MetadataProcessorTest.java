package io.littlehorse.server.streams.topology.core.processors;

import static org.mockito.Mockito.*;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetadataProcessorTest {

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();

    private MetadataProcessor metadataProcessor;

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();

    private final Headers metadata =
            HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
    }

    @Test
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
    }
}
