package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.EnumMemberDef;
import io.littlehorse.sdk.common.proto.InlineEnumDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHEnumValueDef;
import org.junit.jupiter.api.Test;

public class LHInlineEnumTypeTest {

    enum Direction {
        NORTH,
        @LHEnumValueDef(10)
        EAST,
        SOUTH,
        WEST {
            @Override
            public String toString() {
                return "westward";
            }
        }
    }

    enum Descending {
        @LHEnumValueDef(5)
        HIGH,
        @LHEnumValueDef(2)
        LOW,
        LOWER
    }

    enum Negative {
        @LHEnumValueDef(-1)
        INVALID
    }

    enum Duplicate {
        FIRST,
        @LHEnumValueDef(0)
        SECOND
    }

    enum Overflow {
        @LHEnumValueDef(Integer.MAX_VALUE)
        LAST,
        OVERFLOW
    }

    enum Empty {}

    @Test
    void shouldResolveBackingValuesInDeclarationOrder() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());

        TypeDefinition actualType = type.getTypeDefinition();
        TypeDefinition expectedType = TypeDefinition.newBuilder()
                .setInlineEnumDef(InlineEnumDef.newBuilder()
                        .addMembers(EnumMemberDef.newBuilder().setName("NORTH").setBackingValue(0))
                        .addMembers(EnumMemberDef.newBuilder().setName("EAST").setBackingValue(10))
                        .addMembers(EnumMemberDef.newBuilder().setName("SOUTH").setBackingValue(11))
                        .addMembers(EnumMemberDef.newBuilder().setName("WEST").setBackingValue(12)))
                .build();

        assertThat(actualType).isEqualTo(expectedType);
    }

    @Test
    void shouldContinueFromExplicitBackingValueLowerThanPreviousMember() {
        LHInlineEnumType type = new LHInlineEnumType(Descending.class, LHTypeAdapterRegistry.empty());

        assertThat(type.getTypeDefinition().getInlineEnumDef().getMembersList())
                .extracting(EnumMemberDef::getBackingValue)
                .containsExactly(5, 2, 3);
    }

    @Test
    void shouldRejectNegativeBackingValue() {
        assertThatThrownBy(() -> new LHInlineEnumType(Negative.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative int32 backing value");
    }

    @Test
    void shouldRejectDuplicateBackingValue() {
        assertThatThrownBy(() -> new LHInlineEnumType(Duplicate.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate enum backing value");
    }

    @Test
    void shouldRejectImplicitBackingValueOverflow() {
        assertThatThrownBy(() -> new LHInlineEnumType(Overflow.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative int32 backing value");
    }

    @Test
    void shouldRejectEnumWithoutMembers() {
        assertThatThrownBy(() -> new LHInlineEnumType(Empty.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one member");
    }

    @Test
    void shouldRejectNonEnumClass() {
        assertThatThrownBy(() -> new LHInlineEnumType(String.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Java enum class");
    }

    @Test
    void shouldSerializeConstantSpecificEnumByNameRatherThanToString() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());

        VariableValue value = type.toVariableValue(Direction.WEST);

        assertThat(value.getValueCase()).isEqualTo(VariableValue.ValueCase.ENUM);
        assertThat(value.getEnum().getName()).isEqualTo("WEST");
    }

    @Test
    void shouldSerializeNullAsUnsetValue() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());

        VariableValue value = type.toVariableValue(null);

        assertThat(value.getValueCase()).isEqualTo(VariableValue.ValueCase.VALUE_NOT_SET);
    }

    @Test
    void shouldDeserializeConstantSpecificEnumByName() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());
        VariableValue value = type.memberNameToVariableValue("WEST");

        assertThat(type.fromVariableValue(value)).isSameAs(Direction.WEST);
    }

    @Test
    void shouldDeserializeUnsetValueAsNull() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());

        assertThat(type.fromVariableValue(VariableValue.getDefaultInstance())).isNull();
    }

    @Test
    void shouldRejectMemberNameWithIncorrectCase() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());

        assertThatThrownBy(() -> type.memberNameToVariableValue("north"))
                .isInstanceOf(LHSerdeException.class)
                .hasMessageContaining("Unknown enum member");
    }

    @Test
    void shouldRejectUnknownMemberWhenDeserializing() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());
        VariableValue value =
                new LHInlineEnumType(Descending.class, LHTypeAdapterRegistry.empty()).memberNameToVariableValue("HIGH");

        assertThatThrownBy(() -> type.fromVariableValue(value))
                .isInstanceOf(LHSerdeException.class)
                .hasMessageContaining("Unknown enum member HIGH");
    }

    @Test
    void shouldRejectStringValueForNativeEnum() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());
        VariableValue value = VariableValue.newBuilder().setStr("NORTH").build();

        assertThatThrownBy(() -> type.fromVariableValue(value))
                .isInstanceOf(LHSerdeException.class)
                .hasMessageContaining("Expected an ENUM");
    }

    @Test
    void shouldAcceptMemberOfDifferentEnumClassWithMatchingName() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());

        VariableValue value = type.toVariableValue(io.littlehorse.sdk.testutils.Direction.EAST);

        assertThat(value.getEnum().getName()).isEqualTo("EAST");
    }

    @Test
    void shouldRejectMemberOfDifferentEnumClassWithUnknownName() {
        LHInlineEnumType type = new LHInlineEnumType(Direction.class, LHTypeAdapterRegistry.empty());

        assertThatThrownBy(() -> type.toVariableValue(Duplicate.FIRST))
                .isInstanceOf(LHSerdeException.class)
                .hasMessageContaining("Unknown enum member FIRST");
    }

    @Test
    void shouldNotOptInBasedOnBackingValueAnnotationsAlone() {
        TypeDefinition typeDef = LHClassType.fromJavaClass(Direction.class, LHTypeAdapterRegistry.empty())
                .getTypeDefinition();

        assertThat(typeDef.getPrimitiveType()).isEqualTo(VariableType.JSON_OBJ);
    }
}
