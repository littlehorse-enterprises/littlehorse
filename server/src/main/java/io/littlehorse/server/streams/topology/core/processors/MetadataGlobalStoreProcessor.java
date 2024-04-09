package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Optional;
import java.util.regex.Pattern;
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
    String patternString = "(\\w+)/(\\w+)/(\\w.+)";

    Pattern pattern = Pattern.compile(patternString);

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
                maybeDeserializeStoredGetable(value).ifPresent(metadataGetableStoredGetable -> {
                    metadataCache.updateCache(key, metadataGetableStoredGetable);
                });
            } else {
                store.delete(key);
                metadataCache.updateMissingKey(key);
            }
        } catch (Exception e) {
            log.error("unable to parse metadata object");
            e.printStackTrace();
        }
    }

    private Optional<StoredGetable<? extends Message, MetadataGetable<?>>> maybeDeserializeStoredGetable(Bytes value)
            throws InvalidProtocolBufferException {
        StoredGetablePb storedGetablePb = StoredGetablePb.parseFrom(value.get());
        boolean isValid = storedGetablePb.hasIndexCache();
        if (!isValid) {
            return Optional.empty();
        }
        return Optional.of(LHSerializable.fromProto(storedGetablePb, StoredGetable.class, new BackgroundContext()));
    }
}
