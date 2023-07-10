package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.EdgeConditionPb;
import java.util.HashSet;
import java.util.Set;

public class EdgeCondition extends LHSerializable<EdgeConditionPb> {

    public ComparatorPb comparator;
    public VariableAssignment left;
    public VariableAssignment right;

    public Class<EdgeConditionPb> getProtoBaseClass() {
        return EdgeConditionPb.class;
    }

    public void initFrom(Message proto) {
        EdgeConditionPb p = (EdgeConditionPb) proto;
        comparator = p.getComparator();
        left = VariableAssignment.fromProto(p.getLeft());
        right = VariableAssignment.fromProto(p.getRight());
    }

    public EdgeConditionPb.Builder toProto() {
        EdgeConditionPb.Builder out = EdgeConditionPb.newBuilder();
        out.setComparator(comparator);
        out.setRight(right.toProto());
        out.setLeft(left.toProto());

        return out;
    }

    public static EdgeCondition fromProto(EdgeConditionPb p) {
        EdgeCondition out = new EdgeCondition();
        out.initFrom(p);
        return out;
    }

    public Edge edge;

    public void validate() throws LHValidationError {
        // TODO: do some type checking here...
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        out.addAll(left.getRequiredWfRunVarNames());
        out.addAll(right.getRequiredWfRunVarNames());

        return out;
    }
}
