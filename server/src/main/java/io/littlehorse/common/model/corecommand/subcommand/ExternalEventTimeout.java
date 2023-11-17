package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.proto.ExternalEventNodeTimeoutPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalEventTimeout extends CoreSubCommand<ExternalEventNodeTimeoutPb> {

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

    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEventNodeTimeoutPb p = (ExternalEventNodeTimeoutPb) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
        time = LHUtil.fromProtoTs(p.getTime());
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    @Override
    public Empty process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        WfRunModel wfRunModel = executionContext.wfService().getWfRun(wfRunId);

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

    public static ExternalEventTimeout fromProto(ExternalEventNodeTimeoutPb p, ExecutionContext context) {
        ExternalEventTimeout out = new ExternalEventTimeout();
        out.initFrom(p, context);
        return out;
    }
}
