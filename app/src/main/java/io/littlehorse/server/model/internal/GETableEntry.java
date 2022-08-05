package io.littlehorse.server.model.internal;

import java.util.ArrayList;
import java.util.List;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.server.GETableEntryPb;
import io.littlehorse.common.proto.server.GETableEntryPbOrBuilder;
import io.littlehorse.common.proto.server.IndexEntryPb;

public class GETableEntry extends LHSerializable<GETableEntryPb>
{
    public byte[] entry;
    public int partition;
    public long lastUpdatedOffset;
    public List<IndexEntry> indexEntries;

    public GETableEntry() {
        indexEntries = new ArrayList<>();
    }

    public Class<GETableEntryPb> getProtoBaseClass() {
        return GETableEntryPb.class;
    }

    public void initFrom(MessageOrBuilder p) {
        GETableEntryPbOrBuilder proto = (GETableEntryPbOrBuilder) p;
        this.entry = proto.getEntry().toByteArray();
        this.partition = proto.getPartition();
        this.lastUpdatedOffset = proto.getLastUpdatedOffset();
        for (IndexEntryPb iepb: proto.getIndexEntriesList()) {
            indexEntries.add(IndexEntry.fromProto(iepb));
        }
    }

    public GETableEntryPb.Builder toProto() {
        GETableEntryPb.Builder out = GETableEntryPb.newBuilder()
            .setPartition(partition)
            .setLastUpdatedOffset(lastUpdatedOffset);

        for (IndexEntry ie: indexEntries) out.addIndexEntries(ie.toProto());

        if (entry != null) out.setEntry(ByteString.copyFrom(entry));
        return out;
    }
}
