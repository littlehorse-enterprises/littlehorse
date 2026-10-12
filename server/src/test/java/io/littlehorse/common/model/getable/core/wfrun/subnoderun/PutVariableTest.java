package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PutVariableRequest;
import io.littlehorse.sdk.common.proto.RescueThreadRunRequest;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.AbstractWorkflowExecutionTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PutVariableTest extends AbstractWorkflowExecutionTest {

    @Test
    void shouldReplaceWorkflowVariableValue() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.declareStr("message").withDefault("original");
                    thread.sleepSeconds(3600);
                },
                Map.of());
        WfRunIdModel wfRunId = new WfRunIdModel(run.getId().getId());
        VariableIdModel variableId = new VariableIdModel(wfRunId, 0, "message");
        PutVariableRequest.Builder putVariable = PutVariableRequest.newBuilder()
                .setId(variableId.toProto())
                .setValue(VariableValue.newBuilder().setStr("updated"));
        VariableValue previous = execute(
                UUID.randomUUID().toString(), command -> command.setPutVariable(putVariable), VariableValue.class);

        assertThat(previous.getStr()).isEqualTo("original");
        assertThat(readGetable(variableId).getValue().toProto().getStr()).isEqualTo("updated");
    }

    @Test
    void shouldThrowForNonExistingVariable() {
        WfRun run = startWorkflow(thread -> thread.sleepSeconds(3600), Map.of());
        WfRunIdModel wfRunId = new WfRunIdModel(run.getId().getId());
        VariableIdModel missingId = new VariableIdModel(wfRunId, 0, "missing");
        PutVariableRequest request = PutVariableRequest.newBuilder()
                .setId(missingId.toProto())
                .setValue(VariableValue.newBuilder().setStr("updated"))
                .build();

        StatusRuntimeException error = assertThrows(
                StatusRuntimeException.class,
                () -> execute(
                        wfRunId.getPartitionKey().orElseThrow(),
                        command -> command.setPutVariable(request),
                        VariableValue.class));

        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void shouldThrowWhenUpdatingCompletedWorkflow() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.declareStr("message").withDefault("original");
                    thread.complete("done");
                },
                Map.of());
        WfRunIdModel wfRunId = new WfRunIdModel(run.getId().getId());
        VariableIdModel variableId = new VariableIdModel(wfRunId, 0, "message");
        PutVariableRequest request = PutVariableRequest.newBuilder()
                .setId(variableId.toProto())
                .setValue(VariableValue.newBuilder().setStr("updated"))
                .build();
        assertThat(readGetable(wfRunId).getStatus()).isEqualTo(LHStatus.COMPLETED);

        StatusRuntimeException error = assertThrows(
                StatusRuntimeException.class,
                () -> execute(
                        wfRunId.getPartitionKey().orElseThrow(),
                        command -> command.setPutVariable(request),
                        VariableValue.class));

        assertThat(error.getStatus().getDescription()).isEqualTo("Cannot update variables on a completed WfRun");
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.FAILED_PRECONDITION);
        assertThat(readGetable(variableId).getValue().toProto().getStr()).isEqualTo("original");
    }

    @Test
    void shouldUpdateFailedWorkflowVariableBeforeRescue() {
        TaskDefModel taskDef = TestUtil.taskDef("rescue-put-variable");
        taskDef.setReturnType(new ReturnTypeModel(VariableType.INT));
        seedMetadata(taskDef);
        WfRun run = startWorkflow(
                thread -> {
                    var message = thread.declareStr("message").withDefault("original");
                    thread.execute("rescue-put-variable");
                    thread.complete(message);
                },
                Map.of());
        WfRunIdModel wfRunId = new WfRunIdModel(run.getId().getId());
        VariableIdModel variableId = new VariableIdModel(wfRunId, 0, "message");
        TaskRunModel task = currentTask(run);
        claimTask(task);
        reportTask(
                task,
                TaskStatus.TASK_SUCCESS,
                VariableValue.newBuilder().setStr("invalid").build());
        assertThat(readGetable(wfRunId).getStatus()).isEqualTo(LHStatus.ERROR);

        VariableValue previous = execute(
                wfRunId.getPartitionKey().orElseThrow(),
                command -> command.setPutVariable(PutVariableRequest.newBuilder()
                        .setId(variableId.toProto())
                        .setValue(VariableValue.newBuilder().setStr("updated"))),
                VariableValue.class);
        assertThat(previous.getStr()).isEqualTo("original");
        assertThat(readGetable(variableId).getValue().toProto().getStr()).isEqualTo("updated");
        assertThat(readGetable(wfRunId).getStatus()).isEqualTo(LHStatus.ERROR);

        execute(
                wfRunId.getPartitionKey().orElseThrow(),
                command -> command.setRescueThreadRun(RescueThreadRunRequest.newBuilder()
                        .setWfRunId(run.getId())
                        .setThreadRunNumber(0)
                        .setSkipCurrentNode(false)),
                WfRun.class);
        TaskRunModel retriedTask = currentTask(run);
        claimTask(retriedTask);
        reportTask(
                retriedTask,
                TaskStatus.TASK_SUCCESS,
                VariableValue.newBuilder().setInt(42).build());

        WfRun completed = readGetable(wfRunId).toProto().build();
        assertThat(completed.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(completed.getThreadRuns(0).getOutput().getStr()).isEqualTo("updated");
    }

    @Test
    void shouldRescheduleSleepAfterUpdatingDuration() {
        WfRun run = startWorkflow(
                thread -> {
                    var duration = thread.declareInt("duration").withDefault(60);
                    thread.sleepSeconds(duration);
                },
                Map.of());
        WfRunIdModel wfRunId = new WfRunIdModel(run.getId().getId());
        VariableIdModel variableId = new VariableIdModel(wfRunId, 0, "duration");
        NodeRunModel sleep = currentNode(run);
        assertThat(sleep.getStatus()).isEqualTo(LHStatus.RUNNING);
        assertThat(forwardedCommands()).hasSize(1);
        ForwardedCommand originalTimer = forwardedCommands().get(0);

        execute(
                wfRunId.getPartitionKey().orElseThrow(),
                command -> command.setPutVariable(PutVariableRequest.newBuilder()
                        .setId(variableId.toProto())
                        .setValue(VariableValue.newBuilder().setInt(120))),
                VariableValue.class);

        long expectedNewMaturationTime = sleep.getArrivalTime().getTime() + 120_000;
        assertThat(readGetable(sleep.getId())
                        .getSleepNodeRun()
                        .getMaturationTime()
                        .getTime())
                .isEqualTo(expectedNewMaturationTime);
        assertThat(forwardedCommands()).hasSize(1);
        ForwardedCommand rescheduledTimer = forwardedCommands().get(0);
        assertThat(LHUtil.fromProtoTs(rescheduledTimer.command().getTime()).getTime())
                .isEqualTo(expectedNewMaturationTime);

        executeForwardedCommand(originalTimer);
        assertThat(readGetable(sleep.getId()).getStatus()).isEqualTo(LHStatus.RUNNING);
        executeForwardedCommand(rescheduledTimer);
        assertThat(readGetable(wfRunId).getStatus()).isEqualTo(LHStatus.COMPLETED);
    }

    @Test
    void shouldShortenSleepAfterUpdatingDuration() {
        WfRun run = startWorkflow(
                thread -> {
                    var duration = thread.declareInt("duration").withDefault(120);
                    thread.sleepSeconds(duration);
                },
                Map.of());
        WfRunIdModel wfRunId = new WfRunIdModel(run.getId().getId());
        VariableIdModel variableId = new VariableIdModel(wfRunId, 0, "duration");
        NodeRunModel sleep = currentNode(run);
        assertThat(forwardedCommands()).hasSize(1);
        ForwardedCommand originalTimer = forwardedCommands().get(0);

        execute(
                wfRunId.getPartitionKey().orElseThrow(),
                command -> command.setPutVariable(PutVariableRequest.newBuilder()
                        .setId(variableId.toProto())
                        .setValue(VariableValue.newBuilder().setInt(10))),
                VariableValue.class);

        long newDeadline = sleep.getArrivalTime().getTime() + 10_000;
        assertThat(readGetable(sleep.getId())
                        .getSleepNodeRun()
                        .getMaturationTime()
                        .getTime())
                .isEqualTo(newDeadline);
        assertThat(forwardedCommands()).hasSize(1);
        ForwardedCommand earlierTimer = forwardedCommands().get(0);
        assertThat(LHUtil.fromProtoTs(earlierTimer.command().getTime()).getTime())
                .isEqualTo(newDeadline)
                .isLessThan(
                        LHUtil.fromProtoTs(originalTimer.command().getTime()).getTime());
        assertThat(readGetable(wfRunId).getStatus()).isEqualTo(LHStatus.RUNNING);

        executeForwardedCommand(earlierTimer);
        assertThat(readGetable(wfRunId).getStatus()).isEqualTo(LHStatus.COMPLETED);
        executeForwardedCommand(originalTimer);
        assertThat(readGetable(wfRunId).getStatus()).isEqualTo(LHStatus.COMPLETED);
    }
}
