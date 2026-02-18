package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.EntrypointNode;
import io.littlehorse.sdk.common.proto.ExitNode;
import io.littlehorse.sdk.common.proto.LegacyEdgeCondition;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.NopNode;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskNode;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.WorkflowIfStatement;
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

            assertThat(firstNOPNode.getOutgoingEdgesCount()).isEqualTo(2);
            assertThat(firstNOPNode.getOutgoingEdges(0))
                    .extracting(
                            (edge) -> edge.getLegacyCondition().getLeft().getVariableName(),
                            (edge) -> edge.getLegacyCondition().getComparator(),
                            (edge) -> edge.getLegacyCondition()
                                    .getRight()
                                    .getLiteralValue()
                                    .getInt(),
                            (edge) -> {
                                return edge.getVariableMutations(0)
                                        .getRhsAssignment()
                                        .getLiteralValue()
                                        .getStr();
                            })
                    .containsExactly("my-var", Comparator.GREATER_THAN, 10L, "if-body");
            assertThat(firstNOPNode.getOutgoingEdges(1))
                    .extracting(
                            (edge) -> edge.getLegacyCondition().getLeft(),
                            (edge) -> edge.getLegacyCondition().getRight(),
                            (edge) -> {
                                return edge.getVariableMutations(0)
                                        .getRhsAssignment()
                                        .getLiteralValue()
                                        .getStr();
                            })
                    .containsExactly(
                            VariableAssignment.getDefaultInstance(),
                            VariableAssignment.getDefaultInstance(),
                            "else-body");
        }

        @Test
        public void testDoIfStatementWithoutElseBody() {
            WorkflowImpl wfSpec = new WorkflowImpl("my-workflow", wf -> {
                wf.doIf(wf.condition(5, Comparator.GREATER_THAN, 4), body -> {
                    body.execute("task-a");
                });
            });

            ThreadSpec entrypointThread = wfSpec.compileWorkflow().getThreadSpecsOrThrow("entrypoint");

            assertThat(entrypointThread.getNodesMap().size()).isEqualTo(5);

            assertThat(entrypointThread.getNodesMap().get("0-entrypoint-ENTRYPOINT"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("1-nop-NOP"))
                            .setEntrypoint(EntrypointNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("1-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("2-task-a-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(5)))
                                            .setComparator(Comparator.GREATER_THAN)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(4)))))
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setNop(NopNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("2-task-a-TASK"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setTask(TaskNode.newBuilder()
                                    .setTaskDefId(TaskDefId.newBuilder().setName("task-a"))
                                    .setTimeoutSeconds(0)
                                    .setRetries(0))
                            .build());
            assertThat(entrypointThread.getNodesMap().get("3-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("4-exit-EXIT"))
                            .setNop(NopNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("4-exit-EXIT"))
                    .isEqualTo(Node.newBuilder()
                            .setExit(ExitNode.getDefaultInstance())
                            .build());
        }

        @Test
        public void testDoElseIfStatement() {
            WorkflowImpl wfSpec = new WorkflowImpl("my-workflow", wf -> {
                wf.doIf(wf.condition(5, Comparator.GREATER_THAN, 4), body -> {
                            body.execute("task-a");
                        })
                        .doElseIf(wf.condition(10, Comparator.EQUALS, 40), body -> {
                            body.execute("task-b");
                        });
            });

            ThreadSpec entrypointThread = wfSpec.compileWorkflow().getThreadSpecsOrThrow("entrypoint");

            assertThat(entrypointThread.getNodesMap().size()).isEqualTo(6);

            assertThat(entrypointThread.getNodesMap().get("0-entrypoint-ENTRYPOINT"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("1-nop-NOP"))
                            .setEntrypoint(EntrypointNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("1-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("2-task-a-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(5)))
                                            .setComparator(Comparator.GREATER_THAN)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(4)))))
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("4-task-b-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(10)))
                                            .setComparator(Comparator.EQUALS)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(40)))))
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setNop(NopNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("2-task-a-TASK"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setTask(TaskNode.newBuilder()
                                    .setTaskDefId(TaskDefId.newBuilder().setName("task-a"))
                                    .setTimeoutSeconds(0)
                                    .setRetries(0))
                            .build());
            assertThat(entrypointThread.getNodesMap().get("4-task-b-TASK"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setTask(TaskNode.newBuilder()
                                    .setTaskDefId(TaskDefId.newBuilder().setName("task-b"))
                                    .setTimeoutSeconds(0)
                                    .setRetries(0))
                            .build());
            assertThat(entrypointThread.getNodesMap().get("3-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("5-exit-EXIT"))
                            .setNop(NopNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("5-exit-EXIT"))
                    .isEqualTo(Node.newBuilder()
                            .setExit(ExitNode.getDefaultInstance())
                            .build());
        }

        @Test
        public void testDoElseIfStatementWithBodyAndVariableMutations() {
            WorkflowImpl wfSpec = new WorkflowImpl("my-workflow", wf -> {
                WfRunVariable myInt = wf.declareInt("my-int");
                wf.doIf(wf.condition(5, Comparator.GREATER_THAN, 4), body -> {
                            myInt.assign(10);
                            body.execute("task-a");
                        })
                        .doElseIf(wf.condition(10, Comparator.EQUALS, 40), body -> {
                            body.execute("task-b");
                            myInt.assign(20);
                        });
                myInt.assign(30);
            });

            ThreadSpec entrypointThread = wfSpec.compileWorkflow().getThreadSpecsOrThrow("entrypoint");

            assertThat(entrypointThread
                            .getNodesMap()
                            .get("1-nop-NOP")
                            .getOutgoingEdges(0)
                            .getVariableMutations(0))
                    .isEqualTo(VariableMutation.newBuilder()
                            .setLhsName("my-int")
                            .setOperation(VariableMutationType.ASSIGN)
                            .setRhsAssignment(VariableAssignment.newBuilder()
                                    .setLiteralValue(VariableValue.newBuilder().setInt(10)))
                            .build());
            assertThat(entrypointThread
                            .getNodesMap()
                            .get("2-task-a-TASK")
                            .getOutgoingEdges(0)
                            .getVariableMutationsCount())
                    .isEqualTo(0);
            assertThat(entrypointThread
                            .getNodesMap()
                            .get("4-task-b-TASK")
                            .getOutgoingEdges(0)
                            .getVariableMutations(0))
                    .isEqualTo(VariableMutation.newBuilder()
                            .setLhsName("my-int")
                            .setOperation(VariableMutationType.ASSIGN)
                            .setRhsAssignment(VariableAssignment.newBuilder()
                                    .setLiteralValue(VariableValue.newBuilder().setInt(20)))
                            .build());
            assertThat(entrypointThread
                            .getNodesMap()
                            .get("3-nop-NOP")
                            .getOutgoingEdges(0)
                            .getVariableMutations(0))
                    .isEqualTo(VariableMutation.newBuilder()
                            .setLhsName("my-int")
                            .setOperation(VariableMutationType.ASSIGN)
                            .setRhsAssignment(VariableAssignment.newBuilder()
                                    .setLiteralValue(VariableValue.newBuilder().setInt(30)))
                            .build());
        }

        @Test
        public void testMultipleDoElseIfStatements() {
            WorkflowImpl wfSpec = new WorkflowImpl("my-workflow", wf -> {
                WfRunVariable myInt = wf.declareInt("my-int");
                wf.doIf(wf.condition(5, Comparator.GREATER_THAN, 4), body -> {
                            body.execute("task-a");
                        })
                        .doElseIf(wf.condition(10, Comparator.EQUALS, 40), body -> {
                            body.execute("task-b");
                        })
                        .doElseIf(wf.condition(myInt, Comparator.LESS_THAN, 100), body -> {
                            body.execute("task-c");
                        });
            });

            ThreadSpec entrypointThread = wfSpec.compileWorkflow().getThreadSpecsOrThrow("entrypoint");

            assertThat(entrypointThread.getNodesMap().size()).isEqualTo(7);

            assertThat(entrypointThread.getNodesMap().get("1-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("2-task-a-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(5)))
                                            .setComparator(Comparator.GREATER_THAN)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(4)))))
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("4-task-b-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(10)))
                                            .setComparator(Comparator.EQUALS)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(40)))))
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("5-task-c-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setVariableName("my-int"))
                                            .setComparator(Comparator.LESS_THAN)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(100)))))
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setNop(NopNode.getDefaultInstance())
                            .build());
        }

        @Test
        public void testDoElseStatementThrowsExceptionWhenCalledTwice() {
            WorkflowImpl wfSpec = new WorkflowImpl("my-workflow", wf -> {
                WfRunVariable myInt = wf.declareInt("my-int");
                WorkflowIfStatement myIfStatement = wf.doIf(wf.condition(5, Comparator.GREATER_THAN, 4), body -> {
                    myInt.assign(10);
                    body.execute("task-a");
                });
                myIfStatement.doElse(body -> {});
                myIfStatement.doElse(body -> {});
                myInt.assign(30);
            });

            assertThatThrownBy(() -> wfSpec.compileWorkflow()).isInstanceOf(IllegalStateException.class);
        }

        @Test
        public void testDoElseIfWithNodeAndMutationAfterIfStatement() {
            WorkflowImpl wfSpec = new WorkflowImpl("my-workflow", wf -> {
                WfRunVariable myInt = wf.declareInt("my-int");
                wf.doIf(wf.condition(5, Comparator.GREATER_THAN, 4), body -> {
                            body.execute("task-a");
                        })
                        .doElseIf(wf.condition(10, Comparator.LESS_THAN_EQ, 200), body -> {
                            body.execute("task-b");
                        });
                myInt.assign(40);
                wf.execute("task-c");
            });

            ThreadSpec entrypointThread = wfSpec.compileWorkflow().getThreadSpecsOrThrow("entrypoint");

            assertThat(entrypointThread.getNodesMap().size()).isEqualTo(7);

            assertThat(entrypointThread.getNodesMap().get("3-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("5-task-c-TASK")
                                    .addVariableMutations(VariableMutation.newBuilder()
                                            .setLhsName("my-int")
                                            .setOperation(VariableMutationType.ASSIGN)
                                            .setRhsAssignment(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(40)))
                                            .build()))
                            .setNop(NopNode.newBuilder().build())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("5-task-c-TASK"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("6-exit-EXIT"))
                            .setTask(TaskNode.newBuilder()
                                    .setTaskDefId(TaskDefId.newBuilder().setName("task-c"))
                                    .setTimeoutSeconds(0)
                                    .setRetries(0))
                            .build());
            assertThat(entrypointThread.getNodesMap().get("6-exit-EXIT"))
                    .isEqualTo(Node.newBuilder()
                            .setExit(ExitNode.getDefaultInstance())
                            .build());
        }

        /**
         * Tests a case where `WorkflowThread#execute()` is called
         * between elseIf statements within the main body of the WorkflowThread.
         */
        @Test
        public void testIfStatementWithExecuteMethodCallBetweenElseIfConditions() {
            WorkflowImpl wfSpec = new WorkflowImpl("my-workflow", wf -> {
                WorkflowIfStatement myIfStatement = wf.doIf(wf.condition(5, Comparator.GREATER_THAN, 4), body -> {
                    body.execute("task-a");
                });
                wf.execute("task-c");
                myIfStatement.doElseIf(wf.condition(10, Comparator.LESS_THAN_EQ, 200), body -> {
                    body.execute("task-b");
                });
            });

            ThreadSpec entrypointThread = wfSpec.compileWorkflow().getThreadSpecsOrThrow("entrypoint");

            assertThat(entrypointThread.getNodesMap().size()).isEqualTo(7);

            assertThat(entrypointThread.getNodesMap().get("0-entrypoint-ENTRYPOINT"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("1-nop-NOP"))
                            .setEntrypoint(EntrypointNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("1-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("2-task-a-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(5)))
                                            .setComparator(Comparator.GREATER_THAN)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(4)))))
                            .addOutgoingEdges(Edge.newBuilder()
                                    .setSinkNodeName("5-task-b-TASK")
                                    .setLegacyCondition(LegacyEdgeCondition.newBuilder()
                                            .setLeft(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(10)))
                                            .setComparator(Comparator.LESS_THAN_EQ)
                                            .setRight(VariableAssignment.newBuilder()
                                                    .setLiteralValue(VariableValue.newBuilder()
                                                            .setInt(200)))))
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setNop(NopNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("2-task-a-TASK"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setTask(TaskNode.newBuilder()
                                    .setTaskDefId(TaskDefId.newBuilder().setName("task-a"))
                                    .setTimeoutSeconds(0)
                                    .setRetries(0))
                            .build());
            assertThat(entrypointThread.getNodesMap().get("5-task-b-TASK"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("3-nop-NOP"))
                            .setTask(TaskNode.newBuilder()
                                    .setTaskDefId(TaskDefId.newBuilder().setName("task-b"))
                                    .setTimeoutSeconds(0)
                                    .setRetries(0))
                            .build());
            assertThat(entrypointThread.getNodesMap().get("3-nop-NOP"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("4-task-c-TASK"))
                            .setNop(NopNode.getDefaultInstance())
                            .build());
            assertThat(entrypointThread.getNodesMap().get("4-task-c-TASK"))
                    .isEqualTo(Node.newBuilder()
                            .addOutgoingEdges(Edge.newBuilder().setSinkNodeName("6-exit-EXIT"))
                            .setTask(TaskNode.newBuilder()
                                    .setTaskDefId(TaskDefId.newBuilder().setName("task-c"))
                                    .setTimeoutSeconds(0)
                                    .setRetries(0))
                            .build());
            assertThat(entrypointThread.getNodesMap().get("6-exit-EXIT"))
                    .isEqualTo(Node.newBuilder()
                            .setExit(ExitNode.getDefaultInstance())
                            .build());
        }
    }
}
