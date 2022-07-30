package io.littlehorse.server.model.internal;

import java.util.Date;
import io.littlehorse.common.model.GETable;

public class IndexEntry {
    public String typeName;
    public String objectId;
    public String label;
    public String value;
    public Date createdAt;

    public IndexEntry() {}

    // TODO: Figure out how to make this more than just strings.
    public IndexEntry(GETable<?> o, String label, byte[] value) {
        createdAt = o.getCreatedAt();
    }

    public byte[] getStoreKey() {
        return null;
    }

    public byte[] getPartitionKey() {
        return null;
    }

    public static String escape(String other) {
        return other;
    }

    public static String unescape(String other) {
        return other;
    }
}
