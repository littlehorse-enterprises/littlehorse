package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PutVariableRequest;
import io.littlehorse.sdk.common.proto.RescueThreadRunRequest;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.AbstractWorkflowExecutionTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PutVariableTest extends AbstractWorkflowExecutionTest {

    @Test
    void shouldValidateArrayElements() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.declareArray("numbers", Long.class).withDefault(new Long[] {1L});
                    thread.sleepSeconds(3600);
                },
                Map.of());
        VariableIdModel id = new VariableIdModel(new WfRunIdModel(run.getId().getId()), 0, "numbers");
        Array validArray = Array.newBuilder()
                .setElementType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                .addItems(VariableValue.newBuilder().setInt(2))
                .addItems(VariableValue.newBuilder().setInt(3))
                .build();
        VariableValue valid = VariableValue.newBuilder().setArray(validArray).build();
        VariableValue before = readGetable(id).getValue().toProto().build();

        assertThat(replaceVariable(id, valid)).isEqualTo(before);
        assertThat(readGetable(id).getValue().toProto().build()).isEqualTo(valid);

        // The client claims INT elements, but the second element is incompatible.
        VariableValue invalid = VariableValue.newBuilder()
                .setArray(validArray.toBuilder()
                        .setItems(1, VariableValue.newBuilder().setBool(true)))
                .build();
        assertRejectedWithoutChangingState(run, id, invalid);
    }

    @Test
    void shouldRejectNumericStringReplacementForIntVariable() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.declareInt("number").withDefault(1);
                    thread.sleepSeconds(3600);
                },
                Map.of());
        VariableIdModel id = new VariableIdModel(new WfRunIdModel(run.getId().getId()), 0, "number");

        assertRejectedWithoutChangingState(
                run, id, VariableValue.newBuilder().setStr("42").build());
    }

    @Test
    void shouldRejectNonNumericStringReplacementForIntVariable() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.declareInt("number").withDefault(1);
                    thread.sleepSeconds(3600);
                },
                Map.of());
        VariableIdModel id = new VariableIdModel(new WfRunIdModel(run.getId().getId()), 0, "number");

        assertRejectedWithoutChangingState(
                run, id, VariableValue.newBuilder().setStr("not-an-int").build());
    }

    @Test
    void shouldRejectJsonObjectKeyForStringKeyedMap() {
        WfRun run = startMapWorkflow();
        VariableIdModel id = new VariableIdModel(new WfRunIdModel(run.getId().getId()), 0, "numbers");
        var validMap = replaceWithValidMap(id);
        var invalidEntry = validMap.getEntries(1).toBuilder()
                .setKey(VariableValue.newBuilder().setJsonObj("{}"));

        // Valid map metadata must not bypass validation of individual keys.
        VariableValue invalid = VariableValue.newBuilder()
                .setMap(validMap.toBuilder().setEntries(1, invalidEntry))
                .build();
        assertRejectedWithoutChangingState(run, id, invalid);
    }

    @Test
    void shouldRejectBooleanValueForIntValuedMap() {
        WfRun run = startMapWorkflow();
        VariableIdModel id = new VariableIdModel(new WfRunIdModel(run.getId().getId()), 0, "numbers");
        var validMap = replaceWithValidMap(id);
        var invalidEntry = validMap.getEntries(1).toBuilder()
                .setValue(VariableValue.newBuilder().setBool(true));

        // Valid map metadata must not bypass validation of individual values.
        VariableValue invalid = VariableValue.newBuilder()
                .setMap(validMap.toBuilder().setEntries(1, invalidEntry))
                .build();
        assertRejectedWithoutChangingState(run, id, invalid);
    }

    private WfRun startMapWorkflow() {
        return startWorkflow(
                thread -> {
                    thread.declareMap("numbers", String.class, Long.class).withDefault(Map.of("original", 1L));
                    thread.sleepSeconds(3600);
                },
                Map.of());
    }

    private io.littlehorse.sdk.common.proto.Map replaceWithValidMap(VariableIdModel id) {
        io.littlehorse.sdk.common.proto.Map validMap = io.littlehorse.sdk.common.proto.Map.newBuilder()
                .setMapType(InlineMapDef.newBuilder()
                        .setKeyType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                        .setValueType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))
                .addEntries(io.littlehorse.sdk.common.proto.Map.Entry.newBuilder()
                        .setKey(VariableValue.newBuilder().setStr("first"))
                        .setValue(VariableValue.newBuilder().setInt(2)))
                .addEntries(io.littlehorse.sdk.common.proto.Map.Entry.newBuilder()
                        .setKey(VariableValue.newBuilder().setStr("second"))
                        .setValue(VariableValue.newBuilder().setInt(3)))
                .build();
        VariableValue valid = VariableValue.newBuilder().setMap(validMap).build();
        VariableValue before = readGetable(id).getValue().toProto().build();

        assertThat(replaceVariable(id, valid)).isEqualTo(before);
        assertThat(readGetable(id).getValue().toProto().build()).isEqualTo(valid);

        return validMap;
    }

    private VariableValue replaceVariable(VariableIdModel id, VariableValue value) {
        return execute(
                id.getPartitionKey().orElseThrow(),
                command -> command.setPutVariable(
                        PutVariableRequest.newBuilder().setId(id.toProto()).setValue(value)),
                VariableValue.class);
    }

    private void assertRejectedWithoutChangingState(WfRun run, VariableIdModel id, VariableValue value) {
        WfRunIdModel wfRunId = id.getWfRunId();
        var beforeVariable = readGetable(id).toProto().build();
        var beforeWfRun = readGetable(wfRunId).toProto().build();
        var nodeId = currentNode(run).getId();
        var beforeNode = readGetable(nodeId).toProto().build();

        StatusRuntimeException error = assertThrows(StatusRuntimeException.class, () -> replaceVariable(id, value));

        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(readGetable(id).toProto().build()).isEqualTo(beforeVariable);
        assertThat(readGetable(wfRunId).toProto().build()).isEqualTo(beforeWfRun);
        assertThat(readGetable(nodeId).toProto().build()).isEqualTo(beforeNode);
        assertThat(forwardedCommands()).isEmpty();
    }

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
    void shouldReturnMaskedValueWhenReplacingMaskedWorkflowVariable() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.declareStr("message").withDefault("original").masked();
                    thread.sleepSeconds(3600);
                },
                Map.of());
        WfRunIdModel wfRunId = new WfRunIdModel(run.getId().getId());
        VariableIdModel variableId = new VariableIdModel(wfRunId, 0, "message");

        VariableValue previous = execute(
                wfRunId.getPartitionKey().orElseThrow(),
                command -> command.setPutVariable(PutVariableRequest.newBuilder()
                        .setId(variableId.toProto())
                        .setValue(VariableValue.newBuilder().setStr("updated"))),
                VariableValue.class);

        assertThat(previous)
                .isEqualTo(VariableValue.newBuilder()
                        .setStr(LHConstants.STRING_MASK)
                        .build());
        assertThat(readGetable(variableId).isMasked()).isTrue();
        assertThat(readGetable(variableId).getValue().toProto().getStr()).isEqualTo(LHConstants.STRING_MASK);
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
