package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.NodeReferenceModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.PutMetricSpecRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.time.Duration;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PutMetricSpecRequestModel extends MetadataSubCommand<PutMetricSpecRequest> {

    private AggregationType aggregationType;
    private Duration windowLength;
    private NodeReferenceModel nodeReference;

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutMetricSpecRequest p = (PutMetricSpecRequest) proto;
        this.aggregationType = p.getAggregationType();
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
        if (p.hasNode()) {
            this.nodeReference = LHSerializable.fromProto(p.getNode(), NodeReferenceModel.class, context);
        }
    }

    @Override
    public PutMetricSpecRequest.Builder toProto() {
        PutMetricSpecRequest.Builder out = PutMetricSpecRequest.newBuilder()
                .setAggregationType(aggregationType)
                .setWindowLength(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(windowLength.getSeconds())
                        .build());
        if (nodeReference != null) {
            out.setNode(nodeReference.toProto());
        }
        return out;
    }

    @Override
    public MetricSpec process(MetadataProcessorContext executionContext) {
        MetricSpecIdModel metricSpecId = null;
        if (nodeReference != null) {
            metricSpecId = new MetricSpecIdModel(nodeReference);
        }
        MetricSpecModel storedMetricSpec = executionContext.metadataManager().get(metricSpecId);
        if (storedMetricSpec == null) {
            MetricSpecModel metricModel = new MetricSpecModel(metricSpecId, windowLength, Set.of(aggregationType));
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
}
