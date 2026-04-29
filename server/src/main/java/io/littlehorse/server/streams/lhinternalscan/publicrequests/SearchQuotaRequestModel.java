package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.QuotaId;
import io.littlehorse.sdk.common.proto.QuotaIdList;
import io.littlehorse.sdk.common.proto.SearchQuotaRequest;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchQuotaRequestReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import java.util.Optional;

public class SearchQuotaRequestModel
        extends PublicScanRequest<SearchQuotaRequest, QuotaIdList, QuotaId, QuotaIdModel, SearchQuotaRequestReply> {

    private String tenantId;
    private PrincipalIdModel principal;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SearchQuotaRequest p = (SearchQuotaRequest) proto;

        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (InvalidProtocolBufferException exn) {
                throw new LHSerdeException(String.format("Failed to load bookmark: %s", exn.getMessage()));
            }
        }
        if (p.hasLimit()) {
            limit = p.getLimit();
        }
        if (p.hasTenantId() && !p.getTenantId().isBlank()) {
            tenantId = p.getTenantId();
        }
        if (p.hasPrincipal()) {
            principal = LHSerializable.fromProto(p.getPrincipal(), PrincipalIdModel.class, context);
        }
    }

    @Override
    public SearchQuotaRequest.Builder toProto() {
        SearchQuotaRequest.Builder builder = SearchQuotaRequest.newBuilder();
        if (bookmark != null) builder.setBookmark(bookmark.toByteString());
        if (limit != null) builder.setLimit(limit);
        if (tenantId != null) builder.setTenantId(tenantId);
        if (principal != null) builder.setPrincipal(principal.toProto());
        return builder;
    }

    @Override
    public Class<SearchQuotaRequest> getProtoBaseClass() {
        return SearchQuotaRequest.class;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.QUOTA;
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
        if (tenantId == null && principal == null) {
            return ObjectIdScanBoundaryStrategy.prefixMetadataScan();
        }
        return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
    }

    @Override
    public List<Attribute> getSearchAttributes() throws LHApiException {
        if (principal != null && tenantId == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Must provide tenant_id when searching by principal");
        }

        if (tenantId != null && principal != null) {
            return List.of(new Attribute("tenantId", tenantId), new Attribute("principalId", principal.getId()));
        }

        if (tenantId != null) {
            return List.of(new Attribute("tenantId", tenantId));
        }

        return List.of();
    }
}
