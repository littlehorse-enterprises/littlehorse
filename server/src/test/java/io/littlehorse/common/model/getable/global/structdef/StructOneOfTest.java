package io.littlehorse.common.model.getable.global.structdef;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.InlineStructModel;
import io.littlehorse.common.util.InlineStructDefUtil;
import io.littlehorse.sdk.common.proto.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StructOneOfTest {
    private StructFieldDef.Builder field() {
        return StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR));
    }

    private InlineStructDefModel definition(Map<String, StructFieldDef> fields) {
        return InlineStructDefModel.fromProto(
                InlineStructDef.newBuilder().putAllFields(fields).build(), null);
    }

    private InlineStructModel value(Map<String, VariableValue> fields) {
        InlineStruct.Builder result = InlineStruct.newBuilder();
        fields.forEach((name, value) ->
                result.putFields(name, StructField.newBuilder().setValue(value).build()));
        return LHSerializable.fromProto(result.build(), InlineStructModel.class, null);
    }

    @Test
    void acceptsEmptyAndSingleSelectionsButRejectsNullAndMultipleSelections() throws Exception {
        var def = definition(Map.of(
                "card",
                field().setOneofGroup("method").build(),
                "bank",
                field().setOneofGroup("method").build()));
        def.validate(null);
        def.validateAgainstSuperset(value(Map.of()), null);
        def.validateAgainstSuperset(
                value(Map.of("card", VariableValue.newBuilder().setStr("").build())), null);
        assertThatThrownBy(() ->
                        def.validateAgainstSuperset(value(Map.of("card", VariableValue.getDefaultInstance())), null))
                .isInstanceOf(StructValidationException.class)
                .hasMessageContaining("not nullable");
        var selected = VariableValue.newBuilder().setStr("selected").build();
        assertThatThrownBy(() -> def.validateAgainstSuperset(value(Map.of("card", selected, "bank", selected)), null))
                .isInstanceOf(StructValidationException.class)
                .hasMessageContaining("multiple selections");
        assertThat(def.toProto().build().getFieldsOrThrow("card").getOneofGroup())
                .isEqualTo("method");
    }

    @Test
    void rejectsInvalidAlternativeDeclarations() {
        for (var field : new StructFieldDef[] {
            field().setOneofGroup("").build(),
            field().setOneofGroup("Bad").build(),
            field().setOneofGroup("method").setIsNullable(true).build(),
            field().setOneofGroup("method")
                    .setDefaultValue(VariableValue.newBuilder().setStr("x"))
                    .build()
        }) {
            assertThatThrownBy(() -> definition(Map.of("card", field)).validate(null))
                    .isInstanceOf(StructDefValidationException.class);
        }
    }

    @Test
    void evolvesOnlyWithoutInvalidatingStoredSelections() {
        var alternative = field().setOneofGroup("method").build();
        var optional = field().setDefaultValue(VariableValue.newBuilder().setStr("default"))
                .build();
        var old = definition(Map.of("card", alternative));
        assertThat(breaking(definition(Map.of("card", alternative, "bank", alternative)), old))
                .isEmpty();
        assertThat(breaking(definition(Map.of("card", alternative)), definition(Map.of("card", optional))))
                .isEmpty();
        assertThat(breaking(
                        definition(Map.of("card", alternative)),
                        definition(Map.of("card", field().setIsNullable(true).build()))))
                .contains("card");
        assertThat(breaking(
                        definition(Map.of("card", alternative, "bank", alternative)),
                        definition(Map.of("card", alternative, "bank", optional))))
                .contains("bank");
        assertThat(breaking(
                        definition(Map.of("card", alternative, "bank", alternative)),
                        definition(Map.of("card", optional, "bank", optional))))
                .contains("card", "bank");
        assertThat(breaking(definition(Map.of("card", optional)), old)).contains("card");
        assertThat(breaking(
                        definition(Map.of("card", field().setOneofGroup("other").build())), old))
                .contains("card");
        assertThat(breaking(definition(Map.of()), old)).contains("card");
    }

    @Test
    void noSchemaUpdatesRejectsRemovalOfAnAlternative() {
        var old = definition(Map.of("card", field().setOneofGroup("method").build()));
        assertThat(InlineStructDefUtil.getIncompatibleFields(
                        StructDefCompatibilityType.NO_SCHEMA_UPDATES, definition(Map.of()), old))
                .contains("card");
    }

    private java.util.Set<String> breaking(InlineStructDefModel next, InlineStructDefModel previous) {
        return InlineStructDefUtil.getIncompatibleFields(
                StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, next, previous);
    }
}
