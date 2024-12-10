package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ConditionalTest {

    @Nested
    class IfElse {

        @Test
        public void shouldCreateIfElseConditionWithoutNodes() {
            WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
                WfRunVariable myVar =
                        thread.addVariable("my-var", VariableType.STR).searchable();
                thread.doIfElse(
                        thread.condition(myVar, Comparator.GREATER_THAN, 10),
                        ifBody -> thread.mutate(myVar, VariableMutationType.ASSIGN, "if-body"),
                        elseBody -> thread.mutate(myVar, VariableMutationType.ASSIGN, "else-body"));
            });

            ThreadSpec thread = wf.compileWorkflow().getThreadSpecsOrThrow("entrypoint");
            Node firstNOPNode = thread.getNodesOrThrow("1-nop-NOP");

            assertThat(firstNOPNode.getOutgoingEdgesList())
                    .extracting(
                            (edge) -> edge.getCondition().getComparator(),
                            (edge) -> edge.getCondition()
                                    .getRight()
                                    .getLiteralValue()
                                    .getInt(),
                            (edge) -> edge.getVariableMutations(0)
                                    .getRhsAssignment()
                                    .getLiteralValue()
                                    .getStr())
                    .containsExactlyInAnyOrder(
                            Tuple.tuple(Comparator.GREATER_THAN, 10L, "if-body"),
                            Tuple.tuple(Comparator.LESS_THAN_EQ, 10L, "else-body"));
        }
    }
}
