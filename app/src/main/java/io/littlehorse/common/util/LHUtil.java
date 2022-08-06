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
        return Date.from(Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos()));
    }

    public static void logBack(int framesBack, Object... things) {
        framesBack += 2;  // 2 frames needed for processing the thing.
        StackTraceElement ste = Thread.currentThread().getStackTrace()[framesBack];

        System.out.print("LHorse: ");
        System.out.print(ste.getMethodName());
        System.out.print(" ");
        System.out.print(ste.getFileName());
        System.out.print(": ");
        System.out.print(ste.getLineNumber());
        System.out.print(": ");
        for (Object thing : things) {
            System.out.print(thing == null ? "null" : thing.toString());
            System.out.print(" ");
        }
        System.out.println();
    }

    public static void log(Object... things) {
        logBack(1, things);  // Add one frame back because of this method call.
    }

    public static String generateGuid() {
        return UUID.randomUUID().toString();
    }

    public static String toLhDbFormat(Date date) {
        return String.format("%012d", date.getTime());
    }
}
