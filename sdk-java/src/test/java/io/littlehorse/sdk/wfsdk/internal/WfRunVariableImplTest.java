package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.Library;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class WfRunVariableImplTest {

    ThreadFunc threadFunction = new ThreadFunc() {
        @Override
        public void threadFunction(WorkflowThread thread) {}
    };

    @Test
    void validateVariableAllowJsonPath() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", threadFunction);
        WorkflowThreadImpl wfThread = new WorkflowThreadImpl("wf-thread", workflow, threadFunction);
        WfRunVariableImpl variable = WfRunVariableImpl.createPrimitiveVar("my-var", VariableType.STR, wfThread);

        LHMisconfigurationException e =
                assertThrows(LHMisconfigurationException.class, () -> variable.jsonPath("&.myPath"));
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
