package io.littlehorse.server;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.mockito.Mockito;

public class TestCommandExecutionContext extends MetadataProcessorContext {
    public TestCommandExecutionContext(
            Headers recordMetadata,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            MetadataCache metadataCache,
            LHServerConfig lhConfig,
            MetadataCommand currentCommand) {
        super(recordMetadata, processorContext, metadataCache, lhConfig, currentCommand);
    }

    public static TestCommandExecutionContext create(MetadataCommand commandToExecute) {
        final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
        KeyValueStore<String, Bytes> globalMetadataNativeStore =
                TestUtil.testStore(ServerTopology.GLOBAL_METADATA_STORE);
        KeyValueStore<String, Bytes> metadataNativeStore = TestUtil.testStore(ServerTopology.METADATA_STORE);
        globalMetadataNativeStore.init(mockProcessorContext.getStateStoreContext(), globalMetadataNativeStore);
        metadataNativeStore.init(mockProcessorContext.getStateStoreContext(), metadataNativeStore);
        MetadataCache metadataCache = new MetadataCache();
        Headers recordMetadata =
                HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);
        LHServerConfig lhConfig = Mockito.mock();
        return new TestCommandExecutionContext(
                recordMetadata, mockProcessorContext, metadataCache, lhConfig, commandToExecute);
    }
}
