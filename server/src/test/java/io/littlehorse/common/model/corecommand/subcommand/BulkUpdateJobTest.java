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
import io.littlehorse.server.LHServer;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.logging.log4j.message.Message;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class BulkUpdateJobTest {

    @Mock
    private final LHServerConfig lhConfig = mock();

    @Mock
    private final LHServer server = mock();

    private final MetadataCache metadataCache = new MetadataCache();
    private final TaskQueueManager queueManager = mock();
    private final AsyncWaiters asyncWaiters = mock();
    private final CommandProcessor processor =
            new CommandProcessor(lhConfig, server, metadataCache, queueManager, asyncWaiters);
    private final Command command = commandProto();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final TestCoreProcessorContext testProcessorContext = TestCoreProcessorContext.create(
            command,
            HeadersUtil.metadataHeadersFor(
                    new TenantIdModel(LHConstants.DEFAULT_TENANT),
                    new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)),
            mockProcessor);
    private final GetableManager getableManager = testProcessorContext.getableManager();

    @Test
    public void shouldExecuteBulkUpdateFromBeginningToEnd() throws Exception {
        when(lhConfig.getMaxBulkJobIterDurationMs()).thenReturn(1);
        WfRunModel wfRun1 = TestUtil.wfRun(UUID.randomUUID().toString());
        WfRunModel wfRun2 = TestUtil.wfRun(UUID.randomUUID().toString());
        WfRunModel wfRun3 = TestUtil.wfRun(UUID.randomUUID().toString());
        getableManager.put(wfRun1);
        getableManager.put(wfRun2);
        getableManager.put(wfRun3);
        getableManager.commit();
        processor.init(mockProcessor);
        CompletableFuture<Message> futureResponse = new CompletableFuture<>();
        when(asyncWaiters.getOrRegisterFuture(eq(command.getCommandId()), any(), any(CompletableFuture.class)))
                .thenReturn(futureResponse);
        processor.process(new Record<>("", command, 0L, testProcessorContext.getRecordMetadata()));
        Assertions.assertThat(futureResponse).isCompleted();
    }

    private Command commandProto() {
        NoOpJob job = NoOpJob.newBuilder().build();
        BulkUpdateJob bulkJob = BulkUpdateJob.newBuilder()
                .setPartition(1)
                .setStartKey(GetableClassEnum.WF_RUN_VALUE + "/")
                .setEndKey(GetableClassEnum.WF_RUN_VALUE + "/~")
                .setNoOp(job)
                .build();
        return Command.newBuilder()
                .setCommandId(UUID.randomUUID().toString())
                .setBulkJob(bulkJob)
                .build();
    }
}
