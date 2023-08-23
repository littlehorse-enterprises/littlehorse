package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
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

    public Date lastWindowStart;
    public String wfSpecName;
    public int wfSpecVersion;
    public int numWindows;
    public MetricsWindowLength windowLength;

    @Override
    public LHStore getStore(ReadOnlyMetadataStore metaStore) {
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
                .setWfSpecName(wfSpecName)
                .setWfSpecVersion(wfSpecVersion);

        return out;
    }

    public void initFrom(Message proto) {
        ListWfMetricsRequest p = (ListWfMetricsRequest) proto;
        lastWindowStart = LHUtil.fromProtoTs(p.getLastWindowStart());
        numWindows = p.getNumWindows();
        windowLength = p.getWindowLength();
        wfSpecName = p.getWfSpecName();
        wfSpecVersion = p.getWfSpecVersion();
        limit = numWindows;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WF_SPEC_METRICS;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) throws LHApiException {
        return null;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        String endKey = WfSpecMetricsModel.getObjectId(windowLength, lastWindowStart, wfSpecName, wfSpecVersion);
        String startKey = WfSpecMetricsModel.getObjectId(
                windowLength,
                new Date(lastWindowStart.getTime() - (LHUtil.getWindowLengthMillis(windowLength) * numWindows)),
                wfSpecName,
                wfSpecVersion);
        return new ObjectIdScanBoundaryStrategy(wfSpecName, startKey, endKey);
    }
}
