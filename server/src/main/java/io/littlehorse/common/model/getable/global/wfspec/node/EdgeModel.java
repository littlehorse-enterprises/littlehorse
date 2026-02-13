package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidEdgeException;
import io.littlehorse.common.exceptions.validation.InvalidMutationException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableMutationModel;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EdgeModel extends LHSerializable<Edge> {

    private String sinkNodeName;
    private LegacyEdgeConditionModel legacyCondition;
    private VariableAssignmentModel condition;

    @Getter
    public List<VariableMutationModel> variableMutations;

    public EdgeModel() {
        variableMutations = new ArrayList<>();
    }

    @Override
    public Class<Edge> getProtoBaseClass() {
        return Edge.class;
    }

    @Override
    public Edge.Builder toProto() {
        Edge.Builder out = Edge.newBuilder().setSinkNodeName(sinkNodeName);

        for (VariableMutationModel v : variableMutations) {
            out.addVariableMutations(v.toProto());
        }

        if (condition != null) {
            out.setCondition(condition.toProto());
        }

        if (legacyCondition != null) {
            out.setLegacyCondition(legacyCondition.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        Edge proto = (Edge) p;
        sinkNodeName = proto.getSinkNodeName();
        if (proto.hasCondition()) {
            condition = VariableAssignmentModel.fromProto(proto.getCondition(), context);
        }
        if (proto.hasLegacyCondition()) {
            if (true) {
                throw new RuntimeException("Legacy condition is not supported anymore!");
            }
            legacyCondition = LegacyEdgeConditionModel.fromProto(proto.getLegacyCondition(), context);
        }

        for (VariableMutation vmpb : proto.getVariableMutationsList()) {
            VariableMutationModel vm = new VariableMutationModel();
            vm.initFrom(vmpb, context);
            variableMutations.add(vm);
        }
    }

    public static EdgeModel fromProto(Edge proto, ExecutionContext context) {
        EdgeModel out = new EdgeModel();
        out.initFrom(proto, context);
        return out;
    }

    // Implementation details below
    private NodeModel sinkNode;

    @Setter
    private ThreadSpecModel threadSpecModel;

    /**
     * Returns the SinkNode pointed to by this Edge
     * @return the NodeModel representing the Node pointed to by this EdgeModel's Edge.
     */
    public NodeModel getSinkNode() {
        if (sinkNode == null) {
            sinkNode = threadSpecModel.getNodes().get(sinkNodeName);
        }
        return sinkNode;
    }

    /**
     * Returns the Set of all variable names used in this Edge. This method is generally
     * used when validating the WfSpec in order to make sure we don't refer to a non-existent
     * variable.
     * @return the Set of all variable names used by this Edge.
     */
    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();

        if (condition != null) {
            out.addAll(condition.getRequiredVariableNames());
        }

        if (legacyCondition != null) {
            out.addAll(legacyCondition.getRequiredVariableNames());
        }

        for (VariableMutationModel mut : variableMutations) {
            out.addAll(mut.getRequiredVariableNames());
        }

        return out;
    }

    /**
     * Given the current ThreadRun, returns true if the EdgeCondition for this Edge are satisfied
     * given the Variables in the ThreadRun.
     * @param threadRun is the ThreadRunModel that this EdgeModel's Edge is a part of.
     * @return true if the edge condition is satisfied (or if there is no condition).
     */
    public boolean isConditionSatisfied(ThreadRunModel threadRun) throws LHVarSubError {
        if (legacyCondition != null) {
            return legacyCondition.isSatisfied(threadRun);
        }
        return condition == null || condition.isSatisfied(threadRun);
    }

    /**
     * Atomically executes all variable mutations set on this Node given an instance of a
     * NodeRunModel.
     * @param threadRun is the ThreadRunModel whose variables we are mutating.
     * @param nodeRunOutput is the output of the NodeRun that owns this EdgeModel.
     * @throws LHVarSubError if one of the mutations is not possible to execute.
     */
    public void mutateVariables(ThreadRunModel threadRun, Optional<VariableValueModel> nodeRunOutput)
            throws LHVarSubError {
        VariableValueModel nodeRunOutputVar = nodeRunOutput.isEmpty() ? null : nodeRunOutput.get();

        // Need to do this atomically in a transaction, so that if one of the
        // mutations fail then we roll them all back.
        // That's why we write to an in-memory Map. If all mutations succeed,
        // then we flush the contents of the Map to the Variables.
        Map<String, VariableValueModel> writeAheadBuffer = new HashMap<>();

        // First thing we do is run through all of the mutations with the "buffer"
        for (VariableMutationModel mutation : variableMutations) {
            try {
                mutation.execute(threadRun, writeAheadBuffer, nodeRunOutputVar);
            } catch (LHVarSubError exn) {
                // We throw the exception and this method exits before actually flushing any
                // of the mutations to the data store. In this way, we can atomically "roll back"
                // any failed mutations.
                //
                // The only thing we do here is add more info to the exception so that
                // it is easier for the user to debug why their WfRun failed.
                throw new LHVarSubError(
                        exn,
                        "Mutating variable %s with operation %s"
                                .formatted(mutation.getLhsName(), mutation.getOperation()));
            }
        }

        // If we got here so far, that means that all of the mutations are valid. We need to flush them
        // to the data store by actually telling the ThreadRun to mutate the variables.
        for (Map.Entry<String, VariableValueModel> entry : writeAheadBuffer.entrySet()) {
            threadRun.mutateVariable(entry.getKey(), entry.getValue());
        }
    }

    public void validate(NodeModel source, MetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidEdgeException {
        if (this.getSinkNodeName().equals(source.getName())) {
            throw new InvalidEdgeException("Self loop not allowed!", this.getSinkNodeName());
        }

        NodeModel sink = threadSpec.nodes.get(this.getSinkNodeName());
        if (sink == null) {
            throw new InvalidEdgeException(
                    String.format("Outgoing edge referring to missing node %s!", this.getSinkNodeName()),
                    this.getSinkNodeName());
        }

        if (sink.type == NodeCase.ENTRYPOINT) {
            throw new InvalidEdgeException(
                    String.format("Entrypoint node has incoming edge from node %s.", threadSpec.name, source.getName()),
                    this.getSinkNodeName());
        }
        if (condition != null) {
            condition.validate(source, manager, threadSpec);
        }
        if (legacyCondition != null) {
            legacyCondition.validate(source, manager, threadSpec);
        }
        for (VariableMutationModel variableMutation : variableMutations) {
            try {
                variableMutation.validate(source, manager, threadSpec);
            } catch (InvalidMutationException exn) {
                throw new InvalidEdgeException(exn, this.getSinkNodeName());
            }
        }
    }
}
