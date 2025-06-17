package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.ExternalEventNodeTimeoutPb;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ExternalEventTimeoutModel extends CoreSubCommand<ExternalEventNodeTimeoutPb> {

    private NodeRunIdModel nodeRunId;

    public ExternalEventTimeoutModel() {}

    public ExternalEventTimeoutModel(NodeRunIdModel nodeRunId) {
        this.nodeRunId = nodeRunId;
    }

    @Override
    public Class<ExternalEventNodeTimeoutPb> getProtoBaseClass() {
        return ExternalEventNodeTimeoutPb.class;
    }

    @Override
    public ExternalEventNodeTimeoutPb.Builder toProto() {
        ExternalEventNodeTimeoutPb.Builder out =
                ExternalEventNodeTimeoutPb.newBuilder().setNodeRunId(nodeRunId.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEventNodeTimeoutPb p = (ExternalEventNodeTimeoutPb) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
    }

    @Override
    public String getPartitionKey() {
        return nodeRunId.getPartitionKey().get();
    }

    @Override
    public Empty process(CoreProcessorContext executionContext, LHServerConfig config) {
        WfRunModel wfRunModel = executionContext.getableManager().get(nodeRunId.getWfRunId());

        if (wfRunModel == null) {
            log.warn("Got an externalEventTimeout for missing wfRun {}", nodeRunId.getWfRunId());
            return null;
        }

        wfRunModel.processExtEvtTimeout(this);

        return Empty.getDefaultInstance();
    }

    public static ExternalEventTimeoutModel fromProto(ExternalEventNodeTimeoutPb p, ExecutionContext context) {
        ExternalEventTimeoutModel out = new ExternalEventTimeoutModel();
        out.initFrom(p, context);
        return out;
    }
}
