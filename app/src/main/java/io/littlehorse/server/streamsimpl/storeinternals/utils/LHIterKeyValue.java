package io.littlehorse.server.streamsimpl.storeinternals.utils;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;

public class LHIterKeyValue<T extends Storeable<?>> {

    private String key;
    private Bytes valBytes;
    private T val;
    private LHConfig config;
    private Class<T> cls;

    public LHIterKeyValue(
        KeyValue<String, Bytes> raw,
        LHConfig config,
        Class<T> cls
    ) {
        valBytes = raw.value;
        this.config = config;
        this.cls = cls;

        // The raw prefix is composite (it contains type/table name before the key)
        key = StoreUtils.stripPrefix(raw.key);
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        if (val == null) {
            try {
                val = LHSerializable.fromBytes(valBytes.get(), cls, config);
            } catch (LHSerdeError exn) {
                // Not possible
                throw new RuntimeException(exn);
            }
        }
        return val;
    }
}
