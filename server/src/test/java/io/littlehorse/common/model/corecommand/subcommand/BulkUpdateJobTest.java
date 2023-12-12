package io.littlehorse.common.model.corecommand.subcommand;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.proto.BulkUpdateJob;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.NoOpJob;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class BulkUpdateJobTest {

    @Mock
    private final LHServerConfig lhConfig = mock();

    @Mock
    private final KafkaStreamsServerImpl server = mock();

    private final MetadataCache metadataCache = new MetadataCache();
    private final TaskQueueManager queueManager = mock();
    private final CommandProcessor processor = new CommandProcessor(lhConfig, server, metadataCache, queueManager);
    private final Command command = commandProto();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final TestProcessorExecutionContext testProcessorContext = TestProcessorExecutionContext.create(
            command, HeadersUtil.metadataHeadersFor("my-tenant", "bob"), mockProcessor);
    private final MetadataManager metadataManager = testProcessorContext.globalMetadataManager();

    @Test
    public void shouldExecuteBulkUpdateFromBeginningToEnd() {
        WfSpecModel wfSpec1 = TestUtil.wfSpec("my-wf-1");
        WfSpecModel wfSpec2 = TestUtil.wfSpec("my-wf-2");
        WfSpecModel wfSpec3 = TestUtil.wfSpec("my-wf-3");
        metadataManager.put(wfSpec1);
        metadataManager.put(wfSpec2);
        metadataManager.put(wfSpec3);
        processor.init(mockProcessor);
        processor.process(new Record<>("", command, 0L, testProcessorContext.getRecordMetadata()));
    }

    private Command commandProto() {
        NoOpJob job = NoOpJob.newBuilder().build();
        BulkUpdateJob bulkJob = BulkUpdateJob.newBuilder()
                .setPartition(1)
                .setStartKey("0/2/")
                .setEndKey("0/2/~")
                .setNoOp(job)
                .build();
        return Command.newBuilder().setBulkJob(bulkJob).build();
    }
}
