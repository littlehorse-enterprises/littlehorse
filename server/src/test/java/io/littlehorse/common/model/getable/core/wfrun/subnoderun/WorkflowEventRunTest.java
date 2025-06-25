package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkflowEventRunTest {

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final Headers metadata =
            HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL);
    private final Command dummyCommand = buildCommand();
    private final TestCoreProcessorContext testProcessorContext =
            TestCoreProcessorContext.create(dummyCommand, metadata, mockProcessor);
    private final NodeRunModel nodeRun = Mockito.spy(new NodeRunModel(testProcessorContext));
    private final WfRunIdModel wfRunId =
            TestUtil.wfRun(UUID.randomUUID().toString()).getId();

    @BeforeEach
    public void setup() throws Exception {
        ThreadRunModel mockThreadRun = Mockito.mock(Answers.RETURNS_DEEP_STUBS);
        NodeModel nodeSpec = Mockito.mock(Answers.RETURNS_DEEP_STUBS);
        Mockito.doReturn(nodeSpec).when(nodeRun).getNode();
        Mockito.doReturn(mockThreadRun).when(nodeRun).getThreadRun();
        Mockito.when(mockThreadRun.assignVariable(Mockito.any())).thenReturn(TestUtil.variableValue());
        nodeRun.setStatus(LHStatus.RUNNING);
        nodeRun.setWfSpecId(TestUtil.wfSpecId());
        nodeRun.setThreadSpecName("my-thread");
        nodeRun.setNodeName("my-node");
        nodeRun.setId(new NodeRunIdModel(wfRunId, 1, 2));
        nodeRun.setArrivalTime(new Date());
    }

    @Test
    public void shouldProcessSubNode() throws Exception {
        WorkflowEventDefIdModel eventDef = new WorkflowEventDefIdModel("user-created");
        ThrowEventNodeRunModel eventRun = new ThrowEventNodeRunModel(eventDef, testProcessorContext);
        nodeRun.setSubNodeRun(eventRun);

        nodeRun.arrive(new Date(), testProcessorContext);
        nodeRun.setStatus(LHStatus.COMPLETED);
        Assertions.assertThat(nodeRun.getSubNodeRun()).isNotNull();
        Assertions.assertThat(nodeRun.getOutput(testProcessorContext)).isEmpty();
        Assertions.assertThat(nodeRun.checkIfProcessingCompleted(testProcessorContext))
                .isTrue();
    }

    @Test
    public void shouldStoreNodeRun() throws Exception {
        WorkflowEventDefIdModel eventDef = new WorkflowEventDefIdModel("user-created");
        ThrowEventNodeRunModel eventRun = new ThrowEventNodeRunModel(eventDef, testProcessorContext);
        nodeRun.setSubNodeRun(eventRun);
        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);
        testProcessorContext.endExecution();
        NodeRunModel storeNodeRun = testProcessorContext.getableManager().get(new NodeRunIdModel(wfRunId, 1, 2));
        Assertions.assertThat(storeNodeRun).isNotNull();
    }

    @Test
    public void shouldStoreWorkflowEvent() throws Exception {
        WorkflowEventDefIdModel eventDef = new WorkflowEventDefIdModel("user-created");
        ThrowEventNodeRunModel eventRun = new ThrowEventNodeRunModel(eventDef, testProcessorContext);
        nodeRun.setSubNodeRun(eventRun);

        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);
        testProcessorContext.endExecution();
        WorkflowEventModel storedEvent =
                testProcessorContext.getableManager().get(new WorkflowEventIdModel(wfRunId, eventDef, 0));
        Assertions.assertThat(storedEvent).isNotNull();
    }

    @Test
    public void shouldIncrementWorkflowEventSequential() throws Exception {
        WorkflowEventDefIdModel eventDef = new WorkflowEventDefIdModel("user-created");
        ThrowEventNodeRunModel eventRun = new ThrowEventNodeRunModel(eventDef, testProcessorContext);
        nodeRun.setSubNodeRun(eventRun);

        // Throw event the first time
        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);
        testProcessorContext.endExecution();
        // Throw event a second time
        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);
        testProcessorContext.endExecution();
        // Throw event a third time
        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);
        testProcessorContext.endExecution();

        WorkflowEventModel storedEvent =
                testProcessorContext.getableManager().get(new WorkflowEventIdModel(wfRunId, eventDef, 2));
        Assertions.assertThat(storedEvent).isNotNull();
    }

    @Test
    public void shouldIncrementWorkflowEventSequentialUsingCache() throws Exception {
        WorkflowEventDefIdModel eventDef = new WorkflowEventDefIdModel("user-created");
        ThrowEventNodeRunModel eventRun = new ThrowEventNodeRunModel(eventDef, testProcessorContext);
        nodeRun.setSubNodeRun(eventRun);

        // Throw event the first time
        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);
        // Throw event a second time
        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);
        // Throw event a third time
        nodeRun.arrive(new Date(), testProcessorContext);
        testProcessorContext.getableManager().put(nodeRun);

        WorkflowEventModel storedEvent =
                testProcessorContext.getableManager().get(new WorkflowEventIdModel(wfRunId, eventDef, 2));
        Assertions.assertThat(storedEvent).isNotNull();
    }

    private Command buildCommand() {
        StopWfRunRequestModel dummyCommand = new StopWfRunRequestModel();
        dummyCommand.wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        dummyCommand.threadRunNumber = 0;
        return new CommandModel(dummyCommand).toProto().build();
    }
}
