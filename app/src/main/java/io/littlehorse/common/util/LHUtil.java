package io.littlehorse.common.util;

import java.time.Instant;
import java.util.Date;
import com.google.protobuf.Timestamp;
import static com.google.protobuf.util.Timestamps.fromMillis;

public class LHUtil {
    public static Timestamp fromDate(Date date) {
        if (date == null) return null;
        return fromMillis(date.getTime());
    }

    public static Date fromProtoTs(Timestamp proto) {
        if (proto == null) return null;
        return Date.from(Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos()));
    }
}
