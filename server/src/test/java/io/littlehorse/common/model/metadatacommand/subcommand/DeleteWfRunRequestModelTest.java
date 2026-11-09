package io.littlehorse.common.model.metadatacommand.subcommand;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.subcommand.InternalDeleteWfRunRequestModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunIterator;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.InlineWfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.junit.jupiter.api.Test;

public class DeleteWfRunRequestModelTest {

    @Test
    void keepsInlineDefinitionUntilResumableCleanupFinishes() {
        CoreProcessorContext context = mock(CoreProcessorContext.class, RETURNS_DEEP_STUBS);
        GetableManager manager = mock(GetableManager.class);
        when(context.getableManager()).thenReturn(manager);
        LHServerConfig config = mock(LHServerConfig.class);
        when(config.getMaxDeletesPerCommand()).thenReturn(1);
        WfRunIdModel owner = new WfRunIdModel("inline-cleanup");
        InlineWfSpecIdModel definitionId = new InlineWfSpecIdModel(owner);
        WfRunModel run = mock(WfRunModel.class);
        when(run.isInline()).thenReturn(true);
        when(run.getThreadRunIterator()).thenReturn(mock(ThreadRunIterator.class));
        when(manager.get(owner)).thenReturn(run);
        when(manager.tryToDeleteAllExternalEventsFor(owner, 1)).thenReturn(false, true);
        InternalDeleteWfRunRequestModel delete = new InternalDeleteWfRunRequestModel();
        delete.setWfRunId(owner);

        delete.process(context, config);
        verify(manager, never()).delete(definitionId);
        verify(manager, never()).delete(owner);

        delete.process(context, config);
        var order = inOrder(manager);
        order.verify(manager).delete(definitionId);
        order.verify(manager).delete(owner);
    }

    private final String wfRunId = UUID.randomUUID().toString();
    private final WfRunModel wfRun = TestUtil.wfRun(wfRunId);
    private final Command command = commandProto();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final TestCoreProcessorContext testProcessorContext = TestCoreProcessorContext.create(
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

    private Command commandProto() {
        DeleteWfRunRequest request = DeleteWfRunRequest.newBuilder()
                .setId(WfRunId.newBuilder().setId(wfRun.getObjectId().getId()))
                .build();
        return Command.newBuilder()
                .setDeleteWfRun(new InternalDeleteWfRunRequestModel(request).toProto())
                .build();
    }
}
