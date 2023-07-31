package io.littlehorse.server.streamsimpl.storeinternals.utils;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Storeable;
import java.io.Closeable;
import java.util.Iterator;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueIterator;

public class LHKeyValueIterator<T extends Storeable<?>>
    implements Iterator<LHIterKeyValue<T>>, Closeable, AutoCloseable {

    private KeyValueIterator<String, Bytes> rawIter;
    private Class<T> cls;
    private LHConfig config;

    public LHKeyValueIterator(
        KeyValueIterator<String, Bytes> rawIter,
        Class<T> cls,
        LHConfig config
    ) {
        this.cls = cls;
        this.rawIter = rawIter;
        this.config = config;
    }

    @Override
    public boolean hasNext() {
        return rawIter.hasNext();
    }

    @Override
    public LHIterKeyValue<T> next() {
        return new LHIterKeyValue<>(rawIter.next(), config, cls);
    }

    public void close() {
        rawIter.close();
    }
}
