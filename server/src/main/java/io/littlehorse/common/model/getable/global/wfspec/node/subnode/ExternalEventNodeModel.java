package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.ExternalEventNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class ExternalEventNodeModel extends SubNode<ExternalEventNode> {

    public String externalEventDefName;
    public VariableAssignmentModel timeoutSeconds;

    public ExternalEventDefModel externalEventDef;

    public ExternalEventNodeModel() {}

    public Class<ExternalEventNode> getProtoBaseClass() {
        return ExternalEventNode.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEventNode p = (ExternalEventNode) proto;
        externalEventDefName = p.getExternalEventDefName();
        if (p.hasTimeoutSeconds()) {
            timeoutSeconds = VariableAssignmentModel.fromProto(p.getTimeoutSeconds(), context);
        }
    }

    public ExternalEventNode.Builder toProto() {
        ExternalEventNode.Builder out = ExternalEventNode.newBuilder().setExternalEventDefName(externalEventDefName);

        if (timeoutSeconds != null) out.setTimeoutSeconds(timeoutSeconds.toProto());
        return out;
    }

    public void validate(ReadOnlyMetadataDAO readOnlyDao, LHServerConfig config) throws LHApiException {
        // Want to be able to release new versions of ExternalEventDef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the EED to ensure that this isn't an issue.
        ExternalEventDefModel eed = readOnlyDao.getExternalEventDef(externalEventDefName);

        // TODO: validate the timeout

        if (eed == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Refers to nonexistent ExternalEventDef " + externalEventDefName);
        }
    }

    public ExternalEventRunModel createSubNodeRun(Date time) {
        ExternalEventRunModel out = new ExternalEventRunModel();
        out.externalEventDefName = externalEventDefName;

        return out;
    }
}
