package io.littlehorse.common.model.index;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.TagPb;
import io.littlehorse.common.proto.TagPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class Tag extends Storeable<TagPb> {

    public GETableClassEnumPb type;
    public List<Pair<String, String>> attributes;
    public Date createdAt;
    public String describedObjectId;

    public List<String> counterKeys;

    public Class<TagPb> getProtoBaseClass() {
        return TagPb.class;
    }

    public TagPb.Builder toProto() {
        TagPb.Builder out = TagPb
            .newBuilder()
            .setType(type)
            .setDescribedObjectId(describedObjectId)
            .setCreated(LHUtil.fromDate(createdAt));

        for (Pair<String, String> attr : attributes) {
            out.addAttributes(
                AttributePb
                    .newBuilder()
                    .setKey(attr.getLeft())
                    .setVal(attr.getRight())
            );
        }

        for (String counterKey : counterKeys) {
            out.addCounterKeys(counterKey);
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TagPbOrBuilder p = (TagPbOrBuilder) proto;
        type = p.getType();
        describedObjectId = p.getDescribedObjectId();
        createdAt = LHUtil.fromProtoTs(p.getCreated());

        for (AttributePb attr : p.getAttributesList()) {
            attributes.add(Pair.of(attr.getKey(), attr.getVal()));
        }
        for (String counterKey : p.getCounterKeysList()) {
            counterKeys.add(counterKey);
        }
    }

    public String getTagAttributes() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getNumber());
        builder.append("/");

        for (Pair<String, String> attr : attributes) {
            builder.append("__");
            builder.append(attr.getLeft());
            builder.append("_");
            builder.append(attr.getRight());
        }

        return builder.toString();
    }

    public String getObjectId() {
        StringBuilder builder = new StringBuilder(getTagAttributes());

        builder.append("/");
        builder.append(LHUtil.toLhDbFormat(createdAt));
        builder.append("/");
        builder.append(describedObjectId);
        return builder.toString();
    }

    public Tag() {
        attributes = new ArrayList<>();
        counterKeys = new ArrayList<>();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public Tag(GETable<?> getable, Pair<String, String>... atts) {
        this.type =
            GETable.getTypeEnum((Class<? extends GETable<?>>) getable.getClass());
        createdAt = getable.getCreatedAt();
        describedObjectId = getable.getObjectId();

        attributes = new ArrayList<>();
        for (Pair<String, String> p : atts) {
            attributes.add(p);
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
