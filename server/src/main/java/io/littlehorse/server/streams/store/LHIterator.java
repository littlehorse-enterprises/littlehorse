package io.littlehorse.server.streams.store;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Iterator;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;

public class LHIterator<T extends LHSerializable<?>> implements Iterator<LHIterator.Entry<T>>, AutoCloseable {

    private final KeyValueIterator<String, Bytes> rawIter;
    private final Class<T> clazz;
    private final ExecutionContext context;

    public LHIterator(KeyValueIterator<String, Bytes> rawIter, Class<T> clazz, ExecutionContext context) {
        this.rawIter = rawIter;
        this.clazz = clazz;
        this.context = context;
    }

    @Override
    public void close() throws Exception {
        this.rawIter.close();
    }

    @Override
    public boolean hasNext() {
        return this.rawIter.hasNext();
    }

    @Override
    public Entry<T> next() {
        KeyValue<String, Bytes> next = rawIter.next();
        return new Entry<>(next.key, LHSerializable.fromBytes(next.value.get(), clazz, context));
    }

    public record Entry<T>(String key, T value) {}
}
