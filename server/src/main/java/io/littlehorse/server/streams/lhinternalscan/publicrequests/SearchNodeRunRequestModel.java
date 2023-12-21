package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.NodeRunIdList;
import io.littlehorse.sdk.common.proto.SearchNodeRunRequest;
import io.littlehorse.sdk.common.proto.SearchNodeRunRequest.NodeType;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.lhinternalscan.util.TagScanModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SearchNodeRunRequestModel
        extends PublicScanRequest<SearchNodeRunRequest, NodeRunIdList, NodeRunId, NodeRunIdModel, SearchNodeRunReply> {

    private NodeType nodeType;
    private LHStatus status;
    private Date earliestStart;
    private Date latestStart;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.NODE_RUN;
    }

    @Override
    public Class<SearchNodeRunRequest> getProtoBaseClass() {
        return SearchNodeRunRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchNodeRunRequest p = (SearchNodeRunRequest) proto;
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

        nodeType = p.getNodeType();
        status = p.getStatus();
    }

    @Override
    public SearchNodeRunRequest.Builder toProto() {
        SearchNodeRunRequest.Builder out =
                SearchNodeRunRequest.newBuilder().setNodeType(nodeType).setStatus(status);
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }

        if (earliestStart != null) out.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) out.setLatestStart(LHUtil.fromDate(latestStart));
        return out;
    }

    public static SearchNodeRunRequestModel fromProto(SearchNodeRunRequest proto, ExecutionContext context) {
        SearchNodeRunRequestModel out = new SearchNodeRunRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.CORE; // only object id prefix scan supported.
    }

    @Override
    public ScanBoundary<?, NodeRunIdModel> getScanBoundary(RequestExecutionContext ctx) throws LHApiException {
        return new TagScanModel<NodeRunIdModel>(getObjectType(), earliestStart, latestStart)
                .addAttributes("status", status.toString())
                .addAttributes("type", nodeType.toString());
    }
}
