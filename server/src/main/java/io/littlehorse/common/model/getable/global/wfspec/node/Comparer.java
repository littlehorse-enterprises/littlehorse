package io.littlehorse.common.model.getable.global.wfspec.node;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.List;

public class Comparer {

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

        // TODO: Decide how to support StructDefs
        if (left.getTypeDefinition().getDefinedTypeCase() != TypeDefinition.DefinedTypeCase.PRIMITIVE_TYPE) {
            throw new LHVarSubError(null, "Can't perform contains on " + left.getTypeDefinition());
        }

        if (left.getTypeDefinition().getPrimitiveType() == VariableType.STR) {
            String rStr = right.asStr().getStrVal();

            return left.asStr().getStrVal().contains(rStr);
        } else if (left.getTypeDefinition().getPrimitiveType() == VariableType.JSON_ARR) {
            Object rObj = right.getVal();
            List<Object> lhs = left.asArr().getJsonArrVal();

            for (Object o : lhs) {
                if (LHUtil.deepEquals(o, rObj)) {
                    return true;
                }
            }
            return false;
        } else if (left.getTypeDefinition().getPrimitiveType() == VariableType.JSON_OBJ) {
            return left.asObj().getJsonObjVal().containsKey(right.asStr().getStrVal());
        } else {
            throw new LHVarSubError(
                    null, "Can't do CONTAINS on " + left.getTypeDefinition().getPrimitiveType());
        }
    }
}
