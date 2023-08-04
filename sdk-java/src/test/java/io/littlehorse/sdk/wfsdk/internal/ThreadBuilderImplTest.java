package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.NodePb;
import io.littlehorse.sdk.common.proto.NodePb.NodeCase;
import io.littlehorse.sdk.common.proto.PutWfSpecPb;
import io.littlehorse.sdk.common.proto.SleepNodePb.SleepLengthCase;
import io.littlehorse.sdk.common.proto.VariableDefPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
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

        assertThat(sleepNode.getSleep()).isNotNull();
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
            .collect(Collectors.toList());

        assertEquals(exitNodes.size(), 2);

        exitNodes.forEach(entry -> {
            NodePb node = entry.getValue();
            assertThat(node.getOutgoingEdgesCount()).isEqualTo(0);
        });
    }

    @AllArgsConstructor
    class Foo {

        public String bar;
        public String baz;
    }

    @Test
    void testDefaultVarVals() {
        WorkflowImpl wf = new WorkflowImpl(
            "asdf",
            thread -> {
                thread.addVariable("int-var", 123);
                thread.addVariable("object-var", new Foo("asdf", "fdsa"));
            }
        );

        PutWfSpecPb wfSpec = wf.compileWorkflow();
        List<VariableDefPb> varDefs = wfSpec
            .getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName())
            .getVariableDefsList();

        VariableDefPb intVar = varDefs.get(0);
        assertThat(intVar.getDefaultValue()).isNotNull();
        assertEquals(intVar.getDefaultValue().getInt(), 123);

        VariableDefPb objVar = varDefs.get(1);
        assertThat(objVar.getDefaultValue()).isNotEqualTo(null);
        assertEquals(objVar.getType(), VariableTypePb.JSON_OBJ);
    }

    @Test
    void testWhileLoopConditional() {
        WorkflowImpl wf = new WorkflowImpl(
            "asdf",
            thread -> {
                thread.execute("asdf");
                thread.doWhile(
                    thread.condition("asf", ComparatorPb.EQUALS, "asf"),
                    loop -> {
                        loop.execute("fdsa");
                    }
                );
            }
        );

        PutWfSpecPb wfSpec = wf.compileWorkflow();
        NodePb lastNodeInLoopBody = wfSpec
            .getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName())
            .getNodesOrThrow("2-nop-NOP");

        assertThat(lastNodeInLoopBody.getOutgoingEdgesCount()).isEqualTo(2);
    }
}
