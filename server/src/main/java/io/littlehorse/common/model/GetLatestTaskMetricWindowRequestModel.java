package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.GetLatestTaskMetricWindowRequest;
import io.littlehorse.sdk.common.proto.GetLatestTaskMetricWindowResponse;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class GetLatestTaskMetricWindowRequestModel extends LHSerializable<GetLatestTaskMetricWindowRequest> {

    private TaskDefIdModel taskDefId;

    @Override
    public GetLatestTaskMetricWindowRequest.Builder toProto() {
        return GetLatestTaskMetricWindowRequest.newBuilder().setTaskDef(taskDefId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        GetLatestTaskMetricWindowRequest p = (GetLatestTaskMetricWindowRequest) proto;
        taskDefId = LHSerializable.fromProto(p.getTaskDef(), TaskDefIdModel.class, context);
    }

    @Override
    public Class<GetLatestTaskMetricWindowRequest> getProtoBaseClass() {
        return GetLatestTaskMetricWindowRequest.class;
    }

    public GetLatestTaskMetricWindowResponse process(BackendInternalComms internalComms, ExecutionContext ctx) {
        TenantIdModel tenantId = ctx.authorization().tenantId();
        Date latestWindowStart = new Date(LHUtil.getCurrentWindowDate().getTime() - 60_000L);
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, taskDefId, latestWindowStart);
        MetricWindowModel window = internalComms.getObject(id, MetricWindowModel.class, ctx);
        GetLatestTaskMetricWindowResponse.Builder response = GetLatestTaskMetricWindowResponse.newBuilder();
        if (window != null) {
            response.setWindow(window.toProto());
        }
        return response.build();
    }
}
