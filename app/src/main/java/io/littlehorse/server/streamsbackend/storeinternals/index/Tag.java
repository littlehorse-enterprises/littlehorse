package io.littlehorse.server.streamsbackend.storeinternals.index;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.TagPb;
import io.littlehorse.common.proto.TagPbOrBuilder;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class Tag extends Storeable<TagPb> {

    public TagStorageTypePb tagType;
    public GETableClassEnumPb objectType;
    public List<Attribute> attributes;
    public Date createdAt;
    public String describedObjectId;

    public Class<TagPb> getProtoBaseClass() {
        return TagPb.class;
    }

    public TagPb.Builder toProto() {
        TagPb.Builder out = TagPb
            .newBuilder()
            .setObjectType(objectType)
            .setDescribedObjectId(describedObjectId)
            .setCreated(LHUtil.fromDate(createdAt))
            .setTagType(tagType);

        for (Attribute attr : attributes) {
            out.addAttributes(attr.toProto());
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TagPbOrBuilder p = (TagPbOrBuilder) proto;
        objectType = p.getObjectType();
        describedObjectId = p.getDescribedObjectId();
        createdAt = LHUtil.fromProtoTs(p.getCreated());

        for (AttributePb attr : p.getAttributesList()) {
            attributes.add(Attribute.fromProto(attr));
        }
    }

    public String getAttributeString() {
        return getAttributeString(objectType, attributes);
    }

    public static String getAttributeString(
        GETableClassEnumPb objectType,
        List<Attribute> attributes
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(objectType.toString());
        builder.append("/");
        for (Attribute attr : attributes) {
            builder.append("__");
            builder.append(attr.getEscapedKey());
            builder.append("_");
            builder.append(attr.getEscapedVal());
        }
        return builder.toString();
    }

    public String getObjectId() {
        StringBuilder builder = new StringBuilder(getAttributeString());

        builder.append("/");
        builder.append(LHUtil.toLhDbFormat(createdAt));
        builder.append("/");
        builder.append(describedObjectId);
        return builder.toString();
    }

    public Tag() {
        attributes = new ArrayList<>();
    }

    public String getCounterKey(int partition) {
        switch (tagType) {
            case LOCAL_COUNTED:
                return DiscreteTagLocalCounter.getObjectId(
                    getAttributeString(),
                    partition
                );
            case LOCAL_HASH_UNCOUNTED:
            case LOCAL_UNCOUNTED:
            case REMOTE_HASH_UNCOUNTED:
            case UNRECOGNIZED:
                return null;
        }
        throw new RuntimeException("Not possible");
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public Tag(
        GETable<?> getable,
        TagStorageTypePb type,
        Pair<String, String>... atts
    ) {
        this();
        this.objectType =
            GETable.getTypeEnum((Class<? extends GETable<?>>) getable.getClass());
        createdAt = getable.getCreatedAt();
        describedObjectId = getable.getObjectId();

        for (Pair<String, String> p : atts) {
            attributes.add(new Attribute(p.getLeft(), p.getRight()));
        }
    }

    public int hashCode() {
        return getObjectId().hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Tag)) return false;
        Tag oe = (Tag) o;
        return getObjectId().equals(oe.getObjectId());
    }
}
