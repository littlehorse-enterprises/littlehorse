package io.littlehorse.common.model.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.WfRun;
import java.util.UUID;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

public class TaskRunTest {

    private Faker faker = new Faker();

    @Test
    void setTaskWorkerVersionAndIdToTaskRun() throws LHVarSubError {
        // arrange. Complex because all the dependencies needed
        TaskRun taskRun = new TaskRun();
        taskRun.nodeRun = mock(NodeRun.class);
        taskRun.nodeRun.wfRunId = "";
        taskRun.nodeRun.threadRun = mock(ThreadRun.class);
        taskRun.nodeRun.threadRun.wfRun = mock(WfRun.class);
        taskRun.nodeRun.threadRun.wfRun.cmdDao = mock(LHDAO.class);
        VariableValue mockVariableValue = mock(VariableValue.class);
        mockVariableValue.intVal = 0L;
        Node mockNode = mock(Node.class);
        mockNode.taskNode = mock(TaskNode.class);

        when(mockVariableValue.asInt()).thenReturn(mockVariableValue);

        when(taskRun.nodeRun.getNode()).thenReturn(mockNode);

        when(taskRun.nodeRun.threadRun.assignVariable(any()))
            .thenReturn(mockVariableValue);

        when(taskRun.nodeRun.threadRun.wfRun.cmdDao.getWfRunEventQueue())
            .thenReturn("");

        TaskClaimEvent taskClaimEvent = new TaskClaimEvent();
        taskClaimEvent.taskWorkerVersion = faker.app().version();
        taskClaimEvent.taskWorkerId = UUID.randomUUID().toString();

        // act
        taskRun.processStartedEvent(taskClaimEvent);

        // assert
        assertThat(taskRun.taskWorkerVersion)
            .isEqualTo(taskClaimEvent.taskWorkerVersion);
        assertThat(taskRun.taskWorkerId).isEqualTo(taskClaimEvent.taskWorkerId);
    }
}
