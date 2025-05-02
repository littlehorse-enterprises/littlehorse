package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.metrics.MetricModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ListMetricsRequest;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListMetricReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;

public class ListMetricsRequestModel
        extends PublicScanRequest<ListMetricsRequest, MetricList, Metric, MetricModel, ListMetricReply> {

    private MetricSpecIdModel metricId;
    private TenantIdModel tenantId;
    private Duration windowLength;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.METRIC;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.REPARTITION;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        return ObjectIdScanBoundaryStrategy.fromPrefix(
                LHUtil.getCompositeId(tenantId.toString(), metricId.toString()),
                LHUtil.getCompositeId(metricId.toString(), String.valueOf(windowLength.getSeconds())));
    }

    @Override
    public ListMetricsRequest.Builder toProto() {
        return ListMetricsRequest.newBuilder().setMetricSpecId(metricId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListMetricsRequest p = (ListMetricsRequest) proto;
        this.tenantId = context.authorization().tenantId();
        this.metricId = LHSerializable.fromProto(p.getMetricSpecId(), MetricSpecIdModel.class, context);
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
    }

    @Override
    public Class<ListMetricsRequest> getProtoBaseClass() {
        return ListMetricsRequest.class;
    }
}
