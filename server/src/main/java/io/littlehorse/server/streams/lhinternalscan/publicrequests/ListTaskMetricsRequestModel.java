package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
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

public class ListTaskMetricsRequestModel
        extends PublicScanRequest<
                ListTaskMetricsRequest,
                ListTaskMetricsResponse,
                TaskDefMetrics,
                TaskDefMetricsModel,
                ListTaskMetricsReply> {

    public Date lastWindowStart;
    public String taskDefName;
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
                .setTaskDefName(taskDefName);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListTaskMetricsRequest p = (ListTaskMetricsRequest) proto;
        lastWindowStart = LHUtil.fromProtoTs(p.getLastWindowStart());
        numWindows = p.getNumWindows();
        windowLength = p.getWindowLength();
        taskDefName = p.getTaskDefName();
        limit = numWindows;
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
        String endKey = TaskDefMetricsModel.getObjectId(windowLength, lastWindowStart, taskDefName);
        String startKey = TaskDefMetricsModel.getObjectId(
                windowLength,
                new Date(lastWindowStart.getTime() - (LHUtil.getWindowLengthMillis(windowLength) * numWindows)),
                taskDefName);
        return new ObjectIdScanBoundaryStrategy(taskDefName, startKey, endKey);
    }
}
