package io.littlehorse.sdk.wfsdk.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.exception.LHWfSpecBuilderException;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.worker.LHStructDef;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkflowCompilationTest {

    @LHStructDef("referenced-root")
    public static class ReferencedRoot {
        private NestedReferencedType nested;

        public NestedReferencedType getNested() {
            return nested;
        }

        public void setNested(NestedReferencedType nested) {
            this.nested = nested;
        }
    }

    public static class NestedReferencedType {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class ChildReferencedType {
        private Long value;

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }
    }

    @LHStructDef("referenced-event")
    public static class EventReferencedType {
        private Boolean active;

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }

    public static class RegisteredExternalEventType {}

    public static class RegisteredWorkflowEventType {}

    @Test
    void shouldExposeJavaTypesReferencedAcrossWorkflowCompilation() {
        WorkflowImpl workflow = new WorkflowImpl("referenced-types", thread -> {
            thread.declareStruct("root", ReferencedRoot.class);
            thread.declareArray("nested-array", NestedReferencedType.class);
            thread.declareMap("nested-map", String.class, NestedReferencedType.class);
            thread.spawnThread(
                    child -> child.declareInlineStruct("child", ChildReferencedType.class), "child", Map.of());
            thread.registerInterruptHandler(
                            "interrupt", handler -> handler.declareInlineStruct("handler", ChildReferencedType.class))
                    .withEventType(EventReferencedType.class);
            thread.waitForEvent("external-event").registeredAs(RegisteredExternalEventType.class);
            thread.throwEvent("workflow-event", "payload").registeredAs(RegisteredWorkflowEventType.class);
        });

        Set<Class<?>> referencedTypes = workflow.getReferencedJavaTypes();

        Assertions.assertThat(referencedTypes)
                .contains(
                        ReferencedRoot.class,
                        NestedReferencedType.class,
                        ChildReferencedType.class,
                        EventReferencedType.class,
                        RegisteredExternalEventType.class,
                        RegisteredWorkflowEventType.class,
                        String.class,
                        Long.class,
                        Boolean.class);
        Assertions.assertThatThrownBy(() -> referencedTypes.add(Object.class))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    // @DisabledUntil(date = "2023-07-22")
    public void shouldThrowMisconfigurationExceptionWhenMultiplesThreadsWithSameName() {
        WorkflowImpl wf = new WorkflowImpl("asdf", thread -> {
            WfRunVariable var = thread.addVariable("my-var", VariableType.INT);
            thread.sleepUntil(var);

            thread.spawnThread(
                    thread1 -> {
                        WfRunVariable var2 = thread1.addVariable("my-var2", VariableType.INT);
                        thread1.sleepUntil(var2);
                    },
                    "my-thread",
                    Map.of());

            thread.spawnThread(
                    thread1 -> {
                        WfRunVariable var3 = thread1.addVariable("my-var3", VariableType.INT);
                        thread1.sleepUntil(var3);
                    },
                    "my-thread",
                    Map.of());
        });
        Throwable throwable = Assertions.catchThrowable(() -> wf.compileWorkflow());
        Assertions.assertThat(throwable).isNotNull();
        Assertions.assertThat(throwable)
                .isInstanceOf(LHWfSpecBuilderException.class)
                .hasMessage("Thread my-thread already exists");
    }

    @Test
    void shouldCompileAWorkflowWhenAParentVarIsAssignedFromChildNestedThreads() {
        WorkflowImpl wf = new WorkflowImpl("TestWorkflow", grandParentWfThread -> {
            WfRunVariable grandParentVar = grandParentWfThread.declareStr("grand-parent-var");
            grandParentWfThread.spawnThread(
                    son -> {
                        grandParentVar.assign("son-value");
                        son.spawnThread(
                                grandchild -> {
                                    grandParentVar.assign("grandchild-value");
                                },
                                "grandchild-thread",
                                null);
                    },
                    "son-thread",
                    null);
        });

        PutWfSpecRequest actualWfSpecRequest = wf.compileWorkflow();
        ThreadSpec entrypointThread = actualWfSpecRequest.getThreadSpecsOrThrow("entrypoint");
        Node nodeWithVariableMutatedInEntryPoint = entrypointThread.getNodesOrThrow("1-son-thread-START_THREAD");

        var expectedEntryPointEdge = Edge.newBuilder()
                .setSinkNodeName("2-exit-EXIT")
                .addVariableMutations(VariableMutation.newBuilder()
                        .setLhsName("1-son-thread-START_THREAD")
                        .setRhsAssignment(VariableAssignment.newBuilder()
                                .setNodeOutput(VariableAssignment.NodeOutputReference.newBuilder()
                                        .setNodeName("1-son-thread-START_THREAD")
                                        .build())
                                .build())
                        .build())
                .build();

        assertEquals(
                expectedEntryPointEdge,
                nodeWithVariableMutatedInEntryPoint.getOutgoingEdgesList().get(0));

        ThreadSpec sonThread = actualWfSpecRequest.getThreadSpecsOrThrow("son-thread");
        Node nodeWithVariableMutatedInSonEntrypointThread = sonThread.getNodesOrThrow("0-entrypoint-ENTRYPOINT");
        Node nodeWithVariableMutatedInSonGrandchildThread =
                sonThread.getNodesOrThrow("1-grandchild-thread-START_THREAD");

        var expectedSonEdge = Edge.newBuilder()
                .setSinkNodeName("1-grandchild-thread-START_THREAD")
                .addVariableMutations(VariableMutation.newBuilder()
                        .setLhsName("grand-parent-var")
                        .setOperation(VariableMutationType.ASSIGN)
                        .setRhsAssignment(VariableAssignment.newBuilder()
                                .setLiteralValue(VariableValue.newBuilder().setStr("son-value"))
                                .build())
                        .build())
                .build();

        assertEquals(
                expectedSonEdge,
                nodeWithVariableMutatedInSonEntrypointThread
                        .getOutgoingEdgesList()
                        .get(0));

        var expectedGrandchildEdge = Edge.newBuilder()
                .setSinkNodeName("2-exit-EXIT")
                .addVariableMutations(VariableMutation.newBuilder()
                        .setLhsName("1-grandchild-thread-START_THREAD")
                        .setOperation(VariableMutationType.ASSIGN)
                        .setRhsAssignment(VariableAssignment.newBuilder()
                                .setNodeOutput(VariableAssignment.NodeOutputReference.newBuilder()
                                        .setNodeName("1-grandchild-thread-START_THREAD")
                                        .build())
                                .build())
                        .build())
                .build();

        assertEquals(
                expectedGrandchildEdge,
                nodeWithVariableMutatedInSonGrandchildThread
                        .getOutgoingEdgesList()
                        .get(0));
    }
}
