package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventNodeRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.ExternalEventNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class ExternalEventNodeModel extends SubNode<ExternalEventNode> {

    private ExternalEventDefIdModel externalEventDefId;
    private VariableAssignmentModel timeoutSeconds;
    private VariableAssignmentModel corrrelationId;

    // Not in the proto
    private ExternalEventDefModel externalEventDef;

    public ExternalEventNodeModel() {}

    public Class<ExternalEventNode> getProtoBaseClass() {
        return ExternalEventNode.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        ExternalEventNode p = (ExternalEventNode) proto;
        externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, ignored);
        if (p.hasTimeoutSeconds()) {
            timeoutSeconds = VariableAssignmentModel.fromProto(p.getTimeoutSeconds(), ignored);
        }

        if (p.hasCorrelationId()) {
            this.corrrelationId = LHSerializable.fromProto(p.getCorrelationId(), VariableAssignmentModel.class, ignored);
        }
    }

    public ExternalEventNode.Builder toProto() {
        ExternalEventNode.Builder out =
                ExternalEventNode.newBuilder().setExternalEventDefId(externalEventDefId.toProto());

        if (timeoutSeconds != null) out.setTimeoutSeconds(timeoutSeconds.toProto());
        if (corrrelationId != null) out.setCorrelationId(corrrelationId.toProto());
        return out;
    }

    @Override
    public Set<String> getNeededVariableNames() {
        HashSet<String> result = new HashSet<>();
        if (timeoutSeconds != null) {
            result.addAll(timeoutSeconds.getRequiredWfRunVarNames());
        }
        if (corrrelationId != null) {
            result.addAll(corrrelationId.getRequiredWfRunVarNames());
        }
        return result;
    }

    @Override
    public void validate(MetadataCommandExecution ctx) throws LHApiException {
        // Want to be able to release new versions of ExternalEventDef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the EED to ensure that this isn't an issue.
        ExternalEventDefModel eed = ctx.metadataManager().get(new ExternalEventDefIdModel(externalEventDefId.getName()));

        // TODO: validate the timeout

        if (eed == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Refers to nonexistent ExternalEventDef " + externalEventDefId);
        }
    }

    public ExternalEventNodeRunModel createSubNodeRun(Date time, ProcessorExecutionContext processorContext) {
        return new ExternalEventNodeRunModel(externalEventDefId, processorContext);
    }
}
