package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.StructDef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StructDefUtil {
    private StructDefUtil() {}

    /**
     * Checks if two StructDefModel objects are equal by comparing their underlying proto representations.
     *
     * @param left  the left StructDefModel object
     * @param right the right StructDefModel object
     * @return true if the underlying proto representations of the objects are equal, false otherwise
     */
    public static boolean equals(StructDefModel left, StructDefModel right) {
        StructDef.Builder copy = left.toProto();
        StructDef.Builder toCopy = right.toProto();

        Timestamp date = LHUtil.fromDate(new Date());
        sanitize(copy, date);
        sanitize(toCopy, date);

        return Arrays.equals(copy.build().toByteArray(), toCopy.build().toByteArray());
    }

    /**
     * Verifies if new StructDefModel has breaking changes compared to old.
     *
     * Returns true when:
     * - A required field is added without a default value
     * - A required field is removed
     * - A required field's type definition changes
     * @param newStructDef New StructDefModel to be compared
     * @param oldStructDef Old StructDefModel compared against
     * @return true when there is a breaking change, false otherwise
     */
    public static List<Entry<String, StructFieldDefModel>> getBreakingChanges(
            StructDefModel newStructDef, StructDefModel oldStructDef) {
        if (newStructDef == null || oldStructDef == null) {
            throw new IllegalArgumentException("StructDefModels cannot be null");
        }

        Map<String, StructFieldDefModel> newRequiredFields =
                newStructDef.getStructDef().getRequiredFields();
        Map<String, StructFieldDefModel> oldRequiredFields =
                oldStructDef.getStructDef().getRequiredFields();

        List<Entry<String, StructFieldDefModel>> incompatibleFields = new ArrayList<>();
        incompatibleFields.addAll(getChangedFields(newRequiredFields, oldRequiredFields));
        incompatibleFields.addAll(getNewFieldsWithoutDefault(newRequiredFields, oldRequiredFields));

        return incompatibleFields;
    }

    private static List<Entry<String, StructFieldDefModel>> getChangedFields(
            Map<String, StructFieldDefModel> newRequiredFields, Map<String, StructFieldDefModel> oldRequiredFields) {
        List<Entry<String, StructFieldDefModel>> incompatibleFields = new ArrayList<>();

        for (Entry<String, StructFieldDefModel> field : oldRequiredFields.entrySet()) {
            // If required field was removed...
            if (!newRequiredFields.containsKey(field.getKey())) {
                incompatibleFields.add(field);
            } else {
                // Check type compatibility
                TypeDefinitionModel oldFieldType = field.getValue().getFieldType();
                TypeDefinitionModel newFieldType =
                        newRequiredFields.get(field.getKey()).getFieldType();

                // If the TypeDefinition changes...
                if (!oldFieldType.equals(newFieldType)) {
                    incompatibleFields.add(field);
                }
            }
        }

        return incompatibleFields;
    }

    private static List<Entry<String, StructFieldDefModel>> getNewFieldsWithoutDefault(
            Map<String, StructFieldDefModel> newRequiredFields, Map<String, StructFieldDefModel> oldRequiredFields) {
        List<Entry<String, StructFieldDefModel>> incompatibleFields = new ArrayList<>();

        // Check for new fields in new StructDef
        for (Entry<String, StructFieldDefModel> field : newRequiredFields.entrySet()) {
            // If new required field was added
            if (!oldRequiredFields.containsKey(field.getKey())) {
                // If new required field does not have a default value
                if (!field.getValue().hasDefaultValue()) {
                    incompatibleFields.add(field);
                }
            }
        }

        return incompatibleFields;
    }

    private static void sanitize(StructDef.Builder structDef, Timestamp time) {
        structDef.setCreatedAt(time);
    }
}
