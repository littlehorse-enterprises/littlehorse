package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.metrics.WfSpecMetricsModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListWfMetricsRequest;
import io.littlehorse.sdk.common.proto.ListWfMetricsResponse;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.sdk.common.proto.WfSpecMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListWfMetricsReply;
import java.util.Date;

public class ListWfMetricsRequestModel
        extends PublicScanRequest<
                ListWfMetricsRequest, ListWfMetricsResponse, WfSpecMetrics, WfSpecMetricsModel, ListWfMetricsReply> {

    public Date lastWindowStart;
    public String wfSpecName;
    public int wfSpecVersion;
    public int numWindows;
    public MetricsWindowLength windowLength;

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
    public TagStorageType indexTypeForSearch(LHGlobalMetaStores stores) throws LHValidationError {
        return null;
    }

    @Override
    public void validate() throws LHValidationError {}

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
