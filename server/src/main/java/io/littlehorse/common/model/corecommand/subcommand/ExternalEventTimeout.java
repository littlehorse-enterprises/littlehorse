package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.ExternalEventNodeTimeoutPb;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ExternalEventTimeout extends CoreSubCommand<ExternalEventNodeTimeoutPb> {

    private NodeRunIdModel nodeRunId;

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
    public void initFrom(Message proto) {
        ExternalEventNodeTimeoutPb p = (ExternalEventNodeTimeoutPb) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class);
    }

    @Override
    public String getPartitionKey() {
        return nodeRunId.getPartitionKey().get();
    }

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config) {
        WfRunModel wfRunModel = dao.get(nodeRunId.getWfRunId());

        if (wfRunModel == null) {
            log.warn("Got an externalEventTimeout for missing wfRun {}", nodeRunId.getWfRunId());
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
