package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListTaskMetricsRequest;
import io.littlehorse.sdk.common.proto.ListTaskMetricsResponse;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskMetricsReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

@Getter
public class ListTaskMetricsRequestModel
        extends PublicScanRequest<
                ListTaskMetricsRequest,
                ListTaskMetricsResponse,
                TaskDefMetrics,
                TaskDefMetricsModel,
                ListTaskMetricsReply> {

    private TaskDefIdModel taskDefId;
    public Date lastWindowStart;
    public int numWindows;
    public MetricsWindowLength windowLength;

    public Class<ListTaskMetricsRequest> getProtoBaseClass() {
        return ListTaskMetricsRequest.class;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.REPARTITION;
    }

    public ListTaskMetricsRequest.Builder toProto() {
        ListTaskMetricsRequest.Builder out = ListTaskMetricsRequest.newBuilder()
                .setLastWindowStart(LHUtil.fromDate(lastWindowStart))
                .setNumWindows(numWindows)
                .setWindowLength(windowLength)
                .setTaskDefId(taskDefId.toProto());

        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListTaskMetricsRequest p = (ListTaskMetricsRequest) proto;
        lastWindowStart = LHUtil.fromProtoTs(p.getLastWindowStart());
        numWindows = p.getNumWindows();
        windowLength = p.getWindowLength();
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        if (p.hasLimit()) {
            limit = p.getLimit();
        } else {
            limit = numWindows;
        }
        if (!p.getBookmark().isEmpty()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_DEF_METRICS;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        String endKey = TaskDefMetricsModel.getObjectId(windowLength, lastWindowStart, taskDefId.toString());
        String startKey = TaskDefMetricsModel.getObjectId(
                windowLength,
                new Date(lastWindowStart.getTime() - (LHUtil.getWindowLengthMillis(windowLength) * numWindows)),
                taskDefId.toString());
        return new ObjectIdScanBoundaryStrategy(taskDefId.toString(), startKey, endKey);
    }
}
