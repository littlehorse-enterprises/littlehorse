package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidEdgeException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.EdgeCondition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;

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

    public void validate(MetadataProcessorContext context) throws InvalidEdgeException {
        throw new NotImplementedException();
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        out.addAll(left.getRequiredWfRunVarNames());
        out.addAll(right.getRequiredWfRunVarNames());

        return out;
    }

    /**
     * Given a ThreadRunModel representing a ThreadRun in a WfRun, returns true if
     * the represented EdgeCondition is satisfied by the variables in that ThreadRun.
     * @param threadRun is the ThreadRunModel representing the ThreadRun to evaluate
     * against.
     * @return true if the condition is satisfied.
     * @throws LHVarSubError if there is a problem getting variables.
     */
    public boolean isSatisfied(ThreadRunModel threadRun) throws LHVarSubError {
        VariableValueModel lhs = threadRun.assignVariable(this.left);
        VariableValueModel rhs = threadRun.assignVariable(this.right);

        switch (this.comparator) {
            case LESS_THAN:
                return Comparer.compare(lhs, rhs) < 0;
            case LESS_THAN_EQ:
                return Comparer.compare(lhs, rhs) <= 0;
            case GREATER_THAN:
                return Comparer.compare(lhs, rhs) > 0;
            case GREATER_THAN_EQ:
                return Comparer.compare(lhs, rhs) >= 0;
            case EQUALS:
                return lhs != null && Comparer.compare(lhs, rhs) == 0;
            case NOT_EQUALS:
                return lhs != null && Comparer.compare(lhs, rhs) != 0;
            case IN:
                return Comparer.contains(rhs, lhs);
            case NOT_IN:
                return !Comparer.contains(rhs, lhs);
            case UNRECOGNIZED:
        }

        // This is impossible; it means that we added a new value to the Comparator proto
        // without updating the LH Server to handle it. So we can throw an IllegalStateException.
        throw new IllegalStateException("Unknown value for Comparator enum %d".formatted(comparator.getNumber()));
    }
}

class Comparer {

    public static int compare(VariableValueModel left, VariableValueModel right) throws LHVarSubError {
        try {
            if (left.getVal() == null && right.getVal() != null) return -1;
            if (right.getVal() == null && left.getVal() != null) return 1;
            if (right.getVal() == null && left.getVal() == null) return 0;

            @SuppressWarnings("all")
            int result = ((Comparable) left.getVal()).compareTo((Comparable) right.getVal());

            return result;
        } catch (Exception exn) {
            throw new LHVarSubError(exn, "Failed comparing the provided values.");
        }
    }

    public static boolean contains(VariableValueModel left, VariableValueModel right) throws LHVarSubError {
        // Can only do for Str, Arr, and Obj

        if (left.getType() == VariableType.STR) {
            String rStr = right.asStr().getStrVal();

            return left.asStr().getStrVal().contains(rStr);
        } else if (left.getType() == VariableType.JSON_ARR) {
            Object rObj = right.getVal();
            List<Object> lhs = left.asArr().getJsonArrVal();

            for (Object o : lhs) {
                if (LHUtil.deepEquals(o, rObj)) {
                    return true;
                }
            }
            return false;
        } else if (left.getType() == VariableType.JSON_OBJ) {
            return left.asObj().getJsonObjVal().containsKey(right.asStr().getStrVal());
        } else {
            throw new LHVarSubError(null, "Can't do CONTAINS on " + left.getType());
        }
    }
}
