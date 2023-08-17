package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.SearchExternalEventRequest;
import io.littlehorse.sdk.common.proto.SearchExternalEventRequest.ExtEvtCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchExternalEventResponse;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SearchExternalEventRequestModel
    extends PublicScanRequest<SearchExternalEventRequest, SearchExternalEventResponse, ExternalEventId, ExternalEventIdModel, SearchExternalEventReply> {

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

    public void initFrom(Message proto) {
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
                SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest externalEventDefNameAndStatus = p.getExternalEventDefNameAndStatus();
                externalEventDefName =
                    externalEventDefNameAndStatus.getExternalEventDefName();
                if (externalEventDefNameAndStatus.hasIsClaimed()) {
                    isClaimed =
                        Optional.of(externalEventDefNameAndStatus.getIsClaimed());
                } else {
                    isClaimed = Optional.empty();
                }
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new IllegalArgumentException(
                    "%s type is not supported yet".formatted(type)
                );
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
                SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest.Builder byExtEvtDefNameAndStatusPb = SearchExternalEventRequest.ByExtEvtDefNameAndStatusRequest
                    .newBuilder()
                    .setExternalEventDefName(externalEventDefName);
                isClaimed.ifPresent(b -> byExtEvtDefNameAndStatusPb.setIsClaimed(b));
                out.setExternalEventDefNameAndStatus(byExtEvtDefNameAndStatusPb);
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new IllegalArgumentException(
                    "%s type is not supported yet".formatted(type)
                );
        }

        return out;
    }

    public List<Attribute> getSearchAttributes() {
        return isClaimed
            .map(claimed ->
                List.of(
                    new Attribute("extEvtDefName", externalEventDefName),
                    new Attribute("isClaimed", String.valueOf(claimed))
                )
            )
            .orElse(List.of(new Attribute("extEvtDefName", externalEventDefName)));
    }

    @Override
    public TagStorageType indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        List<String> searchAttributes = getSearchAttributes()
            .stream()
            .map(Attribute::getEscapedKey)
            .toList();
        List<GetableIndex<? extends Getable<?>>> indexConfigurations = new ExternalEventModel()
            .getIndexConfigurations();
        GetableIndex<? extends Getable<?>> getableIndex = indexConfigurations
            .stream()
            .filter(getableIndexConfiguration -> {
                return getableIndexConfiguration.searchAttributesMatch(
                    searchAttributes
                );
            })
            .findFirst()
            .orElse(null);
        if (getableIndex != null) {
            return getableIndex.getTagStorageTypePb().get();
        } else {
            return TagStorageType.LOCAL;
        }
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (type == ExtEvtCriteriaCase.WF_RUN_ID) {
            return new ObjectIdScanBoundaryStrategy(wfRunId);
        } else if (
            type.equals(ExtEvtCriteriaCase.EXTERNAL_EVENT_DEF_NAME_AND_STATUS)
        ) {
            return new TagScanBoundaryStrategy(
                searchAttributeString,
                Optional.empty(),
                Optional.empty()
            );
        } else {
            throw new IllegalArgumentException(
                "%s type is not supported yet".formatted(type)
            );
        }
    }
}
