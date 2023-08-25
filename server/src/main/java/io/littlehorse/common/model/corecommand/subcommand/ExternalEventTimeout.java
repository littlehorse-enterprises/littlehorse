package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
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
        ExternalEventNodeTimeoutPb.Builder out = ExternalEventNodeTimeoutPb.newBuilder()
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

    public Empty process(CoreProcessorDAO dao, LHConfig config) {
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);

        if (wfRunModel == null) {
            log.warn("Got an externalEventTimeout for missing wfRun {}", wfRunId);
            return null;
        }

        wfRunModel.processExtEvtTimeout(this);

        return Empty.getDefaultInstance();
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
