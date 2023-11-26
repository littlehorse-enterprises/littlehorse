package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListWfMetricsRequest;
import io.littlehorse.sdk.common.proto.ListWfMetricsResponse;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListWfMetricsReply;
import java.util.Date;

public class ListWfMetricsRequestModel
        extends PublicScanRequest<
                ListWfMetricsRequest, ListWfMetricsResponse, WfSpecMetrics, WfSpecMetricsModel, ListWfMetricsReply> {

    private WfSpecIdModel wfSpecId;
    public Date lastWindowStart;
    public int numWindows;
    public MetricsWindowLength windowLength;

    @Override
    public LHStore getStoreType() {
        return LHStore.REPARTITION;
    }

    public Class<ListWfMetricsRequest> getProtoBaseClass() {
        return ListWfMetricsRequest.class;
    }

    public ListWfMetricsRequest.Builder toProto() {
        ListWfMetricsRequest.Builder out = ListWfMetricsRequest.newBuilder()
                .setLastWindowStart(LHUtil.fromDate(lastWindowStart))
                .setNumWindows(numWindows)
                .setWindowLength(windowLength)
                .setWfSpecId(wfSpecId.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        ListWfMetricsRequest p = (ListWfMetricsRequest) proto;
        lastWindowStart = LHUtil.fromProtoTs(p.getLastWindowStart());
        numWindows = p.getNumWindows();
        windowLength = p.getWindowLength();
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class);
        limit = numWindows;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WF_SPEC_METRICS;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataDAO readOnlyDao) throws LHApiException {
        return null;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        String endKey = WfSpecMetricsModel.getObjectId(windowLength, lastWindowStart, wfSpecId);
        String startKey = WfSpecMetricsModel.getObjectId(
                windowLength,
                new Date(lastWindowStart.getTime() - (LHUtil.getWindowLengthMillis(windowLength) * numWindows)),
                wfSpecId);
        return new ObjectIdScanBoundaryStrategy(wfSpecId.toString(), startKey, endKey);
    }
}
