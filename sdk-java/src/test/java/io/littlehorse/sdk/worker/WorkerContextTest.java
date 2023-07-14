package io.littlehorse.sdk.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.proto.NodeRunIdPb;
import io.littlehorse.sdk.common.proto.ScheduledTaskPb;
import io.littlehorse.sdk.common.proto.TaskNodeReferencePb;
import io.littlehorse.sdk.common.proto.TaskRunSourcePb;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReferencePb;
import org.junit.jupiter.api.Test;

public class WorkerContextTest {

    @Test
    void checkTaskRunSourceWfRunId() {
        TaskRunSourcePb source = TaskRunSourcePb
            .newBuilder()
            .setTaskNode(
                TaskNodeReferencePb
                    .newBuilder()
                    .setNodeRunId(NodeRunIdPb.newBuilder().setWfRunId("hi"))
            )
            .build();

        WorkerContext context = new WorkerContext(
            ScheduledTaskPb.newBuilder().setSource(source).build(),
            null
        );

        assertEquals(context.getWfRunId(), "hi");
    }

    @Test
    void checkUserTaskSourceWfRunId() {
        TaskRunSourcePb source = TaskRunSourcePb
            .newBuilder()
            .setUserTaskTrigger(
                UserTaskTriggerReferencePb
                    .newBuilder()
                    .setNodeRunId(NodeRunIdPb.newBuilder().setWfRunId("hi"))
            )
            .build();

        WorkerContext context = new WorkerContext(
            ScheduledTaskPb.newBuilder().setSource(source).build(),
            null
        );

        assertEquals(context.getWfRunId(), "hi");
    }
}
