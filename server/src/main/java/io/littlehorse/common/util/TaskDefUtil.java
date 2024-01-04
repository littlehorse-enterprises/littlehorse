package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.sdk.common.proto.TaskDef;
import java.util.Arrays;
import java.util.Date;

public class TaskDefUtil {
    private TaskDefUtil() {}

    public static boolean equals(TaskDefModel left, TaskDefModel right) {
        TaskDef.Builder copy = left.toProto();
        TaskDef.Builder toCopy = right.toProto();

        Timestamp date = LHUtil.fromDate(new Date());
        sanitize(copy, date);
        sanitize(toCopy, date);

        return Arrays.equals(copy.build().toByteArray(), toCopy.build().toByteArray());
    }

    private static void sanitize(TaskDef.Builder task, Timestamp time) {
        task.setCreatedAt(time);
    }
}
