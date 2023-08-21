package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.corecommand.AbstractResponse;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.proto.SleepNodeMaturedPb;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SleepNodeMatured extends SubCommand<SleepNodeMaturedPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;

    public Class<SleepNodeMaturedPb> getProtoBaseClass() {
        return SleepNodeMaturedPb.class;
    }

    public SleepNodeMaturedPb.Builder toProto() {
        SleepNodeMaturedPb.Builder out = SleepNodeMaturedPb.newBuilder()
                .setThreadRunNumber(threadRunNumber)
                .setNodeRunPosition(nodeRunPosition)
                .setWfRunId(wfRunId);
        return out;
    }

    public void initFrom(Message proto) {
        SleepNodeMaturedPb p = (SleepNodeMaturedPb) proto;
        threadRunNumber = p.getThreadRunNumber();
        wfRunId = p.getWfRunId();
        nodeRunPosition = p.getNodeRunPosition();
    }

    public static SleepNodeMatured fromProto(SleepNodeMaturedPb proto) {
        SleepNodeMatured out = new SleepNodeMatured();
        out.initFrom(proto);
        return out;
    }

    public boolean hasResponse() {
        return false;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public AbstractResponse<?> process(CoreProcessorDAO dao, LHConfig config) {
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel == null) {
            log.debug("Uh oh, invalid timer event, no associated WfRun found.");
            return null;
        }

        WfSpecModel wfSpecModel = dao.getWfSpec(wfRunModel.wfSpecName, wfRunModel.wfSpecVersion);
        if (wfSpecModel == null) {
            log.debug("Uh oh, invalid timer event, no associated WfSpec found.");
            return null;
        }
        wfRunModel.wfSpecModel = wfSpecModel;

        try {
            wfRunModel.processSleepNodeMatured(this, dao.getEventTime());
        } catch (LHValidationError exn) {
            log.debug("Uh, invalid timer event: {}", exn.getMessage(), exn);
        }

        return null;
    }
}
