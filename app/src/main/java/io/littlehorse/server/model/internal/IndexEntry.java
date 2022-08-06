package io.littlehorse.server.model.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.server.AttributePb;
import io.littlehorse.common.proto.server.GETableClassEnumPb;
import io.littlehorse.common.proto.server.IndexEntryPb;
import io.littlehorse.common.proto.server.IndexEntryPbOrBuilder;
import io.littlehorse.common.proto.server.IndexKeyPb;
import io.littlehorse.common.util.LHUtil;

public class IndexEntry extends LHSerializable<IndexEntryPb>{
    public GETableClassEnumPb type;
    public List<Pair<String, String>> attributes;
    public Date createdAt;

    public String resultObjectId;

    public IndexEntry() {
        attributes = new ArrayList<>();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public IndexEntry(GETable<?> getable, Pair<String, String>...atts) {
        this.type = GETable.getTypeEnum(
            (Class<? extends GETable<?>>) getable.getClass()
        );
        createdAt = getable.getCreatedAt();
        resultObjectId = getable.getObjectId();

        attributes = new ArrayList<>();
        for (Pair<String, String> p: atts) {
            attributes.add(p);
        }
    }

    public static String getPartitionKey(
        List<Pair<String, String>> attributes, GETableClassEnumPb type
    ) {
        String out = "" + type.getNumber();
        for (Pair<String, String> att: attributes) {
            out += "_" + att.getLeft() + "::" + att.getRight();
        }
        return out;
    }

    public String getPartitionKey() {
        return IndexEntry.getPartitionKey(attributes, type);
    }

    public String getStoreKey() {
        return getPartitionKey() + "_" + LHUtil.toLhDbFormat(createdAt);
    }

    public Class<IndexEntryPb> getProtoBaseClass() {
        return IndexEntryPb.class;
    }

    public void initFrom(MessageOrBuilder p) {
        IndexEntryPbOrBuilder proto = (IndexEntryPbOrBuilder) p;
        type = proto.getKey().getType();
        for (AttributePb a: proto.getKey().getAttributesList()) {
            attributes.add(Pair.of(a.getKey(), a.getVal()));
        }
        createdAt = LHUtil.fromProtoTs(proto.getKey().getCreated());
        resultObjectId = proto.getStoreKey();
    }

    public IndexEntryPb.Builder toProto() {
        IndexKeyPb.Builder ib = IndexKeyPb.newBuilder()
            .setCreated(LHUtil.fromDate(createdAt))
            .setType(type);

        for (Pair<String, String> attribute: attributes) {
            ib.addAttributes(AttributePb.newBuilder()
                .setKey(attribute.getLeft())
                .setVal(attribute.getRight())
            );
        }
        return IndexEntryPb.newBuilder()
            .setKey(ib)
            .setStoreKey(resultObjectId);
    }

    public int hashCode() {
        String theString = getPartitionKey() + "asdfadpjgfawepo" + resultObjectId;
        return theString.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof IndexEntry)) return false;
        IndexEntry oe = (IndexEntry) o;
        return (
            oe.getPartitionKey().equals(getPartitionKey())
            && oe.resultObjectId.equals(resultObjectId)
        );
    }
}
