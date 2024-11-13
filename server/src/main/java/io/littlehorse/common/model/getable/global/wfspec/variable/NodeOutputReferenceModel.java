package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.VariableAssignment.NodeOutputReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

@Getter
public class NodeOutputReferenceModel extends LHSerializable<NodeOutputReference> {

    private String nodeName;

    @Override
    public Class<NodeOutputReference> getProtoBaseClass() {
        return NodeOutputReference.class;
    }

    @Override
    public NodeOutputReference.Builder toProto() {
        return NodeOutputReference.newBuilder().setNodeName(nodeName);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        NodeOutputReference p = (NodeOutputReference) proto;
        nodeName = p.getNodeName();
    }

    public VariableValueModel getValue(
            ThreadRunModel threadRun, Map<String, VariableValueModel> txnCache, ProcessorExecutionContext context)
            throws LHVarSubError {
        // We need to get all NodeRun's from the threadRun.
        int currentPosition = threadRun.getCurrentNodePosition();

        // The only way to find a previous NodeRun is to walk backwards from the current position
        // until we either:
        // 1) Find a NodeRun that matches the nodeName
        // 2) Reach the Entrypoint Node
        //
        // If we reach the entrypoint node, it means that there is no NodeRun that satisfies
        // the requirement; therefore we fail the Variable Assignment with LHVarSubError.
        //
        // TODO: We can find a way to optimize this. Range scans are expensive, and also
        // doing it in this way means that the NodeRun's will be re-put into the GetableManager's
        // buffer, which could get _very_ expensive.
        for (int i = currentPosition - 1; i > 0; i--) {
            NodeRunModel nodeRun = threadRun.getNodeRun(i);
            if (!nodeRun.getNodeName().equals(nodeName)) continue;

            // Check for value
            Optional<VariableValueModel> output = nodeRun.getOutput(context);
            if (output.isEmpty()) {
                throw new LHVarSubError(
                        null,
                        "Specified node " + nodeName + " of type " + nodeRun.getType() + ", number "
                                + nodeRun.getId().getPosition() + " has no output.");
            }
            return output.get();
        }

        throw new LHVarSubError(null, "Specified node " + nodeName + " does not have any previous runs.");
    }
}
