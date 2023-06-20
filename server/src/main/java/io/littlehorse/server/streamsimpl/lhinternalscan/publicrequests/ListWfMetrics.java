package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.metrics.WfSpecMetrics;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ListWfMetricsPb;
import io.littlehorse.jlib.common.proto.ListWfMetricsReplyPb;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.WfSpecMetricsPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
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
    }

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.WF_SPEC_METRICS;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_REPARTITION_STORE;
        out.resultType = ScanResultTypePb.OBJECT;
        out.limit = numWindows;
        out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;

        // TODO: Need to make WfSpecName a required field. When client wants to
        // search for all WfSpecs, then they need to provide a reserved WfSpec name
        // such as '__LH_ALL'
        out.partitionKey = wfSpecName;

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
        out.boundedObjectIdScan =
            BoundedObjectIdScanPb
                .newBuilder()
                .setStartObjectId(startKey)
                .setEndObjectId(endKey)
                .build();

        return out;
    }
}
