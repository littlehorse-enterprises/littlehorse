package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.PrincipalIdList;
import io.littlehorse.sdk.common.proto.SearchPrincipalRequest;
import io.littlehorse.sdk.common.proto.SearchPrincipalRequest.PrincipalCriteriaCase;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchPrincipalRequestReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchPrincipalRequestModel
        extends PublicScanRequest<
                SearchPrincipalRequest, PrincipalIdList, PrincipalId, PrincipalIdModel, SearchPrincipalRequestReply> {

    private PrincipalCriteriaCase type;
    private boolean isAdmin;
    private String tenant;
    private Date createdAt;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.PRINCIPAL;
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

    @Override
    public SearchPrincipalRequest.Builder toProto() throws LHApiException {
        SearchPrincipalRequest.Builder builder = SearchPrincipalRequest.newBuilder();

        if (bookmark != null) builder.setBookmark(bookmark.toByteString());

        if (limit != null) builder.setLimit(limit);

        switch (type) {
            case ISADMIN:
                builder.setIsAdmin(isAdmin);
                break;
            case TENANT:
                builder.setTenant(tenant);
                break;
            case CREATED_AT:
                builder.setCreatedAt(LHUtil.fromDate(createdAt));
                break;
            case PRINCIPALCRITERIA_NOT_SET:
                throw new LHApiException(Status.FAILED_PRECONDITION, "Principal query criteria is not valid.");
        }

        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        SearchPrincipalRequest p = (SearchPrincipalRequest) proto;

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

        type = p.getPrincipalCriteriaCase();
        switch (type) {
            case ISADMIN:
                isAdmin = p.getIsAdmin();
                break;
            case TENANT:
                tenant = p.getTenant();
                break;
            case CREATED_AT:
                createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
                break;
            case PRINCIPALCRITERIA_NOT_SET:
        }
    }

    @Override
    public Class<SearchPrincipalRequest> getProtoBaseClass() {
        return SearchPrincipalRequest.class;
    }
}
