package io.littlehorse.common.util;

import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class InlineStructDefUtil {
    private InlineStructDefUtil() {}

    /**
     * Checks if two InlineStructDefModel objects are equal by comparing their underlying proto representations.
     *
     * @param left  the left InlineStructDefModel object
     * @param right the right InlineStructDefModel object
     * @return true if the underlying proto representations of the objects are equal, false otherwise
     */
    public static boolean equals(InlineStructDefModel left, InlineStructDefModel right) {
        InlineStructDef.Builder copy = left.toProto();
        InlineStructDef.Builder toCopy = right.toProto();

        return Arrays.equals(copy.build().toByteArray(), toCopy.build().toByteArray());
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
