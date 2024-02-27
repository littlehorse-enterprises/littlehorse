package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkflowEventRunTest {

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final Headers metadata =
            HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);
    private final Command dummyCommand = buildCommand();
    private final TestProcessorExecutionContext testProcessorContext =
            TestProcessorExecutionContext.create(dummyCommand, metadata, mockProcessor);

    @Test
    public void shouldStoreNode() throws Exception {
        NodeRunModel node = new NodeRunModel(testProcessorContext);
        node.setStatus(LHStatus.RUNNING);
        node.setWfSpecId(TestUtil.wfSpecId());
        node.setThreadSpecName("my-thread");
        node.setNodeName("my-node");
        WfRunIdModel wfRunId = TestUtil.wfRun(UUID.randomUUID().toString()).getId();
        node.setId(new NodeRunIdModel(wfRunId, 1, 2));
        node.setArrivalTime(new Date());
        WorkflowEventDefIdModel eventDef = new WorkflowEventDefIdModel("user-created");
        WorkflowEventRunModel eventRun = new WorkflowEventRunModel(eventDef, testProcessorContext);
        node.setSubNodeRun(eventRun);
        node.arrive(new Date());
        node.setStatus(LHStatus.COMPLETED);
        Assertions.assertThat(node.getSubNodeRun()).isNotNull();
        Assertions.assertThat(node.getOutput()).isEmpty();
        Assertions.assertThat(node.checkIfProcessingCompleted()).isTrue();
        testProcessorContext.getableManager().put(node);
        testProcessorContext.endExecution();
        NodeRunModel storeNodeRun = testProcessorContext.getableManager().get(new NodeRunIdModel(wfRunId, 1, 2));
        Assertions.assertThat(storeNodeRun).isNotNull();
        testProcessorContext.endExecution();
        WorkflowEventModel storedEvent =
                testProcessorContext.getableManager().get(new WorkflowEventIdModel(wfRunId, eventDef, 0));
        Assertions.assertThat(storedEvent).isNotNull();
    }

    private Command buildCommand() {
        StopWfRunRequestModel dummyCommand = new StopWfRunRequestModel();
        dummyCommand.wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        dummyCommand.threadRunNumber = 0;
        return new CommandModel(dummyCommand).toProto().build();
    }
}
