package io.littlehorse.common.model.getable.core.events;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkflowEventModelTest {
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final Command dummyCommand = buildCommand();
    private final Headers metadata = HeadersUtil.metadataHeadersFor("my-tenant", "my-principal");

    private final WorkflowEventDefIdModel workflowEventDefId = new WorkflowEventDefIdModel("user-updated");

    private final TestProcessorExecutionContext testProcessorContext =
            TestProcessorExecutionContext.create(dummyCommand, metadata, mockProcessor);

    @Test
    public void shouldStoreNewWorkflowEvent() {
        WfRunIdModel wfRunId = TestUtil.wfRun(UUID.randomUUID().toString()).getId();
        VariableValueModel content = TestUtil.variableValue();
        WorkflowEventIdModel eventId = new WorkflowEventIdModel(wfRunId, workflowEventDefId, 1);
        WorkflowEventModel eventToStore = new WorkflowEventModel(eventId, content, TestUtil.nodeRun());
        testProcessorContext.getableManager().put(eventToStore);
        testProcessorContext.endExecution();
        WorkflowEventModel storedEvent = testProcessorContext.getableManager().get(eventId);
        Assertions.assertThat(storedEvent).isNotNull();
        Assertions.assertThat(storedEvent.getObjectId().getWfRunId()).isNotNull();
        Assertions.assertThat(storedEvent.getObjectId().getWorkflowEventDefId().getName())
                .isEqualTo("user-updated");
        Assertions.assertThat(storedEvent.getNodeRunId()).isNotNull();
        Assertions.assertThat(storedEvent.getContent()).isEqualTo(content);
        Assertions.assertThat(storedEvent.getCreatedAt()).isNotNull();
    }

    @Test
    public void shouldFormatEventDefId() {
        WfRunIdModel wfRunId = TestUtil.wfRun("my-wf-run").getId();
        WorkflowEventIdModel eventId = new WorkflowEventIdModel(wfRunId, workflowEventDefId, 2);
        Assertions.assertThat(eventId.toString()).isEqualTo("my-wf-run/user-updated/2");
        WorkflowEventIdModel deserializedId = (WorkflowEventIdModel)
                WorkflowEventIdModel.fromString("my-wf-run/user-updated/2", WorkflowEventIdModel.class);
        Assertions.assertThat(deserializedId).isEqualTo(eventId);
    }

    private Command buildCommand() {
        StopWfRunRequestModel dummyCommand = new StopWfRunRequestModel();
        dummyCommand.wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        dummyCommand.threadRunNumber = 0;
        return new CommandModel(dummyCommand).toProto().build();
    }
}
