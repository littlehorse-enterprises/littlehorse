package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.SleepNodeMaturedPb;
import io.littlehorse.common.proto.SleepNodeMaturedPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.CommandProcessorDao;

public class SleepNodeMatured extends SubCommand<SleepNodeMaturedPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;

    public Class<SleepNodeMaturedPb> getProtoBaseClass() {
        return SleepNodeMaturedPb.class;
    }

    public SleepNodeMaturedPb.Builder toProto() {
        SleepNodeMaturedPb.Builder out = SleepNodeMaturedPb
            .newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setNodeRunPosition(nodeRunPosition)
            .setWfRunId(wfRunId);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        SleepNodeMaturedPbOrBuilder p = (SleepNodeMaturedPbOrBuilder) proto;
        threadRunNumber = p.getThreadRunNumber();
        wfRunId = p.getWfRunId();
        nodeRunPosition = p.getNodeRunPosition();
    }

    public static SleepNodeMatured fromProto(SleepNodeMaturedPbOrBuilder proto) {
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

    public LHSerializable<?> process(CommandProcessorDao dao, LHConfig config) {
        WfRun wfRun = dao.getWfRun(wfRunId);
        if (wfRun == null) {
            LHUtil.log("Uh oh, invalid timer event, no associated WfRun found.");
            return null;
        }

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            LHUtil.log("Uh oh, invalid timer event, no associated WfSpec found.");
            return null;
        }
        wfRun.wfSpec = wfSpec;
        wfRun.cmdDao = dao;

        try {
            wfRun.processSleepNodeMatured(this, dao.getEventTime());
        } catch (LHValidationError exn) {
            LHUtil.log("Uh, invalid timer event: " + exn.getMessage());
        }

        return null;
    }
}
