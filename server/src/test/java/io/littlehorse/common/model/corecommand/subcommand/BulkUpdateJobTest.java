package io.littlehorse.common.model.corecommand.subcommand;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.BulkUpdateJob;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.NoOpJob;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.UUID;
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
            command,
            HeadersUtil.metadataHeadersFor(
                    new TenantIdModel(LHConstants.DEFAULT_TENANT),
                    new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)),
            mockProcessor);
    private final GetableManager getableManager = testProcessorContext.getableManager();

    @Test
    public void shouldExecuteBulkUpdateFromBeginningToEnd() {
        when(lhConfig.getMaxBulkJobIterDurationMs()).thenReturn(1);
        WfRunModel wfRun1 = TestUtil.wfRun(UUID.randomUUID().toString());
        WfRunModel wfRun2 = TestUtil.wfRun(UUID.randomUUID().toString());
        WfRunModel wfRun3 = TestUtil.wfRun(UUID.randomUUID().toString());
        getableManager.put(wfRun1);
        getableManager.put(wfRun2);
        getableManager.put(wfRun3);
        getableManager.commit();
        processor.init(mockProcessor);
        processor.process(new Record<>("", command, 0L, testProcessorContext.getRecordMetadata()));
        verify(server, never()).sendErrorToClient(anyString(), any());
    }

    private Command commandProto() {
        NoOpJob job = NoOpJob.newBuilder().build();
        BulkUpdateJob bulkJob = BulkUpdateJob.newBuilder()
                .setPartition(1)
                .setStartKey(GetableClassEnum.WF_RUN_VALUE + "/")
                .setEndKey(GetableClassEnum.WF_RUN_VALUE + "/~")
                .setNoOp(job)
                .build();
        return Command.newBuilder().setBulkJob(bulkJob).build();
    }
}
