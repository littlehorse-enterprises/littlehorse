package io.littlehorse.server.streamsimpl.searchutils;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.LHInternalSearchPb;
import io.littlehorse.common.proto.LHInternalSearchPb.SubsearchCase;
import io.littlehorse.common.proto.LHInternalSearchPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.internalsearches.HashedTagScan;
import io.littlehorse.server.streamsimpl.searchutils.internalsearches.LocalTagScan;
import io.littlehorse.server.streamsimpl.searchutils.internalsearches.ObjectIdPrefixScan;

public class LHInternalSearch extends LHSerializable<LHInternalSearchPb> {

    public BookmarkPb bookmark;
    public int limit;
    public GETableClassEnumPb objectType;

    public SubsearchCase type;
    public LocalTagScan localTag;
    public ObjectIdPrefixScan objectIdPrefix;
    public HashedTagScan hashedTag;

    public Class<LHInternalSearchPb> getProtoBaseClass() {
        return LHInternalSearchPb.class;
    }

    public LHInternalSearchPb.Builder toProto() {
        LHInternalSearchPb.Builder out = LHInternalSearchPb
            .newBuilder()
            .setLimit(limit)
            .setBookmark(bookmark)
            .setObjectType(objectType);

        switch (type) {
            case LOCAL_TAG:
                out.setLocalTag(localTag.toProto());
                break;
            case OBJECT_ID_PREFIX:
                out.setObjectIdPrefix(objectIdPrefix.toProto());
                break;
            case HASHED_TAG:
                out.setHashedTag(hashedTag.toProto());
                break;
            case SUBSEARCH_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        LHInternalSearchPbOrBuilder p = (LHInternalSearchPbOrBuilder) proto;
        limit = p.getLimit();
        if (p.hasBookmark()) bookmark = p.getBookmark();
        type = p.getSubsearchCase();

        switch (type) {
            case LOCAL_TAG:
                localTag = LocalTagScan.fromProto(p.getLocalTagOrBuilder());
                break;
            case OBJECT_ID_PREFIX:
                objectIdPrefix =
                    ObjectIdPrefixScan.fromProto(p.getObjectIdPrefixOrBuilder());
                break;
            case HASHED_TAG:
                hashedTag = HashedTagScan.fromProto(p.getHashedTagOrBuilder());
                break;
            case SUBSEARCH_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }

    public LHInternalSubSearch<?> getSubSearch() {
        switch (type) {
            case LOCAL_TAG:
                return localTag;
            case OBJECT_ID_PREFIX:
                return objectIdPrefix;
            case HASHED_TAG:
                return hashedTag;
            case SUBSEARCH_NOT_SET:
            default:
                throw new RuntimeException("not possible");
        }
    }

    public void setSubsearch(LHInternalSubSearch<?> ss) {
        if (ss.getClass().equals(LocalTagScan.class)) {
            type = SubsearchCase.LOCAL_TAG;
            localTag = (LocalTagScan) ss;
        } else if (ss.getClass().equals(ObjectIdPrefixScan.class)) {
            type = SubsearchCase.OBJECT_ID_PREFIX;
            objectIdPrefix = (ObjectIdPrefixScan) ss;
        } else if (ss.getClass().equals(HashedTagScan.class)) {
            type = SubsearchCase.HASHED_TAG;
            hashedTag = (HashedTagScan) ss;
        } else {
            throw new RuntimeException("Not possible");
        }
    }
}
