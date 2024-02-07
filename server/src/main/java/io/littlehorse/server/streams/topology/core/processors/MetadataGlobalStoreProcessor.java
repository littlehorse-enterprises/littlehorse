package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.util.MetadataCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class MetadataGlobalStoreProcessor implements Processor<String, Bytes, Void, Void> {

    private KeyValueStore<String, Bytes> store;
    private final MetadataCache metadataCache;

    public MetadataGlobalStoreProcessor(MetadataCache metadataCache) {
        this.metadataCache = metadataCache;
    }

    @Override
    public void init(final ProcessorContext<Void, Void> ctx) {
        store = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
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
        try {
            if (value != null) {
                store.put(key, value);
                metadataCache.updateCache(key, StoredGetablePb.parseFrom(value.get()));
            } else {
                store.delete(key);
                metadataCache.evictCache(key);
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("unable to parse metadata object");
        }
    }
}
