package io.littlehorse.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

public class InlineStructDefUtilTest {

    @Test
    public void testTwoEqualStructDefsAreEqual() {
        InlineStructDefModel structDef1 = makeCarStructDef();
        InlineStructDefModel structDef2 = makeCarStructDef();

        assertThat(InlineStructDefUtil.equals(structDef1, structDef2)).isTrue();
    }

    @Test
    public void testDifferentStructDefsAreNotEqual() {
        InlineStructDefModel structDef1 = makeCarStructDef();
        InlineStructDefModel structDef2 = makeCarStructDef(makeStructField("horsepower", VariableType.INT));

        assertThat(InlineStructDefUtil.equals(structDef1, structDef2)).isFalse();
    }

    @Test
    public void testNoBreakingChangesWhenStructDefsAreEqual() {
        InlineStructDefModel structDef1 = makeCarStructDef();
        InlineStructDefModel structDef2 = makeCarStructDef();

        assertThat(InlineStructDefUtil.getIncompatibleFields(
                        StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, structDef1, structDef2))
                .isEmpty();
    }

    @Test
    public void testBreakingChangesWhenRequiredFieldIsAdded() {
        InlineStructDefModel oldStructDef = makeCarStructDef();
        InlineStructDefModel newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT));

        assertThat(InlineStructDefUtil.getIncompatibleFields(
                                StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, newStructDef, oldStructDef)
                        .size())
                .isEqualTo(1);
    }

    @Test
    public void testBreakingChangesWhenRequiredFieldIsRemoved() {
        InlineStructDefModel oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT));
        InlineStructDefModel newStructDef = makeCarStructDef();

        assertThat(InlineStructDefUtil.getIncompatibleFields(
                                StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, newStructDef, oldStructDef)
                        .size())
                .isEqualTo(1);
    }

    @Test
    public void testNoBreakingChangesWhenOptionalFieldIsAdded() {
        InlineStructDefModel oldStructDef = makeCarStructDef();
        InlineStructDefModel newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, 5));

        assertThat(InlineStructDefUtil.getIncompatibleFields(
                                StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, newStructDef, oldStructDef)
                        .size())
                .isEqualTo(0);
    }

    @Test
    public void testStructDefsAreCompatibleWhenOptionalFieldIsRemoved() {
        InlineStructDefModel oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, 10));
        InlineStructDefModel newStructDef = makeCarStructDef();

        assertThat(InlineStructDefUtil.getIncompatibleFields(
                                StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, newStructDef, oldStructDef)
                        .size())
                .isEqualTo(0);
    }

    @Test
    public void testBreakingChangesWhenChangingOptionalFieldToRequired() {
        InlineStructDefModel oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, 5));
        InlineStructDefModel newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT));

        assertThat(InlineStructDefUtil.getIncompatibleFields(
                                StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, newStructDef, oldStructDef)
                        .size())
                .isEqualTo(1);
    }

    @Test
    public void testBreakingChangesWhenRequiredFieldTypeChanges() {
        InlineStructDefModel oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT));
        InlineStructDefModel newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.BOOL));

        assertThat(InlineStructDefUtil.getIncompatibleFields(
                                StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, newStructDef, oldStructDef)
                        .size())
                .isEqualTo(1);
    }

    /**
     * A helper method for making StructFieldDefs that are optional
     *
     * @param name The name of the StructFieldDef
     * @param type The field type of the StructFieldDef
     * @return An Entry where the key is the name of the field and the value is the StructFieldDef
     */
    private static Entry<String, StructFieldDef> makeStructField(String name, VariableType type) {
        return Map.entry(
                name,
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(type))
                        .build());
    }

    /**
     * A helper method for making StructFieldDefs with a default value
     *
     * @param name The name of the StructFieldDef
     * @param type The field type of the StructFieldDef
     * @param defaultValue The default value of the StructFieldDef
     * @return An Entry where the key is the name of the field and the value is the StructFieldDef
     */
    private static Entry<String, StructFieldDef> makeStructField(String name, VariableType type, Object defaultValue) {
        return Map.entry(
                name,
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(type))
                        .setDefaultValue(LHLibUtil.objToVarVal(defaultValue))
                        .build());
    }

    /**
     * @return an InlineStructDef representing a Car
     */
    private static InlineStructDefModel makeCarStructDef() {
        return makeCarStructDef(Map.of());
    }

    /**
     * @return an InlineStructDef representing a Car
     */
    private static InlineStructDefModel makeCarStructDef(Entry<String, StructFieldDef> extraField) {
        return makeCarStructDef(Map.ofEntries(extraField));
    }

    /**
     * @param extraFields A set of StructDef fields denoted by their String name and StructFieldDef value
     * @return an InlineStructDef representing a Car with additional StructFieldDefs
     */
    private static InlineStructDefModel makeCarStructDef(Map<String, StructFieldDef> extraFields) {
        InlineStructDef.Builder structFields = InlineStructDef.newBuilder()
                .putFields(
                        "model",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.STR))
                                .build())
                .putFields(
                        "year",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                                .build())
                .putFields(
                        "is_sold",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.BOOL))
                                .build());

        for (Entry<String, StructFieldDef> extraField : extraFields.entrySet()) {
            structFields.putFields(extraField.getKey(), extraField.getValue());
        }

        return InlineStructDefModel.fromProto(structFields.build(), InlineStructDefModel.class, null);
    }
}
