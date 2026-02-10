package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListWfMetricsRequest;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricsList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListMetricsReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class ListWfMetricsRequestModel
        extends PublicScanRequest<
        ListWfMetricsRequest, MetricsList, MetricWindow, MetricWindowModel, ListMetricsReply> {

    private WfSpecIdModel wfSpecId;
    private Date windowStart;
    private Date windowEnd;

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }

    @Override
    public Class<ListWfMetricsRequest> getProtoBaseClass() {
        return ListWfMetricsRequest.class;
    }

    @Override
    public ListWfMetricsRequest.Builder toProto() {
        ListWfMetricsRequest.Builder out = ListWfMetricsRequest.newBuilder()
                .setWfSpec(wfSpecId.toProto())
                .setWindowStart(LHUtil.fromDate(windowStart));
        if (windowEnd != null) {
            out.setWindowEnd(LHUtil.fromDate(windowEnd));
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListWfMetricsRequest p = (ListWfMetricsRequest) proto;
        wfSpecId = LHSerializable.fromProto(p.getWfSpec(), WfSpecIdModel.class, context);
        // Use windowStart and windowEnd from the request (default to 5 hours ago if not provided)
        if (p.hasWindowStart()) {
            windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        } else {
            windowStart = new Date(System.currentTimeMillis() - 5 * 60 * 60 * 1000L);
        }
        if (p.hasWindowEnd()) {
            windowEnd = LHUtil.fromProtoTs(p.getWindowEnd());
        } else {
            windowEnd = new Date();
        }
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.METRIC_WINDOW;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return null;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        // Scan all metrics for the wfSpec, ignoring time
        String prefix = wfSpecId.toString() + "/";
        System.out.println("ListWfMetricsRequestModel - StartKey: " + prefix + ", EndKey: " + (prefix + "~"));
        return new ObjectIdScanBoundaryStrategy(wfSpecId.toString(), prefix, prefix + "~");
    }
}
