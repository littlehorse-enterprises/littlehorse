package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.proto.DeleteMetricWindow;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMetricWindowModel extends CoreSubCommand<DeleteMetricWindow> {

    private MetricWindowIdModel id;

    @Override
    public String getPartitionKey() {
        return id.getPartitionKey().get();
    }

    @Override
    public Class<DeleteMetricWindow> getProtoBaseClass() {
        return DeleteMetricWindow.class;
    }

    @Override
    public DeleteMetricWindow.Builder toProto() {
        return DeleteMetricWindow.newBuilder().setId(id.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        DeleteMetricWindow p = (DeleteMetricWindow) proto;
        id = LHSerializable.fromProto(p.getId(), MetricWindowIdModel.class, context);
    }

    @Override
    public Empty process(CoreProcessorContext executionContext, LHServerConfig config) {
        if (executionContext.getableManager().get(id) != null) {
            executionContext.getableManager().delete(id);
        }
        return Empty.getDefaultInstance();
    }
}
