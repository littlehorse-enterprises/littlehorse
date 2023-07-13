package io.littlehorse.common.model.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class TaskRunTest {

    @Test
    void setTaskWorkerVersionAndIdToTaskRun() throws LHVarSubError {
        // arrange. Complex because all the dependencies needed
        TaskRun taskRun = new TaskRun();
        taskRun.setId(new TaskRunId("asdf"));
        taskRun.setInputVariables(new ArrayList<>());
        taskRun.setDao(mock(LHDAO.class));

        taskRun.scheduleAttempt();

        TaskClaimEvent taskClaimEvent = new TaskClaimEvent();

        // act
        taskRun.processStart(taskClaimEvent);

        // assert
        assertThat(taskRun.getLatestAttempt().getTaskWorkerVersion())
            .isEqualTo(taskClaimEvent.getTaskWorkerVersion());
        assertThat(taskRun.getLatestAttempt().getTaskWorkerId())
            .isEqualTo(taskClaimEvent.getTaskWorkerId());
    }
}
