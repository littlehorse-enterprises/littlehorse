package io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class DeleteWfRunRequestModelTest {

    @Mock
    private final LHServerConfig lhConfig = mock();

    @Mock
    private final LHServer server = mock();

    private final MetadataCache metadataCache = new MetadataCache();
    private final TaskQueueManager queueManager = mock();
    private final CommandProcessor processor = new CommandProcessor(lhConfig, server, metadataCache, queueManager);
    private final String wfRunId = UUID.randomUUID().toString();
    private final WfRunModel wfRun = TestUtil.wfRun(wfRunId);
    private final Command command = commandProto();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final TestProcessorExecutionContext testProcessorContext = TestProcessorExecutionContext.create(
            command,
            HeadersUtil.metadataHeadersFor(
                    new TenantIdModel(LHConstants.DEFAULT_TENANT),
                    new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)),
            mockProcessor);
    private final GetableManager getableManager = testProcessorContext.getableManager();

    // VariableModel's "index" implementation relies on a litany of other objects
    // that we do not need to create or store to test this specific feature.
    // So we mock the VariableModel and hide the "index" functionality with our when() statements.
    public VariableModel mockVariableModel() {
        VariableModel variableModel = spy();
        variableModel.setId(new VariableIdModel(wfRun.getId(), 0, "test-name"));
        variableModel.setValue(TestUtil.variableValue());
        variableModel.setMasked(false);
        WfSpecModel wfSpec = TestUtil.wfSpec("testWfSpecName");
        variableModel.setWfSpec(wfSpec);
        variableModel.setWfSpecId(wfSpec.getId());

        when(variableModel.getIndexConfigurations()).thenReturn(List.of());
        when(variableModel.getIndexEntries()).thenReturn(List.of());

        return variableModel;
    }

    // @Test
    // public void shouldDeleteWfRunObjects() {
    //     TaskRunModel taskRunModel = TestUtil.taskRun(
    //             new TaskRunIdModel(wfRun.getId(), UUID.randomUUID().toString()),
    //             new TaskDefIdModel(UUID.randomUUID().toString()));
    //     VariableModel variableModel = mockVariableModel();
    //     ExternalEventModel externalEventModel = TestUtil.externalEvent(wfRunId);
    //     UserTaskRunModel userTaskRunModel = TestUtil.userTaskRun(wfRunId, testProcessorContext);
    //     WorkflowEventModel workflowEventModel = TestUtil.workflowEvent(wfRunId);
    //     NodeRunModel nodeRunModel = TestUtil.nodeRun(wfRunId);

    //     getableManager.put(wfRun);
    //     getableManager.put(taskRunModel);
    //     getableManager.put(variableModel);
    //     getableManager.put(externalEventModel);
    //     getableManager.put(userTaskRunModel);
    //     getableManager.put(workflowEventModel);
    //     getableManager.put(nodeRunModel);

    //     getableManager.commit();
    //     processor.init(mockProcessor);
    //     processor.process(new Record<>("", command, 0L, testProcessorContext.getRecordMetadata()));

    //     WfRunModel storedWfRunModel = getableManager.get(wfRun.getId());
    //     TaskRunModel storedTaskRunModel = getableManager.get(taskRunModel.getId());
    //     VariableModel storedVariableModel = getableManager.get(variableModel.getId());
    //     ExternalEventModel storedExternalEventModel = getableManager.get(externalEventModel.getId());
    //     UserTaskRunModel storedUserTaskRunModel = getableManager.get(userTaskRunModel.getId());
    //     WorkflowEventModel storedWorkflowEventModel = getableManager.get(workflowEventModel.getId());
    //     NodeRunModel storedNodeRunModel = getableManager.get(nodeRunModel.getId());

    //     assertThat(storedWfRunModel).isNull();
    //     assertThat(storedTaskRunModel).isNull();
    //     assertThat(storedVariableModel).isNull();
    //     assertThat(storedExternalEventModel).isNull();
    //     assertThat(storedUserTaskRunModel).isNull();
    //     assertThat(storedWorkflowEventModel).isNull();
    //     assertThat(storedNodeRunModel).isNull();
    //     verify(server, never()).sendErrorToClient(anyString(), any());
    // }

    private Command commandProto() {
        DeleteWfRunRequest request = DeleteWfRunRequest.newBuilder()
                .setId(WfRunId.newBuilder().setId(wfRun.getObjectId().getId()))
                .build();
        return Command.newBuilder().setDeleteWfRun(request).build();
    }
}
