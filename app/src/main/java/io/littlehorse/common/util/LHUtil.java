package io.littlehorse.common.util;

import static com.google.protobuf.util.Timestamps.fromMillis;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import com.google.protobuf.Timestamp;

public class LHUtil {
    public static Timestamp fromDate(Date date) {
        if (date == null) return null;
        return fromMillis(date.getTime());
    }

    public static Date fromProtoTs(Timestamp proto) {
        if (proto == null) return null;
        Date out = Date.from(
            Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos())
        );

        if (out.getTime() == 0) {
            out = new Date();
        }

        return out;
    }

    public static void logBack(int framesBack, Object... things) {
        framesBack += 2;  // 2 frames needed for processing the thing.
        StackTraceElement ste = Thread.currentThread().getStackTrace()[framesBack];

        StringBuilder builder = new StringBuilder();

        builder.append("LHorse: ");
        builder.append(ste.getMethodName());
        builder.append(" ");
        builder.append(ste.getFileName());
        builder.append(": ");
        builder.append(ste.getLineNumber());
        builder.append(": ");
        for (Object thing : things) {
            builder.append(thing == null ? "null" : thing.toString());
            builder.append(" ");
        }
        System.out.println(builder.toString());
    }

    public static void log(Object... things) {
        logBack(1, things);  // Add one frame back because of this method call.
    }

    public static String generateGuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String toLhDbFormat(Date date) {
        return String.format("%012d", date.getTime());
    }
}
