package io.littlehorse.common.util;

import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import java.util.ArrayList;
import java.util.Arrays;
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

        return Arrays.equals(leftProto.toByteArray(), rightProto.toByteArray());
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
            if (!Objects.equals(leftDesc, rightDesc)) {
                return false;
            }
        }

        return true;
    }

    private static InlineStructDef withoutFieldDescriptions(InlineStructDef proto) {
        InlineStructDef.Builder b = proto.toBuilder();
        proto.getFieldsMap()
                .forEach((name, fieldDef) -> b.putFields(
                        name, fieldDef.toBuilder().clearDescription().build()));
        return b.build();
    }

    public static boolean equals(InlineStructDefModel left, InlineStructDefModel right) {
        return left.equals(right);
    }

    public static Set<String> getIncompatibleFields(
            StructDefCompatibilityType compatibilityType,
            InlineStructDefModel newStructDef,
            InlineStructDefModel oldStructDef) {
        switch (compatibilityType) {
            case FULLY_COMPATIBLE_SCHEMA_UPDATES:
                return getFullCompatibleBreakingChanges(newStructDef, oldStructDef);
            case NO_SCHEMA_UPDATES:
                return getNoSchemaUpdatesBreakingChanges(newStructDef, oldStructDef);
            case UNRECOGNIZED:
            default:
                return Set.of();
        }
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
        oldFields.stream()
                .filter(entry -> !newStructDef.getFields().containsKey(entry.getKey()))
                .forEach(changedFields::add);

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
        addOneofBreakingChanges(newStructDef, oldStructDef, "", incompatibleFields);
        return incompatibleFields;
    }

    // Recurses into embedded schemas because oneof guarantees apply at every Struct level.
    private static void addOneofBreakingChanges(
            InlineStructDefModel newStructDef, InlineStructDefModel oldStructDef, String prefix, Set<String> out) {
        Map<String, StructFieldDefModel> newFields = newStructDef.getFields();
        for (var entry : oldStructDef.getFields().entrySet()) {
            String path = prefix + entry.getKey();
            StructFieldDefModel oldField = entry.getValue();
            StructFieldDefModel newField = newFields.get(entry.getKey());
            boolean wasAlternative = oldField.getOneofGroup() != null;
            boolean isAlternative = newField != null && newField.getOneofGroup() != null;
            // Old versions fill in defaults, so existing fields can't safely join a group.
            if ((wasAlternative || isAlternative)
                    && (newField == null
                            || !Objects.equals(oldField.getOneofGroup(), newField.getOneofGroup())
                            || !oldField.getFieldType().equals(newField.getFieldType()))) {
                out.add(path);
            }
            if (newField != null) {
                addEmbeddedOneofBreakingChanges(newField.getFieldType(), oldField.getFieldType(), path, out);
            }
        }
    }

    private static void addEmbeddedOneofBreakingChanges(
            TypeDefinitionModel newType, TypeDefinitionModel oldType, String path, Set<String> out) {
        if (newType == null || oldType == null || newType.getDefinedTypeCase() != oldType.getDefinedTypeCase()) {
            return;
        }
        switch (newType.getDefinedTypeCase()) {
            case INLINE_STRUCT_DEF:
                addOneofBreakingChanges(newType.getInlineStructDef(), oldType.getInlineStructDef(), path + ".", out);
                break;
            case INLINE_ARRAY_DEF:
                addEmbeddedOneofBreakingChanges(
                        newType.getInlineArrayDef().getArrayType(),
                        oldType.getInlineArrayDef().getArrayType(),
                        path + "[]",
                        out);
                break;
            case INLINE_MAP_DEF:
                addEmbeddedOneofBreakingChanges(
                        newType.getInlineMapDef().getKeyType(),
                        oldType.getInlineMapDef().getKeyType(),
                        path + "{key}",
                        out);
                addEmbeddedOneofBreakingChanges(
                        newType.getInlineMapDef().getValueType(),
                        oldType.getInlineMapDef().getValueType(),
                        path + "{}",
                        out);
                break;
            default:
        }
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
                if (!oldField.getFieldType().equals(newField.getFieldType())) {
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
