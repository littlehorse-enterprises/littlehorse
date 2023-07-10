package io.littlehorse.server.streamsimpl.storeinternals.utils;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;

public class LHKeyValueStream<T extends Storeable<?>> {

    private KeyValueIterator<String, Bytes> rawIterator;
    private Class<T> storeableClass;

    private LHConfig lhConfig;

    public LHKeyValueStream(
        KeyValueIterator<String, Bytes> rawIterator,
        Class<T> storeableClass,
        LHConfig lhConfig
    ) {
        this.rawIterator = rawIterator;
        this.storeableClass = storeableClass;
        this.lhConfig = lhConfig;
    }

    public Stream<KeyValue<String, T>> stream() {
        Spliterator<KeyValue<String, Bytes>> keyValueSpliterator = Spliterators.spliteratorUnknownSize(
            rawIterator,
            Spliterator.ORDERED
        );
        return StreamSupport
            .stream(keyValueSpliterator, false)
            .map(this::deserializeKeyValue);
    }

    private KeyValue<String, T> deserializeKeyValue(
        KeyValue<String, Bytes> rawKeyValue
    ) {
        Bytes rawValue = rawKeyValue.value;
        String key = rawKeyValue.key;
        try {
            T value = LHSerializable.fromBytes(
                rawValue.get(),
                storeableClass,
                lhConfig
            );
            return KeyValue.pair(key, value);
        } catch (LHSerdeError exn) {
            throw new RuntimeException(exn);
        }
    }
}
