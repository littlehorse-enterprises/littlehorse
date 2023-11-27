package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.SleepNodeMaturedPb;
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

    public void initFrom(Message proto) {
        SleepNodeMaturedPb p = (SleepNodeMaturedPb) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class);
    }

    public static SleepNodeMaturedModel fromProto(SleepNodeMaturedPb proto) {
        SleepNodeMaturedModel out = new SleepNodeMaturedModel();
        out.initFrom(proto);
        return out;
    }

    public boolean hasResponse() {
        return false;
    }

    public String getPartitionKey() {
        return nodeRunId.getPartitionKey().get();
    }

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config) {
        WfRunModel wfRunModel = dao.getWfRun(nodeRunId.getWfRunId().getId());
        if (wfRunModel == null) {
            log.debug("Uh oh, invalid timer event, no associated WfRun found.");
            return null;
        }

        WfSpecModel wfSpecModel = dao.getWfSpec(wfRunModel.getWfSpecId());
        if (wfSpecModel == null) {
            log.debug("Uh oh, invalid timer event, no associated WfSpec found.");
            return null;
        }

        try {
            wfRunModel.processSleepNodeMatured(this, dao.getEventTime());
        } catch (LHValidationError exn) {
            log.debug("Uh, invalid timer event: {}", exn.getMessage(), exn);
        }

        return Empty.getDefaultInstance();
    }
}
