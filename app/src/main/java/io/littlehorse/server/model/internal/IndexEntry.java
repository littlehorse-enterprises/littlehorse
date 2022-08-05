package io.littlehorse.server.model.internal;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.server.IndexEntryPb;
import io.littlehorse.common.proto.server.IndexEntryPbOrBuilder;

public class IndexEntry extends LHSerializable<IndexEntryPb> {
    public String partitionKey;
    public String value;

    public Class<IndexEntryPb> getProtoBaseClass() {
        return IndexEntryPb.class;
    }

    public void initFrom(MessageOrBuilder p) {
        IndexEntryPbOrBuilder proto = (IndexEntryPbOrBuilder) p;
        partitionKey = proto.getPartitionKey();
        value = proto.getValue();
    }

    public IndexEntryPb.Builder toProto() {
        IndexEntryPb.Builder out = IndexEntryPb.newBuilder()
            .setPartitionKey(partitionKey)
            .setValue(value);

        return out;
    }

    public static IndexEntry fromProto(IndexEntryPb proto) {
        IndexEntry out = new IndexEntry();
        out.initFrom(proto);
        return out;
    }
}
