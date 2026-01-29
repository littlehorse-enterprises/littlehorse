package io.littlehorse.server.streams.topology.core;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LHTransition;
import io.littlehorse.sdk.common.proto.MetricRecordingLevel;
import io.littlehorse.sdk.common.proto.MetricScope;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.StatusTransition;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.TaskTransition;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.UserTaskTransition;
import java.util.List;

public class DefaultMetricsRegistry {

    public static List<MetricSpec> builtIn() {
        return List.of(
                wfMetric("wf-started", LHStatus.STARTING, LHStatus.RUNNING),
                wfMetric("wf-completed", LHStatus.RUNNING, LHStatus.COMPLETED),
                wfMetric("wf-halted", LHStatus.RUNNING, LHStatus.HALTED),
                wfMetric("wf-exception", LHStatus.RUNNING, LHStatus.EXCEPTION),
                wfMetric("wf-error", LHStatus.RUNNING, LHStatus.ERROR),
                taskMetric("task-started", TaskStatus.TASK_SCHEDULED, TaskStatus.TASK_RUNNING),
                taskMetric("task-scheduled-to-running", TaskStatus.TASK_SCHEDULED, TaskStatus.TASK_RUNNING),
                taskMetric("task-running-to-success", TaskStatus.TASK_RUNNING, TaskStatus.TASK_SUCCESS),
                taskMetric("task-scheduled-to-success", TaskStatus.TASK_SCHEDULED, TaskStatus.TASK_SUCCESS),
                taskMetric("task-running-to-failed", TaskStatus.TASK_RUNNING, TaskStatus.TASK_FAILED),
                taskMetric("task-running-to-exception", TaskStatus.TASK_RUNNING, TaskStatus.TASK_EXCEPTION),
                taskMetric("task-timeout", TaskStatus.TASK_RUNNING, TaskStatus.TASK_TIMEOUT),
                userTaskMetric("usertask-assigned", UserTaskRunStatus.UNASSIGNED, UserTaskRunStatus.ASSIGNED),
                userTaskMetric("usertask-done", UserTaskRunStatus.ASSIGNED, UserTaskRunStatus.DONE),
                userTaskMetric("usertask-cancelled", UserTaskRunStatus.ASSIGNED, UserTaskRunStatus.CANCELLED),
                userTaskMetric("usertask-assigned-to-done", UserTaskRunStatus.ASSIGNED, UserTaskRunStatus.DONE),
                nodeMetric("node-started", LHStatus.STARTING, LHStatus.RUNNING),
                nodeMetric("node-completed", LHStatus.RUNNING, LHStatus.COMPLETED),
                nodeMetric("node-error", LHStatus.RUNNING, LHStatus.ERROR),
                nodeMetric("node-exception", LHStatus.RUNNING, LHStatus.EXCEPTION));
    }

    private static MetricSpec wfMetric(String id, LHStatus from, LHStatus to) {
        return MetricSpec.newBuilder()
                .setId(id)
                .setScope(MetricScope.newBuilder().setGlobal(true).build())
                .setTransition(StatusTransition.newBuilder()
                        .setLhTransition(LHTransition.newBuilder()
                                .setFromStatus(from)
                                .setToStatus(to)
                                .build())
                        .build())
                .build();
    }

    private static MetricSpec taskMetric(String id, TaskStatus from, TaskStatus to) {
        return MetricSpec.newBuilder()
                .setId(id)
                .setScope(MetricScope.newBuilder().setGlobal(true).build())
                .setTransition(StatusTransition.newBuilder()
                        .setTaskTransition(TaskTransition.newBuilder()
                                .setFromStatus(from)
                                .setToStatus(to)
                                .build())
                        .build())
                .build();
    }

    private static MetricSpec userTaskMetric(String id, UserTaskRunStatus from, UserTaskRunStatus to) {
        return MetricSpec.newBuilder()
                .setId(id)
                .setScope(MetricScope.newBuilder().setGlobal(true).build())
                .setTransition(StatusTransition.newBuilder()
                        .setUserTaskTransition(UserTaskTransition.newBuilder()
                                .setFromStatus(from)
                                .setToStatus(to)
                                .build())
                        .build())
                .build();
    }

    private static MetricSpec nodeMetric(String id, LHStatus from, LHStatus to) {
        return MetricSpec.newBuilder()
                .setId(id)
                .setScope(MetricScope.newBuilder().setGlobal(true).build())
                .setTransition(StatusTransition.newBuilder()
                        .setNodeTransition(LHTransition.newBuilder()
                                .setFromStatus(from)
                                .setToStatus(to)
                                .build())
                        .build())
                .build();
    }

    public static MetricRecordingLevel getDefaultRecordingLevel() {
        return MetricRecordingLevel.INFO;
    }
}
