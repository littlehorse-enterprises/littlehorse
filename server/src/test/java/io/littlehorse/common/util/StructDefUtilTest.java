package io.littlehorse.common.util;

import static org.junit.Assert.assertEquals;

import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;

public class StructDefUtilTest {

    /**
     * @return a Car StructDef
     */
    private StructDef getCarStructDef() {
        return this.getCarStructDef(Set.of());
    }

    /**
     * @param extraFields A set of StructDef fields denoted by their String name and StructFieldDef value
     * @return a Car StructDef with additional StructFieldDefs
     */
    private StructDef getCarStructDef(Set<Entry<String, StructFieldDef>> extraFields) {
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
                        "color",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.STR))
                                .build())
                .putFields(
                        "is_sold",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.BOOL))
                                .build());

        for (Entry<String, StructFieldDef> extraField : extraFields) {
            structFields.putFields(extraField.getKey(), extraField.getValue());
        }

        return StructDef.newBuilder()
                .setId(StructDefId.newBuilder().setName("car"))
                .setStructDef(structFields.build())
                .build();
    }

    @Test
    public void testStructDefComparisonSucceedsWithTwoEqualStructDefs() {
        StructDef structDef1 = getCarStructDef();
        StructDef structDef2 = getCarStructDef();

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(true, StructDefUtil.equals(structDefModel1, structDefModel2));
    }

    @Test
    public void testStructDefComparisonFailsWithTwoNonEqualStructDefs() {
        StructDef structDef1 = getCarStructDef();

        Map<String, StructFieldDef> newFields = Map.of(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                        .build());
        StructDef structDef2 = getCarStructDef(newFields.entrySet());

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(false, StructDefUtil.equals(structDefModel1, structDefModel2));
    }

    @Test
    public void testStructFieldDefComparison() {
        StructFieldDef structFieldDef1 = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                .setOptional(false)
                .build();

        StructFieldDef structFieldDef2 = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                .setOptional(false)
                .build();

        assertEquals(true, structFieldDef1.equals(structFieldDef2));
    }

    @Test
    public void testStructFieldDefComparisonWhenNotEquals() {
        StructFieldDef structFieldDef1 = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                .setOptional(false)
                .build();

        StructFieldDef structFieldDef2 = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                .setOptional(false)
                .setDefaultValue(VariableValue.newBuilder().setBool(true))
                .build();

        assertEquals(false, structFieldDef1.equals(structFieldDef2));
    }

    @Test
    public void testStructDefCompatibilityWhenStructDefsAreEqual() {
        StructDef structDef1 = getCarStructDef();
        StructDef structDef2 = getCarStructDef();

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(false, StructDefUtil.hasBreakingChanges(structDefModel1, structDefModel2));
    }

    @Test
    public void testStructDefsAreNotCompatibleWhenRequiredFieldIsAdded() {
        StructDef structDef1 = getCarStructDef();

        Map<String, StructFieldDef> newFields = new HashMap<>();
        newFields.put(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                        .build());
        StructDef structDef2 = getCarStructDef(newFields.entrySet());

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(true, StructDefUtil.hasBreakingChanges(structDefModel2, structDefModel1));
    }

    @Test
    public void testStructDefsAreCompatibleWhenRequiredFieldIsAddedWithDefault() {
        StructDef structDef1 = getCarStructDef();

        Map<String, StructFieldDef> newFields = Map.of(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                        .setDefaultValue(VariableValue.newBuilder().setInt(5))
                        .build());
        StructDef structDef2 = getCarStructDef(newFields.entrySet());

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(false, StructDefUtil.hasBreakingChanges(structDefModel2, structDefModel1));
    }

    @Test
    public void testStructDefsAreNotCompatibleWhenRequiredFieldIsRemoved() {
        Map<String, StructFieldDef> oldFields = Map.of(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                        .setDefaultValue(VariableValue.newBuilder().setInt(5))
                        .build());
        StructDef structDef1 = getCarStructDef(oldFields.entrySet());

        StructDef structDef2 = getCarStructDef();

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(true, StructDefUtil.hasBreakingChanges(structDefModel2, structDefModel1));
    }

    @Test
    public void testStructDefsAreCompatibleWhenOptionalFieldIsAdded() {
        StructDef structDef1 = getCarStructDef();

        Map<String, StructFieldDef> newFields = Map.of(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                        .setOptional(true)
                        .build());
        StructDef structDef2 = getCarStructDef(newFields.entrySet());

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(false, StructDefUtil.hasBreakingChanges(structDefModel2, structDefModel1));
    }

    @Test
    public void testStructDefsAreCompatibleWhenOptionalFieldIsRemoved() {
        Map<String, StructFieldDef> oldFields = Map.of(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                        .setOptional(true)
                        .build());
        StructDef structDef1 = getCarStructDef(oldFields.entrySet());

        StructDef structDef2 = getCarStructDef();

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(false, StructDefUtil.hasBreakingChanges(structDefModel2, structDefModel1));
    }

    @Test
    public void testStructDefsAreNotCompatibleWhenRequiredFieldTypeChanges() {
        Map<String, StructFieldDef> oldFields = Map.of(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.INT))
                        .build());
        StructDef structDef1 = getCarStructDef(oldFields.entrySet());

        Map<String, StructFieldDef> newFields = Map.of(
                "horsepower",
                StructFieldDef.newBuilder()
                        .setFieldType(TypeDefinition.newBuilder().setType(VariableType.BOOL))
                        .build());
        StructDef structDef2 = getCarStructDef(newFields.entrySet());

        StructDefModel structDefModel1 = StructDefModel.fromProto(structDef1, null);
        StructDefModel structDefModel2 = StructDefModel.fromProto(structDef2, null);

        assertEquals(true, StructDefUtil.hasBreakingChanges(structDefModel2, structDefModel1));
    }
}
