package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.server.ServerTopology;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

public class GlobalMetadataProcessor implements Processor<String, Bytes, Void, Void> {

    private KeyValueStore<String, Bytes> store;

    public void init(final ProcessorContext<Void, Void> ctx) {
        store = ctx.getStateStore(ServerTopology.GLOBAL_STORE);
    }

    /*
     * All of the difficult processing (i.e. figuring out store keys etc) has
     * been done beforehand in the CommandProcessorDaoImpl::flush() method.
     *
     * We just need to do a simple thing.
     */
    public void process(final Record<String, Bytes> record) {
        String key = record.key();
        Bytes value = record.value();

        if (value == null) {
            store.delete(key);
        } else {
            store.put(key, value);
        }
    }
}
