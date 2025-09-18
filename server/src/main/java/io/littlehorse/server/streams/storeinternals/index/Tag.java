package io.littlehorse.server.streams.storeinternals.index;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.TagPb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Setter
public class Tag extends Storeable<TagPb> {

    public TagStorageType tagType;
    public GetableClassEnum objectType;
    public List<Attribute> attributes;
    public Date createdAt;
    public String describedObjectId;

    public Tag() {
        attributes = new ArrayList<>();
    }

    public TagStorageType getTagStorageType() {
        return this.tagType;
    }

    @SafeVarargs
    public Tag(AbstractGetable<?> getable, TagStorageType type, Pair<String, String>... atts) {
        this(getable, type, Arrays.asList(atts));
    }

    @SuppressWarnings("unchecked")
    public Tag(AbstractGetable<?> getable, TagStorageType type, Collection<Pair<String, String>> atts) {
        this();
        this.objectType = AbstractGetable.getTypeEnum((Class<? extends AbstractGetable<?>>) getable.getClass());
        createdAt = getable.getCreatedAt();
        describedObjectId = getable.getObjectId().toString();
        this.tagType = type;

        for (Pair<String, String> p : atts) {
            attributes.add(new Attribute(p.getLeft(), p.getRight()));
        }
    }

    public Tag(TagStorageType type, GetableClassEnum objectType, Collection<Attribute> attributes) {
        this();
        this.tagType = type;
        this.objectType = objectType;
        this.createdAt = new Date();
        this.attributes.addAll(attributes);
    }

    public Class<TagPb> getProtoBaseClass() {
        return TagPb.class;
    }

    @Override
    public TagPb.Builder toProto() {
        TagPb.Builder out = TagPb.newBuilder()
                .setObjectType(objectType)
                .setDescribedObjectId(describedObjectId)
                .setCreated(LHUtil.fromDate(createdAt))
                .setStoreKey(this.getStoreKey())
                .setTagType(tagType);

        for (Attribute attr : attributes) {
            out.addAttributes(attr.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TagPb p = (TagPb) proto;
        objectType = p.getObjectType();
        describedObjectId = p.getDescribedObjectId();
        createdAt = LHUtil.fromProtoTs(p.getCreated());

        for (AttributePb attr : p.getAttributesList()) {
            attributes.add(Attribute.fromProto(attr, context));
        }

        tagType = p.getTagType();
    }

    @Override
    public StoreableType getType() {
        return StoreableType.TAG;
    }

    public static boolean isLocal(TagStorageType type) {
        return type == TagStorageType.LOCAL;
    }

    public String getAttributeString() {
        return getAttributeString(objectType, attributes);
    }

    public String getPartitionKey() {
        return getAttributeString(getObjectType(), attributes);
    }

    public static String getAttributeString(GetableClassEnum objectType, List<Attribute> attributes) {
        StringBuilder builder = new StringBuilder();
        builder.append(objectType.getNumber());
        builder.append("/");
        for (Attribute attr : attributes) {
            builder.append("__");
            builder.append(attr.getEscapedKey());
            builder.append("_");
            builder.append(attr.getEscapedVal());
        }
        return builder.toString();
    }

    public boolean isRemote() {
        // When we re-enable REMOTE tags, we will need to check this.tagType;
        return false;
    }

    public String getStoreKey() {
        StringBuilder builder = new StringBuilder(getAttributeString());

        builder.append("/");
        builder.append(LHUtil.toLhDbFormat(createdAt));
        builder.append("/");
        // builder.append(describedObjectId);
        return builder.toString();
    }

    public int hashCode() {
        return getStoreKey().hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Tag)) return false;
        Tag oe = (Tag) o;
        return getStoreKey().equals(oe.getStoreKey());
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public String getDescribedObjectId() {
        return describedObjectId;
    }

    public GetableClassEnum getObjectType() {
        return objectType;
    }
}
