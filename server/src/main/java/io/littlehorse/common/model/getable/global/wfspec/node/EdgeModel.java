package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableMutationModel;
import io.littlehorse.sdk.common.proto.Edge;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

public class EdgeModel extends LHSerializable<Edge> {

    public String sinkNodeName;
    public EdgeConditionModel condition;

    @Getter
    public List<VariableMutationModel> variableMutations;

    public EdgeModel() {
        variableMutations = new ArrayList<>();
    }

    public Class<Edge> getProtoBaseClass() {
        return Edge.class;
    }

    public Edge.Builder toProto() {
        Edge.Builder out = Edge.newBuilder().setSinkNodeName(sinkNodeName);

        for (VariableMutationModel v : variableMutations) {
            out.addVariableMutations(v.toProto());
        }

        if (condition != null) {
            out.setCondition(condition.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        Edge proto = (Edge) p;
        sinkNodeName = proto.getSinkNodeName();
        if (proto.hasCondition()) {
            condition = EdgeConditionModel.fromProto(proto.getCondition(), context);
            condition.edge = this;
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

    public ThreadSpecModel threadSpecModel;

    private NodeModel sinkNode;

    public NodeModel getSinkNode() {
        if (sinkNode == null) {
            sinkNode = threadSpecModel.nodes.get(sinkNodeName);
        }
        return sinkNode;
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();

        if (condition != null) {
            out.addAll(condition.getRequiredVariableNames());
        }

        for (VariableMutationModel mut : variableMutations) {
            out.addAll(mut.getRequiredVariableNames());
        }

        return out;
    }
}
