package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.ExternalEventIdList;
import io.littlehorse.sdk.common.proto.SearchExternalEventRequest;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
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

    private Date earliestStart;
    private Date latestStart;
    private ExternalEventDefIdModel externalEventDefId;
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

        if (p.hasEarliestStart()) earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        if (p.hasLatestStart()) latestStart = LHUtil.fromProtoTs(p.getLatestStart());

        externalEventDefId =
                ExternalEventDefIdModel.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, context);

        if (p.hasIsClaimed()) isClaimed = Optional.of(p.getIsClaimed());
    }

    public SearchExternalEventRequest.Builder toProto() {
        SearchExternalEventRequest.Builder builder = SearchExternalEventRequest.newBuilder();

        if (bookmark != null) builder.setBookmark(bookmark.toByteString());

        if (limit != null) builder.setLimit(limit);

        if (earliestStart != null) builder.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) builder.setLatestStart(LHUtil.fromDate(latestStart));

        builder.setExternalEventDefId(externalEventDefId.toProto());

        if (isClaimed.isPresent()) builder.setIsClaimed(isClaimed.get());

        return builder;
    }

    public List<Attribute> getSearchAttributes() {
        if (isClaimed.isPresent())
            return List.of(
                    new Attribute("externalEventDefId", externalEventDefId.toString()),
                    new Attribute("isClaimed", isClaimed.toString()));

        return List.of(new Attribute("externalEventDefId", externalEventDefId.toString()));
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
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }
}
