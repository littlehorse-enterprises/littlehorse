package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class LHInlineStructDefTypeTest {

    @Getter
    static class SimpleAddress {
        public String street;
        public int zipCode;
    }

    @LHStructDef("annotated")
    @Getter
    static class AnnotatedPojo {
        public String value;
    }

    @Test
    public void getDefinedTypeCaseReturnsInlineStructDef() {
        LHInlineStructDefType type = new LHInlineStructDefType(SimpleAddress.class);
        assertThat(type.getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_STRUCT_DEF);
    }

    @Test
    public void getTypeDefinitionEmitsInlineStructDef() {
        LHInlineStructDefType type = new LHInlineStructDefType(SimpleAddress.class);
        TypeDefinition td = type.getTypeDefinition();
        assertThat(td.getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_STRUCT_DEF);
    }

    @Test
    public void getTypeDefinitionContainsPrimitiveFields() {
        LHInlineStructDefType type = new LHInlineStructDefType(SimpleAddress.class, LHTypeAdapterRegistry.empty());
        InlineStructDef inlineStructDef = type.getTypeDefinition().getInlineStructDef();

        assertThat(inlineStructDef.getFieldsMap()).containsKey("street");
        assertThat(inlineStructDef.getFieldsMap()).containsKey("zipCode");

        // street → STR, zipCode → INT
        assertThat(inlineStructDef.getFieldsMap().get("street").getFieldType().getPrimitiveType())
                .isEqualTo(VariableType.STR);
        assertThat(inlineStructDef.getFieldsMap().get("zipCode").getFieldType().getPrimitiveType())
                .isEqualTo(VariableType.INT);
    }

    @Test
    public void doesNotRequireLHStructDefAnnotation() {
        // SimpleAddress has no @LHStructDef — construction must succeed
        assertThat(new LHInlineStructDefType(SimpleAddress.class)).isNotNull();
    }

    @Test
    public void worksWithAnnotatedPojoToo() {
        // @LHStructDef-annotated class is also accepted; still emits INLINE_STRUCT_DEF
        LHInlineStructDefType type = new LHInlineStructDefType(AnnotatedPojo.class);
        assertThat(type.getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThat(type.getTypeDefinition().getInlineStructDef().getFieldsMap()).containsKey("value");
    }

    /**
     * Verifies that the INLINE_STRUCT_DEF case in LHTypeConstraintValidator recurses into fields
     * and rejects JSON_OBJ / JSON_ARR primitive types.
     */
    @Test
    public void ensureNoJsonPrimitiveTypesThrowsForInlineStructDefContainingJsonObjField() {
        // Build a TypeDefinition with inline_struct_def containing a JSON_OBJ field
        TypeDefinition jsonObjField = TypeDefinition.newBuilder()
                .setPrimitiveType(VariableType.JSON_OBJ)
                .build();

        StructFieldDef fieldDef =
                StructFieldDef.newBuilder().setFieldType(jsonObjField).build();

        TypeDefinition inlineStructTypeDef = TypeDefinition.newBuilder()
                .setInlineStructDef(io.littlehorse.sdk.common.proto.InlineStructDef.newBuilder()
                        .putFields("badField", fieldDef))
                .build();

        assertThatThrownBy(() -> LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(inlineStructTypeDef))
                .isInstanceOf(ForbiddenJsonTypeException.class);
    }

    @Test
    public void ensureNoJsonPrimitiveTypesThrowsForInlineStructDefContainingJsonArrField() {
        TypeDefinition jsonArrField = TypeDefinition.newBuilder()
                .setPrimitiveType(VariableType.JSON_ARR)
                .build();

        StructFieldDef fieldDef =
                StructFieldDef.newBuilder().setFieldType(jsonArrField).build();

        TypeDefinition inlineStructTypeDef = TypeDefinition.newBuilder()
                .setInlineStructDef(io.littlehorse.sdk.common.proto.InlineStructDef.newBuilder()
                        .putFields("badField", fieldDef))
                .build();

        assertThatThrownBy(() -> LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(inlineStructTypeDef))
                .isInstanceOf(ForbiddenJsonTypeException.class);
    }

    @Test
    public void ensureNoJsonPrimitiveTypesDoesNotThrowForPrimitiveOnlyInlineStructDef()
            throws ForbiddenJsonTypeException {
        TypeDefinition strField =
                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR).build();

        StructFieldDef fieldDef =
                StructFieldDef.newBuilder().setFieldType(strField).build();

        TypeDefinition inlineStructTypeDef = TypeDefinition.newBuilder()
                .setInlineStructDef(io.littlehorse.sdk.common.proto.InlineStructDef.newBuilder()
                        .putFields("name", fieldDef))
                .build();

        // Must not throw
        LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(inlineStructTypeDef);
    }
}
