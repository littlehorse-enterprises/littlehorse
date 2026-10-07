package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.server.AbstractWorkflowExecutionTest;
import java.util.Map;
import org.junit.jupiter.api.TestTemplate;

class SleepNodeTest extends AbstractWorkflowExecutionTest {
    @TestTemplate
    void completesWhenForwardedSleepCommandRuns() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.sleepSeconds(5);
                    thread.complete("awake");
                },
                Map.of());
        NodeRunModel node = currentNode(run);
        assertThat(forwardedCommands()).hasSize(1);

        assertThat(node.getStatus()).isEqualTo(LHStatus.RUNNING);
        assertThat(node.toProto().getSleep().getMatured()).isFalse();

        executeForwardedCommand(forwardedCommands().get(0));
        assertThat(forwardedCommands()).isEmpty();

        WfRun.Builder completed =
                readGetable(new WfRunIdModel(run.getId().getId())).toProto();
        assertThat(completed.getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(completed.getThreadRuns(0).getOutput().getStr()).isEqualTo("awake");
        assertThat(readGetable(node.getId()).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(readGetable(node.getId()).toProto().getSleep().getMatured()).isTrue();
    }

    @TestTemplate
    void completesTimestampSleepWhenForwardedCommandRuns() {
        long wakeAt = System.currentTimeMillis() + 60_000;
        WfRun run = startWorkflow(
                thread -> {
                    WfRunVariable timestamp = thread.declareInt("wakeAt");
                    thread.sleepUntil(timestamp);
                },
                Map.of("wakeAt", VariableValue.newBuilder().setInt(wakeAt).build()));
        NodeRunModel node = currentNode(run);
        assertThat(forwardedCommands()).hasSize(1);
        assertThat(node.toProto().getSleep().getMatured()).isFalse();

        executeForwardedCommand(forwardedCommands().get(0));
        assertThat(forwardedCommands()).isEmpty();
        assertThat(readGetable(node.getId()).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(readGetable(new WfRunIdModel(run.getId().getId())).getStatus())
                .isEqualTo(LHStatus.COMPLETED);
    }

    @TestTemplate
    void staleCommandDoesNotAdvanceNextSleep() {
        WfRun run = startWorkflow(
                thread -> {
                    thread.sleepSeconds(1);
                    thread.sleepSeconds(2);
                },
                Map.of());
        NodeRunModel firstNode = currentNode(run);
        assertThat(forwardedCommands()).hasSize(1);
        ForwardedCommand firstTimer = forwardedCommands().get(0);

        executeForwardedCommand(firstTimer);
        NodeRunModel secondNode = currentNode(run);
        assertThat(secondNode.getId()).isNotEqualTo(firstNode.getId());
        assertThat(secondNode.getStatus()).isEqualTo(LHStatus.RUNNING);
        assertThat(forwardedCommands()).hasSize(1);
        ForwardedCommand secondTimer = forwardedCommands().get(0);

        executeForwardedCommand(firstTimer);
        assertThat(forwardedCommands()).isEmpty();
        assertThat(readGetable(secondNode.getId()).getStatus()).isEqualTo(LHStatus.RUNNING);
        assertThat(readGetable(secondNode.getId()).toProto().getSleep().getMatured())
                .isFalse();

        executeForwardedCommand(secondTimer);
        assertThat(forwardedCommands()).isEmpty();
        assertThat(readGetable(secondNode.getId()).getStatus()).isEqualTo(LHStatus.COMPLETED);
        assertThat(readGetable(new WfRunIdModel(run.getId().getId())).getStatus())
                .isEqualTo(LHStatus.COMPLETED);
    }
}
