package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.ListNodeRunsRequest;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListNodeRunReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListNodeRunsRequestModel
        extends PublicScanRequest<ListNodeRunsRequest, NodeRunList, NodeRun, NodeRunModel, ListNodeRunReply> {

    public WfRunIdModel wfRunId;
    public Integer threadRunNumber;

    public Class<ListNodeRunsRequest> getProtoBaseClass() {
        return ListNodeRunsRequest.class;
    }

    public ListNodeRunsRequest.Builder toProto() {
        ListNodeRunsRequest.Builder out = ListNodeRunsRequest.newBuilder().setWfRunId(wfRunId.toProto());

        if (bookmark != null) out.setBookmark(bookmark.toByteString());
        if (limit != null) out.setLimit(limit);
        if (threadRunNumber != null) out.setThreadRunNumber(threadRunNumber);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListNodeRunsRequest p = (ListNodeRunsRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasThreadRunNumber()) threadRunNumber = p.getThreadRunNumber();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.NODE_RUN;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (threadRunNumber == null) return ObjectIdScanBoundaryStrategy.from(wfRunId);
        else
            return new ObjectIdScanBoundaryStrategy(
                    wfRunId.getPartitionKey().get(),
                    wfRunId + "/" + threadRunNumber + "/",
                    wfRunId + "/" + threadRunNumber + "/~");
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }
}
