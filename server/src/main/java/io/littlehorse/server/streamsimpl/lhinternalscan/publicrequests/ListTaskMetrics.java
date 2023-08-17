package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.metrics.TaskDefMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListTaskMetricsPb;
import io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.TaskDefMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListTaskMetricsReply;
import java.util.Date;

public class ListTaskMetrics
    extends PublicScanRequest<ListTaskMetricsPb, ListTaskMetricsReplyPb, TaskDefMetrics, TaskDefMetricsModel, ListTaskMetricsReply> {

    public Date lastWindowStart;
    public String taskDefName;
    public int numWindows;
    public MetricsWindowLength windowLength;

    public Class<ListTaskMetricsPb> getProtoBaseClass() {
        return ListTaskMetricsPb.class;
    }

    public ListTaskMetricsPb.Builder toProto() {
        ListTaskMetricsPb.Builder out = ListTaskMetricsPb
            .newBuilder()
            .setLastWindowStart(LHUtil.fromDate(lastWindowStart))
            .setNumWindows(numWindows)
            .setWindowLength(windowLength)
            .setTaskDefName(taskDefName);

        return out;
    }

    public void initFrom(Message proto) {
        ListTaskMetricsPb p = (ListTaskMetricsPb) proto;
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
    public TagStorageType indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        return TagStorageType.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        String endKey = TaskDefMetricsModel.getObjectId(
            windowLength,
            lastWindowStart,
            taskDefName
        );
        String startKey = TaskDefMetricsModel.getObjectId(
            windowLength,
            new Date(
                lastWindowStart.getTime() -
                (LHUtil.getWindowLengthMillis(windowLength) * numWindows)
            ),
            taskDefName
        );
        return new ObjectIdScanBoundaryStrategy(taskDefName, startKey, endKey);
    }
}
