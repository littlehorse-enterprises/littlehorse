package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.ExternalEventNodeTimeoutPb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalEventTimeout extends SubCommand<ExternalEventNodeTimeoutPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;
    public Date time;

    public Class<ExternalEventNodeTimeoutPb> getProtoBaseClass() {
        return ExternalEventNodeTimeoutPb.class;
    }

    public ExternalEventNodeTimeoutPb.Builder toProto() {
        ExternalEventNodeTimeoutPb.Builder out = ExternalEventNodeTimeoutPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setNodeRunPosition(nodeRunPosition)
            .setTime(LHUtil.fromDate(time));
        return out;
    }

    public void initFrom(Message proto) {
        ExternalEventNodeTimeoutPb p = (ExternalEventNodeTimeoutPb) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
        time = LHUtil.fromProtoTs(p.getTime());
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public AbstractResponse<?> process(LHDAO dao, LHConfig config) {
        WfRun wfRun = dao.getWfRun(wfRunId);

        if (wfRun == null) {
            log.warn("Got an externalEventTimeout for missing wfRun {}", wfRunId);
            return null;
        }

        wfRun.wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        wfRun.processExtEvtTimeout(this);

        return null;
    }

    public boolean hasResponse() {
        return false;
    }

    public static ExternalEventTimeout fromProto(ExternalEventNodeTimeoutPb p) {
        ExternalEventTimeout out = new ExternalEventTimeout();
        out.initFrom(p);
        return out;
    }
}
