package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.NodePb;
import io.littlehorse.sdk.common.proto.NodePb.NodeCase;
import io.littlehorse.sdk.common.proto.PutWfSpecPb;
import io.littlehorse.sdk.common.proto.SleepNodePb.SleepLengthCase;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.util.List;
import java.util.Map;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

public class ThreadBuilderImplTest {

    Faker faker = new Faker();

    @Test
    void testSleepUntil() {
        WorkflowImpl wf = new WorkflowImpl(
            "asdf",
            thread -> {
                WfRunVariable var = thread.addVariable("my-var", VariableTypePb.INT);
                thread.sleepUntil(var);
            }
        );

        PutWfSpecPb wfSpec = wf.compileWorkflow();

        NodePb sleepNode = wfSpec
            .getThreadSpecsOrThrow("entrypoint")
            .getNodesOrThrow("1-sleep-SLEEP");

        assertThat(sleepNode.getSleep() != null);
        assertEquals(
            sleepNode.getSleep().getSleepLengthCase(),
            SleepLengthCase.TIMESTAMP
        );
        assertEquals(sleepNode.getSleep().getTimestamp().getVariableName(), "my-var");
    }

    @Test
    void testEarlyReturn() {
        WorkflowImpl wf = new WorkflowImpl(
            "asdf",
            thread -> {
                WfRunVariable var = thread.addVariable("my-var", VariableTypePb.INT);
                thread.doIf(
                    thread.condition(var, ComparatorPb.LESS_THAN, 10),
                    ifBody -> {
                        ifBody.execute("foo");
                        ifBody.complete();
                    }
                );
                thread.execute("bar");
            }
        );

        PutWfSpecPb wfSpec = wf.compileWorkflow();
        List<Map.Entry<String, NodePb>> exitNodes = wfSpec
            .getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName())
            .getNodesMap()
            .entrySet()
            .stream()
            .filter(nodePair -> {
                return (nodePair.getValue().getNodeCase() == NodeCase.EXIT);
            })
            .toList();

        assertEquals(exitNodes.size(), 2);

        exitNodes.forEach(entry -> {
            NodePb node = entry.getValue();
            assertThat(node.getOutgoingEdgesCount() == 0);
        });
    }
}
