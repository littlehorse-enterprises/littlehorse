package io.littlehorse.common.model.corecommand.subcommand;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.proto.BulkUpdateJob;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.streams.processor.api.Record;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;


public class BulkUpdateJobTest {

    private TestProcessorExecutionContext testProcessorContext;
    private final MetadataManager metadataManager = testProcessorContext.metadataManager();
    @Mock
    private final LHServerConfig lhConfig = mock();
    @Mock
    private final KafkaStreamsServerImpl server = mock();

    private final MetadataCache metadataCache = new MetadataCache();
    private final TaskQueueManager queueManager = mock();
    private CommandProcessor processor = new CommandProcessor(lhConfig, server, metadataCache, queueManager);

    private BulkUpdateJobModel bulkCommand = BulkUpdateJobModel.fromProto(commandProto(), BulkUpdateJobModel.class, testProcessorContext);
    private CommandModel command = new CommandModel(bulkCommand);

    @Test
    public void shouldExecuteBulkUpdateFromBeginningToEnd() {
        WfSpecModel wfSpec1 = TestUtil.wfSpec("my-wf-1");
        WfSpecModel wfSpec2 = TestUtil.wfSpec("my-wf-2");
        WfSpecModel wfSpec3 = TestUtil.wfSpec("my-wf-3");
        metadataManager.put(wfSpec1);
        metadataManager.put(wfSpec2);
        metadataManager.put(wfSpec3);
        processor.process(new Record<>("", command.toProto().build(), 0L));
    }

    private BulkUpdateJob commandProto() {
        return BulkUpdateJob.newBuilder()
                .setPartition(1)
                .setStartKey("wf/")
                .setEndKey("wf/~")
                .build();
    }

}
