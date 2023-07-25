package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListTaskMetricsPb;
import io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb;
import io.littlehorse.sdk.common.proto.MetricsWindowLengthPb;
import io.littlehorse.sdk.common.proto.TaskDefMetricsPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListTaskMetricsReply;
import java.util.Date;

public class ListTaskMetrics
    extends PublicScanRequest<ListTaskMetricsPb, ListTaskMetricsReplyPb, TaskDefMetricsPb, TaskDefMetrics, ListTaskMetricsReply> {

    public Date lastWindowStart;
    public String taskDefName;
    public int numWindows;
    public MetricsWindowLengthPb windowLength;

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

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.TASK_DEF_METRICS;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        return null;
    }

    @Override
    public TagStorageTypePb indexTypeForSearch() throws LHValidationError {
        return TagStorageTypePb.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        String endKey = TaskDefMetrics.getObjectId(
            windowLength,
            lastWindowStart,
            taskDefName
        );
        String startKey = TaskDefMetrics.getObjectId(
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
