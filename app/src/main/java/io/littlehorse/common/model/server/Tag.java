package io.littlehorse.common.model.server;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.IndexKeyPb;
import io.littlehorse.common.proto.TagPb;
import io.littlehorse.common.proto.TagPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class Tag extends LHSerializable<TagPb> {

    public GETableClassEnumPb type;
    public List<Pair<String, String>> attributes;
    public Date createdAt;

    public String resultObjectId;

    public Tag() {
        attributes = new ArrayList<>();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public Tag(GETable<?> getable, Pair<String, String>... atts) {
        this.type =
            GETable.getTypeEnum((Class<? extends GETable<?>>) getable.getClass());
        createdAt = getable.getCreatedAt();
        resultObjectId = getable.getSubKey();

        attributes = new ArrayList<>();
        for (Pair<String, String> p : atts) {
            attributes.add(p);
        }
    }

    public static String getPartitionKey(
        List<Pair<String, String>> attributes,
        GETableClassEnumPb type
    ) {
        String out = "" + type.getNumber();
        for (Pair<String, String> att : attributes) {
            out += "_" + att.getLeft() + "::" + att.getRight();
        }
        return out;
    }

    public String getPartitionKey() {
        return Tag.getPartitionKey(attributes, type);
    }

    public String getStoreKey() {
        return getPartitionKey() + "_" + LHUtil.toLhDbFormat(createdAt);
    }

    public Class<TagPb> getProtoBaseClass() {
        return TagPb.class;
    }

    public void initFrom(MessageOrBuilder p) {
        TagPbOrBuilder proto = (TagPbOrBuilder) p;
        type = proto.getKey().getType();
        for (AttributePb a : proto.getKey().getAttributesList()) {
            attributes.add(Pair.of(a.getKey(), a.getVal()));
        }
        createdAt = LHUtil.fromProtoTs(proto.getKey().getCreated());
        resultObjectId = proto.getStoreKey();
    }

    public TagPb.Builder toProto() {
        IndexKeyPb.Builder ib = IndexKeyPb
            .newBuilder()
            .setCreated(LHUtil.fromDate(createdAt))
            .setType(type);

        for (Pair<String, String> attribute : attributes) {
            ib.addAttributes(
                AttributePb
                    .newBuilder()
                    .setKey(attribute.getLeft())
                    .setVal(
                        attribute.getRight() == null ? "null" : attribute.getRight()
                    )
            );
        }
        return TagPb.newBuilder().setKey(ib).setStoreKey(resultObjectId);
    }

    public int hashCode() {
        String theString = getPartitionKey() + "asdfadpjgfawepo" + resultObjectId;
        return theString.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Tag)) return false;
        Tag oe = (Tag) o;
        return (
            oe.getPartitionKey().equals(getPartitionKey()) &&
            oe.resultObjectId.equals(resultObjectId)
        );
    }
}
