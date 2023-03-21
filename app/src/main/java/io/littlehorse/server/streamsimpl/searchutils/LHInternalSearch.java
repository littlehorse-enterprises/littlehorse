package io.littlehorse.server.streamsimpl.searchutils;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.AttributePbOrBuilder;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.LHInternalSearchPb;
import io.littlehorse.common.proto.LHInternalSearchPb.PrefixCase;
import io.littlehorse.common.proto.LHInternalSearchPb.RepeatedAttributePb;
import io.littlehorse.common.proto.LHInternalSearchPbOrBuilder;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.ArrayList;
import java.util.List;

public class LHInternalSearch extends LHSerializable<LHInternalSearchPb> {

    public int limit;
    public BookmarkPb bookmark;
    public GETableClassEnumPb objectType;
    public String partitionKey;

    public PrefixCase prefixType;
    public String objectIdPrefix;
    public List<Attribute> tagPrefix;

    public LHInternalSearch() {
        tagPrefix = new ArrayList<>();
    }

    public Class<LHInternalSearchPb> getProtoBaseClass() {
        return LHInternalSearchPb.class;
    }

    public LHInternalSearchPb.Builder toProto() {
        LHInternalSearchPb.Builder out = LHInternalSearchPb
            .newBuilder()
            .setLimit(limit)
            .setObjectType(objectType);

        if (bookmark != null) out.setBookmark(bookmark);
        if (partitionKey != null) {
            out.setPartitionKey(partitionKey);
        }

        switch (prefixType) {
            case OBJECT_ID_PREFIX:
                out.setObjectIdPrefix(objectIdPrefix);
                break;
            case TAG_PREFIX:
                RepeatedAttributePb.Builder tpb = RepeatedAttributePb.newBuilder();
                for (Attribute attr : tagPrefix) {
                    tpb.addAttributes(attr.toProto());
                }
                out.setTagPrefix(tpb);
                break;
            case PREFIX_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        LHInternalSearchPbOrBuilder p = (LHInternalSearchPbOrBuilder) proto;
        limit = p.getLimit();
        objectType = p.getObjectType();
        if (p.hasBookmark()) bookmark = p.getBookmark();
        if (p.hasPartitionKey()) partitionKey = p.getPartitionKey();

        prefixType = p.getPrefixCase();
        switch (prefixType) {
            case OBJECT_ID_PREFIX:
                objectIdPrefix = p.getObjectIdPrefix();
                break;
            case TAG_PREFIX:
                for (AttributePbOrBuilder apb : p
                    .getTagPrefix()
                    .getAttributesList()) {
                    tagPrefix.add(Attribute.fromProto(apb));
                }
                break;
            case PREFIX_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public String getStartPrefix() {
        switch (prefixType) {
            case OBJECT_ID_PREFIX:
                return (
                    StoreUtils.getFullStoreKey(
                        objectIdPrefix,
                        GETable.getCls(objectType)
                    ) +
                    "/"
                );
            case TAG_PREFIX:
                return (
                    StoreUtils.getFullStoreKey(
                        Tag.getAttributeString(objectType, tagPrefix),
                        Tag.class
                    ) +
                    "/"
                );
            case PREFIX_NOT_SET:
            default:
                throw new RuntimeException("not possible");
        }
    }

    public String getEndPrefix() {
        switch (prefixType) {
            case OBJECT_ID_PREFIX:
            case TAG_PREFIX:
                return getStartPrefix() + "~";
            case PREFIX_NOT_SET:
            default:
                throw new RuntimeException("not possible");
        }
    }
}
