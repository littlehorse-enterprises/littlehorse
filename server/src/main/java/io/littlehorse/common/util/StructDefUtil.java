package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.sdk.common.proto.StructDef;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
     * Verifies if left StructDefModel has breaking changes compared to right when:
     * - set of left required fields does not match right required fields
     * @param left WfSpecModel to be compared
     * @param right WfSpecModel compared against
     * @return true when there is a breaking change, false otherwise
     */
    public static boolean hasBreakingChanges(StructDefModel left, StructDefModel right) {
        // TODO: Allow users to add Required Fields with Default Values
        Collection<Entry<String, StructFieldDefModel>> leftRequiredFields = left.getStructDef().getRequiredFields();
        Collection<Entry<String, StructFieldDefModel>> rightRequiredFields = right.getStructDef().getRequiredFields();

        return leftRequiredFields.size() != rightRequiredFields.size() || !leftRequiredFields.containsAll(rightRequiredFields);
    }

    private static void sanitize(StructDef.Builder structDef, Timestamp time) {
        structDef.setCreatedAt(time);
    }
}
