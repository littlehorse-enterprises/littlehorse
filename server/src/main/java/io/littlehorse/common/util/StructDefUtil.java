package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.StructDef;
import java.util.Arrays;
import java.util.Date;
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
    public static boolean hasBreakingChanges(StructDefModel newStructDef, StructDefModel oldStructDef) {
        Map<String, StructFieldDefModel> newRequiredFields =
                newStructDef.getStructDef().getRequiredFields();
        Map<String, StructFieldDefModel> oldRequiredFields =
                oldStructDef.getStructDef().getRequiredFields();

        // Check for required fields in old StructDef
        for (Entry<String, StructFieldDefModel> field : oldRequiredFields.entrySet()) {
            // If required field was removed...
            if (!newRequiredFields.containsKey(field.getKey())) {
                return true;
            }

            // Check type compatibility
            TypeDefinitionModel oldFieldType = field.getValue().getFieldType();
            TypeDefinitionModel newFieldType =
                    newRequiredFields.get(field.getKey()).getFieldType();

            // If the TypeDefinition changes...
            if (!oldFieldType.equals(newFieldType)) {
                return true;
            }
        }

        // Check for new fields in new StructDef
        for (Entry<String, StructFieldDefModel> field : newRequiredFields.entrySet()) {
            // If new required field was added
            if (!oldRequiredFields.containsKey(field.getKey())) {
                // If new required field does not have a default value
                if (!field.getValue().hasDefaultValue()) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void sanitize(StructDef.Builder structDef, Timestamp time) {
        structDef.setCreatedAt(time);
    }
}
