package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.metrics.WfSpecMetrics;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListWfMetricsPb;
import io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb;
import io.littlehorse.sdk.common.proto.MetricsWindowLengthPb;
import io.littlehorse.sdk.common.proto.WfSpecMetricsPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListWfMetricsReply;
import java.util.Date;

public class ListWfMetrics
    extends PublicScanRequest<ListWfMetricsPb, ListWfMetricsReplyPb, WfSpecMetricsPb, WfSpecMetrics, ListWfMetricsReply> {

    public Date lastWindowStart;
    public String wfSpecName;
    public int wfSpecVersion;
    public int numWindows;
    public MetricsWindowLengthPb windowLength;

    public Class<ListWfMetricsPb> getProtoBaseClass() {
        return ListWfMetricsPb.class;
    }

    public ListWfMetricsPb.Builder toProto() {
        ListWfMetricsPb.Builder out = ListWfMetricsPb
            .newBuilder()
            .setLastWindowStart(LHUtil.fromDate(lastWindowStart))
            .setNumWindows(numWindows)
            .setWindowLength(windowLength)
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion);

        return out;
    }

    public void initFrom(Message proto) {
        ListWfMetricsPb p = (ListWfMetricsPb) proto;
        lastWindowStart = LHUtil.fromProtoTs(p.getLastWindowStart());
        numWindows = p.getNumWindows();
        windowLength = p.getWindowLength();
        wfSpecName = p.getWfSpecName();
        wfSpecVersion = p.getWfSpecVersion();
        limit = numWindows;
    }

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.WF_SPEC_METRICS;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        return null;
    }

    @Override
    public TagStorageTypePb indexTypeForSearch() throws LHValidationError {
        return null;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        String endKey = WfSpecMetrics.getObjectId(
            windowLength,
            lastWindowStart,
            wfSpecName,
            wfSpecVersion
        );
        String startKey = WfSpecMetrics.getObjectId(
            windowLength,
            new Date(
                lastWindowStart.getTime() -
                (LHUtil.getWindowLengthMillis(windowLength) * numWindows)
            ),
            wfSpecName,
            wfSpecVersion
        );
        return new ObjectIdScanBoundaryStrategy(wfSpecName, startKey, endKey);
    }
}
