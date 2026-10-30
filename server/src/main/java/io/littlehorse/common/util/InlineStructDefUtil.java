package io.littlehorse.common.util;

import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class InlineStructDefUtil {
    private InlineStructDefUtil() {}

    /**
     * Checks if two InlineStructDefModel objects have the same schema by comparing their underlying
     * proto representations, excluding field-level descriptions (which are metadata, not schema).
     *
     * @param left  the left InlineStructDefModel object
     * @param right the right InlineStructDefModel object
     * @return true if the schema (excluding field descriptions) is equal, false otherwise
     */
    public static boolean schemasEqual(InlineStructDefModel left, InlineStructDefModel right) {
        InlineStructDef leftProto = withoutFieldDescriptions(left.toProto().build());
        InlineStructDef rightProto = withoutFieldDescriptions(right.toProto().build());

        return leftProto.equals(rightProto);
    }

    /**
     * Returns true when the field descriptions are identical across both InlineStructDefModels.
     * Used to detect description-only changes that bump the version without a schema compatibility check.
     */
    public static boolean fieldDescriptionsEqual(InlineStructDefModel left, InlineStructDefModel right) {
        Map<String, StructFieldDefModel> leftFields = left.getFields();
        Map<String, StructFieldDefModel> rightFields = right.getFields();

        if (!leftFields.keySet().equals(rightFields.keySet())) {
            return false;
        }

        for (Map.Entry<String, StructFieldDefModel> entry : leftFields.entrySet()) {
            String leftDesc = entry.getValue().getDescription();
            String rightDesc = rightFields.get(entry.getKey()).getDescription();
            if (!Objects.equals(leftDesc, rightDesc)
                    || !nestedDescriptionsEqual(
                            entry.getValue().getFieldType().toProto().build(),
                            rightFields
                                    .get(entry.getKey())
                                    .getFieldType()
                                    .toProto()
                                    .build())) {
                return false;
            }
        }

        return true;
    }

    private static boolean nestedDescriptionsEqual(TypeDefinition left, TypeDefinition right) {
        if (left.getDefinedTypeCase() != right.getDefinedTypeCase()) return false;
        switch (left.getDefinedTypeCase()) {
            case INLINE_STRUCT_DEF:
                return fieldDescriptionsEqual(
                        InlineStructDefModel.fromProto(left.getInlineStructDef(), null),
                        InlineStructDefModel.fromProto(right.getInlineStructDef(), null));
            case INLINE_ARRAY_DEF:
                return nestedDescriptionsEqual(
                        left.getInlineArrayDef().getArrayType(),
                        right.getInlineArrayDef().getArrayType());
            case INLINE_MAP_DEF:
                return nestedDescriptionsEqual(
                        left.getInlineMapDef().getValueType(),
                        right.getInlineMapDef().getValueType());
            default:
                return true;
        }
    }

    private static InlineStructDef withoutFieldDescriptions(InlineStructDef proto) {
        InlineStructDef.Builder b = proto.toBuilder();
        proto.getFieldsMap()
                .forEach((name, fieldDef) -> b.putFields(
                        name,
                        fieldDef.toBuilder()
                                .clearDescription()
                                .setFieldType(withoutFieldDescriptions(fieldDef.getFieldType()))
                                .build()));
        return b.build();
    }

    public static boolean equals(InlineStructDefModel left, InlineStructDefModel right) {
        return left.equals(right);
    }

    public static Set<String> getIncompatibleFields(
            StructDefCompatibilityType compatibilityType,
            InlineStructDefModel newStructDef,
            InlineStructDefModel oldStructDef) {
        Set<String> incompatible = new HashSet<>();
        switch (compatibilityType) {
            case FULLY_COMPATIBLE_SCHEMA_UPDATES:
                incompatible.addAll(getFullCompatibleBreakingChanges(newStructDef, oldStructDef));
                break;
            case NO_SCHEMA_UPDATES:
                incompatible.addAll(getNoSchemaUpdatesBreakingChanges(newStructDef, oldStructDef));
                break;
            default:
                return Set.of();
        }
        // Optional parent fields must not hide changes to a nested choice.
        Map<String, InlineStructDef> newChoices = new HashMap<>();
        Map<String, InlineStructDef> oldChoices = new HashMap<>();
        collectChoices(
                TypeDefinition.newBuilder()
                        .setInlineStructDef(newStructDef.toProto())
                        .build(),
                "",
                newChoices);
        collectChoices(
                TypeDefinition.newBuilder()
                        .setInlineStructDef(oldStructDef.toProto())
                        .build(),
                "",
                oldChoices);
        Set<String> paths = new HashSet<>(newChoices.keySet());
        paths.addAll(oldChoices.keySet());
        for (String path : paths) {
            if (!Objects.equals(newChoices.get(path), oldChoices.get(path))) incompatible.add(path);
        }
        return incompatible;
    }

    private static void collectChoices(TypeDefinition type, String path, Map<String, InlineStructDef> choices) {
        switch (type.getDefinedTypeCase()) {
            case INLINE_STRUCT_DEF:
                InlineStructDef struct = type.getInlineStructDef();
                if (struct.hasOneofConfig()) choices.put(path, withoutFieldDescriptions(struct));
                struct.getFieldsMap()
                        .forEach((name, field) -> collectChoices(
                                field.getFieldType(), path.isEmpty() ? name : path + "." + name, choices));
                break;
            case INLINE_ARRAY_DEF:
                collectChoices(type.getInlineArrayDef().getArrayType(), path + "[]", choices);
                break;
            case INLINE_MAP_DEF:
                collectChoices(type.getInlineMapDef().getValueType(), path + "{}", choices);
                break;
            default:
                break;
        }
    }

    private static TypeDefinition withoutFieldDescriptions(TypeDefinition type) {
        TypeDefinition.Builder out = type.toBuilder();
        switch (type.getDefinedTypeCase()) {
            case INLINE_STRUCT_DEF:
                out.setInlineStructDef(withoutFieldDescriptions(type.getInlineStructDef()));
                break;
            case INLINE_ARRAY_DEF:
                out.setInlineArrayDef(type.getInlineArrayDef().toBuilder()
                        .setArrayType(withoutFieldDescriptions(
                                type.getInlineArrayDef().getArrayType())));
                break;
            case INLINE_MAP_DEF:
                out.setInlineMapDef(type.getInlineMapDef().toBuilder()
                        .setValueType(
                                withoutFieldDescriptions(type.getInlineMapDef().getValueType())));
                break;
            default:
                break;
        }
        return out.build();
    }

    /**
     * Verifies if new InlineStructDefModel has breaking changes compared to old.
     *
     * Returns true when:
     * - A required field is added without a default value
     * - A required field is removed
     * - A required field's type definition changes
     * @param newStructDef New InlineStructDefModel to be compared
     * @param oldStructDef Old InlineStructDefModel compared against
     * @return true when there is a breaking change, false otherwise
     */
    private static Set<String> getNoSchemaUpdatesBreakingChanges(
            InlineStructDefModel newStructDef, InlineStructDefModel oldStructDef) {
        if (newStructDef == null || oldStructDef == null) {
            throw new IllegalArgumentException("InlineStructDefModels cannot be null");
        }

        Set<Entry<String, StructFieldDefModel>> newFields =
                newStructDef.getFields().entrySet();
        Set<Entry<String, StructFieldDefModel>> oldFields =
                oldStructDef.getFields().entrySet();
        Set<Entry<String, StructFieldDefModel>> changedFields = new HashSet<>(newFields);
        changedFields.removeAll(oldFields);

        return changedFields.stream().map(Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * Verifies if new InlineStructDefModel has breaking changes compared to old.
     *
     * Returns true when:
     * - A required field is added without a default value
     * - A required field is removed
     * - A required field's type definition changes
     * @param newStructDef New InlineStructDefModel to be compared
     * @param oldStructDef Old InlineStructDefModel compared against
     * @return true when there is a breaking change, false otherwise
     */
    private static Set<String> getFullCompatibleBreakingChanges(
            InlineStructDefModel newStructDef, InlineStructDefModel oldStructDef) {
        if (newStructDef == null || oldStructDef == null) {
            throw new IllegalArgumentException("InlineStructDefModels cannot be null");
        }

        Map<String, StructFieldDefModel> newRequiredFields = newStructDef.getRequiredFields();
        Map<String, StructFieldDefModel> oldRequiredFields = oldStructDef.getRequiredFields();

        Set<String> incompatibleFields = new HashSet<>();
        incompatibleFields.addAll(getChangedFields(newRequiredFields, oldRequiredFields));
        incompatibleFields.addAll(getNewFieldsWithoutDefault(newRequiredFields, oldRequiredFields));

        return incompatibleFields;
    }

    private static List<String> getChangedFields(
            Map<String, StructFieldDefModel> newFields, Map<String, StructFieldDefModel> oldFields) {
        List<String> incompatibleFields = new ArrayList<>();

        for (String oldFieldName : oldFields.keySet()) {
            // If required field was removed...
            if (!newFields.containsKey(oldFieldName)) {
                incompatibleFields.add(oldFieldName);
            } else {
                StructFieldDefModel oldField = oldFields.get(oldFieldName);
                StructFieldDefModel newField = newFields.get(oldFieldName);

                // If the TypeDefinition changes...
                if (!withoutFieldDescriptions(oldField.getFieldType().toProto().build())
                        .equals(withoutFieldDescriptions(
                                newField.getFieldType().toProto().build()))) {
                    incompatibleFields.add(oldFieldName);
                }
            }
        }

        return incompatibleFields;
    }

    private static List<String> getNewFieldsWithoutDefault(
            Map<String, StructFieldDefModel> newRequiredFields, Map<String, StructFieldDefModel> oldRequiredFields) {
        List<String> incompatibleFields = new ArrayList<>();

        // Check for new fields in new StructDef
        for (String newFieldName : newRequiredFields.keySet()) {
            // If new required field was added
            if (!oldRequiredFields.containsKey(newFieldName)) {
                // If new required field does not have a default value
                incompatibleFields.add(newFieldName);
            }
        }

        return incompatibleFields;
    }
}
