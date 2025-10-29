package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExitRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.ExitNode;
import io.littlehorse.sdk.common.proto.ExitNode.ResultCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExitNodeModel extends SubNode<ExitNode> {

    private ResultCase resultCase;
    private FailureDefModel failureDef;
    private VariableAssignmentModel returnContent;

    public Class<ExitNode> getProtoBaseClass() {
        return ExitNode.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExitNode p = (ExitNode) proto;
        resultCase = p.getResultCase();
        switch (resultCase) {
            case FAILURE_DEF:
                failureDef = FailureDefModel.fromProto(p.getFailureDef(), context);
                break;
            case RETURN_CONTENT:
                returnContent = LHSerializable.fromProto(p.getReturnContent(), VariableAssignmentModel.class, context);
                break;
            case RESULT_NOT_SET:
                // there's nothing to do: this ExitNode completes the ThreadRun successfully
                // without returning content.
        }
    }

    public ExitNode.Builder toProto() {
        ExitNode.Builder out = ExitNode.newBuilder();
        switch (resultCase) {
            case FAILURE_DEF:
                out.setFailureDef(failureDef.toProto());
                break;
            case RETURN_CONTENT:
                out.setReturnContent(returnContent.toProto());
                break;
            case RESULT_NOT_SET:
                // there's nothing to do: this ExitNode completes the ThreadRun successfully
                // without returning content.
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
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) throws InvalidExpressionException {
        if (returnContent == null) {
            return Optional.of(new ReturnTypeModel());
        }
        return Optional.of(new ReturnTypeModel(returnContent.resolveType(
                manager, node.getThreadSpec().getWfSpec(), node.getThreadSpec().getName())));
    }

    public Optional<ReturnTypeModel> getThreadReturnType(ReadOnlyMetadataManager manager)
            throws InvalidExpressionException {
        if (returnContent == null) return Optional.of(new ReturnTypeModel());
        Optional<TypeDefinitionModel> typeDefOption = returnContent.resolveType(
                manager, node.getThreadSpec().getWfSpec(), node.getThreadSpec().getName());

        return Optional.of(new ReturnTypeModel(typeDefOption.get()));
    }
}
