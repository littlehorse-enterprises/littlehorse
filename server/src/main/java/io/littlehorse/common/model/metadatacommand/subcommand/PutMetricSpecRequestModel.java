package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.PutMetricSpecRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.time.Duration;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PutMetricSpecRequestModel extends MetadataSubCommand<PutMetricSpecRequest> {

    private MeasurableObject measurable;
    private AggregationType aggregationType;
    private Duration windowLength;

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutMetricSpecRequest p = (PutMetricSpecRequest) proto;
        this.aggregationType = p.getAggregationType();
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
        this.measurable = p.getObject();
    }

    @Override
    public PutMetricSpecRequest.Builder toProto() {
        return PutMetricSpecRequest.newBuilder()
                .setObject(measurable)
                .setAggregationType(aggregationType)
                .setWindowLength(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(windowLength.getSeconds())
                        .build());
    }

    @Override
    public MetricSpec process(MetadataCommandExecution executionContext) {
        MetricSpecModel storedMetricSpec = executionContext.metadataManager().get(new MetricSpecIdModel(measurable));
        if (storedMetricSpec == null) {
            MetricSpecModel metricModel =
                    new MetricSpecModel(new MetricSpecIdModel(measurable), windowLength, Set.of(aggregationType));
            executionContext.metadataManager().put(metricModel);
            return metricModel.toProto().build();
        } else {
            storedMetricSpec.addWindowLength(windowLength);
            executionContext.metadataManager().put(storedMetricSpec);
            return storedMetricSpec.toProto().build();
        }
    }

    @Override
    public Class<PutMetricSpecRequest> getProtoBaseClass() {
        return PutMetricSpecRequest.class;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }
}
