package io.littlehorse.common.model.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class TaskRunModelTest {

    private final String tenantId = "myTenantId";

    @Test
    void setTaskWorkerVersionAndIdToTaskRun() throws LHVarSubError {
        // arrange. Complex because all the dependencies needed
        TaskRunModel taskRun = new TaskRunModel();
        ExecutionContext executionContext = mock(ExecutionContext.class);
        AuthorizationContext mockContext = mock(AuthorizationContext.class);
        when(mockContext.tenantId()).thenReturn(tenantId);
        when(executionContext.authorization()).thenReturn(mockContext);
        taskRun.setId(new TaskRunIdModel("asdf"));
        taskRun.setInputVariables(new ArrayList<>());
        // taskRun.setDao(mockDao);

        taskRun.scheduleAttempt();

        TaskClaimEvent taskClaimEvent = new TaskClaimEvent();
        // act
        taskRun.processStart(taskClaimEvent);

        // assert
        assertThat(taskRun.getLatestAttempt().getTaskWorkerVersion()).isEqualTo(taskClaimEvent.getTaskWorkerVersion());
        assertThat(taskRun.getLatestAttempt().getTaskWorkerId()).isEqualTo(taskClaimEvent.getTaskWorkerId());
    }
}
