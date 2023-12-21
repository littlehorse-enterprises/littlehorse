package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefMetricsIdModel;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListTaskMetricsRequest;
import io.littlehorse.sdk.common.proto.ListTaskMetricsResponse;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskMetricsReply;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
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
    public LHStoreType getStoreType() {
        return LHStoreType.REPARTITION;
    }

    public ListTaskMetricsRequest.Builder toProto() {
        ListTaskMetricsRequest.Builder out = ListTaskMetricsRequest.newBuilder()
                .setLastWindowStart(LHUtil.fromDate(lastWindowStart))
                .setNumWindows(numWindows)
                .setWindowLength(windowLength)
                .setTaskDefId(taskDefId.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListTaskMetricsRequest p = (ListTaskMetricsRequest) proto;
        lastWindowStart = LHUtil.fromProtoTs(p.getLastWindowStart());
        numWindows = p.getNumWindows();
        windowLength = p.getWindowLength();
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        limit = numWindows;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_DEF_METRICS;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public ScanBoundary<?> getScanBoundary(RequestExecutionContext ctx) {
        Date earliestWindowStart =
                new Date(lastWindowStart.getTime() - (LHUtil.getWindowLengthMillis(windowLength) * numWindows));
        return new BoundedObjectIdScanModel(
                new TaskDefMetricsIdModel(earliestWindowStart, windowLength, taskDefId),
                new TaskDefMetricsIdModel(lastWindowStart, windowLength, taskDefId));
    }
}
