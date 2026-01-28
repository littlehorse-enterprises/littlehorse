package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.exceptions.validation.InvalidThreadSpecException;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.RunChildWfNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.RunChildWfNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RunChildWfNodeModel extends SubNode<RunChildWfNode> {

    private String wfSpecName;
    private VariableAssignmentModel wfSpecVar;
    private int majorVersion;
    private Map<String, VariableAssignmentModel> inputs = new HashMap<>();

    @Override
    public Class<RunChildWfNode> getProtoBaseClass() {
        return RunChildWfNode.class;
    }

    @Override
    public RunChildWfNode.Builder toProto() {
        RunChildWfNode.Builder out = RunChildWfNode.newBuilder().setMajorVersion(majorVersion);
        if (wfSpecName != null) {
            out.setWfSpecName(wfSpecName);
        } else if (wfSpecVar != null) {
            out.setWfSpecVar(wfSpecVar.toProto());
        }

        for (Map.Entry<String, VariableAssignmentModel> inputVar : inputs.entrySet()) {
            out.putInputs(inputVar.getKey(), inputVar.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        RunChildWfNode p = (RunChildWfNode) proto;
        this.majorVersion = p.getMajorVersion();
        if (p.hasWfSpecName()) {
            this.wfSpecName = p.getWfSpecName();
        } else if (p.hasWfSpecVar()) {
            this.wfSpecVar = VariableAssignmentModel.fromProto(p.getWfSpecVar(), ignored);
        }

        for (Map.Entry<String, VariableAssignment> entry : p.getInputsMap().entrySet()) {
            this.inputs.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), VariableAssignmentModel.class, ignored));
        }
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws InvalidNodeException {
        if (wfSpecName != null) {
            WfSpecModel childWfSpec =
                    ctx.service().getWfSpec(wfSpecName, majorVersion == -1 ? null : majorVersion, null);
            if (childWfSpec == null) {
                String message = "Could not find WfSpec " + wfSpecName;
                if (majorVersion != -1) {
                    message += " with version " + majorVersion;
                }
                throw new InvalidNodeException(message, this.node);
            }
            ThreadSpecModel childEntrypoint = childWfSpec.getEntrypointThread();
            try {
                childEntrypoint.validateStartVariablesByType(inputs, node.getThreadSpec());
            } catch (InvalidThreadSpecException exn) {
                throw new InvalidNodeException(exn, node);
            }
            // Pin major version to the latest available right now.
            if (majorVersion == -1) {
                this.majorVersion = childWfSpec.getId().getMajorVersion();
            }
        } else if (wfSpecVar != null) {
            boolean validType = wfSpecVar.canBeType(VariableType.STR, node.threadSpec);
            if (!validType) {
                throw new InvalidNodeException("Only STR variables are valid", node);
            }
        } else {
            throw new InvalidNodeException("A valid WfSpec name must be specified", node);
        }
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        return Optional.of(new ReturnTypeModel(VariableType.WF_RUN_ID));
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

    @Override
    public Set<String> getNeededNodeNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignmentModel assignment : inputs.values()) {
            if (assignment.getRhsSourceType() == SourceCase.NODE_OUTPUT) {
                out.add(assignment.getNodeOutputReference().getNodeName());
            }
        }
        return out;
    }

    public WfSpecModel getWfSpecToRun(ReadOnlyMetadataManager manager) {
        if (wfSpecVar == null) return null; // We can't resolve the spec output type at this point
        WfService service = new WfService(manager);
        return service.getWfSpec(wfSpecName, majorVersion == -1 ? null : majorVersion, null);
    }

    public WfSpecModel getWfSpecToRun(NodeRunModel nodeRun, ReadOnlyMetadataManager manager)
            throws NodeFailureException {
        WfService service = new WfService(manager);
        if (wfSpecVar != null) {
            try {
                VariableValueModel specNameVal =
                        nodeRun.getThreadRun().assignVariable(wfSpecVar).asStr();
                String wfSpecName = specNameVal.getStrVal();
                WfSpecModel wfSpec = service.getWfSpec(wfSpecName, majorVersion == -1 ? null : majorVersion, 0);
                // Pin major version to the latest available right now.
                if (wfSpec == null) {
                    throw new NodeFailureException(new FailureModel(
                            "Couldn't find WfSpec %s".formatted(wfSpecName), LHConstants.CHILD_FAILURE));
                }
                if (majorVersion == -1) {
                    this.majorVersion = wfSpec.getId().getMajorVersion();
                }
                return wfSpec;
            } catch (LHVarSubError e) {
                throw new NodeFailureException(new FailureModel(e.getMessage(), LHConstants.VAR_SUB_ERROR));
            }
        } else {
            return service.getWfSpec(wfSpecName, majorVersion, null);
        }
    }
}
