package io.littlehorse.server.processors;

import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.server.IndexActionEnum;
import io.littlehorse.server.model.internal.Tags;
import io.littlehorse.server.model.internal.Tag;
import io.littlehorse.server.model.internal.IndexEntryAction;

public class TaggingProcessor<U extends MessageOrBuilder, T extends GETable<U>>
implements Processor<String, T, String, IndexEntryAction> {
    private ProcessorContext<String, IndexEntryAction> ctx;
    private KeyValueStore<String, Tags> store;
    // private Class<T> cls;
    private String storeName;

    public TaggingProcessor(Class<T> cls, String storeName) {
        this.storeName = storeName;
    }

    public void init(final ProcessorContext<String, IndexEntryAction> ctx) {
        this.ctx = ctx;
        this.store = ctx.getStateStore(storeName);
    }

    public void process(final Record<String, T> record) {
        String objectId = new String(record.headers().lastHeader(
            LHConstants.OBJECT_ID_HEADER
        ).value());
        T newT = record.value();

        Tags oldEntriesObj = store.get(objectId);
        List<Tag> oldIdx = (
            oldEntriesObj == null ? new ArrayList<>() : oldEntriesObj.entries
        );

        List<Tag> newIdx = (
            newT == null ? new ArrayList<>() : newT.getTags()
        );

        if (newT == null) {
            store.delete(objectId);
        } else {
            Tags newIdxEntries = new Tags();
            newIdxEntries.entries = newIdx;
            store.put(objectId, newIdxEntries);
        }

        for (Tag ie: newIdx) {
            if (!oldIdx.contains(ie)) {
                IndexEntryAction action = new IndexEntryAction();
                action.action = IndexActionEnum.CREATE_IDX_ENTRY;
                action.indexEntry = ie;
                Record<String, IndexEntryAction> rec = new Record<>(
                    ie.getPartitionKey(),
                    action,
                    ie.createdAt.getTime()
                );
                rec.headers().add(LHConstants.OBJECT_ID_HEADER, objectId.getBytes());
                ctx.forward(rec);
            }
        }
        for (Tag ie: oldIdx) {
            if (!newIdx.contains(ie)) {
                IndexEntryAction action = new IndexEntryAction();
                action.action = IndexActionEnum.DELETE_IDX_ENTRY;
                action.indexEntry = ie;
                ctx.forward(
                    new Record<String, IndexEntryAction>(
                        ie.getPartitionKey(),
                        action,
                        ie.createdAt.getTime()
                    )
                );
            }
        }
    }
}
