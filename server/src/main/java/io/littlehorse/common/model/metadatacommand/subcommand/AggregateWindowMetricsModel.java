package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.DeleteMetricWindowModel;
import io.littlehorse.common.model.getable.core.metrics.MetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.proto.AggregateWindowMetrics;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class AggregateWindowMetricsModel extends CoreSubCommand<AggregateWindowMetrics> {
    private PartitionMetricWindowModel metricWindow;

    public AggregateWindowMetricsModel() {}

    public AggregateWindowMetricsModel(PartitionMetricWindowModel metricWindow) {
        this.metricWindow = metricWindow;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        AggregateWindowMetrics p = (AggregateWindowMetrics) proto;
        this.metricWindow = LHSerializable.fromProto(p.getMetricWindow(), PartitionMetricWindowModel.class, context);
    }

    @Override
    public AggregateWindowMetrics.Builder toProto() {
        AggregateWindowMetrics.Builder out = AggregateWindowMetrics.newBuilder();
        out.setMetricWindow(metricWindow.toProto());
        return out;
    }

    @Override
    public Class<AggregateWindowMetrics> getProtoBaseClass() {
        return AggregateWindowMetrics.class;
    }

    @Override
    public String getPartitionKey() {
        String partitionKey = metricWindow
                .getId()
                .getPartitionKey()
                .orElseThrow(() -> new IllegalStateException("PartitionMetricWindowModel must have a partition key"));
        return partitionKey;
    }

    @SuppressWarnings("unchecked")
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        MetricWindowIdModel id = metricWindow.getId();
        MetricWindowModel aggregatedWindowMetric =
                executionContext.getableManager().get(id);
        if (aggregatedWindowMetric == null) {
            aggregatedWindowMetric = new MetricWindowModel(id, metricWindow.getMetrics());
            Date deletionTime = new Date(id.getWindowStart().getTime() + config.getMetricWindowRetentionMs());
            DeleteMetricWindowModel deleteSubcomand = new DeleteMetricWindowModel(id);
            CommandModel deleteCommand = new CommandModel(deleteSubcomand, deletionTime);
            executionContext.getTaskManager().scheduleTimer(new LHTimer(deleteCommand));
        } else {
            aggregatedWindowMetric.mergeFrom(metricWindow.getMetrics());
        }
        executionContext.getableManager().put(aggregatedWindowMetric);
        return null;
    }

    public PartitionMetricWindowModel getMetricWindow() {
        return this.metricWindow;
    }
}
