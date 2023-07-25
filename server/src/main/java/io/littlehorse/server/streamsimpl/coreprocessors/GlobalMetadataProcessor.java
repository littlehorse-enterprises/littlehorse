package io.littlehorse.server.streamsimpl.coreprocessors;

import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.storeinternals.LHROStoreWrapper;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

class WfSpecCache {

    private ConcurrentHashMap<String, WfSpec> wfSpecCache;

    public WfSpec getLatestWfSpec(String name, LHROStoreWrapper store) {
        if (!wfSpecCache.containsKey(name)) {
            // query the global store, and save into cache map
        }
    }

    public WfSpec getWfSpec(String name, int version, LHROStoreWrapper store) {}
}

public class GlobalMetadataProcessor implements Processor<String, Bytes, Void, Void> {

    private KeyValueStore<String, Bytes> store;

    public GlobalMetadataProcessor(WfSpecCache cache) {
        this.wfSpecCache = cache;
    }

    public void init(final ProcessorContext<Void, Void> ctx) {
        store = ctx.getStateStore(ServerTopology.GLOBAL_STORE);
    }

    /*
     * All of the difficult processing (i.e. figuring out store keys etc) has
     * been done beforehand in the CommandProcessorDaoImpl::flush() method.
     *
     * Unfortunately, that's necessary because of the following JIRA:
     * https://issues.apache.org/jira/browse/KAFKA-7663
     *
     * Basically, while we are required to give a ProcessorSupplier when
     * building the global store, and the processor does actually process
     * the bytes during normal processing, the Processor is completely
     * bypassed during restoration and the input topic is simply treated
     * as a changelog.
     */
    public void process(final Record<String, Bytes> record) {
        String key = record.key();
        Bytes value = record.value();

        if (key.startsWith("WfSpec")) {
            if (value == null) {
                String name = getNameFromKey(key);
                cache.remove(name);
            } else {
                WfSpec spec = WfSpec.fromBytes(value.get());
                cache.put(spec.getName(), spec);
            }
        }

        if (value == null) {
            store.delete(key);
        } else {
            store.put(key, value);
        }
    }
}
