package io.littlehorse.sdk.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskNodeReference;
import io.littlehorse.sdk.common.proto.TaskRunSource;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import io.littlehorse.sdk.common.proto.WfRunId;
import org.junit.jupiter.api.Test;

public class WorkerContextTest {

    @Test
    void checkTaskRunSourceWfRunId() {
        TaskRunSource source = TaskRunSource.newBuilder()
                .setTaskNode(TaskNodeReference.newBuilder()
                        .setNodeRunId(NodeRunId.newBuilder()
                                .setWfRunId(WfRunId.newBuilder().setId("hi"))))
                .build();

        WorkerContext context =
                new WorkerContext(ScheduledTask.newBuilder().setSource(source).build(), null);

        assertEquals(context.getWfRunId().getId(), "hi");
    }

    @Test
    void checkUserTaskSourceWfRunId() {
        TaskRunSource source = TaskRunSource.newBuilder()
                .setUserTaskTrigger(UserTaskTriggerReference.newBuilder()
                        .setNodeRunId(NodeRunId.newBuilder()
                                .setWfRunId(WfRunId.newBuilder().setId("hi"))))
                .build();

        WorkerContext context =
                new WorkerContext(ScheduledTask.newBuilder().setSource(source).build(), null);

        assertEquals(context.getWfRunId().getId(), "hi");
    }

    @Test
    void checkWorkerContextLogOutput() {
        WorkerContext context = new WorkerContext(ScheduledTask.newBuilder().build(), null);
        context.log("test log");
        assertEquals(context.getLogOutput(), "test log");
    }

    @Test
    void checkWorkerContextLogOutputWNullValue() {
        WorkerContext context = new WorkerContext(ScheduledTask.newBuilder().build(), null);
        context.log(null);
        assertEquals(context.getLogOutput(), "null");
    }
}
