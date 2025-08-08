package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.SearchStructDefRequest;
import io.littlehorse.sdk.common.proto.SearchStructDefRequest.StructDefCriteriaCase;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructDefIdList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchStructDefReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchStructDefRequestModel
        extends PublicScanRequest<
                SearchStructDefRequest, StructDefIdList, StructDefId, StructDefIdModel, SearchStructDefReply> {

    private StructDefCriteriaCase type;
    private String name;
    private String prefix;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.STRUCT_DEF;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.GLOBAL_METADATA;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        if (prefix != null && !prefix.equals("")) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, prefix, prefix + "~");
        } else if (name != null && !name.isEmpty()) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, name, name + "/");
        } else {
            return ObjectIdScanBoundaryStrategy.prefixMetadataScan();
        }
    }

    @Override
    public Builder<SearchStructDefRequest.Builder> toProto() {
        SearchStructDefRequest.Builder out = SearchStructDefRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }

        switch (type) {
            case NAME:
                out.setName(name);
                break;
            case PREFIX:
                out.setPrefix(prefix);
                break;
            case STRUCTDEFCRITERIA_NOT_SET:
                // nothing to do, we just return all the StuctDef's
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SearchStructDefRequest p = (SearchStructDefRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getStructDefCriteriaCase();
        switch (type) {
            case NAME:
                name = p.getName();
                break;
            case PREFIX:
                prefix = p.getPrefix();
                break;
            case STRUCTDEFCRITERIA_NOT_SET:
                // nothing to do, we just return all the StructDef's
        }
    }

    @Override
    public Class<SearchStructDefRequest> getProtoBaseClass() {
        return SearchStructDefRequest.class;
    }

    public static SearchStructDefRequestModel fromProto(SearchStructDefRequest proto, ExecutionContext context) {
        SearchStructDefRequestModel out = new SearchStructDefRequestModel();
        out.initFrom(proto, context);
        return out;
    }
}
