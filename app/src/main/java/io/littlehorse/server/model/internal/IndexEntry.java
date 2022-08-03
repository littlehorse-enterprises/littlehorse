package io.littlehorse.server.model.internal;

import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.IndexEntryPb;

public class IndexEntry extends LHSerializable<IndexEntryPb> {
    public String partitionKey;
    public String storeKey;

    public Class<IndexEntryPb> getProtoBaseClass() {
        return IndexEntryPb.class;
    }

    public void initFrom(IndexEntryPb proto) {
        partitionKey = proto.getPartitionKey();
        storeKey = proto.getStoreKey();
    }

    public IndexEntryPb.Builder toProto() {
        IndexEntryPb.Builder out = IndexEntryPb.newBuilder()
            .setPartitionKey(partitionKey)
            .setStoreKey(storeKey);

        return out;
    }

    public static IndexEntry fromProto(IndexEntryPb proto) {
        IndexEntry out = new IndexEntry();
        out.initFrom(proto);
        return out;
    }
}
