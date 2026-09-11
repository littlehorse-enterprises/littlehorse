package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.exception.LHWfSpecBuilderException;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.Library;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class WfRunVariableImplTest {

    public static class InlineAddress {
        private String street;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }
    }

    ThreadFunc threadFunction = new ThreadFunc() {
        @Override
        public void threadFunction(WorkflowThread thread) {}
    };

    @Test
    void validateVariableAllowJsonPath() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", threadFunction);
        WorkflowThreadImpl wfThread = new WorkflowThreadImpl("wf-thread", workflow, threadFunction);
        WfRunVariableImpl variable = WfRunVariableImpl.createPrimitiveVar("my-var", VariableType.STR, wfThread);

        LHWfSpecBuilderException e = assertThrows(LHWfSpecBuilderException.class, () -> variable.jsonPath("&.myPath"));
        assertThat(e.getMessage()).isEqualTo("JsonPath not allowed in a STR variable");
    }

    @Test
    void shouldThrowAnExceptionWhenVariableHaveNullParentThread() {
        NullPointerException e = assertThrows(
                NullPointerException.class,
                () -> WfRunVariableImpl.createPrimitiveVar("my-var", VariableType.STR, null));

        assertEquals("Parent thread cannot be null.", e.getMessage());
    }

    @Test
    void shouldSerializeDefaultArrayAsNativeLHArray() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            io.littlehorse.sdk.wfsdk.WfRunVariable arrVar = thread.declareArray("my-array", Long.class);
            arrVar.withDefault(new Long[] {1L, 2L});
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        VariableValue def = varDef.getVarDef().getDefaultValue();

        assertThat(def.getValueCase()).isEqualTo(VariableValue.ValueCase.ARRAY);
        assertThat(def.getArray().getItemsCount()).isEqualTo(2);
        assertThat(def.getArray().getItems(0).getInt()).isEqualTo(1L);
        assertThat(def.getArray().getItems(1).getInt()).isEqualTo(2L);
    }

    @Test
    void shouldSerializeSizeAsUnaryVariableAssignmentSource() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            var inputArray = thread.declareArray("input-array", Long.class);
            var arraySize = thread.declareInt("array-size");
            arraySize.assign(inputArray.size());
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();

        VariableMutation sizeMutation = null;
        for (Node node : pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName())
                .getNodesMap()
                .values()) {
            for (Edge edge : node.getOutgoingEdgesList()) {
                for (VariableMutation mutation : edge.getVariableMutationsList()) {
                    if (mutation.getLhsName().equals("array-size")) {
                        sizeMutation = mutation;
                    }
                }
            }
        }

        assertThat(sizeMutation).isNotNull();
        assertThat(sizeMutation.getRhsAssignment().hasSizeOf()).isTrue();
        assertThat(sizeMutation.getRhsAssignment().getSizeOf().getOperand().getVariableName())
                .isEqualTo("input-array");
    }

    @Test
    void shouldRejectPutOnNonMapVariable() {
        assertThrows(LHWfSpecBuilderException.class, () -> new WorkflowImpl("my-workflow", thread -> {
                    thread.declareStr("not-a-map").put("key", 42L);
                })
                .compileWorkflow());
    }

    @Test
    void shouldDeclareMapWithCorrectTypeDefinition() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            thread.declareMap("my-map", String.class, Long.class);
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        TypeDefinition typeDef = varDef.getVarDef().getTypeDef();

        assertThat(typeDef.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_MAP_DEF);
        InlineMapDef mapDef = typeDef.getInlineMapDef();
        assertThat(mapDef.getKeyType().getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(mapDef.getValueType().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    void shouldDeclareInlineStructWithEmbeddedSchema() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", thread -> {
            thread.declareInlineStruct("address", InlineAddress.class);
        });

        PutWfSpecRequest request = workflow.compileWorkflow();
        TypeDefinition typeDef = request.getThreadSpecsOrThrow(request.getEntrypointThreadName())
                .getVariableDefs(0)
                .getVarDef()
                .getTypeDef();

        assertThat(typeDef.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThat(typeDef.getInlineStructDef()
                        .getFieldsOrThrow("street")
                        .getFieldType()
                        .getPrimitiveType())
                .isEqualTo(VariableType.STR);
    }

    @Test
    void shouldSerializeInlineStructDefaultWithoutStructDefId() {
        InlineAddress address = new InlineAddress();
        address.setStreet("Main St");
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", thread -> {
            thread.declareInlineStruct("address", InlineAddress.class).withDefault(address);
        });

        VariableValue defaultValue = workflow.compileWorkflow()
                .getThreadSpecsOrThrow("entrypoint")
                .getVariableDefs(0)
                .getVarDef()
                .getDefaultValue();

        assertThat(defaultValue.getValueCase()).isEqualTo(VariableValue.ValueCase.STRUCT);
        assertThat(defaultValue.getStruct().hasStructDefId()).isFalse();
        assertThat(defaultValue
                        .getStruct()
                        .getStruct()
                        .getFieldsOrThrow("street")
                        .getValue()
                        .getStr())
                .isEqualTo("Main St");
    }

    @Test
    void shouldSerializeNamedStructDefaultWithStructDefId() {
        Library library = new Library();
        library.setName("Central");
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", thread -> {
            thread.declareStruct("library", Library.class).withDefault(library);
        });

        VariableValue defaultValue = workflow.compileWorkflow()
                .getThreadSpecsOrThrow("entrypoint")
                .getVariableDefs(0)
                .getVarDef()
                .getDefaultValue();

        assertThat(defaultValue.getValueCase()).isEqualTo(VariableValue.ValueCase.STRUCT);
        assertThat(defaultValue.getStruct().getStructDefId().getName()).isEqualTo("library");
        assertThat(defaultValue
                        .getStruct()
                        .getStruct()
                        .getFieldsOrThrow("name")
                        .getValue()
                        .getStr())
                .isEqualTo("Central");
    }

    @Test
    void shouldSerializeInlineStructPayloadAsNamedStructDefault() {
        InlineStruct library = InlineStruct.newBuilder()
                .putFields(
                        "name",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder().setStr("Central"))
                                .build())
                .build();
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", thread -> {
            thread.declareStruct("library", "library").withDefault(library);
        });

        VariableValue defaultValue = workflow.compileWorkflow()
                .getThreadSpecsOrThrow("entrypoint")
                .getVariableDefs(0)
                .getVarDef()
                .getDefaultValue();

        assertThat(defaultValue.getValueCase()).isEqualTo(VariableValue.ValueCase.STRUCT);
        assertThat(defaultValue.getStruct().getStructDefId().getName()).isEqualTo("library");
    }

    @Test
    void shouldCreateNestedPathOnNamedStructWithoutDefaultValue() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", thread -> {
            thread.declareStruct("library", Library.class)
                    .required()
                    .get("books")
                    .get(0);
        });

        assertDoesNotThrow(() -> {
            workflow.compileWorkflow();
        });
    }

    @Test
    void shouldDeclareAndDefaultNativeContainersWithInlinePojoMembers() {
        InlineAddress address = new InlineAddress();
        address.setStreet("Main St");
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", thread -> {
            thread.declareArray("addresses", InlineAddress.class).withDefault(new InlineAddress[] {address});
            thread.declareMap("address-map", String.class, InlineAddress.class).withDefault(Map.of("home", address));
        });

        PutWfSpecRequest request = workflow.compileWorkflow();
        ThreadVarDef arrayVar = request.getThreadSpecsOrThrow("entrypoint").getVariableDefs(0);
        ThreadVarDef mapVar = request.getThreadSpecsOrThrow("entrypoint").getVariableDefs(1);

        assertThat(arrayVar.getVarDef()
                        .getTypeDef()
                        .getInlineArrayDef()
                        .getArrayType()
                        .getDefinedTypeCase())
                .isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThat(arrayVar.getVarDef().getDefaultValue().getArray().getItems(0).getValueCase())
                .isEqualTo(VariableValue.ValueCase.STRUCT);
        assertThat(mapVar.getVarDef()
                        .getTypeDef()
                        .getInlineMapDef()
                        .getValueType()
                        .getDefinedTypeCase())
                .isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThat(mapVar.getVarDef()
                        .getDefaultValue()
                        .getMap()
                        .getEntries(0)
                        .getValue()
                        .getValueCase())
                .isEqualTo(VariableValue.ValueCase.STRUCT);
    }

    @Test
    void shouldCompileMapPutAsPathBasedAssign() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            var map = thread.declareMap("my-map", String.class, Long.class);
            var key = thread.declareStr("key");
            map.put(key, 42L);
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("my-map"))
                        .findFirst()
                        .orElseThrow();

        assertThat(mutation.getOperation()).isEqualTo(VariableMutationType.ASSIGN);
        assertThat(mutation.hasLhsLhPath()).isTrue();
        assertThat(mutation.getLhsLhPath().getPathList()).singleElement().satisfies(selector -> {
            assertThat(selector.hasDynamic()).isTrue();
            assertThat(selector.getDynamic().getVariableName()).isEqualTo("key");
        });
        assertThat(mutation.getRhsAssignment().getLiteralValue().getInt()).isEqualTo(42L);
    }

    @Test
    void shouldSerializeDefaultMapAsNativeLHMap() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            io.littlehorse.sdk.wfsdk.WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            mapVar.withDefault(Map.of("x", 1L, "y", 2L));
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        VariableValue def = varDef.getVarDef().getDefaultValue();

        assertThat(def.getValueCase()).isEqualTo(VariableValue.ValueCase.MAP);
        assertThat(def.getMap().getEntriesCount()).isEqualTo(2);
    }

    @Test
    void shouldRejectNonPrimitiveMapKeyType() {
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkflowImpl("my-workflow", thread -> {
                        // String[] is not a primitive key type
                        thread.declareMap("bad-map", String[].class, Long.class);
                    })
                    .compileWorkflow();
        });
    }

    @Test
    void shouldDeclareMapWithArrayValueType() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            thread.declareMap("my-map", String.class, Long[].class);
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        TypeDefinition typeDef = varDef.getVarDef().getTypeDef();

        assertThat(typeDef.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_MAP_DEF);
        InlineMapDef mapDef = typeDef.getInlineMapDef();
        assertThat(mapDef.getKeyType().getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(mapDef.getValueType().getDefinedTypeCase())
                .isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_ARRAY_DEF);
        assertThat(mapDef.getValueType().getInlineArrayDef().getArrayType().getPrimitiveType())
                .isEqualTo(VariableType.INT);
    }

    @Test
    void shouldDeclareMapWithStructValueType() {
        WorkflowImpl wf = new WorkflowImpl("my-workflow", thread -> {
            thread.declareMap("my-map", String.class, Library.class);
        });

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        TypeDefinition typeDef = varDef.getVarDef().getTypeDef();

        assertThat(typeDef.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_MAP_DEF);
        InlineMapDef mapDef = typeDef.getInlineMapDef();
        assertThat(mapDef.getKeyType().getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(mapDef.getValueType().getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.STRUCT_DEF_ID);
        assertThat(mapDef.getValueType().getStructDefId().getName()).isEqualTo("library");
    }
}
