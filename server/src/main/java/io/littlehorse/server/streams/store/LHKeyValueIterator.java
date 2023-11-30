package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Iterator;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueIterator;

public class LHKeyValueIterator<T extends Storeable<?>> implements Iterator<LHIterKeyValue<T>>, AutoCloseable {

    private KeyValueIterator<String, Bytes> rawIter;
    private Class<T> cls;
    private ExecutionContext executionContext;

    public LHKeyValueIterator(
            KeyValueIterator<String, Bytes> rawIter, Class<T> cls, ExecutionContext executionContext) {
        this.cls = cls;
        this.rawIter = rawIter;
        this.executionContext = executionContext;
    }

    @Override
    public boolean hasNext() {
        return rawIter.hasNext();
    }

    @Override
    public LHIterKeyValue<T> next() {
        return new LHIterKeyValue<>(rawIter.next(), cls, executionContext);
    }

    public void close() {
        rawIter.close();
    }
}
