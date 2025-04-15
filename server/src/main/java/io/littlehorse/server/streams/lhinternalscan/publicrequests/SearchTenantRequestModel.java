package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.SearchTenantRequest;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.sdk.common.proto.TenantIdList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTenantRequestReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchTenantRequestModel
        extends PublicScanRequest<
                SearchTenantRequest, TenantIdList, TenantId, TenantIdModel, SearchTenantRequestReply> {
    private ExecutionContext context;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        this.context = context;
        SearchTenantRequest p = (SearchTenantRequest) proto;
        if (p.hasBookmark()) {
            try {
                this.bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        if (p.hasLimit()) {
            this.limit = p.getLimit();
        }
    }

    @Override
    public SearchTenantRequest.Builder toProto() {
        SearchTenantRequest.Builder builder = SearchTenantRequest.newBuilder();

        if (bookmark != null) builder.setBookmark(bookmark.toByteString());

        if (limit != null) builder.setLimit(limit);

        return builder;
    }

    @Override
    public Class<SearchTenantRequest> getProtoBaseClass() {
        return SearchTenantRequest.class;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TENANT;
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
        return ObjectIdScanBoundaryStrategy.prefixMetadataScan();
    }

    public static SearchTenantRequestModel fromProto(SearchTenantRequest proto, ExecutionContext context) {
        SearchTenantRequestModel out = new SearchTenantRequestModel();
        out.initFrom(proto, context);
        return out;
    }
}
