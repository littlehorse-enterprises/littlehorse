package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.global.metrics.MetricModel;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricType;
import io.littlehorse.sdk.common.proto.PutMetricRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;

public class PutMetricRequestModel extends MetadataSubCommand<PutMetricRequest> {

    private MeasurableObject measurable;
    private MetricType metricType;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PutMetricRequest p = (PutMetricRequest) proto;
        this.measurable = p.getMeasurable();
        this.metricType = p.getType();
    }

    @Override
    public PutMetricRequest.Builder toProto() {
        return PutMetricRequest.newBuilder()
                .setMeasurable(measurable)
                .setType(metricType);
    }

    @Override
    public Metric process(MetadataCommandExecution executionContext) {
        String id = LHUtil.generateGuid();
        MetricModel metricModel = new MetricModel(new MetricIdModel(id), measurable, metricType);
        executionContext.metadataManager().put(metricModel);
        return metricModel.toProto().build();
    }

    @Override
    public Class<PutMetricRequest> getProtoBaseClass() {
        return PutMetricRequest.class;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }
}
