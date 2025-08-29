package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.RunChildWfNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.RunChildWfNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

@Getter
public class RunChildWfNodeModel extends SubNode<RunChildWfNode> {

    private String wfSpecName;
    private int majorVersion;
    private Map<String, VariableAssignmentModel> inputs = new HashMap<>();

    @Override
    public Class<RunChildWfNode> getProtoBaseClass() {
        return RunChildWfNode.class;
    }

    @Override
    public RunChildWfNode.Builder toProto() {
        RunChildWfNode.Builder out =
                RunChildWfNode.newBuilder().setWfSpecName(wfSpecName).setMajorVersion(majorVersion);

        for (Map.Entry<String, VariableAssignmentModel> inputVar : inputs.entrySet()) {
            out.putInputs(inputVar.getKey(), inputVar.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        RunChildWfNode p = (RunChildWfNode) proto;
        this.wfSpecName = p.getWfSpecName();
        this.majorVersion = p.getMajorVersion();

        for (Map.Entry<String, VariableAssignment> entry : p.getInputsMap().entrySet()) {
            this.inputs.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), VariableAssignmentModel.class, ignored));
        }
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws InvalidNodeException {
        // TODO: later.
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        // TODO: later.
        return Optional.empty();
    }

    @Override
    public RunChildWfNodeRunModel createSubNodeRun(Date arrivalTime, CoreProcessorContext ctx) {
        return new RunChildWfNodeRunModel();
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignmentModel assn : inputs.values()) {
            out.addAll(assn.getRequiredWfRunVarNames());
        }
        return out;
    }
}
