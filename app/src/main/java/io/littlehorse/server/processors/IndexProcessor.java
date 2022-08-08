package io.littlehorse.server.processors;

import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.proto.server.IndexActionEnum;
import io.littlehorse.server.model.internal.IndexEntries;
import io.littlehorse.server.model.internal.IndexEntry;
import io.littlehorse.server.model.internal.IndexEntryAction;

public class IndexProcessor implements Processor<
    String, IndexEntryAction, Void, Void
> {
    private KeyValueStore<String, IndexEntries> store;

    @Override public void init(final ProcessorContext<Void, Void> ctx) {
        this.store = ctx.getStateStore(LHConstants.INDEX_STORE_NAME);
    }

    @Override public void process(final Record<String, IndexEntryAction> record) {
        String partitionKey = record.key();
        IndexEntryAction action = record.value();
        IndexEntry entry = action.indexEntry;

        if (!partitionKey.equals(entry.getPartitionKey())) {
            throw new RuntimeException("This should be impossible");
        }

        String storeKey = entry.getStoreKey();
        IndexEntries entries = store.get(storeKey);

        if (action.action == IndexActionEnum.CREATE_IDX_ENTRY) {
            if (entries == null) entries = new IndexEntries();
            entries.entries.add(entry);
            store.put(storeKey, entries);
        } else {
            if (entries == null) {
                throw new RuntimeException("Should be impossible to have null");
            }

            if (!entries.entries.contains(entry)) {
                throw new RuntimeException("Impossible to not have it contained");
            }

            entries.entries.remove(entry);

            if (entries.entries.contains(entry)) {
                throw new RuntimeException("Should be impossible to still have");
            }

            if (entries.entries.isEmpty()) {
                store.delete(storeKey);
            } else {
                store.put(storeKey, entries);
            }
        }
    }
}
