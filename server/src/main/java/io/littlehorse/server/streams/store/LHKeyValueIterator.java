package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import java.util.Iterator;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueIterator;

public class LHKeyValueIterator<T extends Storeable<?>> implements Iterator<LHIterKeyValue<T>>, AutoCloseable {

    private KeyValueIterator<String, Bytes> rawIter;
    private Class<T> cls;

    public LHKeyValueIterator(KeyValueIterator<String, Bytes> rawIter, Class<T> cls) {
        this.cls = cls;
        this.rawIter = rawIter;
    }

    @Override
    public boolean hasNext() {
        return rawIter.hasNext();
    }

    @Override
    public LHIterKeyValue<T> next() {
        return new LHIterKeyValue<>(rawIter.next(), cls);
    }

    public void close() {
        rawIter.close();
    }
}
