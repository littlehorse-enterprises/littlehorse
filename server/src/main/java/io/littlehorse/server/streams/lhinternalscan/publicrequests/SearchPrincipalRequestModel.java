package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.PrincipalIdList;
import io.littlehorse.sdk.common.proto.SearchPrincipalRequest;
import io.littlehorse.sdk.common.proto.SearchPrincipalRequest.PrincipalCriteriaCase;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchPrincipalRequestReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchPrincipalRequestModel
        extends PublicScanRequest<
                SearchPrincipalRequest, PrincipalIdList, PrincipalId, PrincipalIdModel, SearchPrincipalRequestReply> {

    private PrincipalCriteriaCase type;
    private Boolean isAdmin;
    private String tenantId;
    private Date earliestStart;
    private Date latestStart;

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
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable((latestStart)));
    }

    @Override
    public SearchPrincipalRequest.Builder toProto() throws LHApiException {
        SearchPrincipalRequest.Builder builder = SearchPrincipalRequest.newBuilder();

        if (bookmark != null) builder.setBookmark(bookmark.toByteString());

        if (limit != null) builder.setLimit(limit);

        if (earliestStart != null) builder.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) builder.setLatestStart(LHUtil.fromDate(latestStart));

        switch (type) {
            case ISADMIN:
                builder.setIsAdmin(isAdmin);
                break;
            case TENANTID:
                builder.setTenantId(tenantId);
                break;
            default:
                break;
        }

        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SearchPrincipalRequest p = (SearchPrincipalRequest) proto;

        if (p.hasBookmark()) {
            try {
                this.bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (InvalidProtocolBufferException exn) {
                throw new LHSerdeException(String.format("Failed to load bookmark: %s", exn.getMessage()));
            }
        }
        if (p.hasLimit()) {
            this.limit = p.getLimit();
        }

        if (p.hasEarliestStart()) earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        if (p.hasLatestStart()) latestStart = LHUtil.fromProtoTs(p.getLatestStart());

        type = p.getPrincipalCriteriaCase();
        switch (type) {
            case ISADMIN:
                isAdmin = p.getIsAdmin();
                break;
            case TENANTID:
                tenantId = p.getTenantId();
                break;
            default:
                break;
        }
    }

    @Override
    public Class<SearchPrincipalRequest> getProtoBaseClass() {
        return SearchPrincipalRequest.class;
    }

    @Override
    public List<Attribute> getSearchAttributes() throws LHApiException {
        if (tenantId != null && isAdmin != null)
            return List.of(new Attribute("tenantId", tenantId), new Attribute("isAdmin", String.valueOf(isAdmin)));

        if (tenantId != null) return List.of(new Attribute("tenantId", tenantId));

        if (isAdmin != null) return List.of(new Attribute("isAdmin", String.valueOf(isAdmin)));

        return List.of();
    }
}
