package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.LHWfSpecBuilderException;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.EnumMemberDef;
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
import io.littlehorse.sdk.testutils.Direction;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.Library;
import io.littlehorse.sdk.worker.LHEnumValueDef;
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

    enum BadDirection {
        @LHEnumValueDef(1)
        NORTH,
        @LHEnumValueDef(1)
        EAST
    }

    ThreadFunc threadFunction = new ThreadFunc() {
        @Override
        public void threadFunction(WorkflowThread thread) {}
    };

    @Test
    void validateVariableAllowJsonPath() {
        WorkflowImpl workflow = (WorkflowImpl) Workflow.newWorkflow("my-workflow", threadFunction, new LHConfig());
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
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    io.littlehorse.sdk.wfsdk.WfRunVariable arrVar = thread.declareArray("my-array", Long.class);
                    arrVar.withDefault(new Long[] {1L, 2L});
                },
                new LHConfig());

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
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    var inputArray = thread.declareArray("input-array", Long.class);
                    var arraySize = thread.declareInt("array-size");
                    arraySize.assign(inputArray.size());
                },
                new LHConfig());

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
        assertThrows(LHWfSpecBuilderException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareStr("not-a-map").put("key", 42L);
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldDeclareMapWithCorrectTypeDefinition() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareMap("my-map", String.class, Long.class);
                },
                new LHConfig());

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
        Workflow workflow = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineStruct("address", InlineAddress.class);
                },
                new LHConfig());

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
        Workflow workflow = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineStruct("address", InlineAddress.class).withDefault(address);
                },
                new LHConfig());

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
        Workflow workflow = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareStruct("library", Library.class).withDefault(library);
                },
                new LHConfig());

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
        Workflow workflow = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareStruct("library", "library").withDefault(library);
                },
                new LHConfig());

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
        Workflow workflow = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareStruct("library", Library.class)
                            .required()
                            .get("books")
                            .get(0);
                },
                new LHConfig());

        assertDoesNotThrow(() -> {
            workflow.compileWorkflow();
        });
    }

    @Test
    void shouldDeclareAndDefaultNativeContainersWithInlinePojoMembers() {
        InlineAddress address = new InlineAddress();
        address.setStreet("Main St");
        Workflow workflow = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareArray("addresses", InlineAddress.class).withDefault(new InlineAddress[] {address});
                    thread.declareMap("address-map", String.class, InlineAddress.class)
                            .withDefault(Map.of("home", address));
                },
                new LHConfig());

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
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    var map = thread.declareMap("my-map", String.class, Long.class);
                    var key = thread.declareStr("key");
                    map.put(key, 42L);
                },
                new LHConfig());

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
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    io.littlehorse.sdk.wfsdk.WfRunVariable mapVar =
                            thread.declareMap("my-map", String.class, Long.class);
                    mapVar.withDefault(Map.of("x", 1L, "y", 2L));
                },
                new LHConfig());

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
            Workflow.newWorkflow(
                            "my-workflow",
                            thread -> {
                                // String[] is not a primitive key type
                                thread.declareMap("bad-map", String[].class, Long.class);
                            },
                            new LHConfig())
                    .compileWorkflow();
        });
    }

    @Test
    void shouldDeclareMapWithArrayValueType() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareMap("my-map", String.class, Long[].class);
                },
                new LHConfig());

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
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareMap("my-map", String.class, Library.class);
                },
                new LHConfig());

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

    @Test
    void shouldDeclareInlineEnumWithEmbeddedSchema() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineEnum("direction", Direction.class);
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        TypeDefinition typeDef = varDef.getVarDef().getTypeDef();

        assertThat(typeDef.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_ENUM_DEF);
        assertThat(typeDef.getInlineEnumDef().getMembersList())
                .extracting(EnumMemberDef::getName)
                .containsExactly("NORTH", "EAST", "SOUTH");
        assertThat(typeDef.getInlineEnumDef().getMembersList())
                .extracting(EnumMemberDef::getBackingValue)
                .containsExactly(0, 10, 11);
    }

    @Test
    void shouldRejectInvalidInlineEnumSchema() {
        assertThatThrownBy(() -> Workflow.newWorkflow(
                                "my-workflow",
                                thread -> {
                                    thread.declareInlineEnum("bad-direction", BadDirection.class);
                                },
                                new LHConfig())
                        .compileWorkflow())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate enum backing value: 1");
    }

    @Test
    void shouldSendStructBuilderEnumFieldAsStrLiteral() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareStruct("route", "route")
                            .assign(thread.buildStruct("route").put("direction", Direction.EAST));
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("route"))
                        .findFirst()
                        .orElseThrow();

        VariableValue field = mutation.getRhsAssignment()
                .getStructBuilder()
                .getValue()
                .getFieldsOrThrow("direction")
                .getSimpleValue()
                .getLiteralValue();
        assertThat(field.getValueCase()).isEqualTo(VariableValue.ValueCase.STR);
        assertThat(field.getStr()).isEqualTo("EAST");
    }

    @Test
    void shouldDeclareArrayOfInlineEnum() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow", thread -> thread.declareArray("directions", Direction.class), new LHConfig());

        TypeDefinition typeDef = wf.compileWorkflow()
                .getThreadSpecsOrThrow("entrypoint")
                .getVariableDefs(0)
                .getVarDef()
                .getTypeDef();

        assertThat(typeDef.getInlineArrayDef().getArrayType().getInlineEnumDef().getMembersList())
                .extracting(EnumMemberDef::getName, EnumMemberDef::getBackingValue)
                .containsExactly(tuple("NORTH", 0), tuple("EAST", 10), tuple("SOUTH", 11));
    }

    @Test
    void shouldRejectGetByFieldOnInlineEnum() {
        assertThrows(LHWfSpecBuilderException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareInlineEnum("direction", Direction.class)
                                    .get("field");
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldRejectGetByIndexOnInlineEnum() {
        assertThrows(LHWfSpecBuilderException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareInlineEnum("direction", Direction.class)
                                    .get(0);
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldRejectGetByVariableOnInlineEnum() {
        assertThrows(LHWfSpecBuilderException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            WfRunVariable key = thread.declareStr("key");
                            thread.declareInlineEnum("direction", Direction.class)
                                    .get(key);
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldSerializeNativeEnumDefault() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineEnum("direction", Direction.class).withDefault(Direction.EAST);
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        VariableValue def = varDef.getVarDef().getDefaultValue();

        assertThat(def.getValueCase()).isEqualTo(VariableValue.ValueCase.ENUM);
        assertThat(def.getEnum().getName()).isEqualTo("EAST");
    }

    @Test
    void shouldSerializeEnumMemberNameDefault() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineEnum("direction", Direction.class).withDefault("SOUTH");
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        VariableValue def = varDef.getVarDef().getDefaultValue();

        assertThat(def.getValueCase()).isEqualTo(VariableValue.ValueCase.ENUM);
        assertThat(def.getEnum().getName()).isEqualTo("SOUTH");
    }

    @Test
    void shouldRejectEnumDefaultWithIncorrectCase() {
        assertThrows(IllegalArgumentException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareInlineEnum("direction", Direction.class)
                                    .withDefault("north");
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldRejectEnumDefaultWithUnknownMemberName() {
        assertThrows(IllegalArgumentException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareInlineEnum("direction", Direction.class)
                                    .withDefault("UNKNOWN");
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldRejectNonEnumDefaultOnInlineEnum() {
        assertThrows(IllegalArgumentException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareInlineEnum("direction", Direction.class)
                                    .withDefault(1L);
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldRejectNullDefaultOnInlineEnum() {
        assertThrows(IllegalArgumentException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareInlineEnum("direction", Direction.class)
                                    .withDefault(null);
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldAcceptEnumDefaultFromDifferentEnumClassByName() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineEnum("direction", Direction.class).withDefault(BadDirection.EAST);
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        ThreadVarDef varDef =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getVariableDefs(0);
        VariableValue def = varDef.getVarDef().getDefaultValue();

        assertThat(def.getValueCase()).isEqualTo(VariableValue.ValueCase.ENUM);
        assertThat(def.getEnum().getName()).isEqualTo("EAST");
    }

    @Test
    void shouldAssignEnumMemberAsStrLiteral() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineEnum("direction", Direction.class).assign(Direction.EAST);
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("direction"))
                        .findFirst()
                        .orElseThrow();

        assertThat(mutation.getOperation()).isEqualTo(VariableMutationType.ASSIGN);
        assertThat(mutation.getRhsAssignment().getLiteralValue().getStr()).isEqualTo("EAST");
    }

    @Test
    void shouldAssignEnumMemberNameAsStringLiteral() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareInlineEnum("direction", Direction.class).assign("NORTH");
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("direction"))
                        .findFirst()
                        .orElseThrow();

        assertThat(mutation.getOperation()).isEqualTo(VariableMutationType.ASSIGN);
        assertThat(mutation.getRhsAssignment().getLiteralValue().getStr()).isEqualTo("NORTH");
    }

    @Test
    void shouldKeepLegacyLiteralWhenAssigningEnumMemberToNonEnumVariable() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    thread.declareStr("direction-name").assign(Direction.EAST);
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("direction-name"))
                        .findFirst()
                        .orElseThrow();

        assertThat(mutation.getRhsAssignment().getLiteralValue().getValueCase()).isEqualTo(VariableValue.ValueCase.STR);
        assertThat(mutation.getRhsAssignment().getLiteralValue().getStr()).isEqualTo("EAST");
    }

    @Test
    void shouldSerializeEnumNameAsUnaryVariableAssignmentSource() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    var direction = thread.declareInlineEnum("direction", Direction.class);
                    var directionName = thread.declareStr("direction-name");
                    directionName.assign(direction.name());
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("direction-name"))
                        .findFirst()
                        .orElseThrow();

        assertThat(mutation.getRhsAssignment().hasEnumName()).isTrue();
        assertThat(mutation.getRhsAssignment().getEnumName().getOperand().getVariableName())
                .isEqualTo("direction");
    }

    @Test
    void shouldRejectNameOnNonEnumVariable() {
        assertThrows(LHWfSpecBuilderException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareStr("direction-name").name();
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldRejectNameOfEnumName() {
        assertThrows(LHWfSpecBuilderException.class, () -> Workflow.newWorkflow(
                        "my-workflow",
                        thread -> {
                            thread.declareInlineEnum("direction", Direction.class)
                                    .name()
                                    .name();
                        },
                        new LHConfig())
                .compileWorkflow());
    }

    @Test
    void shouldSerializeEnumNameOfJsonPath() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    var json = thread.declareJsonObj("json");
                    var directionName = thread.declareStr("direction-name");
                    directionName.assign(json.jsonPath("$.direction").name());
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("direction-name"))
                        .findFirst()
                        .orElseThrow();

        assertThat(mutation.getRhsAssignment().getEnumName().getOperand().getVariableName())
                .isEqualTo("json");
        assertThat(mutation.getRhsAssignment().getEnumName().getOperand().getJsonPath())
                .isEqualTo("$.direction");
    }

    @Test
    void shouldSerializeEnumNameOfTaskOutput() {
        Workflow wf = Workflow.newWorkflow(
                "my-workflow",
                thread -> {
                    var directionName = thread.declareStr("direction-name");
                    directionName.assign(thread.execute("enum-task").name());
                },
                new LHConfig());

        PutWfSpecRequest pwf = wf.compileWorkflow();
        VariableMutation mutation =
                pwf.getThreadSpecsOrThrow(pwf.getEntrypointThreadName()).getNodesMap().values().stream()
                        .flatMap(node -> node.getOutgoingEdgesList().stream())
                        .flatMap(edge -> edge.getVariableMutationsList().stream())
                        .filter(candidate -> candidate.getLhsName().equals("direction-name"))
                        .findFirst()
                        .orElseThrow();

        assertThat(mutation.getRhsAssignment().hasEnumName()).isTrue();
        assertThat(mutation.getRhsAssignment()
                        .getEnumName()
                        .getOperand()
                        .getNodeOutput()
                        .getNodeName())
                .isEqualTo("1-enum-task-TASK");
    }

    @Test
    void shouldRejectSearchableEnumVariable() {
        assertThatThrownBy(() -> Workflow.newWorkflow(
                                "my-workflow",
                                thread -> {
                                    thread.declareInlineEnum("direction", Direction.class)
                                            .searchable();
                                },
                                new LHConfig())
                        .compileWorkflow())
                .isInstanceOf(LHWfSpecBuilderException.class)
                .hasMessage("Cannot make enum variable direction searchable");
    }
}
