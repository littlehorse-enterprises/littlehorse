package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.FormDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormDefId;
import io.littlehorse.sdk.common.proto.FormDefIdList;
import io.littlehorse.sdk.common.proto.SearchFormDefRequest;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchFormDefReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchFormDefRequestModel
        extends PublicScanRequest<SearchFormDefRequest, FormDefIdList, FormDefId, FormDefIdModel, SearchFormDefReply> {

    private String name;
    private String prefix;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.FORM_DEF;
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
        if (prefix != null && !prefix.isEmpty()) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, prefix, prefix + "~");
        } else if (name != null && !name.isEmpty()) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, name + "/", name + "/~");
        }
        return ObjectIdScanBoundaryStrategy.prefixMetadataScan();
    }

    @Override
    public SearchFormDefRequest.Builder toProto() {
        SearchFormDefRequest.Builder out = SearchFormDefRequest.newBuilder();
        if (bookmark != null) out.setBookmark(bookmark.toByteString());
        if (limit != null) out.setLimit(limit);
        if (name != null) out.setName(name);
        if (prefix != null) out.setPrefix(prefix);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SearchFormDefRequest p = (SearchFormDefRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        if (p.hasName()) name = p.getName();
        if (p.hasPrefix()) prefix = p.getPrefix();
    }

    @Override
    public Class<SearchFormDefRequest> getProtoBaseClass() {
        return SearchFormDefRequest.class;
    }

    public static SearchFormDefRequestModel fromProto(SearchFormDefRequest proto, ExecutionContext context) {
        SearchFormDefRequestModel out = new SearchFormDefRequestModel();
        out.initFrom(proto, context);
        return out;
    }
}
