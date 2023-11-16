package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.ExternalEventIdList;
import io.littlehorse.sdk.common.proto.SearchExternalEventRequest;
import io.littlehorse.sdk.common.proto.SearchExternalEventRequest.ExtEvtCriteriaCase;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SearchExternalEventRequestModel
        extends PublicScanRequest<
                SearchExternalEventRequest,
                ExternalEventIdList,
                ExternalEventId,
                ExternalEventIdModel,
                SearchExternalEventReply> {

    public ExtEvtCriteriaCase type;
    public String wfRunId;
    private String externalEventDefName;
    private Optional<Boolean> isClaimed = Optional.empty();

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.EXTERNAL_EVENT;
    }

    public Class<SearchExternalEventRequest> getProtoBaseClass() {
        return SearchExternalEventRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchExternalEventRequest p = (SearchExternalEventRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        type = p.getExtEvtCriteriaCase();
        switch (type) {
            case WF_RUN_ID:
                wfRunId = p.getWfRunId();
                break;
            case EXTERNAL_EVENT_DEF_NAME_AND_STATUS:
                SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest externalEventDefNameAndStatus =
                        p.getExternalEventDefNameAndStatus();
                externalEventDefName = externalEventDefNameAndStatus.getExternalEventDefName();
                if (externalEventDefNameAndStatus.hasIsClaimed()) {
                    isClaimed = Optional.of(externalEventDefNameAndStatus.getIsClaimed());
                } else {
                    isClaimed = Optional.empty();
                }
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new IllegalArgumentException("%s type is not supported yet".formatted(type));
        }
    }

    public SearchExternalEventRequest.Builder toProto() {
        SearchExternalEventRequest.Builder out = SearchExternalEventRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case WF_RUN_ID:
                out.setWfRunId(wfRunId);
                break;
            case EXTERNAL_EVENT_DEF_NAME_AND_STATUS:
                SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest.Builder byExtEvtDefNameAndStatusPb =
                        SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest.newBuilder()
                                .setExternalEventDefName(externalEventDefName);
                isClaimed.ifPresent(b -> byExtEvtDefNameAndStatusPb.setIsClaimed(b));
                out.setExternalEventDefNameAndStatus(byExtEvtDefNameAndStatusPb);
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new IllegalArgumentException("%s type is not supported yet".formatted(type));
        }

        return out;
    }

    public List<Attribute> getSearchAttributes() {
        return isClaimed
                .map(claimed -> List.of(
                        new Attribute("extEvtDefName", externalEventDefName),
                        new Attribute("isClaimed", String.valueOf(claimed))))
                .orElse(List.of(new Attribute("extEvtDefName", externalEventDefName)));
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        List<String> searchAttributes =
                getSearchAttributes().stream().map(Attribute::getEscapedKey).toList();
        List<GetableIndex<? extends AbstractGetable<?>>> indexConfigurations =
                new ExternalEventModel().getIndexConfigurations();
        GetableIndex<? extends AbstractGetable<?>> getableIndex = indexConfigurations.stream()
                .filter(getableIndexConfiguration -> {
                    return getableIndexConfiguration.searchAttributesMatch(searchAttributes);
                })
                .findFirst()
                .orElse(null);
        if (getableIndex != null) {
            return getableIndex.getTagStorageType().get();
        } else {
            return TagStorageType.LOCAL;
        }
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (type == ExtEvtCriteriaCase.WF_RUN_ID) {
            return new ObjectIdScanBoundaryStrategy(wfRunId);
        } else if (type.equals(ExtEvtCriteriaCase.EXTERNAL_EVENT_DEF_NAME_AND_STATUS)) {
            return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
        } else {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Unrecognized search criteria: %s".formatted(type));
        }
    }

    @Override
    public LHStore getStoreType() {
        if (type == ExtEvtCriteriaCase.WF_RUN_ID) {
            return LHStore.CORE;
        } else if (type.equals(ExtEvtCriteriaCase.EXTERNAL_EVENT_DEF_NAME_AND_STATUS)) {
            return LHStore.REPARTITION;
        } else {
            throw new IllegalArgumentException("%s type is not supported yet".formatted(type));
        }
    }
}
