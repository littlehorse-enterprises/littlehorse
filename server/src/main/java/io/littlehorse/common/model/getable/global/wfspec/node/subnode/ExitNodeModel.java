package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExitRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.ExitNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

@Getter
public class ExitNodeModel extends SubNode<ExitNode> {

    public FailureDefModel failureDef;

    public Class<ExitNode> getProtoBaseClass() {
        return ExitNode.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExitNode p = (ExitNode) proto;
        if (p.hasFailureDef()) failureDef = FailureDefModel.fromProto(p.getFailureDef(), context);
    }

    public ExitNode.Builder toProto() {
        ExitNode.Builder out = ExitNode.newBuilder();
        if (failureDef != null) {
            out.setFailureDef(failureDef.toProto());
        }
        return out;
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws LHApiException {
        if (failureDef != null) failureDef.validate();
    }

    public ExitRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new ExitRunModel();
    }

    @Override
    public Set<String> getNeededVariableNames() {
        HashSet<String> out = new HashSet<>();
        if (failureDef != null) {
            out.addAll(failureDef.getNeededVariableNames());
        }
        return out;
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        // No output.
        return Optional.of(new ReturnTypeModel());
    }
}
