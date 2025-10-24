package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.SleepNodeMaturedPb;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SleepNodeMaturedModel extends CoreSubCommand<SleepNodeMaturedPb> {

    private NodeRunIdModel nodeRunId;

    public SleepNodeMaturedModel() {}

    public SleepNodeMaturedModel(NodeRunIdModel nodeRunId) {
        this.nodeRunId = nodeRunId;
    }

    public Class<SleepNodeMaturedPb> getProtoBaseClass() {
        return SleepNodeMaturedPb.class;
    }

    public SleepNodeMaturedPb.Builder toProto() {
        SleepNodeMaturedPb.Builder out = SleepNodeMaturedPb.newBuilder().setNodeRunId(nodeRunId.toProto());
        return out;
    }

    public void initFrom(Message proto, ExecutionContext context) {
        SleepNodeMaturedPb p = (SleepNodeMaturedPb) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
    }

    public static SleepNodeMaturedModel fromProto(SleepNodeMaturedPb proto, ExecutionContext context) {
        SleepNodeMaturedModel out = new SleepNodeMaturedModel();
        out.initFrom(proto, context);
        return out;
    }

    public String getPartitionKey() {
        return nodeRunId.getPartitionKey().get();
    }

    @Override
    public Empty process(CoreProcessorContext executionContext, LHServerConfig config) {
        GetableManager getableManager = executionContext.getableManager();
        WfService service = executionContext.service();
        WfRunModel wfRunModel = getableManager.get(nodeRunId.getWfRunId());
        if (wfRunModel == null) {
            log.debug("Uh oh, invalid timer event, no associated WfRun found.");
            return null;
        }

        WfSpecModel wfSpecModel = service.getWfSpec(wfRunModel.getWfSpecId());
        if (wfSpecModel == null) {
            log.debug("Uh oh, invalid timer event, no associated WfSpec found.");
            return null;
        }

        try {
            wfRunModel.processSleepNodeMatured(
                    this, executionContext.currentCommand().getTime());
        } catch (LHValidationException exn) {
            log.debug("Uh, invalid timer event: {}", exn.getMessage(), exn);
        }

        return Empty.getDefaultInstance();
    }
}
