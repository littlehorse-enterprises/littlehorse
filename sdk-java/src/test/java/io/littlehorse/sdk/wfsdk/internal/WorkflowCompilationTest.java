package io.littlehorse.sdk.wfsdk.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.LHWfSpecBuilderException;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHStructDef;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
        Workflow workflow = Workflow.newWorkflow(
                "referenced-types",
                thread -> {
                    thread.declareStruct("root", ReferencedRoot.class);
                    thread.declareArray("root-array", ReferencedRoot.class);
                    thread.declareMap("root-map", String.class, ReferencedRoot[].class);
                    thread.declareArray("nested-array", NestedReferencedType.class);
                    thread.declareArray("matrix", NestedReferencedType[].class);
                    thread.declareMap("array-map", String.class, NestedReferencedType[][].class);
                    thread.declareMap("nested-map", String.class, NestedReferencedType.class);
                    thread.spawnThread(
                            child -> {
                                child.declareInlineStruct("child", ChildReferencedType.class);
                                child.declareArray("child-array", ChildReferencedType.class);
                            },
                            "child",
                            Map.of());
                    thread.registerInterruptHandler("interrupt", handler -> {
                                handler.declareInlineStruct("handler", ChildReferencedType.class);
                                handler.declareMap("handler-map", String.class, ChildReferencedType[].class);
                            })
                            .withEventType(EventReferencedType.class);
                    thread.waitForEvent("external-event").registeredAs(RegisteredExternalEventType.class);
                    thread.throwEvent("workflow-event", "payload").registeredAs(RegisteredWorkflowEventType.class);
                },
                new LHConfig());

        Set<Class<?>> referencedTypes = workflow.getReferencedJavaTypes();

        Assertions.assertThat(referencedTypes)
                .contains(
                        ReferencedRoot.class,
                        ReferencedRoot[].class,
                        NestedReferencedType.class,
                        NestedReferencedType[].class,
                        NestedReferencedType[][].class,
                        Map.class,
                        ChildReferencedType.class,
                        ChildReferencedType[].class,
                        EventReferencedType.class,
                        RegisteredExternalEventType.class,
                        RegisteredWorkflowEventType.class,
                        String.class,
                        Long.class,
                        Boolean.class);
        Assertions.assertThat(workflow.getReferencedJavaTypes()).isEqualTo(referencedTypes);
        workflow.compileWorkflow();
        Assertions.assertThat(workflow.getReferencedJavaTypes()).isEqualTo(referencedTypes);
        Assertions.assertThatThrownBy(() -> referencedTypes.add(Object.class))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    public static class ContainerProperties {
        public NestedReferencedType[][] getCustomers() {
            return null;
        }

        public void setCustomers(NestedReferencedType[][] customers) {}

        public Map<String, NestedReferencedType[]> getByName() {
            return null;
        }

        public void setByName(Map<String, NestedReferencedType[]> byName) {}
    }

    @LHStructDef("named-container-properties")
    public static class NamedContainerProperties extends ContainerProperties {}

    @Test
    void shouldDiscoverContainersInInlineAndNamedBeanProperties() {
        for (boolean named : new boolean[] {false, true}) {
            Workflow workflow = Workflow.newWorkflow("container-properties", thread -> {
                if (named) {
                    thread.declareStruct("container", NamedContainerProperties.class);
                } else {
                    thread.declareInlineStruct("container", ContainerProperties.class);
                }
            });
            Assertions.assertThat(workflow.getReferencedJavaTypes())
                    .containsExactlyInAnyOrder(
                            named ? NamedContainerProperties.class : ContainerProperties.class,
                            NestedReferencedType[][].class,
                            NestedReferencedType[].class,
                            NestedReferencedType.class,
                            Map.class,
                            String.class);
        }
    }

    @Test
    void shouldDiscoverAdaptedArrayAndMapTypesWithoutInventingScalarArrays() {
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new UUIDAdapter()).build();
        Workflow workflow = Workflow.newWorkflow(
                "adapted-containers",
                thread -> {
                    thread.declareArray("ids", UUID[].class);
                    thread.declareMap("ids-by-name", String.class, UUID[].class);
                    thread.declareMap("scalar-map", UUID.class, Long.class);
                },
                config);

        Assertions.assertThat(workflow.getReferencedJavaTypes())
                .containsExactlyInAnyOrder(
                        UUID[][].class, UUID[].class, UUID.class, Map.class, String.class, Long.class);
    }

    @Test
    void shouldNotInferTypesFromJsonDefaultsOrNamedStructIds() {
        Workflow workflow = Workflow.newWorkflow("runtime-values", thread -> {
            thread.declareJsonObj("json").withDefault(new HashMap<>(Map.of("customer", new NestedReferencedType())));
            thread.declareStruct("server-defined", "server-defined");
            thread.declareStr("scalar");
        });

        Assertions.assertThat(workflow.getReferencedJavaTypes())
                .doesNotContain(
                        HashMap.class, NestedReferencedType.class, NestedReferencedType[].class, String[].class);
    }

    private static class UUIDAdapter implements LHStringAdapter<UUID> {
        public Class<UUID> getTypeClass() {
            return UUID.class;
        }

        public String toString(UUID value) {
            return value.toString();
        }

        public UUID fromString(String value) {
            return UUID.fromString(value);
        }
    }

    @Test
    // @DisabledUntil(date = "2023-07-22")
    public void shouldThrowMisconfigurationExceptionWhenMultiplesThreadsWithSameName() {
        Workflow wf = Workflow.newWorkflow(
                "asdf",
                thread -> {
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
                },
                new LHConfig());
        Throwable throwable = Assertions.catchThrowable(() -> wf.compileWorkflow());
        Assertions.assertThat(throwable).isNotNull();
        Assertions.assertThat(throwable)
                .isInstanceOf(LHWfSpecBuilderException.class)
                .hasMessage("Thread my-thread already exists");
    }

    @Test
    void shouldCompileAWorkflowWhenAParentVarIsAssignedFromChildNestedThreads() {
        Workflow wf = Workflow.newWorkflow(
                "TestWorkflow",
                grandParentWfThread -> {
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
                },
                new LHConfig());

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
