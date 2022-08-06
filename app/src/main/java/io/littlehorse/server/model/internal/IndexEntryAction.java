package io.littlehorse.server.model.internal;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.server.IndexActionEnum;
import io.littlehorse.common.proto.server.IndexEntryActionPb;
import io.littlehorse.common.proto.server.IndexEntryActionPbOrBuilder;

public class IndexEntryAction extends LHSerializable<IndexEntryActionPb> {
    public IndexActionEnum action;
    public IndexEntry indexEntry;

    public Class<IndexEntryActionPb> getProtoBaseClass() {
        return IndexEntryActionPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        IndexEntryActionPbOrBuilder p = (IndexEntryActionPbOrBuilder) proto;
        action = p.getAction();
        indexEntry = new IndexEntry();
        indexEntry.initFrom(p.getEntry());
    }

    public IndexEntryActionPb.Builder toProto() {
        IndexEntryActionPb.Builder out = IndexEntryActionPb.newBuilder();
        out.setAction(action);
        out.setEntry(indexEntry.toProto());
        return out;
    }
}
