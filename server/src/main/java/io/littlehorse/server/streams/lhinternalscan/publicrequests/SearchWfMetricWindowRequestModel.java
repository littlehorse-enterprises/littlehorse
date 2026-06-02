package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.sdk.common.proto.MetricWindowIdList;
import io.littlehorse.sdk.common.proto.SearchWfMetricWindowRequest;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfMetricWindowReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchWfMetricWindowRequestModel
        extends PublicScanRequest<
                SearchWfMetricWindowRequest,
                MetricWindowIdList,
                MetricWindowId,
                MetricWindowIdModel,
                SearchWfMetricWindowReply> {

    private String wfSpecName;
    private Date earliestStart;
    private Date latestStart;
    private boolean latestOnly;

    @Override
    public Class<SearchWfMetricWindowRequest> getProtoBaseClass() {
        return SearchWfMetricWindowRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SearchWfMetricWindowRequest p = (SearchWfMetricWindowRequest) proto;
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        if (p.hasLimit()) limit = p.getLimit();
        wfSpecName = p.getWfSpecName();
        latestOnly = p.hasLatestOnly() && p.getLatestOnly();
        if (latestOnly) {
            Date windowStart = LHUtil.getPreviousWindowDate();
            earliestStart = windowStart;
            latestStart = windowStart;
        } else {
            if (p.hasEarliestStart()) earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
            if (p.hasLatestStart()) latestStart = LHUtil.fromProtoTs(p.getLatestStart());
        }
    }

    @Override
    public SearchWfMetricWindowRequest.Builder toProto() {
        SearchWfMetricWindowRequest.Builder out =
                SearchWfMetricWindowRequest.newBuilder().setWfSpecName(wfSpecName);
        if (bookmark != null) out.setBookmark(bookmark.toByteString());
        if (limit != null) out.setLimit(limit);
        if (earliestStart != null) out.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) out.setLatestStart(LHUtil.fromDate(latestStart));
        out.setLatestOnly(latestOnly);
        return out;
    }

    public static SearchWfMetricWindowRequestModel fromProto(
            SearchWfMetricWindowRequest proto, ExecutionContext context) {
        SearchWfMetricWindowRequestModel out = new SearchWfMetricWindowRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.METRIC_WINDOW;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        if (wfSpecName == null || wfSpecName.isBlank()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "wfSpecName is required");
        }
        return TagStorageType.LOCAL;
    }

    @Override
    public List<Attribute> getSearchAttributes() throws LHApiException {
        return List.of(new Attribute("wfSpecName", wfSpecName));
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }
}
