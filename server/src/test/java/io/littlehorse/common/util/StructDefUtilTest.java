package io.littlehorse.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

public class StructDefUtilTest {

    @Test
    public void testTwoEqualStructDefsAreEqual() {
        StructDef structDef1 = makeCarStructDef();
        StructDef structDef2 = makeCarStructDef();

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertThat(StructDefUtil.equals(structDefModel1, structDefModel2)).isTrue();
    }

    @Test
    public void testDifferentStructDefsAreNotEqual() {
        StructDef structDef1 = makeCarStructDef();
        StructDef structDef2 = makeCarStructDef(makeStructField("horsepower", VariableType.INT));

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertThat(StructDefUtil.equals(structDefModel1, structDefModel2)).isFalse();
    }

    @Test
    public void testNoBreakingChangesWhenStructDefsAreEqual() {
        StructDef structDef1 = makeCarStructDef();
        StructDef structDef2 = makeCarStructDef();

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertThat(StructDefUtil.getBreakingChanges(structDefModel1, structDefModel2))
                .isEmpty();
    }

    @Test
    public void testBreakingChangesWhenRequiredFieldIsAdded() {
        StructDef oldStructDef = makeCarStructDef();
        StructDef newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT));

        StructDefModel oldStructDefModel = StructDefModel.fromProto(oldStructDef, null);
        StructDefModel newStructDefModel = StructDefModel.fromProto(newStructDef, null);

        assertThat(StructDefUtil.getBreakingChanges(newStructDefModel, oldStructDefModel)
                        .size())
                .isEqualTo(1);
    }

    @Test
    public void testNoBreakingChangesWhenRequiredFieldIsAddedWithDefault() {
        StructDef oldStructDef = makeCarStructDef();
        StructDef newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, 5));

        StructDefModel oldStructDefModel = StructDefModel.fromProto(oldStructDef, null);
        StructDefModel newStructDefModel = StructDefModel.fromProto(newStructDef, null);

        assertThat(StructDefUtil.getBreakingChanges(newStructDefModel, oldStructDefModel)
                        .size())
                .isEqualTo(0);
    }

    @Test
    public void testBreakingChangesWhenRequiredFieldIsRemoved() {
        StructDef oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, 5));
        StructDef newStructDef = makeCarStructDef();

        StructDefModel oldStructDefModel = StructDefModel.fromProto(oldStructDef, null);
        StructDefModel newStructDefModel = StructDefModel.fromProto(newStructDef, null);

        assertThat(StructDefUtil.getBreakingChanges(newStructDefModel, oldStructDefModel)
                        .size())
                .isEqualTo(1);
    }

    @Test
    public void testNoBreakingChangesWhenOptionalFieldIsAdded() {
        StructDef oldStructDef = makeCarStructDef();
        StructDef newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, true));

        StructDefModel oldStructDefModel = StructDefModel.fromProto(oldStructDef, null);
        StructDefModel newStructDefModel = StructDefModel.fromProto(newStructDef, null);

        assertThat(StructDefUtil.getBreakingChanges(newStructDefModel, oldStructDefModel)
                        .size())
                .isEqualTo(0);
    }

    @Test
    public void testStructDefsAreCompatibleWhenOptionalFieldIsRemoved() {
        StructDef oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, true));
        StructDef newStructDef = makeCarStructDef();

        StructDefModel oldStructDefModel = StructDefModel.fromProto(oldStructDef, null);
        StructDefModel newStructDefModel = StructDefModel.fromProto(newStructDef, null);

        assertThat(StructDefUtil.getBreakingChanges(newStructDefModel, oldStructDefModel)
                        .size())
                .isEqualTo(0);
    }

    @Test
    public void testBreakingChangesWhenRequiredFieldTypeChanges() {
        StructDef oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT));
        StructDef newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.BOOL));

        StructDefModel oldStructDefModel = StructDefModel.fromProto(oldStructDef, null);
        StructDefModel newStructDefModel = StructDefModel.fromProto(newStructDef, null);

        assertThat(StructDefUtil.getBreakingChanges(newStructDefModel, oldStructDefModel)
                        .size())
                .isEqualTo(1);
    }

    @Test
    public void testBreakingChangesWhenRemovingDefaultFromRequiredField() {
        StructDef oldStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT, 5));
        StructDef newStructDef = makeCarStructDef(makeStructField("horsepower", VariableType.INT));

        StructDefModel oldStructDefModel = StructDefModel.fromProto(oldStructDef, null);
        StructDefModel newStructDefModel = StructDefModel.fromProto(newStructDef, null);

        assertThat(StructDefUtil.getBreakingChanges(newStructDefModel, oldStructDefModel)
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
     * A helper method for making StructFieldDefs that are optional
     *
     * @param name The name of the StructFieldDef
     * @param type The field type of the StructFieldDef
     * @param isOptional The optional configuration for the StructFieldDef
     * @return An Entry where the key is the name of the field and the value is the StructFieldDef
     */
    private static Entry<String, StructFieldDef> makeStructField(String name, VariableType type, boolean isOptional) {
        return Map.entry(
                name,
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(type))
                        .setOptional(isOptional)
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
     * @return a Car StructDef
     */
    private static StructDef makeCarStructDef() {
        return makeCarStructDef(Map.of());
    }

    /**
     * @return a Car StructDef
     */
    private static StructDef makeCarStructDef(Entry<String, StructFieldDef> extraField) {
        return makeCarStructDef(Map.ofEntries(extraField));
    }

    /**
     * @param extraFields A set of StructDef fields denoted by their String name and StructFieldDef value
     * @return a Car StructDef with additional StructFieldDefs
     */
    private static StructDef makeCarStructDef(Map<String, StructFieldDef> extraFields) {
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

        return StructDef.newBuilder()
                .setId(StructDefId.newBuilder().setName("car"))
                .setStructDef(structFields.build())
                .build();
    }
}
