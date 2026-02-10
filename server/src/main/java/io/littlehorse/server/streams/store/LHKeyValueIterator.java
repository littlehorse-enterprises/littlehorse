package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueIterator;

@Slf4j
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
        var next = rawIter.next();
        if(!cls.getSimpleName().equals("StoredGetable")){
            System.out.println("LHKeyValueIterator - Next key: " + next.key + ", cls: " + cls.getSimpleName());
        }
        
        return new LHIterKeyValue<>(next, cls, executionContext);
    }

    public void close() {
        rawIter.close();
    }
}
