package io.littlehorse.server.streams.store;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;

public class LHIterKeyValue<T extends Storeable<?>> {

    private String key;
    private Bytes valBytes;
    private T val;
    private Class<T> cls;

    public LHIterKeyValue(KeyValue<String, Bytes> raw, Class<T> cls) {
        valBytes = raw.value;
        this.cls = cls;

        // The raw prefix is composite (it contains type/table name before the key)
        key = Storeable.stripPrefix(raw.key);
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        if (val == null) {
            try {
                val = LHSerializable.fromBytes(valBytes.get(), cls, null); // TODO eduwer
            } catch (LHSerdeError exn) {
                // Not possible
                throw new RuntimeException(exn);
            }
        }
        return val;
    }
}
