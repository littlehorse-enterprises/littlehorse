package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;

public class ExternalEventNodeModel extends SubNode<ExternalEventNode> {

    public String externalEventDefName;
    public VariableAssignmentModel timeoutSeconds;

    public ExternalEventDefModel externalEventDef;
    private ReadOnlyMetadataManager metadataManager;
    private ProcessorExecutionContext processorContext;

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
        this.metadataManager = context.metadataManager();
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    public ExternalEventNode.Builder toProto() {
        ExternalEventNode.Builder out = ExternalEventNode.newBuilder().setExternalEventDefName(externalEventDefName);

        if (timeoutSeconds != null) out.setTimeoutSeconds(timeoutSeconds.toProto());
        return out;
    }

    @Override
    public void validate() throws LHApiException {
        // Want to be able to release new versions of ExternalEventDef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the EED to ensure that this isn't an issue.
        ExternalEventDefModel eed = this.metadataManager.get(new ExternalEventDefIdModel(externalEventDefName));

        // TODO: validate the timeout

        if (eed == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Refers to nonexistent ExternalEventDef " + externalEventDefName);
        }
    }

    public ExternalEventRunModel createSubNodeRun(Date time) {
        ExternalEventRunModel out = new ExternalEventRunModel(processorContext);
        out.externalEventDefName = externalEventDefName;

        return out;
    }
}
