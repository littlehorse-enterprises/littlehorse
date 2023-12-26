package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import java.util.Arrays;
import java.util.Date;

public class UserTaskUtil {
    private UserTaskUtil() {}

    private static Integer baseVersion = 0;

    public static boolean equals(UserTaskDefModel left, UserTaskDefModel right) {
        UserTaskDef.Builder copy = left.toProto();
        UserTaskDef.Builder toCopy = right.toProto();

        Timestamp date = LHUtil.fromDate(new Date());
        sanitize(copy, date);
        sanitize(toCopy, date);

        return Arrays.equals(copy.build().toByteArray(), toCopy.build().toByteArray());
    }

    private static void sanitize(UserTaskDef.Builder task, Timestamp time) {
        task.setCreatedAt(time).setVersion(baseVersion);
    }
}
