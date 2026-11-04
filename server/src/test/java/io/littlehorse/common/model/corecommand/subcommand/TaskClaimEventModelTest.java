package io.littlehorse.common.model.corecommand.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import org.junit.jupiter.api.Test;

class TaskClaimEventModelTest {

    @Test
    void shouldReturnEmptyPollResponseForDeletedTaskRun() {
        CoreProcessorContext context = mock(CoreProcessorContext.class);
        when(context.getableManager()).thenReturn(mock(GetableManager.class));
        TaskClaimEventModel claim = new TaskClaimEventModel();
        claim.setTaskRunId(new TaskRunIdModel(new WfRunIdModel("deleted-run"), "0-1"));

        assertThat(claim.process(context, mock(LHServerConfig.class))).isEqualTo(PollTaskResponse.getDefaultInstance());
    }
}
