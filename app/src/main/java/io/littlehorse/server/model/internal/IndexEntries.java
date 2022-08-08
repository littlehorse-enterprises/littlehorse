package io.littlehorse.server.model.internal;

import java.util.ArrayList;
import java.util.List;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.server.IndexEntriesPb;
import io.littlehorse.common.proto.server.IndexEntriesPbOrBuilder;
import io.littlehorse.common.proto.server.IndexEntryPb;

public class IndexEntries extends LHSerializable<IndexEntriesPb> {
    public List<IndexEntry> entries;

    public IndexEntries() {
        this.entries = new ArrayList<>();
    }

    public Class<IndexEntriesPb> getProtoBaseClass() {
        return IndexEntriesPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        IndexEntriesPbOrBuilder p = (IndexEntriesPbOrBuilder) proto;
        for (IndexEntryPb iepb: p.getEntriesList()) {
            IndexEntry entry = new IndexEntry();
            entry.initFrom(iepb);
            entries.add(entry);
        }
    }

    public IndexEntriesPb.Builder toProto() {
        IndexEntriesPb.Builder out = IndexEntriesPb.newBuilder();
        for (IndexEntry e: entries) {
            out.addEntries(e.toProto());
        }

        return out;
    }
}
