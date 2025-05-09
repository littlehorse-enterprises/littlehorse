package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.sdk.common.proto.StructDef;
import java.util.Arrays;
import java.util.Date;

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

    private static void sanitize(StructDef.Builder structDef, Timestamp time) {
        structDef.setCreatedAt(time);
    }
}
