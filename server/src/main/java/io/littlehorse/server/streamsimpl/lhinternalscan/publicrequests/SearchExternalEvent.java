package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ExternalEventIdPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventPb.ExtEvtCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb;
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
public class SearchExternalEvent
    extends PublicScanRequest<SearchExternalEventPb, SearchExternalEventReplyPb, ExternalEventIdPb, ExternalEventId, SearchExternalEventReply> {

    public ExtEvtCriteriaCase type;
    public String wfRunId;
    private String externalEventDefName;
    private Optional<Boolean> isClaimed = Optional.empty();

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.EXTERNAL_EVENT;
    }

    public Class<SearchExternalEventPb> getProtoBaseClass() {
        return SearchExternalEventPb.class;
    }

    public void initFrom(Message proto) {
        SearchExternalEventPb p = (SearchExternalEventPb) proto;
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
                SearchExternalEventPb.ByExtEvtDefNameAndStatusPb externalEventDefNameAndStatus = p.getExternalEventDefNameAndStatus();
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

    public SearchExternalEventPb.Builder toProto() {
        SearchExternalEventPb.Builder out = SearchExternalEventPb.newBuilder();
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
                SearchExternalEventPb.ByExtEvtDefNameAndStatusPb.Builder byExtEvtDefNameAndStatusPb = SearchExternalEventPb.ByExtEvtDefNameAndStatusPb
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
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        List<String> searchAttributes = getSearchAttributes()
            .stream()
            .map(Attribute::getEscapedKey)
            .toList();
        List<GetableIndex<? extends Getable<?>>> indexConfigurations = new ExternalEvent()
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
            return TagStorageTypePb.LOCAL;
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
