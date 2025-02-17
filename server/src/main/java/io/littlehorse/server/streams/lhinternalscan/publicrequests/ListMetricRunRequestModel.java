package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.metrics.MetricRunModel;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ListMetricRunRequest;
import io.littlehorse.sdk.common.proto.MetricRun;
import io.littlehorse.sdk.common.proto.MetricRunList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListMetricRunReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ListMetricRunRequestModel
        extends PublicScanRequest<ListMetricRunRequest, MetricRunList, MetricRun, MetricRunModel, ListMetricRunReply> {

    private MetricIdModel metricId;
    private TenantIdModel tenantId;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.METRIC_RUN;
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
                LHUtil.getCompositeId(tenantId.toString(), metricId.toString()), metricId.toString());
    }

    @Override
    public ListMetricRunRequest.Builder toProto() {
        return ListMetricRunRequest.newBuilder().setMetricId(metricId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ListMetricRunRequest p = (ListMetricRunRequest) proto;
        this.tenantId = context.authorization().tenantId();
        this.metricId = LHSerializable.fromProto(p.getMetricId(), MetricIdModel.class, context);
    }

    @Override
    public Class<ListMetricRunRequest> getProtoBaseClass() {
        return ListMetricRunRequest.class;
    }
}
