package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.GetLatestWfMetricWindowRequest;
import io.littlehorse.sdk.common.proto.GetLatestWfMetricWindowResponse;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetLatestWfMetricWindowRequestModel extends LHSerializable<GetLatestWfMetricWindowRequest> {

    private String wfSpecName;
    private Integer majorVersion;
    private Integer revision;

    @Override
    public GetLatestWfMetricWindowRequest.Builder toProto() {
        GetLatestWfMetricWindowRequest.Builder out =
                GetLatestWfMetricWindowRequest.newBuilder().setWfSpecName(wfSpecName);
        if (majorVersion != null) out.setMajorVersion(majorVersion);
        if (revision != null) out.setRevision(revision);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        GetLatestWfMetricWindowRequest p = (GetLatestWfMetricWindowRequest) proto;
        wfSpecName = p.getWfSpecName();
        if (p.hasMajorVersion()) majorVersion = p.getMajorVersion();
        if (p.hasRevision()) revision = p.getRevision();
    }

    @Override
    public Class<GetLatestWfMetricWindowRequest> getProtoBaseClass() {
        return GetLatestWfMetricWindowRequest.class;
    }

    public GetLatestWfMetricWindowResponse process(BackendInternalComms internalComms, ExecutionContext ctx) {
        WfSpecModel wfSpec = ctx.service().getWfSpec(wfSpecName, majorVersion, revision);
        if (wfSpec == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WfSpec %s".formatted(wfSpecName));
        }
        Date latestWindowStart = new Date(LHUtil.getCurrentWindowDate().getTime() - 60_000L);
        MetricWindowIdModel id =
                new MetricWindowIdModel(ctx.authorization().tenantId(), wfSpec.getObjectId(), latestWindowStart);
        MetricWindowModel window = internalComms.getObject(id, MetricWindowModel.class, ctx);
        GetLatestWfMetricWindowResponse.Builder response = GetLatestWfMetricWindowResponse.newBuilder();
        if (window != null) {
            response.setWindow(window.toProto());
        }
        return response.build();
    }
}
