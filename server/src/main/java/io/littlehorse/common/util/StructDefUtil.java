package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
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
     * - required fields are added without default values
     * @param newStructDef New StructDefModel to be compared
     * @param oldStructDef Old StructDefModel compared against
     * @return true when there is a breaking change, false otherwise
     */
    public static boolean hasBreakingChanges(StructDefModel newStructDef, StructDefModel oldStructDef) {
        Map<String, StructFieldDefModel> newRequiredFields =
                newStructDef.getStructDef().getRequiredFields();
        Map<String, StructFieldDefModel> oldRequiredFields =
                oldStructDef.getStructDef().getRequiredFields();

        for (Entry<String, StructFieldDefModel> field : newRequiredFields.entrySet()) {
            // If a new required field is found...
            if (field.getValue().isRequired() && !oldRequiredFields.containsKey(field.getKey())) {
                // If the new required field does not have a default value
                if (field.getValue().getDefaultValue().isEmpty()) {
                    System.out.println("New required field: " + field.getKey() + " Value: "
                            + field.getValue().toJson());
                    return true;
                }
            }
        }

        for (Entry<String, StructFieldDefModel> field : oldRequiredFields.entrySet()) {
            // If required field was removed...
            if (field.getValue().isRequired() && !newRequiredFields.containsKey(field.getKey())) {
                System.out.println("Required field removed: " + field.getKey() + " Value: "
                        + field.getValue().toJson());
                return true;
            }
        }

        return false;
    }

    private static void sanitize(StructDef.Builder structDef, Timestamp time) {
        structDef.setCreatedAt(time);
    }
}
