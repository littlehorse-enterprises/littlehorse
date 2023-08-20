package io.littlehorse.sdk.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskNodeReference;
import io.littlehorse.sdk.common.proto.TaskRunSource;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import org.junit.jupiter.api.Test;

public class WorkerContextTest {

    @Test
    void checkTaskRunSourceWfRunId() {
        TaskRunSource source = TaskRunSource.newBuilder()
                .setTaskNode(TaskNodeReference.newBuilder()
                        .setNodeRunId(NodeRunId.newBuilder().setWfRunId("hi")))
                .build();

        WorkerContext context =
                new WorkerContext(ScheduledTask.newBuilder().setSource(source).build(), null);

        assertEquals(context.getWfRunId(), "hi");
    }

    @Test
    void checkUserTaskSourceWfRunId() {
        TaskRunSource source = TaskRunSource.newBuilder()
                .setUserTaskTrigger(UserTaskTriggerReference.newBuilder()
                        .setNodeRunId(NodeRunId.newBuilder().setWfRunId("hi")))
                .build();

        WorkerContext context =
                new WorkerContext(ScheduledTask.newBuilder().setSource(source).build(), null);

        assertEquals(context.getWfRunId(), "hi");
    }
}
