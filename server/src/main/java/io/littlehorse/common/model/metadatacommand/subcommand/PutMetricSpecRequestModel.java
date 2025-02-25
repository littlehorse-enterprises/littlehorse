package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.MetricType;
import io.littlehorse.sdk.common.proto.PutMetricSpecRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PutMetricSpecRequestModel extends MetadataSubCommand<PutMetricSpecRequest> {

    private MeasurableObject measurable;
    private MetricType metricType;
    private Duration windowLength;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PutMetricSpecRequest p = (PutMetricSpecRequest) proto;
        this.metricType = p.getType();
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
        this.measurable = p.getObject();
    }

    @Override
    public PutMetricSpecRequest.Builder toProto() {
        return PutMetricSpecRequest.newBuilder()
                .setObject(measurable)
                .setType(metricType)
                .setWindowLength(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(windowLength.getSeconds())
                        .build());
    }

    @Override
    public MetricSpec process(MetadataCommandExecution executionContext) {
        MetricSpecModel metricModel = new MetricSpecModel(new MetricSpecIdModel(measurable, metricType), windowLength);
        log.info("putting {}", metricModel.getObjectId().getStoreableKey());
        executionContext.metadataManager().put(metricModel);
        return metricModel.toProto().build();
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
