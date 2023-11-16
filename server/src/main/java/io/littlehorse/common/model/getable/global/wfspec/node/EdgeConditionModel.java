package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.EdgeCondition;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashSet;
import java.util.Set;

public class EdgeConditionModel extends LHSerializable<EdgeCondition> {

    public Comparator comparator;
    public VariableAssignmentModel left;
    public VariableAssignmentModel right;

    public Class<EdgeCondition> getProtoBaseClass() {
        return EdgeCondition.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        EdgeCondition p = (EdgeCondition) proto;
        comparator = p.getComparator();
        left = VariableAssignmentModel.fromProto(p.getLeft(), context);
        right = VariableAssignmentModel.fromProto(p.getRight(), context);
    }

    public EdgeCondition.Builder toProto() {
        EdgeCondition.Builder out = EdgeCondition.newBuilder();
        out.setComparator(comparator);
        out.setRight(right.toProto());
        out.setLeft(left.toProto());

        return out;
    }

    public static EdgeConditionModel fromProto(EdgeCondition p, ExecutionContext context) {
        EdgeConditionModel out = new EdgeConditionModel();
        out.initFrom(p, context);
        return out;
    }

    public EdgeModel edge;

    public void validate() throws LHApiException {
        // TODO: do some type checking here...
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        out.addAll(left.getRequiredWfRunVarNames());
        out.addAll(right.getRequiredWfRunVarNames());

        return out;
    }
}
