package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.CorrelatedEventIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.CorrelatedEventId;
import io.littlehorse.sdk.common.proto.CorrelatedEventIdList;
import io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchCorrelatedEventReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SearchCorrelatedEventRequestModel
        extends PublicScanRequest<
                SearchCorrelatedEventRequest,
                CorrelatedEventIdList,
                CorrelatedEventId,
                CorrelatedEventIdModel,
                SearchCorrelatedEventReply> {

    private Date earliestStart;
    private Date latestStart;
    private ExternalEventDefIdModel externalEventDefId;
    private Boolean hasExternalEvents;

    @Override
    public Class<SearchCorrelatedEventRequest> getProtoBaseClass() {
        return SearchCorrelatedEventRequest.class;
    }

    @Override
    public SearchCorrelatedEventRequest.Builder toProto() {
        SearchCorrelatedEventRequest.Builder out =
                SearchCorrelatedEventRequest.newBuilder().setExternalEventDefId(externalEventDefId.toProto());

        if (earliestStart != null) out.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) out.setLatestStart(LHUtil.fromDate(latestStart));
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }

        if (hasExternalEvents != null) out.setHasExternalEvents(hasExternalEvents);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        SearchCorrelatedEventRequest p = (SearchCorrelatedEventRequest) proto;
        this.externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, ignored);

        if (p.hasHasExternalEvents()) hasExternalEvents = p.getHasExternalEvents();

        if (p.hasEarliestStart()) earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        if (p.hasLatestStart()) latestStart = LHUtil.fromProtoTs(p.getLatestStart());

        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.CORRELATED_EVENT;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        List<Attribute> out = new ArrayList<>();
        out.add(new Attribute("extEvtDefName", this.getExternalEventDefId().getName()));

        if (this.hasExternalEvents != null) {
            out.add(new Attribute("hasExtEvts", String.valueOf(hasExternalEvents)));
        }
        return out;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }
}
