package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.ExternalEventIdPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventPb;
import io.littlehorse.jlib.common.proto.SearchExternalEventPb.ExtEvtCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchExternalEventReplyPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndexRegistry;
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
    private Optional<Boolean> isClaimed;

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
                isClaimed.ifPresent(byExtEvtDefNameAndStatusPb::setIsClaimed);
                out.setExternalEventDefNameAndStatus(byExtEvtDefNameAndStatusPb);
                break;
            case EXTEVTCRITERIA_NOT_SET:
                throw new IllegalArgumentException(
                    "%s type is not supported yet".formatted(type)
                );
        }

        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();

        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;
        GetableIndex getableIndex = GetableIndexRegistry
            .getInstance()
            .findConfigurationForAttributes(
                ExternalEvent.class,
                getSearchAttributes()
            );
        List<Attribute> attributes = buildTagAttributes();

        if (type == ExtEvtCriteriaCase.WF_RUN_ID) {
            out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
            out.partitionKey = wfRunId;
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(wfRunId + "/")
                    .build();
        } else if (
            type.equals(ExtEvtCriteriaCase.EXTERNAL_EVENT_DEF_NAME_AND_STATUS)
        ) {
            out.setType(ScanBoundaryCase.TAG_SCAN);
            InternalScanPb.TagScanPb.Builder tagScanBuilder = InternalScanPb.TagScanPb
                .newBuilder()
                .addAllAttributes(
                    attributes
                        .stream()
                        .map(attribute -> attribute.toProto().build())
                        .toList()
                );
            out.setTagScan(tagScanBuilder.build());
            if (getableIndex.getTagStorageTypePb() == TagStorageTypePb.LOCAL) {
                out.setStoreName(ServerTopology.CORE_STORE);
                out.setResultType(ScanResultTypePb.OBJECT_ID);
            } else {
                out.setStoreName(ServerTopology.CORE_REPARTITION_STORE);
                out.setResultType(ScanResultTypePb.OBJECT_ID);
                out.setPartitionKey(getableIndex.getPartitionKeyForAttrs(attributes));
            }
        } else {
            throw new IllegalArgumentException(
                "%s type is not supported yet".formatted(type)
            );
        }
        return out;
    }

    private List<Attribute> buildTagAttributes() {
        return isClaimed
            .map(claimed ->
                List.of(
                    new Attribute("extEvtDefName", externalEventDefName),
                    new Attribute("isClaimed", String.valueOf(claimed))
                )
            )
            .orElse(List.of(new Attribute("extEvtDefName", externalEventDefName)));
    }

    private List<String> getSearchAttributes() {
        switch (type) {
            case EXTERNAL_EVENT_DEF_NAME_AND_STATUS:
                return isClaimed
                    .map(aBoolean -> List.of("extEvtDefName", "isClaimed"))
                    .orElse(List.of("extEvtDefName"));
            default:
                throw new IllegalArgumentException(
                    "%s type is not supported yet".formatted(type)
                );
        }
    }
}
